package eywa.projectcodex.components.sightMarks.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.components.sightMarks.SightMark
import eywa.projectcodex.components.sightMarks.SightMarksState
import java.util.*
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Draw a sight tape whose height and width are based on the text size of the labels ([CodexTypography.NORMAL])
 */
@Composable
internal fun SightTape(
        state: SightMarksState,
) {
    val tapeColour = CodexTheme.colors.sightMarksTapeBackground
    val tickColour = CodexTheme.colors.sightMarksTicksAndLabels
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
                            style = CodexTypography.NORMAL.copy(color = tickColour),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                    .background(tapeColour)
                                    .padding(horizontal = 2.dp)
                    )
                }
                // Major tick lines
                repeat(majorTicks.size) { Divider(color = tickColour, thickness = 3.dp) }
                // Half tick lines
                repeat(majorTicks.size - 1) { Divider(color = tickColour, thickness = 2.dp) }
                // Half tick lines
                repeat((majorTicks.size - 1) * 8) { Divider(color = tickColour, thickness = 1.dp) }
            },
            modifier = Modifier.background(tapeColour)
    ) { measurables, constraints ->
        var current = 0
        fun getNext(n: Int) = measurables.subList(current, current + n).apply { current += n }

        val labelPlaceables = getNext(majorTicks.size)
                .map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }
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
