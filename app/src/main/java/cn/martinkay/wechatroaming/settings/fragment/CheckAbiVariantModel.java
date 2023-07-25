
package cn.martinkay.wechatroaming.settings.fragment;

import android.content.Context;
import android.system.Os;
import android.system.StructUtsname;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import cn.martinkay.wechatroaming.config.ConfigManager;
import cn.martinkay.wechatroaming.utils.hookstatus.AbiUtils;


public class CheckAbiVariantModel {

    private CheckAbiVariantModel() {
        throw new AssertionError("no instance");
    }

    public static final String[] HOST_PACKAGES = new String[]{
            "com.tencent.mm"
    };

    @NonNull
    public static AbiInfo collectAbiInfo(@NonNull Context context) {
        AbiInfo abiInfo = new AbiInfo();
        StructUtsname uts = Os.uname();
        String sysAbi = uts.machine;
        abiInfo.sysArchName = sysAbi;
        abiInfo.sysArch = AbiUtils.archStringToArchInt(sysAbi);

        HashSet<String> requestAbis = new HashSet<>();
        requestAbis.add(AbiUtils.archStringToLibDirName(sysAbi));
        for (String pkg : HOST_PACKAGES) {
            String activeAbi = AbiUtils.getApplicationActiveAbi(pkg);
            if (activeAbi == null) {
                continue;
            }
            String abi = AbiUtils.archStringToLibDirName(activeAbi);
            if (!isPackageIgnored(pkg)) {
                requestAbis.add(abi);
            }
            AbiInfo.Package pi = new AbiInfo.Package();
            pi.abi = AbiUtils.archStringToArchInt(activeAbi);
            pi.ignored = isPackageIgnored(pkg);
            pi.packageName = pkg;
            abiInfo.packages.put(pkg, pi);
        }
        String[] modulesAbis = AbiUtils.queryModuleAbiList();
        HashSet<String> missingAbis = new HashSet<>();
        // check if modulesAbis contains all requestAbis
        for (String abi : requestAbis) {
            if (!Arrays.asList(modulesAbis).contains(abi)) {
                missingAbis.add(abi);
            }
        }
        abiInfo.isAbiMatch = missingAbis.isEmpty();
        int abi = 0;
        for (String name : requestAbis) {
            abi |= AbiUtils.archStringToArchInt(name);
        }
        abiInfo.suggestedApkAbiVariant = AbiUtils.getSuggestedAbiVariant(abi);
        return abiInfo;
    }

    public static void setPackageIgnored(@NonNull String packageName, boolean ignored) {
        ConfigManager cfg = ConfigManager.getDefaultConfig();
        cfg.putBoolean("native_lib_abi_ignore." + packageName, ignored);
    }

    public static boolean isPackageIgnored(@NonNull String packageName) {
        ConfigManager cfg = ConfigManager.getDefaultConfig();
        return cfg.getBoolean("native_lib_abi_ignore." + packageName, false);
    }

    public static class AbiInfo {

        public static class Package {

            public String packageName;
            public int abi;
            public boolean ignored;
        }

        @NonNull
        public Map<String, Package> packages = new HashMap<>();
        public String sysArchName;
        public int sysArch;
        public boolean isAbiMatch;
        @Nullable
        public String suggestedApkAbiVariant;
    }

}
