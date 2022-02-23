package com.aliucord.themer.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener

@Composable
@Preview
private fun ColorPickerItemPreview() {
    ColorPickerItem(
        title = "Title",
        color = Color.Green
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ColorPickerItem(
    title: String,
    color: Color,
    setColor: (Int?) -> Unit = {},
) {
    val ctx = LocalContext.current
    ListItem(
        text = { Text(title) },
        trailing = {
            BoxWithConstraints(
                Modifier
                    .clip(CircleShape)
                    .size(28.dp)
                    .border(
                        width = 1.dp,
                        color = if (color.luminance() <= 0.5) Color.White else Color.Black,
                        shape = CircleShape,
                    ),
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    AlphaPattern(maxWidth, maxHeight)
                    drawRect(color)
                }
            }
        },
        modifier = Modifier.clickable {
            ColorPickerDialog
                .newBuilder()
                .setShowAlphaSlider(true)
                .setColor((color.value shr 32).toInt())
                .create()
                .apply {
                    setColorPickerDialogListener(object : ColorPickerDialogListener {
                        override fun onColorSelected(dialogId: Int, color: Int) = setColor(color)

                        override fun onColorReset(dialogId: Int) = setColor(null)

                        override fun onDialogDismissed(dialogId: Int) {}
                    })

                    show((ctx as FragmentActivity).supportFragmentManager, null)
                }
        },
    )
}
