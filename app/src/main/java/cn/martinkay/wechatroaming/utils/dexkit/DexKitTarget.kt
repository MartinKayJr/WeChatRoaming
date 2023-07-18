package cn.martinkay.wechatroaming.utils.dexkit

import cn.martinkay.wechatroaming.config.ConfigManager
import cn.martinkay.wechatroaming.utils.Log
import cn.martinkay.wechatroaming.utils.dexkit.data.ConfigData
import com.livefront.sealedenum.GenSealedEnum

sealed class DexKitTarget {

    sealed class UsingStr : DexKitTarget() {
        // with 'OR' relationship
        abstract val traitString: Array<String>
    }

    sealed class UsingDexkit : DexKitTarget()

    abstract val declaringClass: String
    open val findMethod: Boolean = false
    abstract val filter: dexkitFilter

    private val descCacheKey by lazy { ConfigData<String>("cache#$name", ConfigManager.getCache()) }
    var descCache: String?
        get() = descCacheKey.value
        set(value) {
            descCacheKey.value = value
        }

    open fun verifyTargetMethod(methods: List<DexMethodDescriptor>): DexMethodDescriptor? {
        return kotlin.runCatching {
            val filter = methods.filter(filter)
            if (filter.size > 1) {
                filter.forEach { Log.e(it.toString()) }
                if (!findMethod) {
                    val sameClass = filter.distinctBy { it.declaringClass }.size == 1
                    if (sameClass) {
                        Log.w("More than one method matched: $name, but has same class")
                        return filter.first()
                    }
                }
                Log.e("More than one method matched: $name, return none for safety")
                return null
            }
            filter.firstOrNull()
        }.onFailure { Log.e(it) }.getOrNull()
    }

    @GenSealedEnum
    companion object
}
