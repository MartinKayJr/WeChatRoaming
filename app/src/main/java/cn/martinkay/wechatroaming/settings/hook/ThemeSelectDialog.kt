package cn.martinkay.wechatroaming.settings.hook

import android.app.Activity
import android.view.View
import androidx.fragment.app.FragmentActivity
import cn.martinkay.wechatroaming.settings.activity.SettingsUiFragmentHostActivity
import cn.martinkay.wechatroaming.settings.base.IUiItemAgent
import cn.martinkay.wechatroaming.settings.base.annotation.UiItemAgentEntry
import cn.martinkay.wechatroaming.settings.dsl.FunctionEntryRouter
import cn.martinkay.wechatroaming.settings.ui.ModuleThemeManager
import cn.martinkay.wechatroaming.utils.SyncUtils
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener
import com.jaredrummler.android.colorpicker.ColorShape
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

@UiItemAgentEntry
object ThemeSelectDialog : BasePlainUiAgentItem(title = "主题色", description = null) {

    override val uiItemLocation: Array<String> =
        FunctionEntryRouter.Locations.ConfigCategory.CONFIG_CATEGORY

    override val valueState: MutableStateFlow<String?> by lazy {
        MutableStateFlow(ModuleThemeManager.getCurrentThemeName())
    }

    override val onClickListener: ((IUiItemAgent, Activity, View) -> Unit) = { _, activity, _ ->
        showThemeSelectDialog(activity)
    }

    private fun showThemeSelectDialog(activity: Activity) {
        val dialog = ColorPickerDialog.newBuilder()
            .setDialogType(ColorPickerDialog.TYPE_PRESETS)
            .setDialogTitle(com.jaredrummler.android.colorpicker.R.string.cpv_default_title)
            .setColorShape(ColorShape.CIRCLE)
            .setPresets(ModuleThemeManager.getColors(activity))
            .setAllowPresets(true)
            .setAllowCustom(false)
            .setShowAlphaSlider(false)
            .setShowColorShades(false)
            .setColor(ModuleThemeManager.getCurrentThemeColor(activity))
            .create()
        dialog.setColorPickerDialogListener(object : ColorPickerDialogListener {
            override fun onColorSelected(dialogId: Int, color: Int) {
                updateThemeColor(activity, color)
            }

            override fun onDialogDismissed(dialogId: Int) {
                // nothing to do
            }
        })
        (activity as FragmentActivity).supportFragmentManager
            .beginTransaction()
            .add(dialog, "color_picker_dialog")
            .commitAllowingStateLoss()
    }

    private fun updateThemeColor(activity: Activity, color: Int) {
        ModuleThemeManager.setCurrentThemeByColor(activity, color)
        valueState.update { ModuleThemeManager.getCurrentThemeName() }
        if (activity is SettingsUiFragmentHostActivity) {
            // refresh ui, wait we are finished
            SyncUtils.postDelayed(100) {
                activity.recreate()
            }
        }
    }
}
