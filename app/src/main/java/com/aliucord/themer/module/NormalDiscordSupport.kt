package com.aliucord.themer.module

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.widget.Toast
import com.aliucord.themer.Constants
import com.topjohnwu.superuser.Shell
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import kotlin.system.exitProcess

fun fixNormalDiscordSupport(packageName: String, cl: ClassLoader) {
    if (packageName == Constants.DISCORD && Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) XposedHelpers.findAndHookMethod(
        "com.discord.app.AppActivity",
        cl,
        "onCreate",
        Bundle::class.java,
        object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                with(param.thisObject as Activity) {
                    if (
                        checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                        (getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager).unsafeCheckOp(
                            "android:no_isolated_storage",
                            Binder.getCallingUid(),
                            "com.discord"
                        ) != AppOpsManager.MODE_ALLOWED
                    ) {
                        AlertDialog.Builder(this)
                            .setTitle("DiscordThemer Info")
                            .setMessage("Discord doesn't have permission to themes folder. You have to grant root permissions to fix that.\nGrant them only just for this time, you can take them away after.")
                            .setPositiveButton(android.R.string.ok) { _, _ ->
                                val success = Shell.su(
                                    "pm grant com.discord android.permission.READ_EXTERNAL_STORAGE",
                                    "pm grant com.discord android.permission.WRITE_EXTERNAL_STORAGE",
                                    "cmd appops set com.discord android:no_isolated_storage allow"
                                ).exec().isSuccess
                                Toast.makeText(
                                    this,
                                    if (success) "Fixed Discord file permissions, you can disable root permissions now."
                                    else "Failed to fix Discord file permissions.",
                                    Toast.LENGTH_LONG
                                ).show()
                                if (success) {
                                    val intent = packageManager.getLaunchIntentForPackage("com.discord")
                                    startActivity(Intent.makeRestartActivityTask(intent!!.component))
                                    exitProcess(0)
                                }
                            }
                            .setNegativeButton(android.R.string.cancel, null)
                            .show()
                    }
                }
            }
        }
    )
}
