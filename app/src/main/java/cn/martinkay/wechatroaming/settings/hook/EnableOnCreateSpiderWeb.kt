package cn.martinkay.wechatroaming.settings.hook

import android.app.Activity
import android.content.Context
import android.os.Bundle
import cn.martinkay.wechatroaming.settings.base.annotation.FunctionHookEntry
import cn.martinkay.wechatroaming.settings.base.annotation.UiItemAgentEntry
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers

@FunctionHookEntry
@UiItemAgentEntry
object EnableOnCreateSpiderWeb : CommonSwitchFunctionHook() {

    override val name = "开启OnCreate蛛网"
    override val uiItemLocation = cn.martinkay.wechatroaming.settings.dsl.FunctionEntryRouter.Locations.Core.CORE_SPIDER

    override fun initOnce(): Boolean {
        XposedHelpers.findAndHookMethod(
            Activity::class.java,
            "onCreate",
            Bundle::class.java,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam?) {
                    super.afterHookedMethod(param)
                }
            })
        return true
    }

}