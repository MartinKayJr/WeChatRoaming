package cn.martinkay.wechatroaming.settings.startup;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.os.Build;

import com.github.kyuubiran.ezxhelper.init.EzXHelperInit;

import org.lsposed.hiddenapibypass.HiddenApiBypass;

import java.lang.reflect.Field;

import cn.martinkay.wechatroaming.settings.core.MainHook;
import cn.martinkay.wechatroaming.settings.core.NativeCoreBridge;
import cn.martinkay.wechatroaming.utils.Initiator;
import cn.martinkay.wechatroaming.utils.Natives;
import cn.martinkay.wechatroaming.utils.host.HostInfo;

public class StartupRoutine {
    private StartupRoutine() {
        throw new AssertionError("No instance for you!");
    }

    /**
     * 从现在开始，kotlin, androidx或第三方库可以在不破坏ART的情况下访问。
     * <p>
     * Kotlin和androidx是危险的，应该在类加载器准备好后调用。
     *
     * @param ctx Application context for host
     */
    public static void execPostStartupInit(Context ctx, Object step, String lpwReserved, boolean bReserved) {
        ensureHiddenApiAccess();
        // init all kotlin utils here
        EzXHelperInit.INSTANCE.initZygote(HookEntry.getInitZygoteStartupParam());
        EzXHelperInit.INSTANCE.initHandleLoadPackage(HookEntry.getLoadPackageParam());
        // resource injection is done somewhere else, do not init it here
        EzXHelperInit.INSTANCE.initAppContext(ctx, false, false);
        EzXHelperInit.INSTANCE.setLogTag("QAuxv");
        HostInfo.init((Application) ctx);
        Initiator.init(ctx.getClassLoader());
        Natives.load(ctx);
        NativeCoreBridge.initNativeCore(ctx.getPackageName(), Build.VERSION.SDK_INT,
                HostInfo.getHostInfo().getVersionName(), HostInfo.getHostInfo().getVersionCode());
        MainHook.getInstance().performHook(ctx, step);
    }


    private static void ensureHiddenApiAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && !isHiddenApiAccessible()) {
            android.util.Log.w("BlackSPider", "Hidden API access not accessible, SDK_INT is " + Build.VERSION.SDK_INT);
            HiddenApiBypass.setHiddenApiExemptions("L");
        }
    }

    @SuppressLint({"BlockedPrivateApi", "PrivateApi"})
    public static boolean isHiddenApiAccessible() {
        Class<?> kContextImpl;
        try {
            kContextImpl = Class.forName("android.app.ContextImpl");
        } catch (ClassNotFoundException e) {
            return false;
        }
        Field mActivityToken = null;
        Field mToken = null;
        try {
            mActivityToken = kContextImpl.getDeclaredField("mActivityToken");
        } catch (NoSuchFieldException ignored) {
        }
        try {
            mToken = kContextImpl.getDeclaredField("mToken");
        } catch (NoSuchFieldException ignored) {
        }
        return mActivityToken != null || mToken != null;
    }
}
