package cn.martinkay.wechatroaming.settings.core;

import android.content.Context;
import android.system.Os;
import android.system.StructUtsname;

import cn.martinkay.wechatroaming.settings.lifecycle.Parasitics;
import cn.martinkay.wechatroaming.utils.Log;
import cn.martinkay.wechatroaming.utils.SyncUtils;

public class MainHook {

    private static MainHook SELF;

    public static final String KEY_SAFE_MODE = "safe_mode";

    boolean third_stage_inited = false;

    private MainHook() {
    }

    public static MainHook getInstance() {
        if (SELF == null) {
            SELF = new MainHook();
        }
        return SELF;
    }

    private static void injectLifecycleForProcess(Context ctx) {
        if (ctx != null) {
            if (ctx.getApplicationContext() != null) {
                Parasitics.injectModuleResources(ctx.getApplicationContext().getResources());
            } else {
                Parasitics.injectModuleResources(ctx.getResources());
            }
        }

//        if (SyncUtils.isMainProcess()) {
//            Parasitics.injectModuleResources(ctx.getApplicationContext().getResources());
//        }

//        if (SyncUtils.isTargetProcess(SyncUtils.PROC_MAIN | SyncUtils.PROC_PEAK | SyncUtils.PROC_TOOL)) {
//            Parasitics.initForStubActivity(ctx);
//        }
//        if (SyncUtils.isTargetProcess(SyncUtils.PROC_MAIN | SyncUtils.PROC_TOOL)) {
//            try {
//                ShadowFileProvider.initHookForFileProvider();
//            } catch (ReflectiveOperationException e) {
//                Log.e(e);
//            }
//        }
    }

    public void performHook(Context ctx) {
        SyncUtils.initBroadcast(ctx);
        injectLifecycleForProcess(ctx);
        if (isWindowsSubsystemForAndroid()) {
            Log.w("WSA detected, aggressive resource injection is required to prevent ResourceNotFound crash.");
            // TODO: 2023-1-20 implement aggressive resource injection
        }
//        boolean safeMode = ConfigManager.getDefaultConfig().getBooleanOrDefault(KEY_SAFE_MODE, false);
//        if (safeMode) {
//            LicenseStatus.sDisableCommonHooks = true;
//            Log.i("Safe mode enabled, disable hooks");
//        }
//        if (!safeMode) {
//            HookInstaller.allowEarlyInit(RevokeMsgHook.INSTANCE);
//            HookInstaller.allowEarlyInit(MuteQZoneThumbsUp.INSTANCE);
//            HookInstaller.allowEarlyInit(MuteAtAllAndRedPacket.INSTANCE);
//            HookInstaller.allowEarlyInit(GagInfoDisclosure.INSTANCE);
//            HookInstaller.allowEarlyInit(CustomSplash.INSTANCE);
//            HookInstaller.allowEarlyInit(RemoveCameraButton.INSTANCE);
//            HookInstaller.allowEarlyInit(RemoveSuperQQShow.INSTANCE);
//            HookInstaller.allowEarlyInit(FileRecvRedirect.INSTANCE);
//            HookInstaller.allowEarlyInit(OptXListViewScrollBar.INSTANCE);
//            HookInstaller.allowEarlyInit(ForcePadMode.INSTANCE);
//        }
        if (!isForegroundStartupForMainProcess(ctx) /*&& !safeMode*/) {
            // since we are in background, we can do some heavy work without compromising user experience
            InjectDelayableHooks.stepForMainBackgroundStartup(ctx);
        }
    }

    private static boolean isForegroundStartupForMainProcess(Context ctx) {
        // TODO: 2022-12-03 find a way to detect foreground startup
        // XXX: BaseApplicationImpl.sIsBgStartup does not work, always false
        return false;
    }


    public static boolean isWindowsSubsystemForAndroid() {
        StructUtsname uts = Os.uname();
        // XXX: is this reliable?
        return uts.release.contains("-windows-subsystem-for-android-");
    }
}
