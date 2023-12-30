package eywa.projectcodex.coaching

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
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
    fun Size.getCentredCrossHair() = CoachingCrossHairParams(width / 2, height / 2, 0f)

    val lineColour = CodexTheme.colors.onAppBackground
    val thickness = 10f

    var canvasSize by remember { mutableStateOf<Size?>(null) }

    val actualCrossHair = params()
            ?: canvasSize?.getCentredCrossHair()
            ?: CoachingCrossHairParams(0f, 0f, 0f)

    val update: CoachingCrossHairParams?.(Offset, Float) -> CoachingCrossHairParams = { pan: Offset, rotation: Float ->
        val s = canvasSize
        check(s != null) { "Size not set" }
        val current = this ?: s.getCentredCrossHair()
        current.plus(pan, rotation, s)
    }

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

        val (lineAStart, lineAEnd) = actualCrossHair.getHorizontalLine(canvasSize!!)
        val (lineBStart, lineBEnd) = actualCrossHair.getVerticalLine(canvasSize!!)

        drawLine(
                color = lineColour,
                start = lineAStart,
                end = lineAEnd,
                strokeWidth = thickness,
        )
        drawLine(
                color = lineColour,
                start = lineBStart,
                end = lineBEnd,
                strokeWidth = thickness,
        )
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
                CoachingCrossHairParams(100f, 100f, (Math.PI / 8).toFloat()),
                CoachingCrossHairParams(100f, 100f, 0f),
                CoachingCrossHairParams(0f, 0f, (Math.PI / 8).toFloat()),
                CoachingCrossHairParams(0f, 0f, 0f),
        )
)

data class CoachingCrossHairParams(
        private val x: Float = 0f,
        private val y: Float = 0f,
        /**
         * Rotation of line anticlockwise from horizontal in radians
         */
        private val rotationRads: Float = 0f,
) {
    fun getHorizontalLine(size: Size) = getLineStartAndEnd(x, y, rotationRads, size)
    fun getVerticalLine(size: Size) = getLineStartAndEnd(x, y, rotationRads + (Math.PI.toFloat() / 2), size)

    fun plus(pan: Offset, rotation: Float, maxSize: Size) =
            CoachingCrossHairParams(
                    (x + pan.x).coerceIn(0f, maxSize.width),
                    (y + pan.y).coerceIn(0f, maxSize.height),
                    rotationRads + rotation,
            )

    companion object {
        /**
         * @param rotationRads Rotation of line anticlockwise from horizontal in radians
         */
        fun getLineStartAndEnd(
                x: Float = 0f,
                y: Float = 0f,
                rotationRads: Float = 0f,
                size: Size,
        ): Pair<Offset, Offset> {
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
            ).filter {
                it.x in (0.0..size.width.toDouble()) && it.y in (0.0..size.height.toDouble())
            }.distinct()

            require(points.size == 2) { "Invalid line" }

            return points[0] to points[1]
        }
    }
}
