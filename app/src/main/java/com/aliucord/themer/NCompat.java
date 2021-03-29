package com.aliucord.themer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Method;

import de.robv.android.xposed.*;

public class NCompat {
    @SuppressLint({"DiscouragedPrivateApi", "PrivateApi", "SoonBlockedPrivateApi", "WorldReadableFiles"})
    public static void hookSharedPreferences(ClassLoader cl) throws Throwable {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N || XposedBridge.getXposedVersion() > 92) return;
        Log.v(Constants.TAG, "Hooking sharedpreferences to fix compatibility");
        Class<?> c = NCompat.class.getClassLoader().loadClass("android.app.ContextImpl");
        Method checkMode = c.getDeclaredMethod("checkMode", int.class);
        XposedBridge.hookMethod(checkMode, new XC_MethodHook() {
            @SuppressWarnings("deprecation")
            protected void afterHookedMethod(MethodHookParam param) {
                if (((int) param.args[0] & Context.MODE_WORLD_READABLE) != 0) param.setThrowable(null);
            }
        });
        Method getPreferencesDir = c.getDeclaredMethod("getPreferencesDir");
        XposedBridge.hookMethod(getPreferencesDir, new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) {
                param.setResult(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS));
            }
        });

        XposedHelpers.findAndHookMethod(
                "com.aliucord.themer.activities.MainActivity",
                cl,
                "shouldAskForStorage",
                XC_MethodReplacement.returnConstant(true)
        );
    }

    public static XSharedPreferences getPrefs() {
        XSharedPreferences prefs;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N || XposedBridge.getXposedVersion() > 92) {
            prefs = new XSharedPreferences(BuildConfig.APPLICATION_ID);
            prefs.makeWorldReadable();
        } else prefs = new XSharedPreferences(new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                BuildConfig.APPLICATION_ID + "_preferences.xml"
        ));
        return prefs;
    }
}
