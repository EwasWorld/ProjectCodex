package eywa.projectcodex.components.sightMarks

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.layout.HorizontalAlignmentLine
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt


/*
 * TODO & IDEAS
 * Help labels
 */

private val START_ALIGNMENT_LINE = HorizontalAlignmentLine(::max)
private val END_ALIGNMENT_LINE = HorizontalAlignmentLine(::max)

@Composable
fun SightMarks(
        state: SightMarksState,
) {
    val tapePadding = 10
    val screenWidth = with(LocalDensity.current) { LocalConfiguration.current.screenWidthDp.dp.toPx() }

    Layout(
            content = {
                SightTape(state = state)
                state.sightMarks.forEach { SightMarkIndicator(it) }
            },
            modifier = Modifier
                    .background(CodexTheme.colors.appBackground)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .horizontalScroll(rememberScrollState())
    ) { measurables, constraints ->
        val tapePlaceable = measurables.first().measure(constraints.copy(minWidth = 0, minHeight = 0))
        val indicatorPlaceables = measurables.drop(1)
                .map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }

        val start = tapePlaceable[START_ALIGNMENT_LINE]
                .takeIf { it != AlignmentLine.Unspecified } ?: 0
        val end = tapePlaceable[END_ALIGNMENT_LINE]
                .takeIf { it != AlignmentLine.Unspecified } ?: tapePlaceable.height
        val diff = end - start

        var (leftRaw, rightRaw) = state.sightMarks
                .zip(indicatorPlaceables)
                .map { (s, p) -> SightMarkIndicatorGroup(s, p, state, diff) }
                .partition { it.indicators.first().isLeft() }

        fun List<SightMarkIndicatorGroup>.getMaxWidth() = takeIf { it.isNotEmpty() }?.maxOf { it.maxWidth } ?: 0

        var tapeOffset = 0
        var rightIndicatorOffset = 0
        var totalWidth = 0

        fun calculateWidths() {
            tapeOffset = leftRaw.getMaxWidth() + tapePadding
            rightIndicatorOffset = tapeOffset + tapePlaceable.width + tapePadding
            totalWidth = rightIndicatorOffset + rightRaw.getMaxWidth()
        }

        calculateWidths()

        // If screen is too small for display on both sides, move all items to the right
        if (totalWidth * 0.8f > screenWidth) {
            rightRaw = leftRaw.plus(rightRaw)
            leftRaw = emptyList()
            calculateWidths()
        }

        val left = leftRaw.sortedBy { it.topOffset }.resolve()
        val right = rightRaw.sortedBy { it.topOffset }.resolve()

        layout(totalWidth, tapePlaceable.height) {
            tapePlaceable.place(x = tapeOffset, y = 0)

            fun List<SightMarkIndicatorGroup>.place(x: Int) {
                forEach { group ->
                    var offset = group.topOffset
                    group.indicators.forEach {
                        it.place(this@layout, x = x, y = start + offset)
                        offset += it.height
                    }
                }
            }

            left.place(0)
            right.place(rightIndicatorOffset)
        }
    }
}

private tailrec fun List<SightMarkIndicatorGroup>.resolve(): List<SightMarkIndicatorGroup> {
    if (size < 2) return this
    val (i, overlappingGroups) = zipWithNext().withIndex()
            .find { (_, pair) -> pair.first.isOverlapping(pair.second) }
            ?: return this
    val newGroup = overlappingGroups.first.mergeWith(overlappingGroups.second)
    return take(i).plus(newGroup).plus(drop(i + 2)).resolve()
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
fun SightMarks_Preview() {
    SightMarks(
            SightMarksState(
                    sightMarks = listOf(
                            SightMark(10, true, Calendar.getInstance(), 3.25f),
                            SightMark(20, true, Calendar.getInstance(), 3.2f),
                            SightMark(30, true, Calendar.getInstance(), 3.15f),
                            SightMark(50, false, Calendar.getInstance(), 2f),
                    ),
            ),
    )
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
        widthDp = 200,
)
@Composable
fun SmallScreen_SightMarks_Preview() {
    SightMarks(
            SightMarksState(
                    sightMarks = listOf(
                            SightMark(10, true, Calendar.getInstance(), 3.25f),
                            SightMark(20, true, Calendar.getInstance(), 3.2f),
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

interface SightMarkIndicator {
    val width: Int
    val height: Int
    fun isLeft(): Boolean
    fun place(scope: Placeable.PlacementScope, x: Int, y: Int)
}

data class SightMarkIndicatorImpl(
        val sightMark: SightMark,
        val placeable: Placeable,
) : SightMarkIndicator {
    override val width: Int = placeable.width
    override val height: Int = placeable.height
    override fun place(scope: Placeable.PlacementScope, x: Int, y: Int) = with(scope) { placeable.place(x, y) }
    override fun isLeft() = !sightMark.isMetric
}

class SightMarkIndicatorGroup @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE) constructor(
        val indicators: List<SightMarkIndicator>,
        val topOffset: Int
) {
    constructor(
            sightMark: SightMark,
            placeable: Placeable,
            state: SightMarksState,
            totalHeight: Int
    ) : this(
            indicators = listOf<SightMarkIndicator>(SightMarkIndicatorImpl(sightMark, placeable)),
            topOffset = (state.getSightMarkAsPercentage(sightMark) * (totalHeight - placeable.height / 2f)).roundToInt(),
    )

    private val height: Int = indicators.sumOf { it.height }
    private val bottomOffset = topOffset + height
    val maxWidth = indicators.maxOf { it.width }

    fun isOverlapping(group: SightMarkIndicatorGroup): Boolean {
        val exclusiveRange = (topOffset + 1) until bottomOffset
        return group.bottomOffset in exclusiveRange || group.topOffset in exclusiveRange
    }

    fun mergeWith(group: SightMarkIndicatorGroup): SightMarkIndicatorGroup {
        val top = if (topOffset < group.topOffset) this else group
        val bottom = if (topOffset < group.topOffset) group else this

        val overlapAmount = abs(top.bottomOffset - bottom.topOffset)
        return SightMarkIndicatorGroup(
                indicators = top.indicators.plus(bottom.indicators),
                topOffset = top.topOffset - overlapAmount / 2,
        )
    }
}
