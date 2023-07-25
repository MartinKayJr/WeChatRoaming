package cn.martinkay.wechatroaming.settings.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.view.ContextThemeWrapper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

import cn.martinkay.wechatroaming.settings.lifecycle.Parasitics;
import cn.martinkay.wechatroaming.utils.SavedInstanceStatePatchedClassReferencer;


/**
 * If you just want to create a MaterialDialog or AppCompatDialog, see {@link #createMaterialDesignContext(Context)} and
 * {@link #createAppCompatContext(Context)}
 **/
public class CommonContextWrapper extends ContextThemeWrapper {

    /**
     * Creates a new context wrapper with the specified theme with correct module ClassLoader.
     *
     * @param base  the base context
     * @param theme the resource ID of the theme to be applied on top of the base context's theme
     */
    public CommonContextWrapper(@NonNull Context base, int theme) {
        this(base, theme, null);
    }

    /**
     * Creates a new context wrapper with the specified theme with correct module ClassLoader.
     *
     * @param base          the base context
     * @param theme         the resource ID of the theme to be applied on top of the base context's theme
     * @param configuration the configuration to override the base one
     */
    public CommonContextWrapper(@NonNull Context base, int theme,
                                @Nullable Configuration configuration) {
        super(base, theme);
        if (configuration != null) {
            mOverrideResources = base.createConfigurationContext(configuration).getResources();
        }
        Parasitics.injectModuleResources(getResources());
    }

    private ClassLoader mXref = null;
    private Resources mOverrideResources;

    @NonNull
    @Override
    public ClassLoader getClassLoader() {
        if (mXref == null) {
            mXref = new SavedInstanceStatePatchedClassReferencer(
                CommonContextWrapper.class.getClassLoader());
        }
        return mXref;
    }

    @Nullable
    private static Configuration recreateNighModeConfig(@NonNull Context base, int uiNightMode) {
        Objects.requireNonNull(base, "base is null");
        Configuration baseConfig = base.getResources().getConfiguration();
        if ((baseConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK) == uiNightMode) {
            // config for base context is already what we want,
            // just return null to avoid unnecessary override
            return null;
        }
        Configuration conf = new Configuration();
        conf.uiMode = uiNightMode | (baseConfig.uiMode & ~Configuration.UI_MODE_NIGHT_MASK);
        return conf;
    }

    @NonNull
    @Override
    public Resources getResources() {
        if (mOverrideResources == null) {
            return super.getResources();
        } else {
            return mOverrideResources;
        }
    }

    public static boolean isAppCompatContext(@NonNull Context context) {
        if (!checkContextClassLoader(context)) {
            return false;
        }
        TypedArray a = context.obtainStyledAttributes(androidx.appcompat.R.styleable.AppCompatTheme);
        try {
            return a.hasValue(androidx.appcompat.R.styleable.AppCompatTheme_windowActionBar);
        } finally {
            a.recycle();
        }
    }

    private static final int[] MATERIAL_CHECK_ATTRS = {com.google.android.material.R.attr.colorPrimaryVariant};

    public static boolean isMaterialDesignContext(@NonNull Context context) {
        if (!isAppCompatContext(context)) {
            return false;
        }
        @SuppressLint("ResourceType") TypedArray a = context.obtainStyledAttributes(MATERIAL_CHECK_ATTRS);
        try {
            return a.hasValue(0);
        } finally {
            a.recycle();
        }
    }

    public static boolean checkContextClassLoader(@NonNull Context context) {
        try {
            ClassLoader cl = context.getClassLoader();
            if (cl == null) {
                return false;
            }
            return cl.loadClass(AppCompatActivity.class.getName()) == AppCompatActivity.class;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @NonNull
    public static Context createAppCompatContext(@NonNull Context base) {
        if (isAppCompatContext(base)) {
            return base;
        }
        return new CommonContextWrapper(base, ModuleThemeManager.getCurrentStyleId(),
            recreateNighModeConfig(base, ResUtils.getNightModeMasked()));
    }

    @NonNull
    public static Context createMaterialDesignContext(@NonNull Context base) {
        if (isMaterialDesignContext(base)) {
            return base;
        }
        // currently all themes by createAppCompatContext are material themes
        // change this if you have a AppCompat theme that is not material theme
        return createAppCompatContext(base);
    }
}
