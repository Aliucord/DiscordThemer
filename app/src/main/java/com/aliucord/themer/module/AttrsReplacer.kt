package com.aliucord.themer.module

import android.content.res.Resources
import android.util.TypedValue
import androidx.core.graphics.ColorUtils
import com.aliucord.themer.Constants
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import org.json.JSONObject

class AttrsReplacer(res: Resources, packageName: String, advanced: Boolean, json: JSONObject) {
    companion object {
        val RAW_MAP = mutableMapOf(
            "color_brand" to ("color_brand_new" to Constants.SIMPLE_ACCENT_COLOR),
            "colorControlBrandForeground" to ("color_brand_360" to Constants.SIMPLE_ACCENT_COLOR),
            "colorSurface" to ("color_primary_dark_800" to Constants.SIMPLE_BG_COLOR),
            "colorBackgroundFloating" to ("color_primary_dark_800" to Constants.SIMPLE_BG_COLOR),
            "colorTabsBackground" to ("color_primary_dark_800" to Constants.SIMPLE_BG_COLOR),
            "colorControlActivated" to ("color_brand_500" to Constants.SIMPLE_ACCENT_COLOR),
            "colorBackgroundTertiary" to ("color_primary_700" to Constants.SIMPLE_BG_SECONDARY_COLOR),
            "colorBackgroundSecondary" to ("color_primary_700" to Constants.SIMPLE_BG_SECONDARY_COLOR),
            "colorTextLink" to ("color_link" to Constants.SIMPLE_ACCENT_COLOR),
            "primary_700" to ("color_primary_700" to Constants.SIMPLE_BG_SECONDARY_COLOR),
            "primary_900" to ("color_primary_900" to "__"),
            "theme_chat_mention_background" to ("color_brand_500_alpha_20" to "__alpha_10_${Constants.SIMPLE_ACCENT_COLOR}"),
            "theme_chat_mention_foreground" to ("color_brand_new_530" to Constants.SIMPLE_ACCENT_COLOR),
            "theme_chat_mentioned_me" to ("mention_highlight" to "mention_highlight"),
            "theme_chat_spoiler_bg" to ("color_primary_700" to Constants.SIMPLE_BG_SECONDARY_COLOR),
            "theme_chat_spoiler_inapp_bg" to ("color_primary_600" to Constants.SIMPLE_BG_COLOR),
        ).apply {
            for (n in arrayOf("600", "660", "800")) put("primary_$n", "color_primary_$n" to Constants.SIMPLE_BG_COLOR)
        }
    }

    private val map = HashMap<Int, Int>()

    init {
        if (advanced) RAW_MAP.forEach {
            val color = it.value.first
            if (json.has(color)) map[res.getIdentifier(it.key, "attr", packageName)] = json.getInt(color)
        } else RAW_MAP.forEach {
            it.value.second.run {
                val a10 = startsWith("__alpha_10_")
                val color = substring(11)
                if (color != "__" && json.has(color)) {
                    val c = json.getInt(color)
                    map[res.getIdentifier(it.key, "attr", packageName)] = if (a10) ColorUtils.setAlphaComponent(c, 0x1a) else c
                }
            }
        }
    }

    fun replaceAttrs() {
        if (map.isEmpty()) return
        val resolveAttr = Resources.Theme::class.java.getDeclaredMethod(
            "resolveAttribute",
            Int::class.javaPrimitiveType, TypedValue::class.java, Boolean::class.javaPrimitiveType
        )
        XposedBridge.hookMethod(resolveAttr, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val attr = param.args[0] as Int
                if (map.containsKey(attr))
                    (param.args[1] as TypedValue).data = map[attr]!!
            }
        })
    }
}
