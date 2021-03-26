package com.aliucord.themer.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.aliucord.themer.R;
import com.aliucord.themer.activities.ColorsActivity;
import com.aliucord.themer.activities.DrawablesActivity;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    @SuppressLint("WorldReadableFiles")
    @SuppressWarnings("deprecation")
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        PreferenceManager preferenceManager = getPreferenceManager();
        preferenceManager.setSharedPreferencesMode(Context.MODE_WORLD_READABLE);

        addPreferencesFromResource(R.xml.prefs);

        configure(preferenceManager.getSharedPreferences().getBoolean("__advanced", false));
        Preference advancedPreference = findPreference("__advanced");
        if (advancedPreference != null) advancedPreference.setOnPreferenceChangeListener((p, v) -> {
            configure((boolean) v);
            return true;
        });

        Preference pref = findPreference("custom");
        if (pref != null) pref.setOnPreferenceClickListener(e -> {
            replaceWithFragment(new CustomValuesFragment());
            return true;
        });
        pref = findPreference("colors");
        if (pref != null) pref.setOnPreferenceClickListener(e -> {
            startActivity(new Intent(getContext(), ColorsActivity.class));
            return true;
        });
        pref = findPreference("drawables");
        if (pref != null) pref.setOnPreferenceClickListener(e -> {
            startActivity(new Intent(getContext(), DrawablesActivity.class));
            return true;
        });
    }

    private void configure(boolean advanced) {
        Preference simpleSettings = findPreference("simple_settings");
        Preference advancedSettings = findPreference("advanced_settings");
        if (simpleSettings != null) simpleSettings.setVisible(!advanced);
        if (advancedSettings != null) advancedSettings.setVisible(advanced);
    }

    private void replaceWithFragment(Fragment fragment) {
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_layout, fragment)
                .addToBackStack(null)
                .commit();
    }
}
