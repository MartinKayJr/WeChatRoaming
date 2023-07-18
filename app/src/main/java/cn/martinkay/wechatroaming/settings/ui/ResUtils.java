package cn.martinkay.wechatroaming.settings.ui;

import java.io.InputStream;
import java.util.Objects;

public class ResUtils {
    private ResUtils() {
    }

    public static InputStream openAsset(String name) {
        return Objects.requireNonNull(ResUtils.class.getClassLoader()).getResourceAsStream("assets/" + name);
    }
}
