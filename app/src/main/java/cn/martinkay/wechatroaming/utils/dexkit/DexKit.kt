package cn.martinkay.wechatroaming.utils.dexkit

import cn.martinkay.wechatroaming.utils.Initiator
import cn.martinkay.wechatroaming.utils.Initiator.getHostClassLoader
import cn.martinkay.wechatroaming.utils.Log
import java.lang.reflect.Method

object DexKit {
    private const val NO_SUCH_CLASS = "Lio/github/qauxv/util/DexKit\$NoSuchClass;"

    @JvmField
    val NO_SUCH_METHOD = DexMethodDescriptor(NO_SUCH_CLASS, "a", "()V")

    /**
     * Test whether we should run the dex deobfuscation.
     * <p>
     * Note that if a dex class is tried to deobfuscate before, but failed, its failed result will be cached,
     * which means that the same dex class will not be deobfuscated again.
     *
     * @param target the dex class target
     * @return true if time is required to deobfuscate the dex class, false if either the dex class is already
     * found or there was already a failed result.
     */
    @JvmStatic
    fun isRunDexDeobfuscationRequired(target: DexKitTarget) : Boolean {
        return if (target.findMethod) {
            getMethodDescFromCacheImpl(target) == null
        } else {
            getMethodDescFromCacheImpl(target) == null && loadClassFromCache(target) == null
        }
    }

    @JvmStatic
    fun doFindClass(target: DexKitTarget): Class<*>? {
        when (target) {
            is DexKitTarget.UsingStr -> {
                loadClassFromCache(target)?.let { return it }
                return DexDeobfsProvider.getCurrentBackend().doFindClass(target)
            }
            else -> throw IllegalArgumentException("Unsupported target type: $target")
        }

    }

    @JvmStatic
    fun doFindMethod(target: DexKitTarget): Method? {
        when (target) {
            is DexKitTarget.UsingStr -> {
                check(target.findMethod) { "$target attempted to access method!" }
                loadMethodFromCache(target)?.let { return it }
                return DexDeobfsProvider.getCurrentBackend().doFindMethod(target)
            }
            else -> throw IllegalArgumentException("Unsupported target type: $target")
        }
    }

    /**
     * Get the method descriptor from cache. If the cache is empty, return null.
     *
     * If the cache is not empty, but the method is not found, return [NO_SUCH_METHOD].
     */
    @JvmStatic
    fun getMethodDescFromCacheImpl(target: DexKitTarget): DexMethodDescriptor? {
        target.descCache.let {
            return if (it.isNullOrEmpty()) {
                null
            } else {
                DexMethodDescriptor(it)
            }
        }
    }

    /**
     * Get the method descriptor from cache. If the cache is empty or not found, return null.
     */
    @JvmStatic
    fun getMethodDescFromCache(target: DexKitTarget): DexMethodDescriptor? {
        target.descCache.let {
            return if (it.isNullOrEmpty() || it == NO_SUCH_METHOD.toString()) {
                null
            } else {
                DexMethodDescriptor(it)
            }
        }
    }

    @JvmStatic
    fun loadClassFromCache(target: DexKitTarget): Class<*>? {
        Initiator.load(target.declaringClass)?.let { return it }
        return getMethodDescFromCache(target)?.let { Initiator.load(it.declaringClass) }
    }

    @JvmStatic
    fun loadMethodFromCache(target: DexKitTarget): Method? {
        check(target.findMethod) { "$target attempted to access method!" }
        val cache = getMethodDescFromCache(target) ?: return null
        if (NO_SUCH_METHOD.toString() == cache.toString()) return null
        if ("<init>" in cache.name || "<clinit>" in cache.name) {
            // TODO: support constructors
            Log.i("getMethodFromCache($target) methodName == ${cache.name} , return null")
            return null
        }
        return kotlin.runCatching {
            cache.getMethodInstance(getHostClassLoader())
        }.onFailure { t -> Log.e(t) }.getOrNull()
    }

    @JvmStatic
    fun requireMethodFromCache(target: DexKitTarget): Method {
        return loadMethodFromCache(target) ?: throw NoSuchMethodException("DexTarget: " + target.name)
    }

    @JvmStatic
    fun requireClassFromCache(target: DexKitTarget): Class<*> {
        return loadClassFromCache(target) ?: throw ClassNotFoundException("DexTarget: " + target.name)
    }
}
