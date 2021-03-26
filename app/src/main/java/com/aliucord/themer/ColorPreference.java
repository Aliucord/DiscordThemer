package com.aliucord.themer;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;

import com.jaredrummler.android.colorpicker.ColorPreferenceCompat;

import java.lang.reflect.Field;

@SuppressWarnings("unused")
public class ColorPreference extends ColorPreferenceCompat {
    public Integer defaultValue;

    public ColorPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ColorPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        if (defaultValue == null) super.onSetInitialValue(defaultValue);
        else setColor(defaultValue);
    }

    @Override
    public void onColorReset(int dialogId) {
        super.onColorReset(dialogId);
        if (hasKey()) {
            if (defaultValue != null) saveValue(defaultValue);
            else {
                Resources res = getContext().getResources();
                saveValue(res.getColor(res.getIdentifier(getKey(), "color", BuildConfig.APPLICATION_ID)));
            }
            getSharedPreferences().edit().remove(getKey()).apply();
        }
    }

    private void setColor(Object color) {
        try {
            Field colorField = ColorPreferenceCompat.class.getDeclaredField("color");
            colorField.setAccessible(true);
            colorField.set(this, color);
        } catch (Throwable ignored) {}
    }
}
