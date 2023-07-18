package cn.martinkay.wechatroaming.utils.dexkit;

import cn.martinkay.wechatroaming.utils.Initiator
import com.github.kyuubiran.ezxhelper.utils.isAbstract
import com.github.kyuubiran.ezxhelper.utils.isStatic

typealias dexkitFilter = (DexMethodDescriptor) -> Boolean

infix fun dexkitFilter.or(other: dexkitFilter) = { it: DexMethodDescriptor -> this(it) || other(it) }
infix fun dexkitFilter.and(other: dexkitFilter) = { it: DexMethodDescriptor -> this(it) && other(it) }

object DexKitFilter {
    val allStaticFields = filter@{ it: DexMethodDescriptor ->
        val clz = Initiator.load(it.declaringClass) ?: return@filter false
        !clz.isAbstract && clz.fields.all { it.isStatic }
    }

    val hasSuper = filter@{ it: DexMethodDescriptor ->
        val clz = Initiator.load(it.declaringClass) ?: return@filter false
        !clz.isEnum && !clz.isAbstract && clz.superclass != Any::class.java
    }

    val notHasSuper = filter@{ it: DexMethodDescriptor ->
        val clz = Initiator.load(it.declaringClass) ?: return@filter false
        !clz.isEnum && !clz.isAbstract && clz.superclass == Any::class.java
    }

    val allowAll = { _: DexMethodDescriptor -> true }

    val clinit = filter@{ it: DexMethodDescriptor ->
        it.name == "<clinit>"
    }

    val defpackage = filter@{ it: DexMethodDescriptor ->
        val clz = Initiator.load(it.declaringClass) ?: return@filter false
        !clz.name.contains(".")
    }

    fun strInClsName(str: String, fullMatch: Boolean = false): dexkitFilter = { it: DexMethodDescriptor ->
        if (fullMatch) str == it.declaringClass else str in it.declaringClass
    }

    fun strInSig(str: String, fullMatch: Boolean = false): dexkitFilter = { it: DexMethodDescriptor ->
        if (fullMatch) str == it.signature else str in it.signature
    }

    fun filterByParams(filter: (Array<Class<*>>) -> Boolean): dexkitFilter = filter@{ it: DexMethodDescriptor ->
        val m = kotlin.runCatching { it.getMethodInstance(Initiator.getHostClassLoader()) }.getOrNull() ?: return@filter false
        filter(m.parameterTypes)
    }
}
