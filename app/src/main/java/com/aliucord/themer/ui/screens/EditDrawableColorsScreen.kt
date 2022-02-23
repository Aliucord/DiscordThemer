package com.aliucord.themer.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.aliucord.themer.R
import com.aliucord.themer.ui.components.SaveButton
import com.aliucord.themer.ui.components.SearchBar
import com.aliucord.themer.utils.ThemeManager
import com.aliucord.themer.utils.Utils
import dalvik.system.PathClassLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun EditDrawableColorsScreen(navController: NavController, themeIdx: Int, m: Boolean) {
    val theme = ThemeManager.themes[themeIdx]
    val json = theme.json

    val modified = remember { mutableStateOf(m) }
    val modifiedArgs = remember { mutableStateOf(false) }
    if (modified.value != m && !modifiedArgs.value) {
        Utils.setRootModified(navController, themeIdx, modified.value)
        modifiedArgs.value = true
    }

    val search = remember { mutableStateOf(false) }
    val query = remember { mutableStateOf("") }

    Scaffold(
        topBar = { SearchBar(navController, R.string.drawable_colors, search, query) },
        floatingActionButton = {
            if (modified.value) SaveButton {
                theme.save()
                modified.value = false
                modifiedArgs.value = false
            }
        },
    ) {
        val ctx = LocalContext.current
        var colors by remember { mutableStateOf<List<String>?>(null) }
        var error by remember { mutableStateOf(false) }

        if (colors == null && !error) rememberCoroutineScope { Dispatchers.IO }.launch {
            try {
                val pm = ctx.packageManager
                val discordPkg = Utils.getDiscordPackage(pm)
                colors = PathClassLoader(pm.getApplicationInfo(discordPkg, 0).sourceDir, null).run {
                    loadClass("com.lytefast.flexinput.R\$d").declaredFields
                }.map { it.name }
            } catch (e: Throwable) {
                Utils.logError(ctx, "Failed to get colors", e)
                error = true
            }
        }

        if (colors != null) LazyColumn {
            items(colors!!.run { if (query.value != "") filter { it.contains(query.value, true) } else this }, key = { it }) {
                ThemerColorPickerItem(
                    json = json,
                    modified = modified,
                    key = "drawablecolor_$it",
                    default = Color.White,
                    title = it,
                )
            }
        } else Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}
