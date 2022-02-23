package com.aliucord.themer.module

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import com.aliucord.themer.BuildConfig
import com.aliucord.themer.aliucordBasePath
import de.robv.android.xposed.*
import java.io.File

@SuppressLint("DiscouragedPrivateApi", "PrivateApi", "SoonBlockedPrivateApi", "WorldReadableFiles")
fun hookSharedPreferences() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N || XposedBridge.getXposedVersion() > 92) return
    Log.v(BuildConfig.TAG, "Hooking sharedpreferences to fix compatibility")
    val c = Class.forName("android.app.ContextImpl")
    val checkMode = c.getDeclaredMethod("checkMode", Int::class.javaPrimitiveType).apply { isAccessible = true }
    XposedBridge.hookMethod(checkMode, object : XC_MethodHook() {
        override fun afterHookedMethod(param: MethodHookParam) {
            if (param.args[0] as Int and Context.MODE_WORLD_READABLE != 0) param.throwable = null
        }
    })
    val getPreferencesDir = c.getDeclaredMethod("getPreferencesDir")
    XposedBridge.hookMethod(getPreferencesDir, object : XC_MethodHook() {
        override fun afterHookedMethod(param: MethodHookParam) {
            param.result = aliucordBasePath
        }
    })
}

fun getPrefs() =
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N || XposedBridge.getXposedVersion() > 92)
        XSharedPreferences(BuildConfig.APPLICATION_ID, BuildConfig.PREFERENCES_NAME).apply { makeWorldReadable() }
    else XSharedPreferences(File(aliucordBasePath, "${BuildConfig.PREFERENCES_NAME}.xml"))
