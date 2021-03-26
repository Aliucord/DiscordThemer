package com.aliucord.themer;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.XResources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.Window;

import androidx.core.content.res.ResourcesCompat;

import java.lang.reflect.Method;
import java.util.Map;

import de.robv.android.xposed.*;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Main implements IXposedHookInitPackageResources, IXposedHookLoadPackage {
    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resparam) {
        XResources res = resparam.res;
        String packageName = resparam.packageName.equals("com.aliucord") ? "com.discord" : resparam.packageName;
        if (packageName.equals(BuildConfig.APPLICATION_ID)) res.setReplacement(R.bool.xposed, true);
        if (!packageName.equals("com.discord") && !packageName.startsWith("com.cutthecord")) return;

        XSharedPreferences prefs = new XSharedPreferences(BuildConfig.APPLICATION_ID);
        prefs.makeWorldReadable();

        boolean advanced = prefs.getBoolean("__advanced", false);
        if (advanced && prefs.getBoolean("__disabled", false)) return;

        if (prefs.contains("mention_highlight"))
            res.setReplacement(packageName, "color", "status_yellow_500", getColorWithAlpha("ff", prefs.getInt("mention_highlight", Color.BLACK)));

        ReplaceAttrs replaceAttrs;
        if (advanced) {
            Map<String, ?> allPrefs = prefs.getAll();
            // Log.v(Constants.TAG, allPrefs.toString());
            if (allPrefs.containsKey("color_brand_500")) fixNitroIcon(res, packageName, (Integer) allPrefs.get("color_brand_500"));
            if (allPrefs.containsKey("active_channel_color")) tintActiveChannel(res, packageName, (Integer) allPrefs.get("active_channel_color"));
            for (Map.Entry<String, ?> pref : allPrefs.entrySet()) {
                String key = pref.getKey();
                if (key.startsWith("color_")) {
                    try {
                        res.setReplacement(packageName, "color", key.substring(6), pref.getValue());
                    } catch (Throwable e) { logError(e); }
                } else if (key.startsWith("drawablecolor_")) {
                    try {
                        tintDrawable(res, packageName, key.substring(14), (Integer) pref.getValue());
                    } catch (Throwable e) { logError(e); }
                }
            }
            replaceAttrs = new ReplaceAttrs(res, packageName, allPrefs);
        } else {
            if (prefs.contains("simple_accent_color")) {
                int accent = prefs.getInt("simple_accent_color", res.getColor(res.getIdentifier("brand", "color", packageName)));
                replaceAllColors(res, packageName, Constants.ACCENT_NAMES, accent);
                res.setReplacement(packageName, "color", "purple_brand_alpha_10", getColorWithAlpha("1a", accent));

                fixNitroIcon(res, packageName, accent);
            }
            if (prefs.contains("simple_bg_color")) {
                replaceAllColors(res, prefs, packageName, Constants.BACKGROUND_NAMES, "simple_bg_color", "primary_600");
            }
            if (prefs.contains("simple_bg_secondary_color")) {
                int color = prefs.getInt("simple_bg_secondary_color", res.getColor(res.getIdentifier("primary_630", "color", packageName)));
                replaceAllColors(res, packageName, Constants.BACKGROUND_SECONDARY_NAMES, color);
                tintActiveChannel(res, packageName, color);
            }
            replaceAttrs = new ReplaceAttrs(res, packageName, prefs);
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Method getColor = Context.class.getDeclaredMethod("getColor", int.class);
                XposedBridge.hookMethod(getColor, new XC_MethodHook() {
                    protected void beforeHookedMethod(MethodHookParam param) {
                        param.setResult(((Context) param.thisObject).getResources().getColor((int) param.args[0]));
                    }
                });
            }

            replaceAttrs.replaceAttrs();
        } catch (Throwable e) { logError(e); }
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        String packageName = lpparam.packageName;
        if (!packageName.equals("com.discord") && !packageName.equals("com.aliucord") && !packageName.startsWith("com.cutthecord")) return;

        ClassLoader cl = lpparam.classLoader;
        XSharedPreferences prefs = new XSharedPreferences(BuildConfig.APPLICATION_ID);
        prefs.makeWorldReadable();

        boolean advanced = prefs.getBoolean("__advanced", false);
        if (advanced) {
            if (prefs.getBoolean("__disabled", false)) return;
            if (prefs.contains("input_background_color")) {
                int color = prefs.getInt("input_background_color", Color.BLACK);
                setInputBackground(cl, color);
            }
            if (prefs.contains("statusbar_color")) {
                int color = prefs.getInt("statusbar_color", Color.BLACK);
                setStatusBarColor(cl, color);
            }
        } else {
            if (prefs.contains("simple_bg_secondary_color")) {
                int bg = prefs.getInt("simple_bg_secondary_color", Color.BLACK);
                setInputBackground(cl, bg);
                setStatusBarColor(cl, bg);
            }
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void replaceAllColors(XResources res, SharedPreferences prefs, String packageName, String[] colors, String key, String defaultColor) {
        replaceAllColors(res, packageName, colors, prefs.getInt(key, res.getColor(res.getIdentifier(defaultColor, "color", packageName))));
    }

    private void replaceAllColors(XResources res, String packageName, String[] colors, int color) {
        for (String name : colors) try {
            res.setReplacement(packageName, "color", name, color);
        } catch (Throwable e) { logError(e); }
    }

    private void fixNitroIcon(XResources res, String packageName, int color) {
        tintDrawable(res, packageName, "ic_nitro_rep", color);
    }

    private void tintActiveChannel(XResources res, String packageName, int color) {
        tintDrawable(res, packageName, "drawable_overlay_channels_active_dark", color);
        tintDrawable(res, packageName, "drawable_overlay_channels_active_light", color);
    }

    private void tintDrawable(XResources res, String packageName, String drawable, int color) {
        res.setReplacement(packageName, "drawable", drawable, new XResources.DrawableLoader() {
            public Drawable newDrawable(XResources res, int id) {
                Drawable d = ResourcesCompat.getDrawable(res, res.getIdentifier(drawable, "drawable", packageName), null);
                if (d != null) d.setTint(color);
                return d;
            }
        });
    }

    private void setInputBackground(ClassLoader classLoader, int color) {
        XposedHelpers.findAndHookMethod(
                "com.google.android.material.textfield.TextInputLayout",
                classLoader,
                "calculateBoxBackgroundColor",
                new XC_MethodHook() {
                    protected void beforeHookedMethod(MethodHookParam param) { param.setResult(color); }
                }
        );
    }

    private void setStatusBarColor(ClassLoader classLoader, int color) {
        try {
            XposedHelpers.findAndHookMethod(
                    "com.discord.utilities.color.ColorCompat",
                    classLoader,
                    "setStatusBarColor",
                    Window.class, int.class, boolean.class,
                    new XC_MethodHook() {
                        protected void beforeHookedMethod(MethodHookParam param) { param.args[1] = color; }
                    }
            );
        } catch (Throwable e) { logError(e); }
    }

    private int getColorWithAlpha(String hexAlpha, int color) {
        return Color.parseColor("#" + hexAlpha + Integer.toHexString(color).substring(2));
    }

    private void logError(Throwable e) {
        XposedBridge.log("DiscordThemer error: ");
        XposedBridge.log(e);
    }
}
