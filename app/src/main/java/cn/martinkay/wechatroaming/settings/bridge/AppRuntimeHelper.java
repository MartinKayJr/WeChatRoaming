
package cn.martinkay.wechatroaming.settings.bridge;

import androidx.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.jetbrains.annotations.Contract;

import cn.martinkay.wechatroaming.utils.Initiator;
import cn.martinkay.wechatroaming.utils.IoUtils;
import cn.martinkay.wechatroaming.utils.Log;
import cn.martinkay.wechatroaming.utils.MainProcess;
import cn.martinkay.wechatroaming.utils.Reflex;

public class AppRuntimeHelper {

    private static Field f_mAppRuntime = null;

    private AppRuntimeHelper() {
    }

    // TODO getLongAccountUin
    public static long getLongAccountUin() {
        return -1;
    }

//    public static long getLongAccountUin() {
//        try {
//            AppRuntime rt = getAppRuntime();
//            if (rt == null) {
//                // getLongAccountUin/E getAppRuntime == null
//                return -1;
//            }
//            return (long) Reflex.invokeVirtual(rt, "getLongAccountUin");
//        } catch (ReflectiveOperationException e) {
//            Log.e(e);
//            IoUtils.unsafeThrowForIteCause(e);
//            // unreachable
//        }
//        return -1;
//    }
//
//    @MainProcess
//    public static AppRuntime getQQAppInterface() {
//        AppRuntime art = getAppRuntime();
//        if (art == null) {
//            return null;
//        }
//        if (Initiator._QQAppInterface().isAssignableFrom(art.getClass())) {
//            return art;
//        } else {
//            throw new IllegalStateException("QQAppInterface is not available in current process");
//        }
//    }

    /**
     * Peek the AppRuntime instance.
     * @return AppRuntime instance, or null if not ready.
     */
//    @Nullable
//    @MainProcess
//    public static AppRuntime getAppRuntime() {
//        Object sMobileQQ = MobileQQ.sMobileQQ;
//        if (sMobileQQ == null) {
//            return null;
//        }
//        try {
//            if (f_mAppRuntime == null) {
//                f_mAppRuntime = MobileQQ.class.getDeclaredField("mAppRuntime");
//                f_mAppRuntime.setAccessible(true);
//            }
//            return (AppRuntime) f_mAppRuntime.get(sMobileQQ);
//        } catch (ReflectiveOperationException e) {
//            Log.e(e);
//            IoUtils.unsafeThrowForIteCause(e);
//            // unreachable
//            return null;
//        }
//    }
//
//    public static String getAccount() {
//        Object rt = getAppRuntime();
//        try {
//            return (String) Reflex.invokeVirtual(rt, "getAccount");
//        } catch (ReflectiveOperationException e) {
//            Log.e(e);
//            IoUtils.unsafeThrowForIteCause(e);
//            // unreachable
//            return null;
//        }
//    }

    @Contract("null -> fail")
    public static void checkUinValid(String uin) {
        if (uin == null || uin.length() == 0) {
            throw new IllegalArgumentException("uin is empty");
        }
        try {
            // allow cases like 9915...
            if (Long.parseLong(uin) < 1000) {
                throw new IllegalArgumentException("uin is invalid: " + uin);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("uin is invalid: " + uin);
        }
    }

    private static Method sMethodGetServerTime = null;

    public static long getServerTime() throws ReflectiveOperationException {
        if (sMethodGetServerTime == null) {
            sMethodGetServerTime = Initiator.loadClass("com.tencent.mobileqq.msf.core.NetConnInfoCenter").getDeclaredMethod("getServerTime");
        }
        return (Long) sMethodGetServerTime.invoke(null);
    }
}
