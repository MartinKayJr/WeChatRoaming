package cn.martinkay.wechatroaming.settings.startup;

import android.content.Context;
import android.text.TextUtils;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookApplication extends XC_MethodHook {

    public static String hookPackage = "";
    public static boolean needToast;
    XC_LoadPackage.LoadPackageParam lpparam;


    HookApplication(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        this.lpparam = loadPackageParam;
    }

    @Override
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        super.afterHookedMethod(param);
        Context context = (Context) param.args[0];


        String label = context != null ? String
                .valueOf(context.getPackageManager().getApplicationLabel(lpparam.appInfo)) : "【包名获取失败】";
        String tip = "包名不匹配，应用名为 = " + label + ",包名为" + lpparam.packageName;

        if (!TextUtils.isEmpty(hookPackage) && hookPackage.contains(lpparam.packageName)) {
            StartupHook.execStartupInit(context, param.thisObject, null, false);
            tip =
                    "包名匹配，执行hook成功，应用名为=" + label + ",包名为" + lpparam.packageName;
        }
    }
}
