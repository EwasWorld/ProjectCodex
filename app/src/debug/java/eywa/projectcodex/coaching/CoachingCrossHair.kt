package eywa.projectcodex.coaching

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ControlCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VerticalAlignCenter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import eywa.projectcodex.coaching.CoachingCrossHairMode.*
import eywa.projectcodex.common.logging.debugLog
import eywa.projectcodex.common.sharedUi.CodexFloatingActionButton
import eywa.projectcodex.common.sharedUi.CodexIconInfo
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan


@Composable
fun CoachingCrossHairTestScreen(
) {
    var params by remember {
        mutableStateOf<CoachingCrossHairParams?>(null)
    }
    CoachingCrossHair(
            params = { params },
            listener = { params = it },
    )
}

@Composable
fun CoachingCrossHair(
        params: () -> CoachingCrossHairParams?,
        listener: (CoachingCrossHairParams) -> Unit,
) {
    fun Size.getCentredCrossHair() = CoachingCrossHairParams(width / 2, 2 * height / 3, 0f, -height / 3)

    val lineColour = CodexTheme.colors.onAppBackground
    val thickness = 10f

    var canvasSize by remember { mutableStateOf<Size?>(null) }

    val actualCrossHair = params()
            ?: canvasSize?.getCentredCrossHair()
            ?: CoachingCrossHairParams(0f, 0f, 0f, 0f)
    debugLog(actualCrossHair.toString())

    val update: CoachingCrossHairParams?.(Offset, Float) -> CoachingCrossHairParams =
            { pan: Offset, rotation: Float ->
                val s = canvasSize
                check(s != null) { "Size not set" }
                val current = this ?: s.getCentredCrossHair()
                current.plus(pan, rotation, s)
            }

    Box(
            contentAlignment = Alignment.BottomEnd,
    ) {
        Canvas(
                modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, _, rotation ->
                                listener(params().update(pan, rotation * Math.PI.toFloat() / 180))
                            }
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                listener(params().update(dragAmount, 0f))
                            }
                        }
        ) {
            canvasSize = size

            val (lineAStart, lineAEnd) = actualCrossHair.getFeetLine(canvasSize!!)
            val (lineBStart, lineBEnd) = actualCrossHair.getBodyLine(canvasSize!!)

            drawLine(
                    lineColour,
                    start = lineAStart,
                    end = lineAEnd,
                    strokeWidth = thickness,
            )
            drawLine(
                    lineColour,
                    start = lineBStart,
                    end = lineBEnd,
                    strokeWidth = thickness,
            )
            if (actualCrossHair.mode != SET_FEET) {
                val (lineCStart, lineCEnd) = actualCrossHair.getShoulderLine(canvasSize!!)
                drawLine(
                        color = Color.Blue,
                        start = lineCStart,
                        end = lineCEnd,
                        strokeWidth = thickness,
                )
            }
        }
        Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(10.dp)
        ) {
            if (actualCrossHair.mode == SET_FEET) {
                CodexFloatingActionButton(
                        icon = CodexIconInfo.VectorIcon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset",
                        ),
                        onClick = { canvasSize?.getCentredCrossHair()?.let { listener(it) } },
                )
            }
            CodexFloatingActionButton(
                    icon = CodexIconInfo.VectorIcon(
                            imageVector = actualCrossHair.mode.icon,
                            contentDescription = "Next",
                    ),
                    onClick = {
                        listener(actualCrossHair.shiftMode(canvasSize!!) ?: actualCrossHair.copy(mode = SET_FEET))
                    },
            )
        }
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun CoachingCrossHair_Preview(
        @PreviewParameter(CoachingCrossHairPreviewPP::class) params: CoachingCrossHairParams?,
) {
    CodexTheme {
        CoachingCrossHair({ params }) {}
    }
}

class CoachingCrossHairPreviewPP : CollectionPreviewParameterProvider<CoachingCrossHairParams?>(
        listOf(
                null,
                CoachingCrossHairParams(600f, 1000f, (Math.PI / 8).toFloat(), 500f),
                CoachingCrossHairParams(1000f, 1000f, 0f, 500f),
                CoachingCrossHairParams(0f, 500f, (Math.PI / 8).toFloat(), 500f),
                CoachingCrossHairParams(0f, 500f, 0f, 500f),
        )
)

enum class CoachingCrossHairMode(val icon: ImageVector) {
    /**
     * Move and rotate a single crosshair to be between the feet
     * so the horizontal line runs through both feet
     * and the vertical line runs through the centre of the body
     */
    SET_FEET(Icons.Default.ControlCamera),

    /**
     * Move a point up and down the vertical line so that a horizontal line at that point runs through the shoulders
     */
    SET_SHOULDERS(Icons.Default.VerticalAlignCenter),
    ;

    fun nextMode() = values().getOrNull(ordinal + 1)
}

fun getHypotenuse(a: Float, b: Float) = sqrt(a.pow(2) + b.pow(2))

