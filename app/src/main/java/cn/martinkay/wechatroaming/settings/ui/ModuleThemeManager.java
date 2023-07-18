package cn.martinkay.wechatroaming.settings.ui;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import cn.martinkay.wechatroaming.R;
import cn.martinkay.wechatroaming.config.ConfigManager;


/**
 * Created by zpp0196 on 2019/5/18.
 */
public class ModuleThemeManager {

    private ModuleThemeManager() {
    }

    private static final Themes THEME_DEFAULT = Themes.FTB;
    private static final String CFG_THEME_ID = "theme_title";
    private static Themes sCurrentTheme = null;

    public static void setCurrentThemeByColor(@NonNull Activity activity, int color) {
        sCurrentTheme = color2Theme(activity, color);
        ConfigManager cfg = ConfigManager.getDefaultConfig();
        cfg.putString(CFG_THEME_ID, sCurrentTheme.title);
        cfg.save();
    }

    public static int getCurrentThemeColor(@NonNull Context context) {
        return ContextCompat.getColor(context, getCurrentThemeInfo().colorId);
    }

    @NonNull
    public static String getCurrentThemeName() {
        return getCurrentThemeInfo().title;
    }

    public static int getCurrentStyleId() {
        return getCurrentThemeInfo().styleId;
    }

    @NonNull
    public static Themes getCurrentThemeInfo() {
        if (sCurrentTheme == null) {
            String title = ConfigManager.getDefaultConfig().getStringOrDefault(CFG_THEME_ID, THEME_DEFAULT.title);
            sCurrentTheme = title2Theme(title);
        }
        return sCurrentTheme;
    }

    public static int[] getColors(@NonNull Context context) {
        Themes[] themes = Themes.values();
        int[] colors = new int[themes.length];
        for (int i = 0; i < colors.length; i++) {
            colors[i] = ContextCompat.getColor(context, themes[i].colorId);
        }
        return colors;
    }

    private static Themes color2Theme(@NonNull Context context, int color) {
        Themes[] themes = Themes.values();
        for (Themes theme : themes) {
            if (ContextCompat.getColor(context, theme.colorId) == color) {
                return theme;
            }
        }
        return THEME_DEFAULT;
    }

    private static Themes title2Theme(@NonNull String title) {
        Themes[] themes = Themes.values();
        for (Themes theme : themes) {
            if (theme.title.equals(title)) {
                return theme;
            }
        }
        return THEME_DEFAULT;
    }

    public enum Themes {
        BLP(R.color.theme_color_blp, R.style.AppTheme_Blp, "哔哩粉"),
        GOL(R.color.theme_color_gol, R.style.AppTheme_Gol, "亮棕色"),
        CAG(R.color.theme_color_cag, R.style.AppTheme_Cag, "酷安绿"),
        FTB(R.color.theme_color_ftb, R.style.AppTheme_Ftb, "胖次蓝"),
        GHP(R.color.theme_color_ghp, R.style.AppTheme_Ghp, "亮紫色"),

        MAR(R.color.theme_color_mar, R.style.AppTheme_Mar, "姨妈红"),
        TPO(R.color.theme_color_tpo, R.style.AppTheme_TPO, "热带橙"),
        TLG(R.color.theme_color_tlg, R.style.AppTheme_Tlg, "水鸭青"),
        RYB(R.color.theme_color_ryb, R.style.AppTheme_Ryb, "皇室蓝"),
        GAP(R.color.theme_color_gap, R.style.AppTheme_Gap, "基佬紫");

        public final int colorId;
        public final int styleId;
        public final String title;

        Themes(int colorId, int styleId, String title) {
            this.colorId = colorId;
            this.styleId = styleId;
            this.title = title;
        }
    }
}
