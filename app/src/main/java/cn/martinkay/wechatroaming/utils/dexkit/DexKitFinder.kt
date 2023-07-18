package cn.martinkay.wechatroaming.utils.dexkit;

interface DexKitFinder {

    val isNeedFind: Boolean

    fun doFind(): Boolean
}
