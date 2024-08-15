package eywa.projectcodex.plotting

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexThemeColors
import kotlin.math.max

@Composable
fun BoxWithConstraintsScope.DrawTargetAndPlotArrows(
        state: () -> PlottingState,
        listener: (PlottingEvent) -> Unit,
) {
    val colors = CodexTheme.colors

    val translation = with(LocalDensity.current) { Offset(maxWidth.toPx() / 2, maxHeight.toPx() / 2) }
    val maxRadius = max(translation.x, translation.y)

    Canvas(
            modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val newOffset = state().movingArrow
                                    .getOffset(maxRadius)
                                    // Negated because the target is moving rather than the arrow
                                    .plus(dragAmount * -1f * state().sensitivity.multiplier)
                            val arrow = ArrowWithLocation.fromOffset(newOffset, maxRadius, false)
                            listener(PlottingEvent.MoveArrow(arrow))
                        }
                    }
    ) {
        // Negated because the target is moving rather than the arrow
        val targetOffset = state().movingArrow.getOffset(maxRadius) * -1f
        translate(translation.x, translation.y) {
            translate(targetOffset.x, targetOffset.y) {

                drawTarget(maxRadius, colors)

                for (arrow in state().fixedArrows) {
                    drawArrow(arrow.getOffset(maxRadius), colors)
                }
            }
            drawArrow(Offset.Zero, colors)
        }
    }
}

private fun DrawScope.drawArrow(
        centre: Offset,
        colors: CodexThemeColors,
) {
    drawCircle(
            color = colors.targetFaceGreen,
            center = centre,
            radius = 10f,
    )
    drawCircle(
            color = colors.targetFaceBlack,
            center = centre,
            radius = 10f,
            style = Stroke(width = 2f),
    )
}

private fun DrawScope.drawTarget(
        maxRadius: Float,
        colors: CodexThemeColors,
) {
    val mainColourStroke = 4f
    val halfZoneLineStroke = 4f
    val inner10Stroke = 2f
    val spiderWidth = 5f
    val spiderStroke = 2f
    val lineColor = colors.targetFaceBlack

    // White
    drawCircle(
            color = colors.targetFaceWhite,
            center = Offset.Zero,
            radius = maxRadius,
    )
    drawCircle(
            color = lineColor,
            center = Offset.Zero,
            radius = maxRadius,
            style = Stroke(width = mainColourStroke),
    )
    drawCircle(
            color = lineColor,
            center = Offset.Zero,
            radius = maxRadius * 4.5f / 5,
            style = Stroke(width = halfZoneLineStroke),
    )

    // Black
    drawCircle(
            color = colors.targetFaceBlack,
            center = Offset.Zero,
            radius = maxRadius * 4 / 5,
    )
    drawCircle(
            color = Color.White,
            center = Offset.Zero,
            radius = maxRadius * 3.5f / 5,
            style = Stroke(width = halfZoneLineStroke),
    )

    // Blue
    drawCircle(
            color = colors.targetFaceBlue,
            center = Offset.Zero,
            radius = maxRadius * 3 / 5,
    )
    drawCircle(
            color = lineColor,
            center = Offset.Zero,
            radius = maxRadius * 3f / 5,
            style = Stroke(width = mainColourStroke),
    )
    drawCircle(
            color = lineColor,
            center = Offset.Zero,
            radius = maxRadius * 2.5f / 5,
            style = Stroke(width = halfZoneLineStroke),
    )

    // Red
    drawCircle(
            color = colors.targetFaceRed,
            center = Offset.Zero,
            radius = maxRadius * 2 / 5,
    )
    drawCircle(
            color = lineColor,
            center = Offset.Zero,
            radius = maxRadius * 2f / 5,
            style = Stroke(width = mainColourStroke),
    )
    drawCircle(
            color = lineColor,
            center = Offset.Zero,
            radius = maxRadius * 1.5f / 5,
            style = Stroke(width = halfZoneLineStroke),
    )

    // Gold
    drawCircle(
            color = colors.targetFaceGold,
            center = Offset.Zero,
            radius = maxRadius / 5,
    )
    drawCircle(
            color = lineColor,
            center = Offset.Zero,
            radius = maxRadius / 5,
            style = Stroke(width = mainColourStroke),
    )
    drawCircle(
            color = lineColor,
            center = Offset.Zero,
            radius = maxRadius * 0.5f / 5,
            style = Stroke(width = halfZoneLineStroke),
    )

    // Inner 10
    drawCircle(
            color = lineColor,
            center = Offset.Zero,
            radius = maxRadius * 0.25f / 5,
            style = Stroke(width = inner10Stroke),
    )

    // Spider
    drawLine(
            color = lineColor,
            start = Offset(-spiderWidth, 0f),
            end = Offset(spiderWidth, 0f),
            strokeWidth = spiderStroke,
    )
    drawLine(
            color = lineColor,
            start = Offset(0f, -spiderWidth),
            end = Offset(0f, spiderWidth),
            strokeWidth = spiderStroke,
    )
}
