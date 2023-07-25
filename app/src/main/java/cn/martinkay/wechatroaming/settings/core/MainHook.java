package cn.martinkay.wechatroaming.settings.core;

import android.content.Context;
import android.system.Os;
import android.system.StructUtsname;

import java.lang.reflect.Method;

import cn.martinkay.wechatroaming.config.SafeModeManager;
import cn.martinkay.wechatroaming.settings.lifecycle.Parasitics;
import cn.martinkay.wechatroaming.utils.Initiator;
import cn.martinkay.wechatroaming.utils.LicenseStatus;
import cn.martinkay.wechatroaming.utils.Log;
import cn.martinkay.wechatroaming.utils.SyncUtils;
import cn.martinkay.wechatroaming.utils.host.HostInfo;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

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

    public void performHook(Context ctx, Object step) {
        SyncUtils.initBroadcast(ctx);
        injectLifecycleForProcess(ctx);
//        if (HostInfo.isQQHD()) {
//        }
        if (isWindowsSubsystemForAndroid()) {
            Log.w("WSA detected, aggressive resource injection is required to prevent ResourceNotFound crash.");
            // TODO: 2023-1-20 implement aggressive resource injection
        }
        boolean safeMode = SafeModeManager.getManager().isEnabled();
        if (safeMode) {
            LicenseStatus.sDisableCommonHooks = true;
            Log.i("Safe mode enabled, disable hooks");
        }
        if (!safeMode) {
//            HookInstaller.allowEarlyInit(DisableQQCrashReportManager.INSTANCE);
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
        }
        if (SyncUtils.isMainProcess()) {
//            ConfigItems.removePreviousCacheIfNecessary();
//            JumpActivityEntryHook.initForJumpActivityEntry(ctx);
            if (!isForegroundStartupForMainProcess(ctx, step) && !safeMode) {
                // since we are in background, we can do some heavy work without compromising user experience
                InjectDelayableHooks.stepForMainBackgroundStartup();
            }
            Class<?> loadData = Initiator.load("com/tencent/mobileqq/startup/step/LoadData");
            if (loadData != null) {
                Method doStep = null;
                for (Method method : loadData.getDeclaredMethods()) {
                    if (method.getReturnType().equals(boolean.class) && method.getParameterTypes().length == 0) {
                        doStep = method;
                        break;
                    }
                }
                XposedBridge.hookMethod(doStep, new XC_MethodHook(51) {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        if (third_stage_inited) {
                            return;
                        }
//                        Object dir = getStartDirector(param.thisObject);
                        if (safeMode) {
//                            SettingEntryHook.INSTANCE.initialize();
                        } else {
//                            InjectDelayableHooks.step(dir);
                        }
                        third_stage_inited = true;
                    }
                });
            } else {
                Log.w("LoadData not found, running third stage hooks in background");
//                InjectDelayableHooks.step(null);
            }
        } else {
//            if (!safeMode && LicenseStatus.hasUserAcceptEula()) {
//                Object dir = getStartDirector(step);
//                InjectDelayableHooks.step(dir);
//            }
        }
    }


    private static boolean isForegroundStartupForMainProcess(Context ctx, Object step) {
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
