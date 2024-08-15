package eywa.projectcodex.plotting

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

data class ArrowWithLocation(
        val score: Int,

        /**
         * 0 is centre, 1 is a line cutter 1 (in the centre of the line).
         * >1 is allowed to score line cutter 1s or misses
         */
        val r: Double,

        /**
         * Angle in radians
         */
        val theta: Double,
) {
    fun getOffset(canvasRadius: Float) =
            Offset(
                    x = (canvasRadius * r * cos(theta)).toFloat(),
                    y = (canvasRadius * r * sin(theta)).toFloat(),
            )

    companion object {
        fun fromOffset(offset: Offset, canvasRadius: Float, isFiveZone: Boolean): ArrowWithLocation {
            val r = (offset.getDistance() / canvasRadius).toDouble()
            val score = if (r > 1) 0 else ceil((1 - r) * 10).toInt()
            val fiveZoneAdjust = if (isFiveZone && score % 2 == 0) 1 else 0

            return ArrowWithLocation(
                    score = score - fiveZoneAdjust,
                    r = r,
                    theta = atan2(offset.y, offset.x).toDouble(),
            )
        }
    }
}

val arrows = listOf(
        ArrowWithLocation(
                score = 7,
                r = 0.4,
                theta = 0.0,
        ),
        ArrowWithLocation(
                score = 9,
                r = 0.2,
                theta = 0.5,
        ),
)

val movingArrowInit =
        ArrowWithLocation(
                score = 10,
                r = 0.0,
                theta = Math.PI / 2,
        )

data class PlottingState(
        val fixedArrows: List<ArrowWithLocation> = arrows,
        val movingArrow: ArrowWithLocation = movingArrowInit,
) {
    val arrows
        get() = fixedArrows + movingArrow
}

sealed class PlottingEvent {
    data class MoveArrow(val arrow: ArrowWithLocation) : PlottingEvent()
    data object CompleteArrow : PlottingEvent()
}

@Composable
fun PlottingScreen() {
    var state by remember { mutableStateOf(PlottingState()) }

    PlottingScreen({ state }) {
        when (it) {
            is PlottingEvent.MoveArrow -> state = state.copy(movingArrow = it.arrow)
            PlottingEvent.CompleteArrow ->
                state = state.copy(fixedArrows = state.fixedArrows + state.movingArrow, movingArrow = movingArrowInit)
        }
    }
}

@Composable
fun PlottingScreen(
        state: () -> PlottingState,
        listener: (PlottingEvent) -> Unit,
) {
    BoxWithConstraints {
        DrawTargetAndPlotArrows(state, listener)

        Text(text = state().movingArrow.score.toString())
    }
}

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
                                    .plus(dragAmount)
                            val arrow = ArrowWithLocation.fromOffset(newOffset, maxRadius, false)
                            listener(PlottingEvent.MoveArrow(arrow))
                        }
                    }
    ) {
        val current = state().movingArrow.getOffset(maxRadius)
        translate(translation.x, translation.y) {
            translate(current.x, current.y) {
                drawTarget(maxRadius, colors)

                for (arrow in arrows) {
                    drawArrow(arrow.getOffset(maxRadius), colors)
                }
            }
            drawArrow(Offset.Zero, colors)
        }
    }
}

fun DrawScope.drawArrow(
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

fun DrawScope.drawTarget(
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
