package cn.martinkay.wechatroaming.utils;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.martinkay.wechatroaming.settings.base.IDynamicHook;
import cn.martinkay.wechatroaming.settings.core.HookInstaller;
import cn.martinkay.wechatroaming.settings.core.HookInstaller;

@SuppressLint("PrivateApi")
public class SyncUtils {
    private static Handler sHandler;

    public static final int PROC_MAIN = 1;
    private static int mProcType = 0;
    private static String mProcName = null;

    public static final String HOOK_DO_INIT = "cn.martinkay.wechatroaming.HOOK_DO_INIT";

    private static boolean inited = false;

    private static int myId = 0;
    private static final Collection<BroadcastListener> sBroadcastListeners = Collections
            .synchronizedCollection(new HashSet<>());
    private static final ExecutorService sExecutor = Executors.newCachedThreadPool();

    private SyncUtils() {
        throw new AssertionError("No instance for you!");
    }

    public static void initBroadcast(Context ctx) {
        if (inited) {
            return;
        }
        BroadcastReceiver recv = new IpcReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(HOOK_DO_INIT);
        ctx.registerReceiver(recv, filter);
        inited = true;
    }

    public static void requestInitHook(int hookId, int process) {
        Context ctx = HostInfo.getApplication();
        Intent changed = new Intent(HOOK_DO_INIT);
        changed.setPackage(ctx.getPackageName());
        initId();
        changed.putExtra("process", process);
        changed.putExtra("hook", hookId);
        ctx.sendBroadcast(changed);
    }

    public static void initId() {
        if (myId == 0) {
            myId = (int) ((Math.random()) * (Integer.MAX_VALUE / 4));
        }
    }

    public static int getProcessType() {
        if (mProcType != 0) {
            return mProcType;
        }
        String[] parts = getProcessName().split(":");
        if (parts.length == 1) {
            if (parts[0].equals("unknown")) {
                return PROC_MAIN;
            } else {
                mProcType = PROC_MAIN;
            }
        } else {
            String tail = parts[parts.length - 1];
        }
        return mProcType;
    }

    public static boolean isMainProcess() {
        return getProcessType() == PROC_MAIN;
    }

    public static boolean isTargetProcess(int target) {
        return (getProcessType() & target) != 0;
    }

    public static String getProcessName() {
        if (mProcName != null) {
            return mProcName;
        }
        String name = "unknown";
        int retry = 0;
        do {
            try {
                List<ActivityManager.RunningAppProcessInfo> runningAppProcesses =
                        ((ActivityManager) HostInfo.getApplication().getSystemService(Context.ACTIVITY_SERVICE))
                                .getRunningAppProcesses();
                if (runningAppProcesses != null) {
                    for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : runningAppProcesses) {
                        if (runningAppProcessInfo != null
                                && runningAppProcessInfo.pid == android.os.Process.myPid()) {
                            mProcName = runningAppProcessInfo.processName;
                            return runningAppProcessInfo.processName;
                        }
                    }
                }
            } catch (Throwable e) {
                Log.e("getProcessName error " + e);
            }
            retry++;
            if (retry >= 3) {
                break;
            }
        } while ("unknown".equals(name));
        return name;
    }

    @SuppressLint("LambdaLast")
    public static void postDelayed(@NonNull Runnable r, long ms) {
        if (sHandler == null) {
            sHandler = new Handler(Looper.getMainLooper());
        }
        sHandler.postDelayed(r, ms);
    }

    public static void runOnUiThread(@NonNull Runnable r) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            r.run();
        } else {
            post(r);
        }
    }

    public static void postDelayed(long ms, @NonNull Runnable r) {
        postDelayed(r, ms);
    }

    public static void post(@NonNull Runnable r) {
        postDelayed(r, 0L);
    }

    public static void async(@NonNull Runnable r) {
        sExecutor.execute(r);
    }

    public interface BroadcastListener {

        boolean onReceive(Context context, Intent intent);
    }

    private static class IpcReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context ctx, Intent intent) {
            String action = intent.getAction();
            if (action == null) {
                return;
            }
            boolean done = false;
            for (BroadcastListener bl : sBroadcastListeners) {
                done = done || bl.onReceive(ctx, intent);
            }
            if (done) {
                return;
            }
            switch (action) {
                case HOOK_DO_INIT:
                    int myType = getProcessType();
                    int targetType = intent.getIntExtra("process", 0);
                    int hookId = intent.getIntExtra("hook", -1);
                    if (hookId != -1 && (myType & targetType) != 0) {
                        IDynamicHook hook = HookInstaller.getHookById(hookId);
                        if (hook != null && hook.isTargetProcess() && !hook.isPreparationRequired()) {
                            try {
                                hook.initialize();
                            } catch (Throwable e) {
                                Log.e(e);
                            }
                        }
                    }
                    break;
                default:
                    Log.e("Unknown action: " + action);
                    break;
            }
        }
    }

}
