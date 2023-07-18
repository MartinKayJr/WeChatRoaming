package cn.martinkay.wechatroaming.utils;

import android.content.Context;

import java.util.Iterator;
import java.util.Map;

import cn.martinkay.wechatroaming.config.ConfigManager;
import cn.martinkay.wechatroaming.settings.startup.HookEntry;import cn.martinkay.wechatroaming.utils.HostInfo;import cn.martinkay.wechatroaming.utils.HostInfoImpl;

public class HostInfos {

    public static final String BS_PACKAGE_NAME = "bs_package_name";

    public static void init(Context context) {
        HostInfoImpl hostInfo = HostInfo.init(context);
        HookEntry.hostInfoMap.put(hostInfo.getPackageName(), hostInfo);
        showAll();
    }

    public static HostInfoImpl getHostInfo(String packageName) {
        return HookEntry.hostInfoMap.get(packageName);
    }

    public static void showAll() {
        Iterator<Map.Entry<String, HostInfoImpl>> iterator =
                HookEntry.hostInfoMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, HostInfoImpl> next = iterator.next();
            Log.i("HostInfos: --" + next.getKey() + "++++" + next.getValue().toString());
        }
    }

    public static HostInfoImpl getHostInfo() {
        showAll();
        String packageName = ConfigManager.getDefaultConfig().getString(HostInfos.BS_PACKAGE_NAME, null);
        if (packageName != null) {
            HostInfoImpl hostInfo = getHostInfo(packageName);
            if (hostInfo == null) {
                return getHostInfo("cn.martinkay.wechatroaming");
            }
            return hostInfo;
        }
        return null;
    }


}
