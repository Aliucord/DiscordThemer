package com.aliucord.themer.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.aliucord.themer.*
import com.aliucord.themer.R
import com.aliucord.themer.preferences.disabledPref
import com.aliucord.themer.ui.Screen
import com.aliucord.themer.ui.components.*
import com.aliucord.themer.utils.ThemeManager
import org.json.JSONObject

class ColorPickerItemData(
    val key: String,
    val default: Color,
    val title: String
)

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun EditScreen(navController: NavController, themeIdx: Int, m: Boolean) {
    val theme = ThemeManager.themes[themeIdx]
    remember {
        theme.reload()
        false
    }
    val json = theme.json

    val modified = remember { mutableStateOf(m) }
    val modifiedArgs = remember { mutableStateOf(false) }
    if (modified.value != m && !modifiedArgs.value) {
        navController.currentBackStackEntry?.arguments?.putBoolean(Screen.m, modified.value)
        modifiedArgs.value = true
    }

    Scaffold(
        topBar = {
            ThemerAppBar(
                navController = navController,
                title = R.string.theme_editor,
                back = true,
            )
        },
        floatingActionButton = {
            if (modified.value) SaveButton {
                theme.save()
                modified.value = false
                modifiedArgs.value = false
            }
        },
    ) {
        Column {
            // TODO
            // ListItem(
            //     text = { Text(stringResource(R.string.manifest_editor)) },
            //     modifier = Modifier.clickable {  },
            // )
            ListItem(
                text = { Text(stringResource(R.string.advanced_settings)) },
                trailing = {
                    Switch(
                        checked = theme.advanced,
                        onCheckedChange = null,
                    )
                },
                modifier = Modifier.clickable { theme.advanced = !theme.advanced },
            )
            Divider()
            if (!theme.advanced) for (data in arrayOf(
                ColorPickerItemData(
                    Constants.SIMPLE_ACCENT_COLOR,
                    colorResource(R.color.simple_accent_color),
                    stringResource(R.string.accent_color),
                ),
                ColorPickerItemData(
                    Constants.MENTION_HIGHLIGHT,
                    colorResource(R.color.mention_highlight),
                    stringResource(R.string.mention_highlight),
                ),
                ColorPickerItemData(
                    Constants.SIMPLE_BG_COLOR,
                    colorResource(R.color.simple_bg_color),
                    stringResource(R.string.bg_color),
                ),
                ColorPickerItemData(
                    Constants.SIMPLE_BG_SECONDARY_COLOR,
                    colorResource(R.color.simple_bg_secondary_color),
                    stringResource(R.string.bg_secondary_color),
                ),
            )) {
                ThemerColorPickerItem(
                    json = json,
                    modified = modified,
                    key = data.key,
                    default = data.default,
                    title = data.title,
                )
            } else {
                if (xposedEnabled) ListItem(
                    text = { Text(stringResource(R.string.force_disable)) },
                    secondaryText = { Text(stringResource(R.string.force_disable_summary)) },
                    trailing = {
                        Switch(
                            checked = disabledPref.get(),
                            onCheckedChange = null,
                        )
                    },
                    modifier = Modifier.clickable { disabledPref.set(!disabledPref.get()) },
                )
                ListItem(
                    text = { Text(stringResource(R.string.custom_values)) },
                    modifier = Modifier.clickable { navController.navigate("editor/${themeIdx}/custom-values/${modified.value}") },
                )
                ListItem(
                    text = {
                        Text(
                            stringResource(R.string.colors),
                            color = if (!xposedEnabled || disabledPref.get()) Color.Unspecified else MaterialTheme.colors.onBackground.copy(
                                ContentAlpha.disabled
                            ),
                        )
                    },
                    modifier = if (!xposedEnabled || disabledPref.get()) Modifier.clickable {
                        navController.navigate("editor/${themeIdx}/colors/${modified.value}")
                    } else Modifier,
                )
                ListItem(
                    text = { Text(stringResource(R.string.drawable_colors)) },
                    secondaryText = { Text(stringResource(R.string.drawable_colors_summary)) },
                    modifier = Modifier.clickable { navController.navigate("editor/${themeIdx}/drawable-colors/${modified.value}") },
                )
            }
        }
    }
}

@Composable
fun ThemerColorPickerItem(
    json: JSONObject,
    modified: MutableState<Boolean>,
    key: String,
    default: Color,
    title: String = key
) {
    var color by remember { mutableStateOf(if (json.has(key)) Color(json.getInt(key)) else default) }
    ColorPickerItem(
        title = title,
        color = color,
        setColor = {
            if (!modified.value) modified.value = true
            color = if (it == null) {
                json.remove(key)
                default
            } else {
                json.put(key, it)
                Color(it)
            }
        }
    )
}
