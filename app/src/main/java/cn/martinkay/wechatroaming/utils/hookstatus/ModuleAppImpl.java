package cn.martinkay.wechatroaming.utils.hookstatus;

import android.app.Application;
import android.os.Build;

import org.lsposed.hiddenapibypass.HiddenApiBypass;

import cn.martinkay.wechatroaming.utils.Natives;
import cn.martinkay.wechatroaming.utils.host.HostInfo;

public class ModuleAppImpl extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // init host info, even if we are not in the host app
        HostInfo.init(this);
        // load native library
        Natives.load(this);
        // bypass hidden api check for current process
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            HiddenApiBypass.setHiddenApiExemptions("L");
        }
    }
}
