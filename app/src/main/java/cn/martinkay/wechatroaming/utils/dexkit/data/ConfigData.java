package cn.martinkay.wechatroaming.utils.dexkit.data;

import android.os.Looper;

import cn.martinkay.wechatroaming.config.ConfigManager;
import cn.martinkay.wechatroaming.utils.Log;


public class ConfigData<T> {

    final String mKeyName;
    final ConfigManager mgr;

    public ConfigData(String keyName) {
        this(keyName, ConfigManager.getDefaultConfig());
    }

    public ConfigData(String keyName, ConfigManager manager) {
        mKeyName = keyName;
        mgr = manager;
    }

    public void remove() {
        try {
            mgr.remove(mKeyName);
        } catch (Exception e) {
            Log.e(e);
        }
    }

    public T getValue() {
        try {
            return (T) mgr.getObject(mKeyName);
        } catch (Exception e) {
            try {
                mgr.remove(mKeyName);
            } catch (Exception ignored) {
            }
            Log.e(e);
            return null;
        }
    }

    public void setValue(T value) {
        try {
            mgr.putObject(mKeyName, value);
            mgr.save();
        } catch (Exception e) {
            try {
                mgr.remove(mKeyName);
            } catch (Exception ignored) {
            }
            Log.e(e);
            if (Looper.myLooper() == Looper.getMainLooper()) {
//                Toasts.error(hostInfo.getApplication(), "设置存储失败, 请重新设置" + e + "");
            } else {
//                SyncUtils.post(() -> Toasts.error(HostInfo.getApplication(), "设置存储失败, 请重新设置" + e + ""));
            }
        }
    }

    public T getOrDefault(T def) {
        try {
            return (T) mgr.getOrDefault(mKeyName, def);
        } catch (Exception e) {
            Log.e(e);
            return def;
        }
    }
}
