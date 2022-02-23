package com.aliucord.themer.preferences

import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateOf
import kotlin.reflect.KProperty

var sharedPreferences: SharedPreferences? = null

val disabledPref by lazy { boolPreference("__disabled") }

class Preference<T>(
    private val key: String,
    defaultValue: T,
    getter: SharedPreferences.(key: String, defaultValue: T) -> T?,
    private val setter: SharedPreferences.Editor.(key: String, newValue: T) -> Unit
) {
    private val value = mutableStateOf(sharedPreferences!!.getter(key, defaultValue) ?: defaultValue)

    operator fun getValue(thisRef: Any?, property: KProperty<*>) = value.value

    operator fun setValue(thisRef: Any?, property: KProperty<*>, newValue: T) = set(newValue)

    fun get() = value.value

    fun set(newValue: T) {
        if (value.value != newValue) {
            value.value = newValue
            val editor = sharedPreferences!!.edit()
            editor.setter(key, newValue)
            editor.apply()
        }
    }
}

fun boolPreference(
    key: String,
    defaultValue: Boolean = false
) = Preference(
    key,
    defaultValue,
    SharedPreferences::getBoolean,
    SharedPreferences.Editor::putBoolean
)
