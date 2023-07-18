package cn.martinkay.wechatroaming.settings.fragment

import android.view.ViewGroup

abstract class BaseRootLayoutFragment : BaseSettingFragment() {

    open var rootLayoutView: ViewGroup? = null

    override fun onResume() {
        super.onResume()
        applyPadding()
    }

    private fun applyPadding() {
        rootLayoutView?.let {
            it.clipToPadding = false
            it.setPadding(it.paddingLeft, layoutPaddingTop, it.paddingRight, layoutPaddingBottom)
        }
    }

    protected fun applyRootLayoutPaddingFor(viewGroup: ViewGroup) {
        viewGroup.clipToPadding = false
        viewGroup.setPadding(viewGroup.paddingLeft, layoutPaddingTop, viewGroup.paddingRight, layoutPaddingBottom)
    }

    override fun onLayoutPaddingsChanged() {
        super.onLayoutPaddingsChanged()
        applyPadding()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        rootLayoutView = null
    }
}
