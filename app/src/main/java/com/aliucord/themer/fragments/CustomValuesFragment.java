package com.aliucord.themer.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.aliucord.themer.R;

public class CustomValuesFragment extends PreferenceFragmentCompat {
    @Override
    @SuppressLint("WorldReadableFiles")
    @SuppressWarnings("deprecation")
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        PreferenceManager preferenceManager = getPreferenceManager();
        preferenceManager.setSharedPreferencesMode(Context.MODE_WORLD_READABLE);

        addPreferencesFromResource(R.xml.custom_values);
    }
}
