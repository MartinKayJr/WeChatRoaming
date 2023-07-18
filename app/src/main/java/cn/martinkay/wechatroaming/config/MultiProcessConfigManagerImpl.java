package cn.martinkay.wechatroaming.config;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.util.Map;
import java.util.Set;

import cn.martinkay.wechatroaming.utils.MultiprocessSharedPreferences;


public class MultiProcessConfigManagerImpl extends ConfigManager {

    private final SharedPreferences sharedPreferences;

    protected MultiProcessConfigManagerImpl(Context context) {
        MultiprocessSharedPreferences.setAuthority("cn.martinkay.wechatroaming.provider");
        sharedPreferences = MultiprocessSharedPreferences
                .getSharedPreferences(context, "cn.martinkay.wechatroaming", MODE_PRIVATE);
    }


    @Nullable
    @Override
    public File getFile() {
        return null;
    }

    @Nullable
    @Override
    public String getString(@NonNull String key) {
        return sharedPreferences.getString(key, null);
    }

    @Nullable
    @Override
    public Object getObject(@NonNull String key) {
        return null;
    }

    @Nullable
    @Override
    public byte[] getBytes(@NonNull String key, @Nullable byte[] defValue) {
        return new byte[0];
    }

    @NonNull
    @Override
    public byte[] getBytesOrDefault(@NonNull String key, @NonNull byte[] defValue) {
        return new byte[0];
    }

    @NonNull
    @Override
    public ConfigManager putBytes(@NonNull String key, @NonNull byte[] value) {
        return null;
    }

    @NonNull
    @Override
    public Map<String, ?> getAll() {
        return sharedPreferences.getAll();
    }

    @Nullable
    @Override
    public String getString(String key, @Nullable String defValue) {
        return sharedPreferences.getString(key, defValue);
    }

    @Nullable
    @Override
    public Set<String> getStringSet(String key, @Nullable Set<String> defValues) {
        return sharedPreferences.getStringSet(key, defValues);
    }

    @Override
    public int getInt(String key, int defValue) {
        return sharedPreferences.getInt(key, defValue);
    }

    @Override
    public long getLong(String key, long defValue) {
        return sharedPreferences.getLong(key, defValue);
    }

    @Override
    public float getFloat(String key, float defValue) {
        return sharedPreferences.getFloat(key, defValue);
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        return sharedPreferences.getBoolean(key, defValue);
    }

    @Override
    public boolean contains(String key) {
        return sharedPreferences.contains(key);
    }

    @Override
    public void save() {
        commit();
    }

    @NonNull
    @Override
    public ConfigManager putObject(@NonNull String key, @NonNull Object v) {
        return null;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public boolean isPersistent() {
        return true;
    }

    @Override
    public Editor putString(String key, @Nullable String value) {
        sharedPreferences.edit().putString(key, value).apply();
        return this;
    }

    @Override
    public Editor putStringSet(String key, @Nullable Set<String> values) {
        sharedPreferences.edit().putStringSet(key, values).apply();
        return this;
    }

    @Override
    public Editor putInt(String key, int value) {
        sharedPreferences.edit().putInt(key, value).apply();
        return this;
    }

    @Override
    public Editor putLong(String key, long value) {
        sharedPreferences.edit().putLong(key, value).apply();
        return this;
    }

    @Override
    public Editor putFloat(String key, float value) {
        sharedPreferences.edit().putFloat(key, value).apply();
        return this;
    }

    @Override
    public Editor putBoolean(String key, boolean value) {
        sharedPreferences.edit().putBoolean(key, value).apply();
        return this;
    }

    @Override
    public Editor remove(String key) {
        sharedPreferences.edit().remove(key).apply();
        return this;
    }

    @Override
    public Editor clear() {
        sharedPreferences.edit().clear().apply();
        return this;
    }

    @Override
    public boolean commit() {
        return true;
    }

    @Override
    public void apply() {

    }
}
