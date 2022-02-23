package com.aliucord.themer.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.fragment.app.FragmentActivity
import com.aliucord.themer.*
import com.aliucord.themer.preferences.sharedPreferences
import com.aliucord.themer.ui.screens.NavGraphs
import com.aliucord.themer.ui.theme.ThemerTheme
import com.aliucord.themer.ui.theme.primaryColorDark
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.defaults.RootNavGraphDefaultAnimations
import com.ramcosta.composedestinations.animations.rememberAnimatedNavHostEngine

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

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialNavigationApi::class)
@Composable
fun MainActivityLayout() {
    val systemUiController = rememberSystemUiController()

    SideEffect {
        systemUiController.setSystemBarsColor(primaryColorDark)
    }

    DestinationsNavHost(
        navGraph = NavGraphs.root,
        engine = rememberAnimatedNavHostEngine(
            rootDefaultAnimations = RootNavGraphDefaultAnimations.ACCOMPANIST_FADING
        )
    )
}
