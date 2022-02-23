package com.aliucord.themer.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.appcompat.app.AlertDialog
import com.aliucord.themer.*
import org.json.JSONObject
import org.json.JSONTokener
import java.io.File
import java.io.FileOutputStream

object Utils {
    fun getImportIntent(ext: String): Intent {
        val type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
        return Intent.createChooser(Intent(Intent.ACTION_GET_CONTENT).setType(type), "Choose a file")
    }

    fun handleImportRes(context: Context, ext: String, res: ActivityResult) {
        val data = res.data
        if (data == null || res.resultCode != Activity.RESULT_OK) return

        try {
            val uri = data.data
            if (uri.toString().endsWith(ext)) doImport(context, uri!!, File(uri.path!!).name, ext)
            else {
                try {
                    val json = JSONObject(JSONTokener(String(context.contentResolver.openInputStream(uri!!)!!.readBytes())))
                    if (json.has("name")) {
                        doImport(context, uri, json.getString("name"), ext)
                        return
                    }
                } catch (ignored: Throwable) {
                }

                val input = EditText(context)
                input.setHint(R.string.filename)
                AlertDialog.Builder(context)
                    .setTitle(R.string.enter_file_name)
                    .setView(input)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        val text = input.text
                        if (text.isNotEmpty()) doImport(context, uri!!, text.toString(), ext)
                    }.show()
            }
        } catch (e: Throwable) {
            logError(context, "Failed to import", e)
        }
    }

    private fun doImport(context: Context, uri: Uri, name: String, ext: String) {
        importComponent(context, uri, name, ext)
        Toast.makeText(context, "Successfully imported", Toast.LENGTH_SHORT).show()
        ThemeManager.loadThemes()
    }

    private fun importComponent(context: Context, uri: Uri, name: String, ext: String) {
        val file = File(themesDir, if (name.endsWith(ext)) name else name + ext)
        context.contentResolver.openInputStream(uri)!!.copyTo(FileOutputStream(file))
    }

    fun logError(context: Context, msg: String, e: Throwable) {
        Log.e(BuildConfig.TAG, msg, e)
        Toast.makeText(context, "$msg $e", Toast.LENGTH_LONG).show()
    }

    private fun PackageManager.isPackageInstalled(packageName: String) = try {
        getPackageInfo(packageName, 0)
        true
    } catch (ignored: Throwable) {
        false
    }

    fun getDiscordPackage(pm: PackageManager) = if (pm.isPackageInstalled(Constants.ALIUCORD)) Constants.ALIUCORD else Constants.DISCORD
}
