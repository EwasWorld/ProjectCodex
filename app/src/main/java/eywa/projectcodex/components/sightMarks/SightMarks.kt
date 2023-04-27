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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import java.util.*

/*
 * TODO & IDEAS
 * Small screen -> only one side of the tape
 */

/**
 * Space between the top of the tape and the first major tick
 */
private const val VERTICAL_PADDING = 40f
private const val TAPE_WIDTH = 80f

@Composable
fun SightMarks(
        state: SightMarksState,
) {
    // TODO_CURRENT Help info

    Box(
            modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp * state.totalMajorTicks)
    ) {
        TapeAndTicks(state)

        BoxWithConstraints(
                modifier = Modifier.fillMaxSize()
        ) {
            val majorTickYGap = with(LocalDensity.current) {
                ((constraints.maxHeight - VERTICAL_PADDING * 2) / state.totalMajorTicks).toDp()
            }

            MajorTickLabels(state, majorTickYGap)
            SightMarkIndicators(state, majorTickYGap, constraints.maxWidth, constraints.maxHeight)
        }
    }
}

@Composable
private fun TapeAndTicks(state: SightMarksState) {
    Canvas(
            modifier = Modifier.fillMaxSize()
    ) {
        val tapeTopLeft = Offset(x = (size.width - TAPE_WIDTH) / 2f, y = 0f)

        drawRect(
                color = Color.White,
                topLeft = Offset(x = (size.width - TAPE_WIDTH) / 2f, y = 0f),
                size = Size(width = TAPE_WIDTH, height = size.height),
        )

        val majorTickYGap = (size.height - VERTICAL_PADDING * 2) / state.totalMajorTicks
        repeat(state.totalMajorTicks * Tick.minorTicksPerMajorTick + 1) { index ->
            val offset = majorTickYGap * index / Tick.minorTicksPerMajorTick
            Tick.getTickType(index).draw(this, tapeTopLeft, TAPE_WIDTH, offset + VERTICAL_PADDING)
        }
    }
}

@Composable
private fun MajorTickLabels(
        state: SightMarksState,
        majorTickYGap: Dp,
) {
    val verticalPadding = with(LocalDensity.current) { VERTICAL_PADDING.toDp() }
    repeat(state.totalMajorTicks + 1) { index ->
        val offset = verticalPadding + majorTickYGap * (index.toFloat() - 0.5f)
        Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                        .offset(x = 0.dp, y = offset)
                        .height(majorTickYGap)
                        .fillMaxWidth()
        ) {
            Text(
                    text = state.getMajorTickLabel(index).toString(),
                    style = CodexTypography.NORMAL,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                            .background(Color.White)
                            .padding(horizontal = 2.dp)
            )
        }
    }
}

@Composable
private fun SightMarkIndicators(
        state: SightMarksState,
        majorTickYGap: Dp,
        maxWidth: Int,
        maxHeight: Int,
) {
    val verticalPadding = with(LocalDensity.current) { VERTICAL_PADDING.toDp() }
    val tapeWidth = with(LocalDensity.current) { TAPE_WIDTH.toDp() }
    val indicatorWidth = with(LocalDensity.current) { ((maxWidth - TAPE_WIDTH) / 2f).toDp() }
    val totalHeight = with(LocalDensity.current) { (maxHeight - VERTICAL_PADDING * 2).toDp() }

    state.sightMarks.forEach { sightMark ->
        val distanceUnit = stringResource(
                if (sightMark.isMetric) R.string.units_meters_short else R.string.units_yards_short
        )
        val text = listOf(
                if (sightMark.isMetric) "<-" else "->",
                sightMark.sightMark.toString(),
                "-",
                "${sightMark.distance}$distanceUnit",
        ).let { if (sightMark.isMetric) it else it.asReversed() }

        val adjustedSightMark = state.getSightMarkAsPercentage(sightMark)
        val offset = totalHeight * adjustedSightMark + verticalPadding
        Box(
                contentAlignment = if (sightMark.isMetric) Alignment.CenterStart else Alignment.CenterEnd,
                modifier = Modifier
                        .offset(x = if (sightMark.isMetric) indicatorWidth + tapeWidth else 0.dp, y = offset)
                        .size(width = indicatorWidth, height = majorTickYGap)
                        .padding(5.dp)
        ) {
            Text(
                    text = text.joinToString(" "),
                    style = CodexTypography.NORMAL,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
            )
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
            SightMarksState(
                    sightMarks = listOf(
                            SightMark(30, true, Calendar.getInstance(), 3.15f),
                            SightMark(50, false, Calendar.getInstance(), 2f),
                    ),
            ),
    )
}

private enum class Tick(
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
