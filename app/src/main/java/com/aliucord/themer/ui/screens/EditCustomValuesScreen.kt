package com.aliucord.themer.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import com.aliucord.themer.Constants
import com.aliucord.themer.R
import com.aliucord.themer.ui.components.SaveButton
import com.aliucord.themer.ui.components.ThemerAppBar
import com.aliucord.themer.utils.ThemeManager
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.result.ResultBackNavigator

@Destination
@Composable
fun EditCustomValuesScreen(themeIdx: Int, m: Boolean, resultNavigator: ResultBackNavigator<Boolean>) {
    val theme = ThemeManager.themes[themeIdx]
    val json = theme.json

    val modified = remember { mutableStateOf(m) }
    resultNavigator.setResult(modified.value)

    Scaffold(
        topBar = {
            ThemerAppBar(
                resultNavigator = resultNavigator,
                title = R.string.custom_values,
                back = true,
            )
        },
        floatingActionButton = {
            if (modified.value) SaveButton {
                theme.save()
                modified.value = false
            }
        },
    ) {
        Column {
            for (data in arrayOf(
                ColorPickerItemData(
                    Constants.INPUT_BG_COLOR,
                    colorResource(R.color.input_background_color),
                    stringResource(R.string.input_background_color),
                ),
                ColorPickerItemData(
                    Constants.STATUSBAR_COLOR,
                    colorResource(R.color.statusbar_color),
                    stringResource(R.string.statusbar_color),
                ),
                ColorPickerItemData(
                    Constants.ACTIVE_CHANNEL_COLOR,
                    colorResource(R.color.active_channel_color),
                    stringResource(R.string.active_channel_color),
                ),
                ColorPickerItemData(
                    Constants.MENTION_HIGHLIGHT,
                    colorResource(R.color.mention_highlight),
                    stringResource(R.string.mention_highlight),
                ),
            )) ThemerColorPickerItem(
                json = json,
                modified = modified,
                key = data.key,
                default = data.default,
                title = data.title,
            )
        }
    }
}
