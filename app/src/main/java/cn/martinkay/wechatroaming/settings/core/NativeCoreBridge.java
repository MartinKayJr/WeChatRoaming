
package cn.martinkay.wechatroaming.settings.core;

import androidx.annotation.Keep;

import cn.martinkay.wechatroaming.settings.base.RuntimeErrorTracer;
import cn.martinkay.wechatroaming.utils.Log;

public class NativeCoreBridge {

    private NativeCoreBridge() {
        throw new AssertionError("No NativeCoreBridge instances for you!");
    }

    public static native void initNativeCore(String packageName, int currentSdkLevel, String versionName, long longVersionCode);

    @Keep
    private static void nativeTraceErrorHelper(Object thiz, Throwable error) {
        if (thiz instanceof RuntimeErrorTracer) {
            RuntimeErrorTracer tracer = (RuntimeErrorTracer) thiz;
            tracer.traceError(error);
        } else {
            Log.e("NativeCoreBridge nativeTraceErrorHelper: thiz is not a RuntimeErrorTracer, got "
                    + thiz.getClass().getName() + ", errorMessage: " + error);
        }
    }

}
