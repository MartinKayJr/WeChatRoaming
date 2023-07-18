package cn.martinkay.wechatroaming.config;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public abstract class ConfigManager implements SharedPreferences, SharedPreferences.Editor {

    private static ConfigManager sDefConfig;
    private static ConfigManager sCache;
    private static final ConcurrentHashMap<Long, ConfigManager> sUinConfig =
            new ConcurrentHashMap<>(4);

    protected ConfigManager() {
    }

    private static Context ctx;

    public static Context getCtx() {
        return ctx;
    }

    public static void setCtx(Context ctx) {
        ConfigManager.ctx = ctx;
    }

    @NonNull
    public static synchronized ConfigManager getDefaultConfig() {
        if (sDefConfig == null) {
            sDefConfig = new MmkvConfigManagerImpl("global_config");
//            try {
//                sDefConfig = new MmkvConfigManagerImpl("global_config");
//            } catch (Exception ignored) {
//                sDefConfig = new MultiProcessConfigManagerImpl(ConfigManager.getCtx());
//            }
        }
        return sDefConfig;
    }



    @NonNull
    public static synchronized ConfigManager getCache() {
        if (sCache == null) {
            sCache = new MmkvConfigManagerImpl("global_cache");
        }
        return sCache;
    }

    @Nullable
    public abstract File getFile();

    @Nullable
    public Object getOrDefault(@NonNull String key, @Nullable Object def) {
        if (!containsKey(key)) {
            return def;
        }
        return getObject(key);
    }

    public boolean getBooleanOrFalse(@NonNull String key) {
        return getBooleanOrDefault(key, false);
    }

    public boolean getBooleanOrDefault(@NonNull String key, boolean def) {
        return getBoolean(key, def);
    }

    public int getIntOrDefault(@NonNull String key, int def) {
        return getInt(key, def);
    }

    @Nullable
    public abstract String getString(@NonNull String key);

    @NonNull
    public String getStringOrDefault(@NonNull String key, @NonNull String defVal) {
        return getString(key, defVal);
    }

    @Nullable
    public abstract Object getObject(@NonNull String key);

    @Nullable
    public byte[] getBytes(@NonNull String key) {
        return getBytes(key, null);
    }

    @Nullable
    public abstract byte[] getBytes(@NonNull String key, @Nullable byte[] defValue);

    @NonNull
    public abstract byte[] getBytesOrDefault(@NonNull String key, @NonNull byte[] defValue);

    @NonNull
    public abstract ConfigManager putBytes(@NonNull String key, @NonNull byte[] value);

    /**
     * @return READ-ONLY all config
     * @deprecated Avoid use getAll(), MMKV only have limited support for this.
     */
    @Override
    @Deprecated
    @NonNull
    public abstract Map<String, ?> getAll();

    public abstract void save();

    public long getLongOrDefault(@Nullable String key, long i) {
        return getLong(key, i);
    }

    @NonNull
    public abstract ConfigManager putObject(@NonNull String key, @NonNull Object v);

    public boolean containsKey(@NonNull String k) {
        return contains(k);
    }

    @NonNull
    @Override
    public Editor edit() {
        return this;
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(
            @NonNull OnSharedPreferenceChangeListener listener) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(
            @NonNull OnSharedPreferenceChangeListener listener) {
        throw new UnsupportedOperationException("not implemented");
    }

    public abstract boolean isReadOnly();

    public abstract boolean isPersistent();
}
