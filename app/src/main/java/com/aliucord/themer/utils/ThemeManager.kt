package com.aliucord.themer.utils

import android.util.Log
import com.aliucord.themer.BuildConfig
import com.aliucord.themer.preferences.boolPreference
import com.aliucord.themer.themesDir
import com.aliucord.themer.ui.screens.hsThemes
import org.json.JSONObject
import org.json.JSONTokener
import java.io.File

object ThemeManager {
    class Theme(private val file: File) {
        var name = file.name.removeSuffix(".json")
        var author = "Anonymous"
        var version = "1.0.0"
        // var license: String? = null
        // var updaterUrl: String? = null

        var json: JSONObject
        var failed = false
        init {
            try {
                json = JSONObject(JSONTokener(file.readText()))
                if (json.has("manifest")) json = json.getJSONObject("manifest")
                if (json.has("name")) name = json.getString("name")
                if (json.has("author")) author = json.getString("author")
                if (json.has("version")) version = json.getString("version")
                // if (json.has("license")) license = json.getString("license")
                // if (json.has("updater")) updaterUrl = json.getString("updater")
            } catch (e: Throwable) {
                json = JSONObject()
                failed = true
                Log.e(BuildConfig.TAG, "Failed to load $name", e)
            }
        }

        private val advancedPreference = boolPreference("advanced-$name")

        var advanced
            get() = advancedPreference.get()
            set(v) = advancedPreference.set(v)

        private val preference = boolPreference("enabled-$name")

        val enabled
            get() = preference.get()

        fun enable() {
            for (theme in themes) theme.disable()
            preference.set(true)
        }

        fun disable() {
            if (enabled) preference.set(false)
        }

        fun reload() {
            try {
                json = JSONObject(JSONTokener(file.readText()))
            } catch (e: Throwable) {
                Log.e(BuildConfig.TAG, "Failed to reload $name", e)
            }
        }

        fun save() {
            file.writeText(json.toString(4))
        }
    }

    val themes = ArrayList<Theme>()

    private var initialized = false
    fun init() {
        if (initialized) return
        initialized = true
        loadThemes()
    }

    fun loadThemes() {
        val files = themesDir.listFiles()
        if (!themesDir.exists()) themesDir.mkdir()
        themes.clear()
        themes.addAll(
            files
                ?.filter { it.name != "default.json" }
                ?.sortedBy { it.name }
                ?.map { Theme(it) }
                ?.filter { !it.failed } ?: emptyList()
        )
        hsThemes?.value = themes
    }
}
