package com.aliucord.themer.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aliucord.themer.R

@Composable
fun SaveButton(onClick: () -> Unit) {
    FloatingActionButton(onClick, contentColor = Color.White) {
        Icon(
            painter = painterResource(R.drawable.icon_save),
            contentDescription = stringResource(R.string.save_settings),
            modifier = Modifier.size(28.dp),
        )
    }
}
