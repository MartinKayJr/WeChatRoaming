package cn.martinkay.wechatroaming.settings.lifecycle;

import android.annotation.SuppressLint;
import android.content.res.AssetManager;
import android.content.res.Resources;

import java.io.File;
import java.lang.reflect.Method;

import cn.martinkay.wechatroaming.R;
import cn.martinkay.wechatroaming.settings.core.MainHook;
import cn.martinkay.wechatroaming.settings.startup.HookEntry;
import cn.martinkay.wechatroaming.utils.Log;
import cn.martinkay.wechatroaming.utils.MainProcess;

/**
 * Inject module Activities into host process and resources injection.
 * <p>
 * Deprecated, private, internal or other restricted APIs will be used.
 *
 * @author cinit
 */
public class Parasitics {

    private Parasitics() {
    }

    private static boolean __stub_hooked = false;
    private static long sResInjectBeginTime = 0;
    private static long sResInjectEndTime = 0;
    private static long sActStubHookBeginTime = 0;
    private static long sActStubHookEndTime = 0;

    public static int getResourceInjectionCost() {
        if (sResInjectEndTime > 0) {
            return (int) (sResInjectEndTime - sResInjectBeginTime);
        }
        return -1;
    }

    public static int getActivityStubHookCost() {
        if (sActStubHookEndTime > 0) {
            return (int) (sActStubHookEndTime - sActStubHookBeginTime);
        }
        return -1;
    }

    @MainProcess
    @SuppressWarnings("JavaReflectionMemberAccess")
    @SuppressLint({"PrivateApi", "DiscouragedPrivateApi"})
    public static void injectModuleResources(Resources res) {
        if (res == null) {
            return;
        }
        try {
            res.getString(R.string.res_inject_success);
            return;
        } catch (Resources.NotFoundException ignored) {
        }
        try {
            String sModulePath = HookEntry.getModulePath();
            if (sModulePath == null) {
                throw new RuntimeException("get module path failed, loader=" + MainHook.class.getClassLoader());
            }
            AssetManager assets = res.getAssets();
            Method addAssetPath = AssetManager.class.getDeclaredMethod("addAssetPath", String.class);
            addAssetPath.setAccessible(true);
            int cookie = (int) addAssetPath.invoke(assets, sModulePath);
            try {
                res.getString(R.string.res_inject_success);
                if (sResInjectEndTime == 0) {
                    sResInjectEndTime = System.currentTimeMillis();
                }
            } catch (Resources.NotFoundException e) {
                Log.e("Fatal: injectModuleResources: test injection failure!");
                Log.e("injectModuleResources: cookie=" + cookie + ", path=" + sModulePath + ", loader=" + MainHook.class.getClassLoader());
                long length = -1;
                boolean read = false;
                boolean exist = false;
                boolean isDir = false;
                try {
                    File f = new File(sModulePath);
                    exist = f.exists();
                    isDir = f.isDirectory();
                    length = f.length();
                    read = f.canRead();
                } catch (Throwable e2) {
                    Log.e(e2);
                }
                Log.e("sModulePath: exists = " + exist + ", isDirectory = " + isDir + ", canRead = " + read + ", fileLength = " + length);
            }
        } catch (Exception e) {
            Log.e(e);
        }
    }
}
