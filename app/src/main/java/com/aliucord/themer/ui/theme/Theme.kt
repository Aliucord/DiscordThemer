package com.aliucord.themer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColors(
    primary = primaryColor,
    primaryVariant = primaryColorDark,
    secondary = primaryColorLight,

    onPrimary = Color.White,
    background = darkBackground,
    surface = Color(0xff424242)
)

private val LightColorPalette = lightColors(
    primary = primaryColor,
    primaryVariant = primaryColorDark,
    secondary = primaryColorLight
)

@Composable
fun ThemerTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkColorPalette else LightColorPalette

    MaterialTheme(
        colors,
        typography = Typography,
        shapes = Shapes,
        content
    )
}
