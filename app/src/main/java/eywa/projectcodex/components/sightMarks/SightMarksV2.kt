package eywa.projectcodex.components.sightMarks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import java.util.*
import kotlin.math.max
import kotlin.math.roundToInt

private const val TICK_LABEL_Y_PADDING = 20
private const val TICK_LABEL_X_PADDING = 50
private const val HALF_TICK_PERCENTAGE_WIDTH = 0.5f
private const val MINOR_TICK_PERCENTAGE_WIDTH = 0.3f

/**
 * Draw a sight tape whose height and width are based on the text size of the labels ([CodexTypography.NORMAL])
 */
@Composable
fun SightMarksV2TapeAndTicks(
        state: SightMarksState,
) {
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
        val tapeWidth = labelPlaceables.maxOf { it.width } + TICK_LABEL_X_PADDING
        val majorTickPlaceables = getNext(majorTicks.size)
                .map { it.measure(Constraints.fixedWidth(tapeWidth)) }
        val halfTickPlaceables = getNext(majorTicks.size - 1)
                .map { it.measure(Constraints.fixedWidth((tapeWidth * HALF_TICK_PERCENTAGE_WIDTH).roundToInt())) }
        val minorTickPlaceables = getNext((majorTicks.size - 1) * 8)
                .map { it.measure(Constraints.fixedWidth((tapeWidth * MINOR_TICK_PERCENTAGE_WIDTH).roundToInt())) }

        val maxLabelHeight = labelPlaceables.maxOf { it.height }
        // Ensure it won't cover the minor tick above/below
        val labelVerticalPadding = max(TICK_LABEL_Y_PADDING, minorTickPlaceables.first().height)
        val yCentreOfFirstLabel = labelPlaceables.first().height / 2
        // The labels sit on a tick and must be contained within the minor ticks above and below
        // Label therefore takes up 2 * minorTickGap. MajorTickGap = 10 * minorTickGap. Hence labelHeight * 5
        val heightPerMajor = (maxLabelHeight + labelVerticalPadding) * 5
        val totalHeight = heightPerMajor * state.totalMajorTicks + yCentreOfFirstLabel +
                labelPlaceables.last().height / 2
        layout(tapeWidth, totalHeight) {
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
fun SightMarksV2_Preview() {
    SightMarksV2TapeAndTicks(
            SightMarksState(
                    sightMarks = listOf(
                            SightMark(30, true, Calendar.getInstance(), 3.15f),
                            SightMark(50, false, Calendar.getInstance(), 2f),
                    ),
            ),
    )
}
