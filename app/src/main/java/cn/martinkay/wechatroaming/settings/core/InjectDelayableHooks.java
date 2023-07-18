package cn.martinkay.wechatroaming.settings.core;

import android.content.Context;

import cn.martinkay.wechatroaming.settings.base.IDynamicHook;
import cn.martinkay.wechatroaming.utils.Log;

public class InjectDelayableHooks {

    private static boolean inited = false;

    public static void stepForMainBackgroundStartup(Context context) {
        IDynamicHook[] hooks = HookInstaller.queryAllAnnotatedHooks();
        for (IDynamicHook h : hooks) {
            try {
                Log.e("stepForMainBackgroundStartup: isEnabled" + h.isEnabled() + "  isPreparationRequired " + h.isPreparationRequired());
                if (h.isEnabled()/* && h.isTargetProcess()*/) {
                    if (!h.isPreparationRequired()) {
                        h.initialize(context);
                    } else {
                        Log.e("InjectDelayableHooks/stepForMainBackgroundStartup not init " + h + ", checkPreconditions == false");
                    }
                }
            } catch (Throwable e) {
                Log.e(e);
            }
        }
    }

//    public static void doInitDelayableHooksMP() {
//        for (IDynamicHook h : HookInstaller.queryAllAnnotatedHooks()) {
//            try {
//                if (h.isEnabled() && h.isTargetProcess() && !h.isPreparationRequired()) {
//                    SyncUtils.requestInitHook(HookInstaller.getHookIndex(h), h.getTargetProcesses());
//                    h.initialize();
//                }
//            } catch (Throwable e) {
//                Log.e(e);
//            }
//        }
//    }
}
