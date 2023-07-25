package cn.martinkay.wechatroaming.config;

import android.os.Environment;

import java.io.File;
import java.io.IOException;

import cn.martinkay.wechatroaming.settings.startup.HookEntry;
import cn.martinkay.wechatroaming.utils.HostInfo;
import cn.martinkay.wechatroaming.utils.Log;

public class SafeModeManager {

    private static SafeModeManager INSTANCE;

    public static final String SAFE_MODE_FILE_NAME = "qauxv_safe_mode";

    private File mSafeModeEnableFile;

    public static SafeModeManager getManager() {
        if (INSTANCE == null) {
            INSTANCE = new SafeModeManager();
        }
        INSTANCE.mSafeModeEnableFile = new File(
                Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/" +
                        HookEntry.sCurrentPackageName + "/" + SAFE_MODE_FILE_NAME
        );
        return INSTANCE;
    }

    private boolean isAvailable() {
        if (HostInfo.isInModuleProcess()) {
            Log.w("SafeModeManager only available in host process, ignored");
            return false;
        }
        return true;
    }

    public boolean isEnabled() {
        return isAvailable() && mSafeModeEnableFile.exists();
    }

    public boolean setEnabled(boolean isEnable) {
        if (!isAvailable()) {
            return false;
        }
        if (HookEntry.sCurrentPackageName == null || HookEntry.sCurrentPackageName.isBlank()) {
            Log.e("Failed to enable or disable safe mode, sCurrentPackageName is null or blank");
            return false;
        }
        if (isEnable) {
            try {
                boolean isCreated = mSafeModeEnableFile.createNewFile();
                if (!isCreated) {
                    throw new IOException("Failed to create file: " + mSafeModeEnableFile.getAbsolutePath());
                }
                return true;
            } catch (SecurityException | IOException e) {
                Log.e("Safe mode enable failed", e);
            }
        } else {
            if (isEnabled()) {
                try {
                    boolean isDeleted = mSafeModeEnableFile.delete();
                    if (!isDeleted) {
                        throw new IOException("Failed to delete file: " + mSafeModeEnableFile.getAbsolutePath());
                    }
                    return true;
                } catch (SecurityException | IOException e) {
                    Log.e("Safe mode disable failed", e);
                }
            } else {
                Log.w("Safe mode is not enabled, ignored");
            }
        }
        return false;
    }
}
