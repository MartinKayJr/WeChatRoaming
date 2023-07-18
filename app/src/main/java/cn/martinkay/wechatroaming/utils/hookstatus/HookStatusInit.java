package cn.martinkay.wechatroaming.utils.hookstatus;

import java.lang.reflect.Field;

import de.robv.android.xposed.XposedBridge;

/**
 * Called in handleLoadPackage, NO KOTLIN, NO ANDROIDX
 **/
public class HookStatusInit {

    private HookStatusInit() {
    }

    public static void init(ClassLoader classLoader) throws Throwable {
        Class<?> kHookStatusImpl = classLoader.loadClass("cn.martinkay.wechatroaming.utils.hookstatus.HookStatusImpl");
        Field f = kHookStatusImpl.getDeclaredField("sZygoteHookMode");
        f.setAccessible(true);
        f.set(null, true);
        boolean dexObfsEnabled = !"de.robv.android.xposed.XposedBridge".equals(XposedBridge.class.getName());
        String hookProvider = null;
        if (dexObfsEnabled) {
            f = kHookStatusImpl.getDeclaredField("sIsLsposedDexObfsEnabled");
            f.setAccessible(true);
            f.set(null, true);
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
            f = kHookStatusImpl.getDeclaredField("sZygoteHookProvider");
            f.setAccessible(true);
            f.set(null, hookProvider);
        }
    }
}
