package com.aliucord.themer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;

import com.jaredrummler.android.colorpicker.ColorPanelView;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

public class ColorPickerView extends FrameLayout implements ColorPickerDialogListener {
    private SharedPreferences prefs;
    private String key;
    private int defaultValue = Color.BLACK;
    private boolean set = false;
    private FragmentManager fragmentManager;

    private ColorPanelView previewView;
    private TextView titleView;
    private ImageView infoView;

    public ColorPickerView(Context context) {
        super(context);
        initView(context);
    }

    public ColorPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public ColorPickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        inflate(context, R.layout.color_picker_view, this);
        titleView = findViewById(R.id.color_picker_title);
        previewView = findViewById(R.id.color_picker_preview);
        infoView = findViewById(R.id.color_picker_info);

        setOnClickListener(e -> {
            ColorPickerDialog dialog = ColorPickerDialog.newBuilder()
                    .setShowAlphaSlider(true)
                    .setColor(previewView.getColor())
                    .create();
            dialog.setColorPickerDialogListener(this);
            fragmentManager.beginTransaction().add(dialog, "color_" + key).commit();
        });
    }

    public void setKey(SharedPreferences prefs, String key) {
        this.prefs = prefs;
        this.key = key;

        int color = prefs.getInt(key, -1);
        if (color != -1) {
            previewView.setColor(color);
            set = true;
        }
    }

    public void setDefaultValue(int color) {
        defaultValue = color;
        if (!set) previewView.setColor(color);
    }

    public void setTitle(CharSequence title) {
        titleView.setText(title);
    }

    public void setFragmentManager(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }

    public void setInfo(CharSequence text) {
        if (text == null) return;
        infoView.setVisibility(VISIBLE);
        infoView.setOnClickListener(e -> new AlertDialog.Builder(getContext())
                .setTitle(R.string.info)
                .setMessage(text)
                .setPositiveButton(android.R.string.ok, null)
                .show()
        );
    }

    @Override
    public void onColorSelected(int dialogId, int color) {
        previewView.setColor(color);
        prefs.edit().putInt(key, color).apply();
    }

    @Override
    public void onColorReset(int dialogId) {
        previewView.setColor(defaultValue);
        prefs.edit().remove(key).apply();
    }

    @Override
    public void onDialogDismissed(int dialogId) {}
}
