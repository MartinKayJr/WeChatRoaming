package cn.martinkay.wechatroaming.settings.dialog

import android.content.Context
import androidx.annotation.UiThread
import androidx.appcompat.app.AlertDialog
import cn.martinkay.wechatroaming.R
import cn.martinkay.wechatroaming.config.ConfigManager
import cn.martinkay.wechatroaming.settings.core.MainHook
import cn.martinkay.wechatroaming.settings.ui.CommonContextWrapper
import cn.martinkay.wechatroaming.utils.HostInfo

// in host process only
object WsaWarningDialog {

    private const val LATEST_WSA_WARNING_VERSION = 1

    private const val WSA_WARNING_DIALOG_TAG = "WsaWarningDialog.Version"

    private var mHasShownThisTime = false

    private var currentWsaWarningVersion: Int
        get() = ConfigManager.getDefaultConfig().getIntOrDefault(WSA_WARNING_DIALOG_TAG, 0)
        set(value) {
            ConfigManager.getDefaultConfig().putInt(WSA_WARNING_DIALOG_TAG, value)
        }

    private fun isNeedShow(): Boolean {
        return HostInfo.isInHostProcess()
            && MainHook.isWindowsSubsystemForAndroid()
            && currentWsaWarningVersion < LATEST_WSA_WARNING_VERSION
            && !mHasShownThisTime
    }

    @JvmStatic
    @UiThread
    fun showWsaWarningDialogIfNecessary(baseContext: Context) {
        if (!isNeedShow()) {
            return
        }
        mHasShownThisTime = true
        val ctx = CommonContextWrapper.createAppCompatContext(baseContext)
        AlertDialog.Builder(ctx).apply {
            setTitle(R.string.wsa_warning_dialog_title)
            setMessage(R.string.wsa_warning_dialog_message)
            setPositiveButton(android.R.string.ok, null)
            setNeutralButton(R.string.btn_do_not_show_again) { _, _ ->
                currentWsaWarningVersion = LATEST_WSA_WARNING_VERSION
            }
            setCancelable(true)
            show()
        }
    }

}
