package com.aliucord.themer;

import java.util.HashMap;
import java.util.Map;

public class Descriptions {
    public static Map<String, String> colors = new HashMap<String, String>(){{
        String accent = "Accent color";
        put("brand", accent);
        put("brand_500", accent);
        put("brand_600", accent);
        put("purple_brand", accent);
        put("purple_brand_dark", accent);
        put("link", "Link/mention color");
        put("link_light", "Link/mention color");
        put("primary_500", "One of background colors of buttons");
        put("primary_600", "Chat background");
        String bg = "One of background colors";
        put("primary_630", bg);
        put("primary_660", bg);
        put("primary_700", "One of background colors (used for example for embeds and spoilers)");
        put("primary_800", bg);
        put("primary_dark_600", "One of background colors (colorBackgroundPrimary)");
        put("primary_dark_630", "One of background colors (used for example for channels list background)");
        put("primary_dark_660", "One of background colors (colorBackgroundSecondaryAlt)");
        put("primary_dark_700", bg);
        put("primary_dark_800", "One of background colors (used for example for tab bar on bottom)");
    }};
}
