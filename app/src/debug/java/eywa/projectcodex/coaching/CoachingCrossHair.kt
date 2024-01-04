package eywa.projectcodex.coaching

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
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
import kotlin.math.pow
import kotlin.math.sqrt


fun getHypotenuse(a: Float, b: Float) = sqrt(a.pow(2) + b.pow(2))

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
            Icon(
                    imageVector = actualCrossHair.mode.icon,
                    contentDescription = actualCrossHair.mode.contentDescription.get(),
                    tint = lineColour,
                    modifier = Modifier
                            .padding(15.dp)
                            .scale(1.5f)
            )
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
                            imageVector = Icons.Default.NavigateBefore,
                            contentDescription = "Previous",
                    ),
                    onClick = {
                        listener(actualCrossHair.previousMode(canvasSize!!) ?: actualCrossHair)
                    },
            )
            CodexFloatingActionButton(
                    icon = CodexIconInfo.VectorIcon(
                            imageVector = Icons.Default.NavigateNext,
                            contentDescription = "Next",
                    ),
                    onClick = {
                        listener(actualCrossHair.nextMode(canvasSize!!) ?: actualCrossHair)
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