data class CoachingCrossHairParams(
        val x: Float,
        val y: Float,
        /**
         * Rotation of line anticlockwise from horizontal in radians
         */
        val rotationRads: Float,
        /**
         * Distance from the point ([x],[y]) along the vertical like ([rotationRads] + PI/2).
         * Positive means below the point (down in the y direction)
         */
        val distance: Float,
        val mode: CoachingCrossHairMode = SET_FEET,
) {
    private val inverseAngle = rotationRads + Math.PI.toFloat() / 2

    /**
     * Line that should run through centre of both feet.
     * Runs through ([x],[y]) with an angle of [rotationRads]
     */
    fun getFeetLine(size: Size) = getLineStartAndEnd(x, y, (rotationRads), size)

    /**
     * Line that should run vertically through the centre of the body.
     * When standing upright, goes from the centre of the head, through the centre of the body and hips,
     * and to the mid-point between the feet.
     * Runs through ([x],[y]) with an angle of [inverseAngle]
     */
    fun getBodyLine(size: Size) = getLineStartAndEnd(x, y, (inverseAngle), size)

    /**
     * Line that should run through the centre of the shoulders.
     * Runs through ([x],[y]) offset by [distance] with an angle of [rotationRads]
     */
    fun getShoulderLine(size: Size) =
            getAltXY().let { (newX, newY) -> getLineStartAndEnd(newX, newY, (rotationRads), size) }

    /**
     * ([x],[y]) offset by [distance]
     */
    private fun getAltXY() = (x + cos(inverseAngle) * distance) to (y + sin(inverseAngle) * distance)

    /**
     * Min and max of [distance]
     */
    private fun getDistanceBounds(size: Size) = getBodyLine(size).toList().map {
        val c = getHypotenuse(it.x - x, it.y - y)
        val mult = if (it.y < y) -1 else 1
        c * mult
    }.sorted()[0] to 10f

    fun plus(pan: Offset, rotation: Float, maxSize: Size) = when (mode) {
        SET_FEET -> {
            CoachingCrossHairParams(
                    x = (x + pan.x).coerceIn(0f, maxSize.width),
                    y = (y + pan.y).coerceIn(0f, maxSize.height),
                    rotationRads = (rotationRads + rotation).coerceIn(-Math.PI.toFloat() / 4, Math.PI.toFloat() / 4),
                    distance = distance,
                    mode = SET_FEET,
            )
        }

        SET_SHOULDERS -> {
            val panMagnitude = getHypotenuse(pan.x, pan.y)

            if (panMagnitude.isEqualTo(0f)) {
                this
            }
            else {
                val panAngle = when {
                    pan.x.isInRange(0f, 0f) && pan.y <= 0f -> 3 * Math.PI.toFloat() / 2
                    pan.x.isInRange(0f, 0f) -> Math.PI.toFloat() / 2
                    else -> atan2(pan.y, pan.x)
                }
                val distanceChange = panMagnitude * cos(inverseAngle - panAngle)

                val (minDistance, maxDistance) = getDistanceBounds(maxSize)
                copy(distance = (distance + distanceChange).coerceIn(minDistance, maxDistance))
            }
        }
    }

    fun shiftMode(maxSize: Size): CoachingCrossHairParams? {
        val nextMode = mode.nextMode() ?: return null
        val (minDistance, maxDistance) = getDistanceBounds(maxSize)
        return copy(
                distance = distance.coerceIn(minDistance, maxDistance),
                mode = nextMode,
        )
    }

    fun Float.toDeg() = 360 * this / (2 * Math.PI.toFloat())

    companion object {
        fun getLineStartAndEnd(
                x: Float = 0f,
                y: Float = 0f,
                rotationRads: Float = 0f,
                size: Size,
        ): Pair<Offset, Offset> {
            if ((rotationRads % Math.PI.toFloat()).isEqualTo(Math.PI.toFloat() / 2)) {
                return Offset(x, 0f) to Offset(x, size.height)
            }

            // y = mx + c
            val m = tan(rotationRads)
            val c = (y - m * x)

            // Find points that intercept with lines x = 0 and x = size.width (same for height)
            // Then remove ones outside of size, leaving just the start/end of the line
            val points = listOf(
                    Offset(0f, (m * 0 + c)),
                    Offset(size.width, (m * size.width + c)),
                    Offset(((0 - c) / m), 0f),
                    Offset(((size.height - c) / m), size.height),
            )
                    .filter {
                        it.x.isInRange(size.width, 0f) && it.y.isInRange(size.height, 0f)
                    }
                    .distinct()
                    .sortedBy { it.x }
                    .toMutableList()

            var i = 1
            while (points.size > 2 && i in points.indices) {
                val current = points[i - 1]
                val next = points[i]
                if (current.x.isEqualTo(next.x) && current.y.isEqualTo(next.y)) {
                    points.removeAt(i)
                    continue
                }
                i++
            }

            if (points.size != 2) {
                throw IllegalStateException("Invalid line $size\n$points")
            }

            return points[0] to points[1]
        }

        private fun Float.isEqualTo(value: Float, delta: Float = 0.01f) =
                this.isInRange(value, value, delta)

        private fun Float.isInRange(max: Float, min: Float, delta: Float = 0.01f) =
                this >= (min - delta) && this <= (max + delta)
    }
}
