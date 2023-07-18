package cn.martinkay.wechatroaming.utils

import android.content.Context
import android.content.res.Resources
import cn.martinkay.wechatroaming.utils.LayoutHelper

interface LayoutHelperViewScope {
    fun getContext(): Context

    val Int.dp: Int get() = LayoutHelper.dip2px(getContext(), this.toFloat())
    val Float.dp: Int get() = LayoutHelper.dip2px(getContext(), this)
}

interface LayoutHelperContextScope {
    fun getResources(): Resources

    val Int.dp: Int
        get() {
            val scale: Float = getResources().displayMetrics.density
            return (this * scale + 0.5f).toInt()
        }
    val Float.dp: Int
        get() {
            val scale: Float = getResources().displayMetrics.density
            return (this * scale + 0.5f).toInt()
        }
}
