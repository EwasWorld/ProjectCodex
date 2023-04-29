package eywa.projectcodex.components.sightMarks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.layout.HorizontalAlignmentLine
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import java.util.*
import kotlin.math.max
import kotlin.math.roundToInt


/*
 * TODO & IDEAS
 * Small screen -> only one side of the tape
 * Help labels
 */

private val START_ALIGNMENT_LINE = HorizontalAlignmentLine(::max)
private val END_ALIGNMENT_LINE = HorizontalAlignmentLine(::max)

@Composable
fun SightMarks(
        state: SightMarksState,
) {
    val tapePadding = 10

    Layout(
            content = {
                SightTape(state = state)
                state.sightMarks.forEach { SightMarkIndicator(it) }
            },
            modifier = Modifier
    ) { measurables, constraints ->
        val tapePlaceable = measurables.first().measure(constraints)
        val indicatorPlaceables = measurables.drop(1).map { it.measure(constraints) }

        val (left, right) = state.sightMarks.zip(indicatorPlaceables).partition { !it.first.isMetric }
        val tapeOffset = left.maxOf { it.second.width } + tapePadding
        val rightIndicatorOffset = tapeOffset + tapePlaceable.width + tapePadding
        val totalWidth = rightIndicatorOffset + right.maxOf { it.second.width }

        val start = tapePlaceable[START_ALIGNMENT_LINE]
                .takeIf { it != AlignmentLine.Unspecified } ?: 0
        val end = tapePlaceable[END_ALIGNMENT_LINE]
                .takeIf { it != AlignmentLine.Unspecified } ?: tapePlaceable.height
        val diff = end - start

        layout(totalWidth, tapePlaceable.height) {
            tapePlaceable.place(x = tapeOffset, y = 0)

            fun List<Pair<SightMark, Placeable>>.place(x: Int) {
                forEach { (sight, placeable) ->
                    placeable.place(
                            x,
                            start + (state.getSightMarkAsPercentage(sight) * diff).roundToInt() - placeable.height / 2
                    )
                }
            }

            left.place(0)
            right.place(rightIndicatorOffset)
        }
    }
}

@Composable
private fun SightMarkIndicator(
        sightMark: SightMark,
) {
    val distanceUnit = stringResource(
            if (sightMark.isMetric) R.string.units_meters_short else R.string.units_yards_short
    )
    val text = listOf(
            if (sightMark.isMetric) "<-" else "->",
            sightMark.sightMark.toString(),
            "-",
            "${sightMark.distance}$distanceUnit",
    ).let { if (sightMark.isMetric) it else it.asReversed() }

    Text(
            text = text.joinToString(" "),
            style = CodexTypography.NORMAL,
            textAlign = TextAlign.Center,
            modifier = Modifier
    )
}

/**
 * Draw a sight tape whose height and width are based on the text size of the labels ([CodexTypography.NORMAL])
 */
@Composable
private fun SightTape(
        state: SightMarksState,
) {
    val tickLabelYPadding = 20
    val tickLabelXPadding = 50
    val halfTickPercentageWidth = 0.5f
    val minorTickPercentageWidth = 0.3f

    val majorTicks = List(state.totalMajorTicks + 1) { state.getMajorTickLabel(it) }
    Layout(
            content = {
                // Labels
                majorTicks.forEach {
                    Text(
                            // 1 significant figure
                            text = state.formatTickLabel(it),
                            style = CodexTypography.NORMAL,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                    .background(Color.White)
                                    .padding(horizontal = 2.dp)
                    )
                }
                // Major tick lines
                repeat(majorTicks.size) { Divider(color = Color.Black, thickness = 3.dp) }
                // Half tick lines
                repeat(majorTicks.size - 1) { Divider(color = Color.Black, thickness = 2.dp) }
                // Half tick lines
                repeat((majorTicks.size - 1) * 8) { Divider(color = Color.Black, thickness = 1.dp) }
            },
            modifier = Modifier.background(Color.White)
    ) { measurables, constraints ->
        var current = 0
        fun getNext(n: Int) = measurables.subList(current, current + n).apply { current += n }

        val labelPlaceables = getNext(majorTicks.size).map { it.measure(constraints) }
        val tapeWidth = labelPlaceables.maxOf { it.width } + tickLabelXPadding
        val majorTickPlaceables = getNext(majorTicks.size)
                .map { it.measure(Constraints.fixedWidth(tapeWidth)) }
        val halfTickPlaceables = getNext(majorTicks.size - 1)
                .map { it.measure(Constraints.fixedWidth((tapeWidth * halfTickPercentageWidth).roundToInt())) }
        val minorTickPlaceables = getNext((majorTicks.size - 1) * 8)
                .map { it.measure(Constraints.fixedWidth((tapeWidth * minorTickPercentageWidth).roundToInt())) }

        val maxLabelHeight = labelPlaceables.maxOf { it.height }
        // Ensure it won't cover the minor tick above/below
        val labelVerticalPadding = max(tickLabelYPadding, minorTickPlaceables.first().height)
        val yCentreOfFirstLabel = labelPlaceables.first().height / 2
        // The labels sit on a tick and must be contained within the minor ticks above and below
        // Label therefore takes up 2 * minorTickGap. MajorTickGap = 10 * minorTickGap. Hence labelHeight * 5
        val heightPerMajor = (maxLabelHeight + labelVerticalPadding) * 5
        val totalHeight = heightPerMajor * state.totalMajorTicks + yCentreOfFirstLabel +
                labelPlaceables.last().height / 2
        val alignmentLines = mapOf<AlignmentLine, Int>(
                START_ALIGNMENT_LINE to yCentreOfFirstLabel,
                END_ALIGNMENT_LINE to yCentreOfFirstLabel + heightPerMajor * state.totalMajorTicks,
        )
        layout(tapeWidth, totalHeight, alignmentLines) {
            fun List<Placeable>.place(indexModifier: (Int) -> Float) {
                forEachIndexed { index, placeable ->
                    placeable.place(
                            x = (tapeWidth - placeable.width) / 2,
                            y = yCentreOfFirstLabel - (placeable.height / 2) +
                                    (indexModifier(index) * heightPerMajor).roundToInt(),
                    )
                }
            }

            majorTickPlaceables.place { it.toFloat() }
            halfTickPlaceables.place { it + 0.5f }
            labelPlaceables.place { it.toFloat() }
            minorTickPlaceables.place { index ->
                // Skip 0 and 4 (where the major and half tick marks are)
                val tickNumber = (index % 8 + 1).let { if (it > 4) it + 1 else it }
                (index / 8) + (tickNumber / 10f)
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
            SightMarksState(
                    sightMarks = listOf(
                            SightMark(30, true, Calendar.getInstance(), 3.15f),
                            SightMark(50, false, Calendar.getInstance(), 2f),
                    ),
            ),
    )
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun Tape_SightMarks_Preview() {
    SightTape(
            SightMarksState(
                    sightMarks = listOf(
                            SightMark(30, true, Calendar.getInstance(), 3.15f),
                            SightMark(50, false, Calendar.getInstance(), 2f),
                    ),
            ),
    )
}
