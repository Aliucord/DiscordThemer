package com.aliucord.themer.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.*
import com.aliucord.themer.*
import com.aliucord.themer.R
import com.aliucord.themer.preferences.sharedPreferences
import com.aliucord.themer.ui.theme.ThemerTheme
import com.aliucord.themer.ui.theme.primaryColorDark
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class MainActivity : FragmentActivity() {
    @SuppressLint("WorldReadableFiles")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        xposedEnabled = resources.getBoolean(R.bool.xposed)
        sharedPreferences = getSharedPreferences(
            BuildConfig.PREFERENCES_NAME,
            if (xposedEnabled) Context.MODE_WORLD_READABLE else Context.MODE_PRIVATE,
        )

        setContent {
            ThemerTheme {
                MainActivityLayout()
            }
        }
    }
}

private const val animationDuration = 350

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainActivityLayout() {
    val systemUiController = rememberSystemUiController()

    SideEffect {
        systemUiController.setSystemBarsColor(primaryColorDark)
    }

    val navController = rememberNavController()
    NavHost(
        navController,
        startDestination = Screen.Home.route,
    ) {
        for (screen in Screen.SCREENS) {
            composable(
                screen.route,
                arguments = screen.args
            ) {
                val navData = NavData(it.arguments, navController)
                if (it.destination.route != Screen.Home.route) AnimatedVisibility(
                    visibleState = remember { MutableTransitionState(initialState = false) }.apply { targetState = true },
                    enter = slideInVertically(tween(animationDuration)) { v -> v / 2 } + fadeIn(animationSpec = tween(animationDuration)),
                    exit = slideOutVertically(tween(animationDuration)) { v -> v / 2 } + fadeOut(animationSpec = tween(animationDuration)),
                ) {
                    screen.content(navData)
                } else screen.content(navData)
            }
        }
    }
}
