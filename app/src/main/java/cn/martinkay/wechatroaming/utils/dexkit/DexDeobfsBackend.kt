package cn.martinkay.wechatroaming.utils.dexkit

import cn.martinkay.wechatroaming.utils.Initiator
import cn.martinkay.wechatroaming.utils.Log
import java.io.Closeable
import java.lang.reflect.Method

interface DexDeobfsBackend : Closeable {
    val id: String
    val name: String
    val isBatchFindMethodSupported: Boolean

    /**
     * Run the dex deobfuscation. This method may take a long time and should only be called in background thread.
     *
     * @param target the dex method target
     * @return target method descriptor, null if the target is not found.
     */
    fun doFindMethodImpl(target: DexKitTarget): DexMethodDescriptor?

    @Throws(UnsupportedOperationException::class)
    fun doBatchFindMethodImpl(targetArray: Array<DexKitTarget>)

    /**
     * Close the backend, memory will be release when ref-count decrease to 0.
     *
     *
     * No other method should be called after this method is called.
     */
    override fun close()
    fun doFindMethod(target: DexKitTarget): Method? {
        var descriptor = DexKit.getMethodDescFromCacheImpl(target)
        if (descriptor == null) {
            descriptor = doFindMethodImpl(target)
            if (descriptor == null) {
                Log.d("${target.name} not found, save null")
                descriptor = DexKit.NO_SUCH_METHOD
                target.descCache = descriptor.toString()
                return null
            }
        }
        try {
            if (DexKit.NO_SUCH_METHOD.toString() == descriptor.toString()) {
                return null
            }
            if (descriptor.name == "<init>" || descriptor.name == "<clinit>") {
                Log.i("doFindMethod(" + target.name + ") methodName == " + descriptor.name + " , return null")
                return null
            }
            return descriptor.getMethodInstance(Initiator.getHostClassLoader())
        } catch (e: NoSuchMethodException) {
            // ignore
        }
        return null
    }

    fun doFindClass(target: DexKitTarget): Class<*>? {
        val ret = Initiator.load(target.declaringClass)
        if (ret != null) {
            return ret
        }
        var descriptor = DexKit.getMethodDescFromCacheImpl(target)
        if (descriptor == null) {
            descriptor = doFindMethodImpl(target)
            if (descriptor == null) {
                Log.d("${target.name} not found, save null")
                descriptor = DexKit.NO_SUCH_METHOD
                target.descCache = descriptor.toString()
                return null
            }
        }
        if (DexKit.NO_SUCH_METHOD.toString() == descriptor.toString()) {
            return null
        }
        if (descriptor.name == "<init>" || descriptor.name == "<clinit>") {
            Log.i("doFindMethod(${target.name}" + ") methodName == " + descriptor.name + " , return null")
            return null
        }
        return Initiator.load(descriptor.declaringClass)
    }
}
