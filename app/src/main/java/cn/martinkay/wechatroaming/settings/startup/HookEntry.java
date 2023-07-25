package cn.martinkay.wechatroaming.settings.startup;

import android.annotation.SuppressLint;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import cn.martinkay.wechatroaming.R;
import cn.martinkay.wechatroaming.utils.host.HostInfoImpl;
import cn.martinkay.wechatroaming.utils.hookstatus.HookStatusInit;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookEntry implements IXposedHookLoadPackage, IXposedHookZygoteInit {
    public static final String PACKAGE_NAME_SELF = "cn.martinkay.wechatroaming";
    //    private static XC_LoadPackage.LoadPackageParam sLoadPackageParam = null;
    public static final String PACKAGE_NAME_WECHAT = "com.tencent.mm";
    private static StartupParam sInitZygoteStartupParam = null;
    private static XC_LoadPackage.LoadPackageParam sLoadPackageParam = null;

    private static String sModulePath = null;
    public static String sCurrentPackageName = null;
    /**
     * 由于是多APP HOOK，因此每一个lpp都需要单独管理
     */
    private static Map<String, XC_LoadPackage.LoadPackageParam> sLoadPackageParamMap = new HashMap<>();

    public static Map<String, HostInfoImpl> hostInfoMap = new HashMap();

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (R.string.res_inject_success >>> 24 == 0x7f) {
            XposedBridge.log("package id must NOT be 0x7f, reject loading...");
            return;
        }
        sLoadPackageParam = lpparam;
        // check LSPosed dex-obfuscation
        Class<?> kXposedBridge = XposedBridge.class;
        if (!"de.robv.android.xposed.XposedBridge".equals(kXposedBridge.getName())) {
            String className = kXposedBridge.getName();
            String pkgName = className.substring(0, className.lastIndexOf('.'));
            HybridClassLoader.setObfuscatedXposedApiPackage(pkgName);
        }
        switch (lpparam.packageName) {
            case PACKAGE_NAME_SELF: {
                HookStatusInit.init(lpparam.classLoader);
                break;
            }
            case PACKAGE_NAME_WECHAT: {
                if (sInitZygoteStartupParam == null) {
                    throw new IllegalStateException("handleLoadPackage: sInitZygoteStartupParam is null");
                }
                sCurrentPackageName = lpparam.packageName;
                // TODO
                StartupHook.getInstance().initialize(lpparam);
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        sInitZygoteStartupParam = startupParam;
        sModulePath = startupParam.modulePath;
    }

    /**
     * Get the {@link XC_LoadPackage.LoadPackageParam} of the current module.
     * <p>
     * Do NOT add @NonNull annotation to this method. *** No kotlin code should be invoked here.*** May cause a crash.
     *
     * @return the LoadPackageParam
     */
    public static XC_LoadPackage.LoadPackageParam getLoadPackageParam() {
        if (sLoadPackageParam == null) {
            throw new IllegalStateException("LoadPackageParam is null");
        }
        return sLoadPackageParam;
    }

    /**
     * Get the path of the current module.
     * <p>
     * Do NOT add @NonNull annotation to this method. *** No kotlin code should be invoked here.*** May cause a crash.
     *
     * @return the module path
     */
    public static String getModulePath() {
        if (sModulePath == null) {
            throw new IllegalStateException("Module path is null");
        }
        return sModulePath;
    }

    /**
     * Get the {@link StartupParam} of the current module.
     * <p>
     * Do NOT add @NonNull annotation to this method. *** No kotlin code should be invoked here.*** May cause a crash.
     *
     * @return the initZygote param
     */
    public static StartupParam getInitZygoteStartupParam() {
        if (sInitZygoteStartupParam == null) {
            throw new IllegalStateException("InitZygoteStartupParam is null");
        }
        return sInitZygoteStartupParam;
    }
}
