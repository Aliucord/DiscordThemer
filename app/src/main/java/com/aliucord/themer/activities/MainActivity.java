package com.aliucord.themer.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.aliucord.themer.BuildConfig;
import com.aliucord.themer.Constants;
import com.aliucord.themer.R;
import com.aliucord.themer.fragments.SettingsFragment;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private final SettingsFragment settingsFragment = new SettingsFragment();
    private boolean error = false;
    public static SharedPreferences prefs;

    @Override
    @SuppressLint("WorldReadableFiles")
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PackageManager pm = getPackageManager();
        boolean xposedEnabled = isXposedModuleEnabled();
        boolean discordInstalled = isPackageInstalled(pm, "com.discord") || isPackageInstalled(pm, "com.aliucord");
        if (!xposedEnabled || !discordInstalled) {
            TextView errorView = findViewById(R.id.error_view);
            errorView.setVisibility(View.VISIBLE);
            if (xposedEnabled) {
                error = true;
                errorView.setText(R.string.install_discord);
                return;
            }
        }

        if (shouldAskForStorage()) checkPermissions();
        prefs = getSharedPreferences(BuildConfig.APPLICATION_ID + "_preferences", xposedEnabled ? Context.MODE_WORLD_READABLE : Context.MODE_PRIVATE);

        getSupportFragmentManager().beginTransaction().add(R.id.settings_layout, settingsFragment).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!error) getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    @SuppressLint("SetTextI18n")
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_discord) startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://discord.gg/EsNDvBaHVU")));
        else if (id == R.id.action_reset) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.reset_settings)
                    .setMessage(R.string.reset_settings_confirm)
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(android.R.string.yes, (dialog, i) -> {
                        prefs.edit().clear().apply();
                        settingsFragment.onCreate(null);
                    }).show();

        } else if (id == R.id.action_save) {
             if (checkPermissions()) {
                 EditText input = new EditText(this);
                 input.setText(prefs.getString("__last_file_name", "theme1"));
                 new AlertDialog.Builder(this)
                         .setTitle(R.string.enter_file_name)
                         .setView(input)
                         .setNegativeButton(android.R.string.cancel, null)
                         .setPositiveButton(android.R.string.ok, (dialog, i) -> {
                             Map<String, ?> allPrefs = prefs.getAll();
                             Map<String, Object> filtered = new HashMap<>();
                             for (Map.Entry<String, ?> entry : allPrefs.entrySet()) {
                                 if (!entry.getKey().startsWith("__")) filtered.put(entry.getKey(), entry.getValue());
                             }
                             Gson gson = new GsonBuilder().setPrettyPrinting().create();
                             try {
                                 String fileName = input.getText().toString();
                                 File outputFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                                         "DiscordThemer_" + fileName + ".json");
                                 File parent = outputFile.getParentFile();
                                 if (parent != null && !parent.exists()) parent.mkdirs();
                                 FileWriter fileWriter = new FileWriter(outputFile);
                                 fileWriter.write(gson.toJson(filtered));
                                 fileWriter.close();
                                 prefs.edit().putString("__last_file_name", fileName).apply();
                                 Toast.makeText(this, "Saved settings to: " + outputFile, Toast.LENGTH_LONG).show();
                             } catch (Throwable e) {
                                 logError("Exception while writing to file:", e);
                             }
                         }).show();
             }
        } else if (id == R.id.action_load) startActivityForResult(new Intent(Intent.ACTION_GET_CONTENT).setType("application/json"), 0);
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 0) {
            Uri uri = data.getData();
            if (uri != null) try {
                InputStreamReader reader = new InputStreamReader(getContentResolver().openInputStream(uri));
                Map<String, Object> map = new Gson().fromJson(reader,
                        TypeToken.getParameterized(Map.class, String.class, Object.class).getType());
                reader.close();
                SharedPreferences.Editor editor = prefs.edit();
                for (String entry : prefs.getAll().keySet()) if (!entry.startsWith("__")) editor.remove(entry);
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    String key = entry.getKey();
                    Object val = entry.getValue();
                    if (val instanceof Double) editor.putInt(key, ((Double) val).intValue());
                }
                editor.apply();
                Toast.makeText(this, getString(R.string.loaded_settings), Toast.LENGTH_SHORT).show();
                settingsFragment.onCreate(null);
            } catch (Throwable e) {
                logError("Exception while loading settings:", e);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public static boolean isPackageInstalled(PackageManager pm, String packageName) {
        try {
            pm.getPackageInfo(packageName, 0);
            return true;
        } catch (Throwable ignored) {}
        return false;
    }

    private boolean isXposedModuleEnabled() { return getResources().getBoolean(R.bool.xposed); }

    private boolean shouldAskForStorage() { return false; }

    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String perm = "android.permission.WRITE_EXTERNAL_STORAGE";
            if (checkSelfPermission(perm) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, getString(R.string.grant_permission), Toast.LENGTH_LONG).show();
                requestPermissions(new String[]{ perm }, 1);
                return false;
            }
        }
        return true;
    }

    private void logError(String msg, Throwable e) {
        Log.e(Constants.TAG, msg, e);
        Toast.makeText(this, msg + " " + e, Toast.LENGTH_LONG).show();
    }
}
