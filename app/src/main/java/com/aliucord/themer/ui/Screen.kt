package com.aliucord.themer.ui

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.navigation.*
import com.aliucord.themer.ui.screens.*

class NavData(
    val args: Bundle?,
    val navController: NavController,
)

sealed class Screen(
    val route: String,
    val content: @Composable (NavData) -> Unit,
    val args: List<NamedNavArgument> = emptyList()
) {
    companion object {
        val SCREENS by lazy { arrayOf(Home, Editor, EditorCustomValues, EditorColors, EditorDrawableColors) }

        const val themeIdx = "themeIdx"
        const val m = "m"

        private val themeIdxArg = navArgument(themeIdx) { type = NavType.IntType }
        private val mArg = navArgument(m) { type = NavType.BoolType }
        private val editorArgs = listOf(themeIdxArg, mArg)
    }

    object Home : Screen(
        "home",
        { HomeScreen(it.navController) }
    )

    object Editor : Screen(
        "editor/{$themeIdx}",
        { EditScreen(it.navController, it.args!!.getInt(themeIdx), it.args.getBoolean(m, false)) },
        listOf(themeIdxArg)
    )

    object EditorCustomValues : Screen(
        "editor/{$themeIdx}/custom-values/{$m}",
        { EditCustomValuesScreen(it.navController, it.args!!.getInt(themeIdx), it.args.getBoolean(m)) },
        editorArgs
    )

    object EditorColors : Screen(
        "editor/{$themeIdx}/colors/{$m}",
        { EditColorsScreen(it.navController, it.args!!.getInt(themeIdx), it.args.getBoolean(m)) },
        editorArgs
    )

    object EditorDrawableColors : Screen(
        "editor/{$themeIdx}/drawable-colors/{$m}",
        { EditDrawableColorsScreen(it.navController, it.args!!.getInt(themeIdx), it.args.getBoolean(m)) },
        editorArgs
    )
}
