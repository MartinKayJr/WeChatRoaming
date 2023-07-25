package cn.martinkay.wechatroaming.utils;

import android.app.Application;

import androidx.annotation.NonNull;

/**
 * Helper class for getting host information. Keep it as simple as possible.
 */
public class HostInfo {

    public static final String PACKAGE_NAME_QQ = "com.tencent.mobileqq";
    public static final String PACKAGE_NAME_SELF = "cn.martinkay.wechatroaming";

    private HostInfo() {
        throw new AssertionError("No instance for you!");
    }

    @NonNull
    public static Application getApplication() {
        return cn.martinkay.wechatroaming.utils.host.HostInfo.getHostInfo().getApplication();
    }

    @NonNull
    public static String getPackageName() {
        return cn.martinkay.wechatroaming.utils.host.HostInfo.getHostInfo().getPackageName();
    }

    @NonNull
    public static String getAppName() {
        return cn.martinkay.wechatroaming.utils.host.HostInfo.getHostInfo().getHostName();
    }

    @NonNull
    public static String getVersionName() {
        return cn.martinkay.wechatroaming.utils.host.HostInfo.getHostInfo().getVersionName();
    }

    public static int getVersionCode32() {
        return cn.martinkay.wechatroaming.utils.host.HostInfo.getHostInfo().getVersionCode32();
    }

    public static int getVersionCode() {
        return getVersionCode32();
    }

    public static long getLongVersionCode() {
        return cn.martinkay.wechatroaming.utils.host.HostInfo.getHostInfo().getVersionCode();
    }

    public static boolean isInModuleProcess() {
        return cn.martinkay.wechatroaming.utils.host.HostInfo.isInModuleProcess();
    }

    public static boolean isInHostProcess() {
        return !isInModuleProcess();
    }

    public static boolean isAndroidxFileProviderAvailable() {
        return cn.martinkay.wechatroaming.utils.host.HostInfo.isAndroidxFileProviderAvailable();
    }

    public static boolean requireMinWechatVersion(long versionCode) {
        return getLongVersionCode() >= versionCode;
    }
}
