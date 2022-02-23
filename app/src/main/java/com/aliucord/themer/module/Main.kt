package com.aliucord.themer.module

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.XResources
import android.content.res.XResources.DrawableLoader
import android.os.Build
import android.util.Log
import android.view.Window
import androidx.core.graphics.ColorUtils
import com.aliucord.themer.*
import com.aliucord.themer.preferences.disabledPref
import com.aliucord.themer.preferences.sharedPreferences
import com.aliucord.themer.utils.ThemeManager
import de.robv.android.xposed.*
import de.robv.android.xposed.callbacks.XC_InitPackageResources
import de.robv.android.xposed.callbacks.XC_LoadPackage

@SuppressLint("UseCompatLoadingForDrawables")
class Main : IXposedHookInitPackageResources, IXposedHookLoadPackage {
    override fun handleInitPackageResources(resparam: XC_InitPackageResources.InitPackageResourcesParam) {
        val res = resparam.res
        val packageName = resparam.packageName.let { if (it == Constants.ALIUCORD) Constants.DISCORD else it }
        if (packageName == BuildConfig.APPLICATION_ID) res.setReplacement(R.bool.xposed, true)
        if (packageName != Constants.DISCORD && !packageName.startsWith(Constants.CTC)) return

        if (sharedPreferences == null) {
            sharedPreferences = getPrefs()
            ThemeManager.init()
        }
        val theme = ThemeManager.themes.find { it.enabled } ?: return
        if (theme.advanced && disabledPref.get()) return

        val json = theme.json
        if (json.has(Constants.MENTION_HIGHLIGHT))
            res.setReplacement(packageName, "color", "status_yellow_500", ColorUtils.setAlphaComponent(json.getInt("mention_highlight"), 0xFF))

        if (theme.advanced) {
            if (json.has("color_brand_500")) res.fixNitroIcon(packageName, json.getInt("color_brand_500"))
            if (json.has(Constants.ACTIVE_CHANNEL_COLOR)) res.tintActiveChannel(packageName, json.getInt(Constants.ACTIVE_CHANNEL_COLOR))
            for (key in json.keys()) {
                if (key.startsWith("color_")) {
                    try {
                        res.setReplacement(packageName, "color", key.substring(6), json.get(key))
                    } catch (e: Throwable) {
                        logError(e)
                    }
                } else if (key.startsWith("drawablecolor_")) {
                    try {
                        res.tintDrawable(packageName, key.substring(14), json.getInt(key))
                    } catch (e: Throwable) {
                        logError(e)
                    }
                }
            }
        } else {
            Log.d(BuildConfig.TAG, "${theme.name} simple")
            if (json.has(Constants.SIMPLE_ACCENT_COLOR)) {
                val accent = json.getInt(Constants.SIMPLE_ACCENT_COLOR)
                res.replaceAllColors(packageName, Constants.ACCENT_NAMES, accent)
                res.fixNitroIcon(packageName, accent)
            }
            if (json.has(Constants.SIMPLE_BG_COLOR))
                res.replaceAllColors(packageName, Constants.BACKGROUND_NAMES, json.getInt(Constants.SIMPLE_BG_COLOR))
            if (json.has(Constants.SIMPLE_BG_SECONDARY_COLOR)) {
                val color = json.getInt(Constants.SIMPLE_BG_SECONDARY_COLOR)
                res.replaceAllColors(packageName, Constants.BACKGROUND_SECONDARY_NAMES, color)
                res.tintActiveChannel(packageName, color)
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val getColor = Context::class.java.getDeclaredMethod("getColor", Int::class.javaPrimitiveType)
                XposedBridge.hookMethod(getColor, object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) =
                        with(param.thisObject as Context) { param.result = resources.getColor(param.args[0] as Int, getTheme()) }
                })
            }

            AttrsReplacer(res, packageName, theme.advanced, json).replaceAttrs()
        } catch (e: Throwable) {
            logError(e)
        }
    }

    private fun XResources.replaceAllColors(packageName: String, colors: Array<String>, color: Int) {
        for (name in colors) try {
            setReplacement(packageName, "color", name, color)
        } catch (e: Throwable) {
            logError(e)
        }
    }

    private fun XResources.fixNitroIcon(packageName: String, color: Int) =
        tintDrawable(packageName, "ic_nitro_rep", color)

    private fun XResources.tintActiveChannel(packageName: String, color: Int) {
        tintDrawable(packageName, "drawable_overlay_channels_active_dark", color)
        tintDrawable(packageName, "drawable_overlay_channels_active_light", color)
    }

    private fun XResources.tintDrawable(packageName: String, drawable: String, color: Int) =
        setReplacement(packageName, "drawable", drawable, object : DrawableLoader() {
            override fun newDrawable(res: XResources, id: Int) =
                getDrawable(getIdentifier(drawable, "drawable", packageName), null).apply { setTint(color) }
        })

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        val cl = lpparam.classLoader
        val packageName = lpparam.packageName
        if (packageName == BuildConfig.APPLICATION_ID) hookSharedPreferences()
        if (packageName != Constants.DISCORD && packageName != Constants.ALIUCORD && !packageName.startsWith(Constants.CTC)) return

        fixNormalDiscordSupport(packageName, cl)

        if (sharedPreferences == null) {
            sharedPreferences = getPrefs()
            ThemeManager.init()
        }
        val theme = ThemeManager.themes.find { it.enabled } ?: return
        val json = theme.json

        if (theme.advanced) {
            if (disabledPref.get()) return
            if (json.has(Constants.INPUT_BG_COLOR)) setInputBackground(cl, json.getInt(Constants.INPUT_BG_COLOR))
            if (json.has(Constants.STATUSBAR_COLOR)) setStatusBarColor(cl, json.getInt(Constants.STATUSBAR_COLOR))
        } else {
            if (json.has(Constants.SIMPLE_BG_SECONDARY_COLOR)) json.getInt(Constants.SIMPLE_BG_SECONDARY_COLOR).let {
                setInputBackground(cl, it)
                setStatusBarColor(cl, it)
            }
        }
    }

    private fun setInputBackground(classLoader: ClassLoader, color: Int) {
        XposedHelpers.findAndHookMethod(
            "com.google.android.material.textfield.TextInputLayout",
            classLoader,
            "calculateBoxBackgroundColor",
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    param.result = color
                }
            }
        )
    }

    private fun setStatusBarColor(classLoader: ClassLoader, color: Int) {
        try {
            XposedHelpers.findAndHookMethod(
                "com.discord.utilities.color.ColorCompat",
                classLoader,
                "setStatusBarColor",
                Window::class.java, Int::class.javaPrimitiveType, Boolean::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        param.args[1] = color
                    }
                }
            )
        } catch (e: Throwable) {
            logError(e)
        }
    }

    private fun logError(e: Throwable) {
        XposedBridge.log("DiscordThemer error: ")
        XposedBridge.log(e)
    }
}
