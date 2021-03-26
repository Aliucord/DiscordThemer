package com.aliucord.themer.activities;

import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.aliucord.themer.ColorPickerView;
import com.aliucord.themer.Constants;
import com.aliucord.themer.Descriptions;

import java.lang.reflect.Field;
import java.util.ArrayList;

import dalvik.system.PathClassLoader;

public class ColorsActivity extends ListActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PackageManager pm = getPackageManager();
        String discordPackage = MainActivity.isPackageInstalled(pm, "com.aliucord") ? "com.aliucord" : "com.discord";
        try {
            PathClassLoader classLoader = new PathClassLoader(pm.getApplicationInfo(discordPackage, 0).sourceDir, getClassLoader());
            Field[] colors = classLoader.loadClass("com.lytefast.flexinput.R$c").getDeclaredFields();

            Resources res = pm.getResourcesForApplication(discordPackage);
            ArrayList<Adapter.Item<Integer>> items = new ArrayList<>();
            for (Field colorField : colors)
                items.add(new Adapter.Item<>(colorField.getName(), res.getColor(colorField.getInt(null))));
            setAdapter(new Adapter<>(
                    getSupportFragmentManager(),
                    items,
                    (root, item, fm) -> {
                        ColorPickerView view = new ColorPickerView(root.getContext());
                        view.setFragmentManager(fm);
                        view.setTitle(item.name);
                        view.setInfo(Descriptions.colors.get(item.name));
                        view.setKey(MainActivity.prefs, "color_" + item.name);
                        view.setDefaultValue(item.value);
                        view.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        root.removeAllViews();
                        root.addView(view);
                    }
            ));
        } catch (Throwable e) {
            Log.e(Constants.TAG, "Failed to create ColorsActivity", e);
        }

        handleIntent(getIntent());
    }
}
