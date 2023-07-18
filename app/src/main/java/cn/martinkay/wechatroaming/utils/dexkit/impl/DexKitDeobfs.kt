package cn.martinkay.wechatroaming.utils.dexkit.impl

import cn.martinkay.wechatroaming.utils.HostInfos
import cn.martinkay.wechatroaming.utils.Log
import cn.martinkay.wechatroaming.utils.dexkit.*
import io.luckypray.dexkit.DexKitBridge
import io.luckypray.dexkit.builder.BatchFindArgs
import java.util.concurrent.locks.Lock
import io.luckypray.dexkit.descriptor.member.DexMethodDescriptor as MethodDescriptor

class DexKitDeobfs private constructor(
    private val mReadLock: Lock,
    private var mDexKitBridge: DexKitBridge?
) : DexDeobfsBackend {

    override val id: String = ID
    override val name: String = NAME
    override val isBatchFindMethodSupported: Boolean = true

    fun getDexKitBridge(): DexKitBridge {
        return mDexKitBridge!!
    }

    override fun doBatchFindMethodImpl(targetArray: Array<DexKitTarget>) {
        ensureOpen()
        mReadLock.lock()
        try {
            val helper = mDexKitBridge!!
            val targets = targetArray.filterIsInstance<DexKitTarget.UsingStr>()
            val methodDescriptorArray = Array(targets.size) {
                DexKit.getMethodDescFromCacheImpl(targets[it])
            }
            val deobfsMap = mutableMapOf<String, Set<String>>()
            for (index in methodDescriptorArray.indices) {
                if (methodDescriptorArray[index] == null) {
                    val target = targets[index]
                    val keys = target.traitString
                    keys.forEachIndexed { idx, key ->
                        // 可能存在不同版本的关键词，所以需要区分开来
                        deobfsMap["${target.name}#_#${idx}"] = setOf(key)
                    }
                }
            }

            val resultMap = helper.batchFindMethodsUsingStrings(BatchFindArgs.build {
                queryMap = deobfsMap
            })
            val resultMap2 = mutableMapOf<String, Set<MethodDescriptor>>()
            resultMap.forEach {
                val key = it.key.split("#").first()
                if (resultMap2.containsKey(key)) {
                    resultMap2[key] = resultMap2[key]!! + it.value
                } else {
                    resultMap2[key] = it.value.toSet()
                }
            }

            resultMap2.forEach { (key, valueArr) ->
                val target = DexKitTarget.valueOf(key)
                val ret =
                    target.verifyTargetMethod(valueArr.map { DexMethodDescriptor(it.descriptor) })
                if (ret == null) {
                    valueArr.map { it.descriptor }.forEach(Log::i)
                    Log.e("${valueArr.size} candidates found for " + key + ", none satisfactory, save null.")
                    target.descCache = DexKit.NO_SUCH_METHOD.toString()
                } else {
                    Log.d("save id: $key,method: $ret")
                    target.descCache = ret.toString()
                }
            }
        } finally {
            mReadLock.unlock()
        }
    }

    override fun doFindMethodImpl(target: DexKitTarget): DexMethodDescriptor? {
        if (target !is DexKitTarget.UsingStr) return null
        ensureOpen()
        mReadLock.lock()
        try {
            var ret = DexKit.getMethodDescFromCacheImpl(target)
            if (ret != null) {
                return ret
            }
            ensureOpen()
            val helper = mDexKitBridge!!
            val keys = target.traitString
            val methods = keys.map { key ->
                helper.findMethodUsingString {
                    usingString = key
                }
            }.flatMap { desc ->
                desc.map { DexMethodDescriptor(it.descriptor) }
            }
            if (methods.isNotEmpty()) {
                ret = target.verifyTargetMethod(methods)
                if (ret == null) {
                    Log.e("${methods.size} methods found for ${target.name}, none satisfactory, save null.")
                    ret = DexKit.NO_SUCH_METHOD
                }
                Log.d("save id: ${target.name},method: $ret")
                target.descCache = ret.toString()
            }
            return ret
        } finally {
            mReadLock.unlock()
        }
    }

    @Synchronized
    private fun ensureOpen() {
        check(mDexKitBridge != null) { "closed" }
    }

    @Synchronized
    override fun close() {
        mSharedResourceImpl.decreaseRefCount()
        mDexKitBridge = null
    }

    companion object {

        const val ID = "DexKit"
        const val NAME = "DexKit(默认, 最快)"

        @JvmStatic
        fun newInstance(): DexKitDeobfs {
            val lock = mSharedResourceImpl.increaseRefCount()
            return DexKitDeobfs(lock.readLock(), mSharedResourceImpl.resources!!)
        }

        private val mSharedResourceImpl by lazy {
            object : SharedRefCountResourceImpl<DexKitBridge>() {
                override fun openResourceInternal(): DexKitBridge {
                    Log.d("open resource: DexKit")
                    return DexKitBridge.create(HostInfos.getHostInfo().context.applicationInfo.sourceDir)!!
                }

                override fun closeResourceInternal(res: DexKitBridge) {
                    res.close()
                    Log.d("close resource: DexKit")
                }
            }
        }
    }
}
