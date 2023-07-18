package cn.martinkay.wechatroaming.utils.hookstatus;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.qiniu.android.common.FixedZone;
import com.qiniu.android.storage.Configuration;
import com.qiniu.android.storage.UploadManager;
import com.tencent.tauth.Tencent;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jetbrains.annotations.NotNull;
import org.lsposed.hiddenapibypass.HiddenApiBypass;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.Security;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.martinkay.wechatroaming.common.utils.StreamUtils;
import cn.martinkay.wechatroaming.model.signer.KeyInfo;
import cn.martinkay.wechatroaming.utils.Natives;
import sun1.security.provider.JavaProvider;

public class CloudSpieApp extends Application implements Thread.UncaughtExceptionHandler {
    private final static ExecutorService CachedThreadPool = Executors.newCachedThreadPool();

    private static Context context;

    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;
    private static UploadManager uploadManager;
    private static Tencent tencent;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        System.out.println("CPU:" + Build.SUPPORTED_ABIS[0]);
        System.out.println("CPU2:" + getApplicationInfo().nativeLibraryDir);
        System.setProperty("project.dir", Objects.requireNonNull(getExternalFilesDir("project")).getAbsolutePath());
        System.setProperty("task.dir", System.getProperty("project.dir") + File.separator + "task");
        System.setProperty("apk.dir", System.getProperty("project.dir") + File.separator + "apk");
        System.setProperty("jks.dir", System.getProperty("project.dir") + File.separator + "jks");
        mkdirs(new File(Objects.requireNonNull(System.getProperty("task.dir"))));
        mkdirs(new File(Objects.requireNonNull(System.getProperty("apk.dir"))));
        mkdirs(new File(Objects.requireNonNull(System.getProperty("jks.dir"))));
        System.setProperty("dexfixer.path", new File(getFilesDir(), "dexfixer.dex").getAbsolutePath());
        context = base;
        Thread.setDefaultUncaughtExceptionHandler(this);
        Security.addProvider(new JavaProvider());
        Security.addProvider(new BouncyCastleProvider());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sharedPreferences = getSharedPreferences("overall", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        uploadManager = new UploadManager(new Configuration.Builder()
                .zone(FixedZone.zoneAs0)
                .connectTimeout(10)
                .useHttps(true)
                .responseTimeout(60)
                .build());
//        tencent = Tencent.createInstance(getString(R.string.tencent_appid), this);
        File debugJks = new File(System.getProperty("jks.dir"), "default.key");
        if (!debugJks.exists()) {
            try (FileOutputStream debug = new FileOutputStream(debugJks)) {
                KeyInfo keyInfo = new KeyInfo("android", "androiddebug", "android", Base64.encodeToString(StreamUtils.toByte(getAssets().open("debug.keystore")), Base64.NO_WRAP));
                debug.write(new Gson().toJson(keyInfo).getBytes());
                debug.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        File DexFixer = new File(Objects.requireNonNull(System.getProperty("dexfixer.path")));
        if (!DexFixer.exists()) {
            try (FileOutputStream outputStream = new FileOutputStream(DexFixer);
                 InputStream inputStream = getAssets().open("dexfixer.dex")) {
                byte[] bytes = new byte[1024 * 8];
                int len;
                while ((len = inputStream.read(bytes)) != -1)
                    outputStream.write(bytes, 0, len);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
//        Executors.newSingleThreadScheduledExecutor().submit(new Runnable() {
//            @Override
//            public void run() {
//                MultiProcess.init();
//            }
//        });

        // load native library
        Natives.load(this);
        // bypass hidden api check for current process
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            HiddenApiBypass.setHiddenApiExemptions("L");
        }
    }

    /**
     * 全局异常日志捕获
     *
     * @param thread
     * @param throwable
     */
    @Override
    public void uncaughtException(@NonNull Thread thread, @NonNull Throwable throwable) {
        Intent intent = new Intent();
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "Trace.txt"));
            fileOutputStream.write(appendPhoneInfo(throwable).getBytes());
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            intent.putExtra("data", appendPhoneInfo(throwable));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        // TODO 显示异常的activity
//        intent.setClass(this, Debug.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        System.exit(0);
    }

    @NotNull
    private String appendPhoneInfo(Throwable throwable) throws PackageManager.NameNotFoundException {
        PackageManager pm = this.getPackageManager();
        PackageInfo pi = pm.getPackageInfo(this.getPackageName(), PackageManager.GET_ACTIVITIES);
        return "App Version: " +
                pi.versionName +
                "_" +
                pi.versionCode + "\n" +
                "OS Version: " +
                Build.VERSION.RELEASE +
                "_" +
                Build.VERSION.SDK_INT + "\n" +
                "Vendor: " +
                Build.MANUFACTURER + "\n" +
                "Model: " +
                Build.MODEL + "\n" +
                "CPU: " +
                Arrays.toString(Build.SUPPORTED_ABIS) +
                "\n" +
                "Debug:\n" +
                Log.getStackTraceString(throwable);
    }

    private void mkdirs(@NotNull File file) {
        if (!file.exists())
            file.mkdirs();
    }

    public static Context getContext() {
        return context;
    }

    public static SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    public static SharedPreferences.Editor getEditor() {
        return editor;
    }

    public static UploadManager getUploadManager() {
        return uploadManager;
    }

    public static Tencent getTencent() {
        return tencent;
    }

    public static ExecutorService getCachedThreadPool() {
        return CachedThreadPool;
    }

}
