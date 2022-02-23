package com.aliucord.themer.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.ceil

private const val rectangleSize = 10
private const val rectangleSizeF = rectangleSize.toFloat()
private val rectangleSizeDp = rectangleSize.dp
private val rectSize = Size(width = rectangleSizeF, height = rectangleSizeF)

fun DrawScope.AlphaPattern(maxWidth: Dp, maxHeight: Dp) {
    val numRectanglesHorizontal = ceil(maxWidth / rectangleSizeDp).toInt() + 2
    val numRectanglesVertical = ceil(maxHeight / rectangleSizeDp).toInt() + 2

    var verticalStartWhite = true
    for (i in 0..numRectanglesVertical) {
        var isWhite = verticalStartWhite
        for (j in 0..numRectanglesHorizontal) {
            drawRect(
                color = if (isWhite) Color.White else Color.LightGray,
                topLeft = Offset(x = i * rectangleSizeF, y = j * rectangleSizeF),
                size = rectSize,
            )
            isWhite = !isWhite
        }
        verticalStartWhite = !verticalStartWhite
    }
}
