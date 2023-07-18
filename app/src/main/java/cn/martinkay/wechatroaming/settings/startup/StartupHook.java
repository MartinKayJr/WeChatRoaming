package cn.martinkay.wechatroaming.settings.startup;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import java.lang.reflect.Field;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class StartupHook {
    private static StartupHook sInstance;

    public static String hookPackage = "";
    public static boolean needToast;

    private StartupHook() {
    }

    public static StartupHook getInstance() {
        if (sInstance == null) {
            sInstance = new StartupHook();
        }
        return sInstance;
    }

    public static void execStartupInit(Context ctx) {
        ClassLoader classLoader = ctx.getClassLoader();
        if (classLoader == null) {
            throw new AssertionError("ERROR: classLoader == null");
        }
        if ("true".equals(System.getProperty(StartupHook.class.getName()))) {
            XposedBridge.log("Err:BlackSpider reloaded??");
            //I don't know... What happened?
            return;
        }
        System.setProperty(StartupHook.class.getName(), "true");
        injectClassLoader(classLoader);
        StartupRoutine.execPostStartupInit(ctx);
    }

    @SuppressWarnings("JavaReflectionMemberAccess")
    @SuppressLint("DiscouragedPrivateApi")
    private static void injectClassLoader(ClassLoader classLoader) {
        if (classLoader == null) {
            throw new NullPointerException("classLoader == null");
        }
        try {
            Field fParent = ClassLoader.class.getDeclaredField("parent");
            fParent.setAccessible(true);
            ClassLoader mine = StartupHook.class.getClassLoader();
            ClassLoader curr = (ClassLoader) fParent.get(mine);
            if (curr == null) {
                curr = XposedBridge.class.getClassLoader();
            }
            if (!curr.getClass().getName().equals(HybridClassLoader.class.getName())) {
                fParent.set(mine, new HybridClassLoader(curr, classLoader));
            }
        } catch (Exception e) {
            log_e(e);
        }
    }


    public void initialize(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        try {
            XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new HookApplication(lpparam));
        } catch (Throwable e) {
            log_e(e);
            throw e;
        }
    }

    static void log_e(Throwable th) {
        if (th == null) {
            return;
        }
        String msg = Log.getStackTraceString(th);
        Log.e("BlackSpider", msg);
        try {
            XposedBridge.log(th);
        } catch (NoClassDefFoundError e) {
            Log.e("Xposed", msg);
            Log.e("EdXposed-Bridge", msg);
        }
    }
}
