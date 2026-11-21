package de.robv.android.xposed;

public class XposedBridge {
    public static void log(String text) {
    }

    public static void log(Throwable t) {
    }

    public static XC_MethodHook.Unhook hookMethod(java.lang.reflect.Member hookMethod,
            XC_MethodHook callback) {
        return null;
    }
}
