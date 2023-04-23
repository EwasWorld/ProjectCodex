package eywa.projectcodex.components.sightMarks

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import java.util.*

data class SightMarksState(
        val sightMarks: List<SightMark> = listOf(),
        val isHighestNumberAtTheTop: Boolean = true,
)

data class SightMark(
        val distance: Int,
        val isMetric: Boolean,
        val dateSet: Calendar,
        val sightMark: Int,
        val note: String,
        val marked: Boolean,
) {
}

enum class Tick(
        private val frequency: Int,
        private val stroke: Float,
        private val padding: Float,
) {
    MAJOR(10, 6f, 0f),
    HALF(5, 4f, 15f),
    MINOR(1, 2f, 25f),
    ;

    fun draw(scope: DrawScope, firstTickTopLeft: Offset, tapeWidth: Float, verticalOffset: Float) {
        with(scope) {
            drawLine(
                    color = Color.Black,
                    start = firstTickTopLeft.plus(Offset(padding, verticalOffset)),
                    end = firstTickTopLeft.plus(Offset(tapeWidth - padding, verticalOffset)),
                    strokeWidth = stroke,
            )
        }
    }

    companion object {
        val minorTicksPerMajorTick = MAJOR.frequency

        fun getTickType(index: Int) = values().first { index % it.frequency == 0 }
    }
}

@Composable
fun SightMarks(
        state: SightMarksState,
) {
    // TODO_CURRENT Help info

    val maxMajorTick = 8
    val minMajorTick = 1

    val verticalPadding = 40f
    val tapeWidth = 80f

    Canvas(
            modifier = Modifier.fillMaxSize()
    ) {
        val tapeTopLeft = Offset(x = (size.width - tapeWidth) / 2f, y = 0f)

        /*
         * Draw tape & ticks
         */
        drawRect(
                color = Color.White,
                topLeft = Offset(x = (size.width - tapeWidth) / 2f, y = 0f),
                size = Size(width = tapeWidth, height = size.height),
        )

        val majorTickYGap = (size.height - verticalPadding * 2) / (maxMajorTick - minMajorTick)
        repeat((maxMajorTick - minMajorTick) * Tick.minorTicksPerMajorTick + 1) { index ->
            val offset = majorTickYGap * index / Tick.minorTicksPerMajorTick
            Tick.getTickType(index).draw(this, tapeTopLeft, tapeWidth, offset + verticalPadding)
        }
    }

    BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
    ) {
        val size = Size(width = constraints.maxWidth.toFloat(), height = constraints.maxHeight.toFloat())
        val majorTickYGap = (size.height - verticalPadding * 2) / (maxMajorTick - minMajorTick)
        repeat(maxMajorTick - minMajorTick + 1) { index ->
            val offset = verticalPadding + majorTickYGap * index - majorTickYGap / 2f
            Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                            .offset(0.dp, with(LocalDensity.current) { offset.toDp() })
                            .height(with(LocalDensity.current) { majorTickYGap.toDp() })
                            .fillMaxWidth()
            ) {
                Text(
                        text = (
                                if (state.isHighestNumberAtTheTop) maxMajorTick - index
                                else minMajorTick + index
                                ).toString(),
                        style = CodexTypography.NORMAL,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                                .background(Color.White)
                                .padding(horizontal = 2.dp)
                )
            }
        }
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun SightMarks_Preview() {
    SightMarks(
            SightMarksState(),
    )
}
