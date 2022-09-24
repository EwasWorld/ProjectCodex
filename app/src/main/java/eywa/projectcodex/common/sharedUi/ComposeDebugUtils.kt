@file:Suppress("unused")

package eywa.projectcodex.common.sharedUi

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

fun DrawScope.drawX(
        topLeft: Offset,
        bottomRight: Offset,
        colour: Color = Color.Yellow,
        strokeWidth: Float = 5f,
) {
    drawLine(
            color = colour,
            start = topLeft,
            end = bottomRight,
            strokeWidth = strokeWidth
    )
    drawLine(
            color = colour,
            start = Offset(topLeft.x, bottomRight.y),
            end = Offset(bottomRight.x, topLeft.y),
            strokeWidth = strokeWidth
    )
}

fun DrawScope.drawGrid(
        topLeft: Offset,
        bottomRight: Offset,
        rows: Int = 1,
        columns: Int = 1,
        colour: Color = Color.Yellow,
        strokeWidth: Float = 5f,
) {
    val left = topLeft.x
    val right = bottomRight.x
    val top = topLeft.y
    val bottom = bottomRight.y

    val width = right - left
    val height = bottom - top

    repeat(rows - 1) { rowIndex ->
        val y = (rowIndex + 1) * height / rows
        drawLine(
                color = colour,
                start = Offset(left, y),
                end = Offset(right, y),
                strokeWidth = strokeWidth
        )
    }
    repeat(columns - 1) { columnIndex ->
        val x = (columnIndex + 1) * width / columns
        drawLine(
                color = colour,
                start = Offset(x, top),
                end = Offset(x, bottom),
                strokeWidth = strokeWidth
        )
    }
}