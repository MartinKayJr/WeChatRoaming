package cn.martinkay.wechatroaming;

import android.app.Activity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class XposedInit implements IXposedHookLoadPackage {

    Activity activity;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpp) throws Throwable {
        if (!lpp.packageName.equals("com.tencent.mm")) {
            return;
        }
        XposedHelpers.findAndHookMethod("android.widget.TextView", lpp.classLoader, "setText", CharSequence.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Object[] args = param.args;
                if (args[0] != null) {
                    String text = args[0].toString();
                    if (StringUtils.isEmpty(text) || !text.equals("设置")) {
                        return;
                    }
                    TextView textView = (TextView) param.thisObject;
                    if ((textView.getParent() instanceof LinearLayout) && textView.getId() == activity.getResources().getIdentifier("text1","id","android")) {
                        textView.setEnabled(true);
                        textView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        });
                    }
                }
            }
        });


        XposedHelpers.findAndHookMethod("com.tencent.mm.ui.MMActivity", lpp.classLoader, "onCreate", android.os.Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                activity = (Activity) param.thisObject;
                super.afterHookedMethod(param);
            }
        });


    }
}
