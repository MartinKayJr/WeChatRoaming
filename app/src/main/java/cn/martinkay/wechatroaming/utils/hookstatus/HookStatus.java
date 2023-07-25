package cn.martinkay.wechatroaming.utils.hookstatus;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.util.HashMap;

import cn.martinkay.wechatroaming.BuildConfig;
import cn.martinkay.wechatroaming.R;
import cn.martinkay.wechatroaming.utils.HostInfo;
import cn.martinkay.wechatroaming.utils.NonUiThread;
import cn.martinkay.wechatroaming.utils.SyncUtils;
import de.robv.android.xposed.XposedBridge;
/**
 * This class is only intended to be used in module process, not in host process.
 */
public class HookStatus {

    private HookStatus() {
    }

    private static boolean sExpCpCalled = false;
    private static boolean sExpCpResult = false;

    public enum HookType {
        /**
         * No hook.
         */
        NONE,
        /**
         * Taichi, BugHook(not implemented), etc.
         */
        APP_PATCH,
        /**
         * Legacy Xposed, EdXposed, LSPosed, Dreamland, etc.
         */
        ZYGOTE,
    }

    @Nullable
    public static String getZygoteHookProvider() {
        return HookStatusImpl.sZygoteHookProvider;
    }

    public static boolean isLsposedDexObfsEnabled() {
        return HookStatusImpl.sIsLsposedDexObfsEnabled;
    }

    public static boolean isZygoteHookMode() {
        return HookStatusImpl.sZygoteHookMode;
    }

    public static boolean isLegacyXposed() {
        try {
            ClassLoader.getSystemClassLoader().loadClass("de.robv.android.xposed.XposedBridge");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean isElderDriverXposed() {
        return new File("/system/framework/edxp.jar").exists();
    }

    @NonUiThread
    public static boolean callTaichiContentProvider(@NonNull Context context) {
        try {
            ContentResolver contentResolver = context.getContentResolver();
            Uri uri = Uri.parse("content://me.weishu.exposed.CP/");
            Bundle result = new Bundle();
            try {
                result = contentResolver.call(uri, "active", null, null);
            } catch (RuntimeException e) {
                // TaiChi is killed, try invoke
                try {
                    Intent intent = new Intent("me.weishu.exp.ACTION_ACTIVE");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                } catch (ActivityNotFoundException anfe) {
                    return false;
                }
            }
            if (result == null) {
                result = contentResolver.call(uri, "active", null, null);
            }
            if (result == null) {
                return false;
            }
            return result.getBoolean("active", false);
        } catch (Exception e) {
            return false;
        }
    }

    public static void init(@NonNull Context context) {
        if (context.getPackageName().equals(BuildConfig.APPLICATION_ID)) {
            SyncUtils.async(() -> {
                sExpCpCalled = callTaichiContentProvider(context);
                sExpCpResult = sExpCpCalled;
            });
        } else {
            // in host process???
            try {
                initHookStatusImplInHostProcess();
            } catch (LinkageError ignored) {
            }
        }
    }

    public static HookType getHookType() {
        if (isZygoteHookMode()) {
            return HookType.ZYGOTE;
        }
        return sExpCpResult ? HookType.APP_PATCH : HookType.NONE;
    }

    private static void initHookStatusImplInHostProcess() throws LinkageError {
        boolean dexObfsEnabled = !"de.robv.android.xposed.XposedBridge".equals(XposedBridge.class.getName());
        String hookProvider = null;
        if (dexObfsEnabled) {
            HookStatusImpl.sIsLsposedDexObfsEnabled = true;
            hookProvider = "LSPosed";
        } else {
            String bridgeTag = null;
            try {
                bridgeTag = (String) XposedBridge.class.getDeclaredField("TAG").get(null);
            } catch (ReflectiveOperationException ignored) {
            }
            if (bridgeTag != null) {
                if (bridgeTag.startsWith("LSPosed")) {
                    hookProvider = "LSPosed";
                } else if (bridgeTag.startsWith("EdXposed")) {
                    hookProvider = "EdXposed";
                }
            }
        }
        if (hookProvider != null) {
            HookStatusImpl.sZygoteHookProvider = hookProvider;
        }
    }

    public static String getHookProviderName() {
        if (isZygoteHookMode()) {
            String name = getZygoteHookProvider();
            if (name != null) {
                return name;
            }
            if (isLegacyXposed()) {
                return "Legacy Xposed";
            }
            if (isElderDriverXposed()) {
                return "EdXposed";
            }
            return "Unknown(Zygote)";
        }
        if (sExpCpResult) {
            return "Taichi";
        }
        return "None";
    }

    public static boolean isTaiChiInstalled(@NonNull Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            pm.getPackageInfo("me.weishu.exp", 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static boolean isModuleEnabled() {
        return getHookType() != HookType.NONE;
    }

    public static HashMap<String, String> getHostABI() {
        CharSequence[] scope = HostInfo.getApplication().getResources().getTextArray(R.array.xposedscope);
        HashMap<String, String> result = new HashMap<>(4);
        for (CharSequence s : scope) {
            String abi = AbiUtils.getApplicationActiveAbi(s.toString());
            if (abi != null) {
                result.put(s.toString(), abi);
            }
        }
        return result;
    }
}
