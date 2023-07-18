package cn.martinkay.wechatroaming.settings.hook

import android.app.Activity
import android.content.Context
import android.os.Bundle
import cn.martinkay.wechatroaming.settings.base.annotation.FunctionHookEntry
import cn.martinkay.wechatroaming.settings.base.annotation.UiItemAgentEntry
import cn.martinkay.wechatroaming.settings.startup.HookEntry
import cn.martinkay.wechatroaming.utils.ToastUtil
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers

@FunctionHookEntry
@UiItemAgentEntry
object EnableOnCreateSpiderWeb : CommonSwitchFunctionHook() {

    override val name = "开启OnCreate蛛网"
    override val uiItemLocation = cn.martinkay.wechatroaming.settings.dsl.FunctionEntryRouter.Locations.Core.CORE_SPIDER

    override fun initOnce(context: Context): Boolean {
        ToastUtil.makeToast(context, "onCreate准备被控制", ToastUtil.LENGTH_SHORT)
        XposedHelpers.findAndHookMethod(
            Activity::class.java,
            "onCreate",
            Bundle::class.java,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam?) {
                    super.afterHookedMethod(param)
                    ToastUtil.makeToast(context, "onCreate被控制", ToastUtil.LENGTH_SHORT)
                    val lpp = HookEntry.getLoadPackageParam(context.packageName)
                }
            })
        return true
    }

}