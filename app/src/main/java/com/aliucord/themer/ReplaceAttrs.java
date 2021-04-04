package com.aliucord.themer;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.TypedValue;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

public class ReplaceAttrs {
    private static class ColorEntry {
        public String advanced;
        public String simple;

        public ColorEntry(String a) {
            advanced = a;
            simple = a;
        }

        public ColorEntry(String a, String s) {
            advanced = a;
            simple = s;
        }
    }

    private Map<String, ColorEntry> getRawMap() {
        return new HashMap<String, ColorEntry>(){{
            String simplea = "simple_accent_color";
            String simplebg = "simple_bg_color";
            String simplebgs = "simple_bg_secondary_color";
            put("colorSurface", new ColorEntry("color_primary_dark_800", simplebg));
            put("colorBackgroundFloating", new ColorEntry("color_primary_dark_800", simplebg));
            put("colorTabsBackground", new ColorEntry("color_primary_dark_800", simplebg));
            put("colorControlActivated", new ColorEntry("color_brand_500", simplea));
            put("colorBackgroundTertiary", new ColorEntry("color_primary_700", simplebgs));
            put("colorBackgroundSecondary", new ColorEntry("color_primary_700", simplebgs));
            put("colorTextLink", new ColorEntry("color_link", simplea));
            put("primary_700", new ColorEntry("color_primary_700", simplebgs));
            put("primary_900", new ColorEntry("color_primary_900", "__"));
            put("theme_chat_mentioned_me", new ColorEntry("mention_highlight"));
            put("theme_chat_spoiler_bg", new ColorEntry("color_primary_700", simplebgs));
            put("theme_chat_spoiler_inapp_bg", new ColorEntry("color_primary_600", simplebg));

            for (String n : new String[]{"600", "660", "800"})
                put("primary_" + n, new ColorEntry("color_primary_" + n, simplebg));
        }};
    }

    private final Map<Integer, Integer> map = new HashMap<>();

    public ReplaceAttrs(Resources res, String packageName, Map<String, ?> allPrefs) {
        for (Map.Entry<String, ColorEntry> entry : getRawMap().entrySet()) {
            String color = entry.getValue().advanced;
            if (allPrefs.containsKey(color))
                map.put(res.getIdentifier(entry.getKey(), "attr", packageName), (Integer) allPrefs.get(color));
        }
    }

    public ReplaceAttrs(Resources res, String packageName, SharedPreferences prefs) {
        for (Map.Entry<String, ColorEntry> entry : getRawMap().entrySet()) {
            String color = entry.getValue().simple;
            if (!color.equals("__") && prefs.contains(color))
                map.put(res.getIdentifier(entry.getKey(), "attr", packageName), prefs.getInt(color, Color.BLACK));
        }
    }

    public void replaceAttrs() throws Throwable {
        if (map.size() == 0) return;
        Method resolveAttr = Resources.Theme.class.getDeclaredMethod("resolveAttribute", int.class, TypedValue.class, boolean.class);
        XposedBridge.hookMethod(resolveAttr, new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) {
                int attr = (int) param.args[0];
                if (map.containsKey(attr)) {
                    TypedValue val = (TypedValue) param.args[1];
                    val.data = map.get(attr);
                }
            }
        });
    }
}
