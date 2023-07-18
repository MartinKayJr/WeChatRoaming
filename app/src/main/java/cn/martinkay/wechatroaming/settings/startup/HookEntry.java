package cn.martinkay.wechatroaming.settings.startup;

import android.annotation.SuppressLint;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import cn.martinkay.wechatroaming.R;
import cn.martinkay.wechatroaming.utils.HostInfoImpl;
import cn.martinkay.wechatroaming.utils.hookstatus.HookStatusInit;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookEntry implements IXposedHookLoadPackage, IXposedHookZygoteInit {
    public static final String PACKAGE_NAME_SELF = "cn.martinkay.wechatroaming";
    //    private static XC_LoadPackage.LoadPackageParam sLoadPackageParam = null;
    private static StartupParam sInitZygoteStartupParam = null;
    private static String sModulePath = null;
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
        // check LSPosed dex-obfuscation
        Class<?> kXposedBridge = XposedBridge.class;
        if (!"de.robv.android.xposed.XposedBridge".equals(kXposedBridge.getName())) {
            String className = kXposedBridge.getName();
            String pkgName = className.substring(0, className.lastIndexOf('.'));
            HybridClassLoader.setObfuscatedXposedApiPackage(pkgName);
        }
        if (PACKAGE_NAME_SELF.equals(lpparam.packageName)) {
            HookStatusInit.init(lpparam.classLoader);
            return;
        }
        if (sInitZygoteStartupParam == null) {
            throw new IllegalStateException("handleLoadPackage: sInitZygoteStartupParam is null");
        }
        sLoadPackageParamMap.put(lpparam.packageName, lpparam);
        StartupHook.getInstance().initialize(lpparam);

        // TODO 多模块以及针对模块的HOOK
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
     * @param packageName application package name
     * @return the LoadPackageParam
     */
    public static XC_LoadPackage.LoadPackageParam getLoadPackageParam(String packageName) {
        if (sLoadPackageParamMap == null || sLoadPackageParamMap.size() <= 0) {
            throw new IllegalStateException("LoadPackageParam is null");
        }
        return sLoadPackageParamMap.get(packageName);
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
