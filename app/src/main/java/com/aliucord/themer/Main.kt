package com.aliucord.themer

import android.os.Environment
import java.io.File

var xposedEnabled = false
val aliucordBasePath = "${Environment.getExternalStorageDirectory().absolutePath}/Aliucord"
val themesDir = File(aliucordBasePath, "themes")
