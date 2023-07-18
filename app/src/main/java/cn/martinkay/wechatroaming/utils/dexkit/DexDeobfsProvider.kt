package cn.martinkay.wechatroaming.utils.dexkit

import cn.martinkay.wechatroaming.utils.dexkit.impl.DexKitDeobfs
import java.util.concurrent.atomic.AtomicInteger

object DexDeobfsProvider {

    private val mDeobfsSection = AtomicInteger(0)

    fun enterDeobfsSection() {
        mDeobfsSection.incrementAndGet()
    }

    fun exitDeobfsSection() {
        mDeobfsSection.decrementAndGet()
    }

    @JvmStatic
    fun checkDeobfuscationAvailable() {
        check(mDeobfsSection.get() > 0) { "dex deobfuscation is not meant to be available now" }
    }

    /**
     * Create a new instance. Call [DexDeobfsBackend.close] when you are done.
     */
    fun getCurrentBackend(): DexDeobfsBackend {
        checkDeobfuscationAvailable()
        return DexKitDeobfs.newInstance()
    }

}
