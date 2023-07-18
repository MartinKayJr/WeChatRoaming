
package cn.martinkay.wechatroaming.utils.ui;

import android.content.Context;
import android.util.TypedValue;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

public class ThemeAttrUtils {

    private ThemeAttrUtils() {
    }

    private static final TypedValue sValue = new TypedValue();

    @Nullable
    public static TypedValue resolveAttribute(@NonNull Context context, @AttrRes int attr) {
        if (!context.getTheme().resolveAttribute(attr, sValue, true)) {
            return null;
        }
        return sValue;
    }

    @NonNull
    public static TypedValue resolveAttrOrError(@NonNull Context context, @AttrRes int attr) {
        if (!context.getTheme().resolveAttribute(attr, sValue, true)) {
            throw new IllegalArgumentException("Could not resolve attribute " + attr);
        }
        return sValue;
    }

    @ColorInt
    public static int resolveColorOrDefaultColorRes(@NonNull Context context, int attr, @ColorRes int defaultValue) {
        if (!context.getTheme().resolveAttribute(attr, sValue, true)) {
            return ResourcesCompat.getColor(context.getResources(), defaultValue, context.getTheme());
        }
        return sValue.data;
    }

    @ColorInt
    public static int resolveColorOrDefaultColorInt(@NonNull Context context, int attr, @ColorInt int defaultValue) {
        if (!context.getTheme().resolveAttribute(attr, sValue, true)) {
            return defaultValue;
        }
        return sValue.data;
    }
}
