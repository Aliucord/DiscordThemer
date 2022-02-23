package com.aliucord.themer.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.aliucord.themer.ui.theme.primaryColor

@Composable
fun ThemerAppBar(
    navController: NavController,
    title: Int,
    back: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {}
) {
    BaseThemerAppBar(
        title = { Text(stringResource(title)) },
        navigationIcon = if (back) {{
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = null,
                )
            }
        }} else null,
        actions = actions,
    )
}

@Composable
fun BaseThemerAppBar(
    title: @Composable () -> Unit,
    navigationIcon: @Composable (() -> Unit)?,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = title,
        navigationIcon = navigationIcon,
        actions = actions,
        backgroundColor = primaryColor,
        contentColor = MaterialTheme.colors.onPrimary,
    )
}
