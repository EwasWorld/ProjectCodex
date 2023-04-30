package eywa.projectcodex.components.sightMarks.ui

import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.components.sightMarks.SightMark
import eywa.projectcodex.components.sightMarks.SightMarksState
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.properties.Delegates

/*
 * TODO & IDEAS
 *  Help labels
 */

internal val START_ALIGNMENT_LINE = HorizontalAlignmentLine(::max)
internal val END_ALIGNMENT_LINE = HorizontalAlignmentLine(::max)

private const val HORIZONTAL_LINE_FIXED_WIDTH = 30
private const val INDICATOR_PADDING = 10
private const val INDENT_AMOUNT = 20

@Composable
fun SightMarks(state: SightMarksState) {
    val totalSightMarks = state.sightMarks.size

    Layout(
            content = {
                SightTape(state = state)
                state.sightMarks.forEach { SightMarkIndicator(it) }
                repeat(totalSightMarks) { Icon(Icons.Default.ChevronRight, null) }
                repeat(totalSightMarks) { Icon(Icons.Default.ChevronLeft, null) }
                // Horizontal line from chevron & to text
                repeat(totalSightMarks * 2) { Divider(color = Color.Black, thickness = 2.dp) }
                // Vertical line up
                repeat(totalSightMarks) { Divider(color = Color.Black, modifier = Modifier.width(3.dp)) }
            },
    ) { measurables, constraints ->
        /*
         * Separate measurables
         */
        var current = 1
        fun getNext(n: Int) = measurables.subList(current, current + n).apply { current += n }

        val tape = Tape(
                measurables.first().measure(constraints.copy(minWidth = 0, minHeight = 0))
        )
        val indicatorPlaceables = IndicatorPlaceables(
                indicatorPlaceables = getNext(totalSightMarks)
                        .map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) },
                sightMarks = state.sightMarks,
                getSightMarkAsPercentage = { state.getSightMarkAsPercentage(it) },
                totalHeight = tape.tickHeight,
        )
        val leftChevronPlaceables = getNext(totalSightMarks)
                .map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }
        val rightChevronPlaceables = getNext(totalSightMarks)
                .map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }

        val horizontalMeasurables = getNext(totalSightMarks * 2)
        val verticalMeasurables = getNext(totalSightMarks)

        /*
         * Run measure calculations
         */
        val offsets = Offsets.getOffsets(
                indicatorPlaceables = indicatorPlaceables,
                leftChevronWidth = leftChevronPlaceables.first().width,
                rightChevronWidth = rightChevronPlaceables.first().width,
                tapeWidth = tape.width,
                singleSideThreshold = constraints.maxWidth / 0.9f
        )
        val (horizontalLines, verticalLines) =
                indicatorPlaceables.measureLines(tape, horizontalMeasurables, verticalMeasurables)

        tape.calculateOverhangs(indicatorPlaceables)

        /*
         * Place
         */
        layout(offsets.totalWidth, tape.heightWithOverhang.roundToInt()) {
            tape.placeable.place(x = offsets.tapeOffset, y = tape.topOverhang.roundToInt())

            val counters = PlaceCounters()
            fun List<SightMarkIndicatorGroup>.placeIndicators(isLeft: Boolean) = place(
                    isLeft = isLeft,
                    offsets = offsets,
                    tape = tape,
                    horizontalLines = horizontalLines,
                    verticalLines = verticalLines,
                    leftChevrons = leftChevronPlaceables,
                    rightChevrons = rightChevronPlaceables,
                    counters = counters,
                    place = { x, y -> place(x, y) },
            )

            indicatorPlaceables.left.placeIndicators(isLeft = true)
            indicatorPlaceables.right.placeIndicators(isLeft = false)
        }
    }
}

private fun List<SightMarkIndicatorGroup>.place(
        counters: PlaceCounters,
        offsets: Offsets,
        tape: Tape,
        isLeft: Boolean,
        horizontalLines: List<Placeable>,
        verticalLines: List<Placeable>,
        leftChevrons: List<Placeable>,
        rightChevrons: List<Placeable>,
        place: Placeable.(x: Int, y: Int) -> Unit,
) {
    fun Placeable.offsetPlace(x: Float, y: Float) {
        val startX = if (isLeft) offsets.tapeOffset else offsets.rightIndicatorOffset
        val offset = (x * if (isLeft) -1 else 1).roundToInt()
        val extra = if (isLeft) -width else 0
        place(startX + offset + extra, y.roundToInt())
    }

    forEach { group ->
        var textOffset = group.topOffset
        group.indicators.forEach { indicator ->
            val chevronCentreY = tape.topOverhang + tape.start + indicator.originalCentreOffset
            val indicatorTop = tape.topOverhang + tape.start + textOffset
            val indicatorCentreY = indicatorTop + indicator.height / 2f

            val chevron =
                    if (isLeft) leftChevrons[counters.leftIndex++]
                    else rightChevrons[counters.rightIndex++]
            chevron.offsetPlace(
                    x = 0f,
                    y = chevronCentreY - chevron.height / 2f,
            )

            val horizontal1 = horizontalLines[counters.verticalIndex * 2]
            val horizontal2 = horizontalLines[counters.verticalIndex * 2 + 1]
            val vertical = verticalLines[counters.verticalIndex++]
            val horizontal1Start = chevron.width / 2.3f
            val horizontal2Start = horizontal1Start + horizontal1.width
            horizontal1.offsetPlace(
                    x = horizontal1Start,
                    y = chevronCentreY - horizontal1.height / 2f,
            )
            horizontal2.offsetPlace(
                    x = horizontal2Start,
                    y = indicatorCentreY - horizontal2.height / 2f,
            )
            if (abs(chevronCentreY - indicatorCentreY) > 0.5f) {
                vertical.offsetPlace(
                        x = horizontal2Start - vertical.width / 2f,
                        y = min(
                                chevronCentreY - horizontal1.height / 2f,
                                indicatorCentreY - horizontal2.height / 2f,
                        ),
                )
            }

            indicator.placeable.offsetPlace(
                    x = 0f + horizontal2Start + horizontal2.width + INDICATOR_PADDING,
                    y = indicatorTop
            )
            textOffset += indicator.height
        }
    }
}

private class Tape(val placeable: Placeable) {
    /**
     * First major tick
     */
    val start = placeable[START_ALIGNMENT_LINE]
            .takeIf { it != AlignmentLine.Unspecified } ?: 0

    /**
     * Last major tick
     */
    val end = placeable[END_ALIGNMENT_LINE]
            .takeIf { it != AlignmentLine.Unspecified } ?: placeable.height

    /**
     * Difference between first and last major tick
     */
    val tickHeight = abs(end - start)

    val width
        get() = placeable.width

    var topOverhang by Delegates.notNull<Float>()
    var bottomOverhang by Delegates.notNull<Float>()

    val heightWithOverhang
        get() = placeable.height + topOverhang + bottomOverhang

    fun calculateOverhangs(indicatorPlaceables: IndicatorPlaceables) {
        topOverhang = abs((indicatorPlaceables.minOffset + start).coerceAtMost(0f))
        bottomOverhang = (indicatorPlaceables.maxOffset + start - placeable.height).coerceAtLeast(0f)
    }
}

private class IndicatorPlaceables(
        indicatorPlaceables: List<Placeable>,
        sightMarks: List<SightMark>,
        getSightMarkAsPercentage: (SightMark) -> Float,
        totalHeight: Int,
) {
    var left: List<SightMarkIndicatorGroup>
        private set
    var right: List<SightMarkIndicatorGroup>
        private set
    val all: List<SightMarkIndicatorGroup>
        get() = left.plus(right)

    val maxOffset
        get() = all.maxOf { it.bottomOffset }
    val minOffset
        get() = all.minOf { it.topOffset }

    init {
        val (l, r) = sightMarks
                .zip(indicatorPlaceables)
                .map { (s, p) ->
                    SightMarkIndicatorGroup(
                            SightMarkIndicatorImpl(
                                    sightMark = s,
                                    placeable = p,
                                    originalCentreOffset = getSightMarkAsPercentage(s) * totalHeight,
                            )
                    )
                }
                .partition { it.indicators.first().isLeft() }

        left = l.resolveList()
        right = r.resolveList()
    }

    fun moveAllRight() {
        right = left.flatMap { it.breakApart() }
                .plus(right.flatMap { it.breakApart() })
                .resolveList()
        left = emptyList()
    }

    /**
     * Measure the horizontal/vertical lines
     */
    fun measureLines(
            tape: Tape,
            horizontalMeasurables: List<Measurable>,
            verticalMeasurables: List<Measurable>,
    ): Pair<List<Placeable>, List<Placeable>> {
        val horizontalLines = mutableListOf<Placeable>()
        val verticalLines = mutableListOf<Placeable>()

        var verticalIndex = 0
        all.forEach { group ->
            var textOffset = group.topOffset
            group.indicators.forEachIndexed { index, indicator ->
                val chevronCentreY = tape.start + indicator.originalCentreOffset
                val indicatorCentreY = (tape.start + textOffset + indicator.height / 2f)
                val indentPaddingTotal = group.getIndentLevel(index) * INDENT_AMOUNT

                val horizontal1 = horizontalMeasurables[verticalIndex * 2]
                        .measure(Constraints.fixedWidth(HORIZONTAL_LINE_FIXED_WIDTH + indentPaddingTotal))
                val horizontal2 = horizontalMeasurables[verticalIndex * 2 + 1]
                        .measure(Constraints.fixedWidth(HORIZONTAL_LINE_FIXED_WIDTH))
                horizontalLines.add(horizontal1)
                horizontalLines.add(horizontal2)

                val height = abs(chevronCentreY - indicatorCentreY) + (horizontal1.height + horizontal2.height) / 2f
                verticalLines.add(
                        verticalMeasurables[verticalIndex++].measure(Constraints.fixedHeight(height.roundToInt()))
                )

                textOffset += indicator.height
            }
        }

        return horizontalLines to verticalLines
    }

    companion object {
        fun List<SightMarkIndicatorGroup>.resolveList() = sortedBy { it.topOffset }.resolve()
    }
}

private class Offsets(
        indicatorPlaceables: IndicatorPlaceables,
        leftChevronWidth: Int,
        rightChevronWidth: Int,
        tapeWidth: Int,
) {
    val tapeOffset: Int = indicatorPlaceables.left.getMaxWidth(leftChevronWidth)
    val rightIndicatorOffset: Int = tapeOffset + tapeWidth
    val totalWidth: Int = rightIndicatorOffset + indicatorPlaceables.right.getMaxWidth(rightChevronWidth)

    companion object {
        private fun List<SightMarkIndicatorGroup>.getMaxWidth(chevronWidth: Int): Int {
            if (isEmpty()) return 0
            return (chevronWidth / 2.3f).roundToInt() +
                    HORIZONTAL_LINE_FIXED_WIDTH * 2 +
                    INDICATOR_PADDING +
                    maxOf { it.getMaxWidth(INDENT_AMOUNT) }
        }

        fun getOffsets(
                indicatorPlaceables: IndicatorPlaceables,
                leftChevronWidth: Int,
                rightChevronWidth: Int,
                tapeWidth: Int,
                singleSideThreshold: Float,
        ): Offsets {
            val offsets = Offsets(indicatorPlaceables, leftChevronWidth, rightChevronWidth, tapeWidth)

            // If screen is too small for display on both sides, move all items to the right
            if (offsets.totalWidth < singleSideThreshold) return offsets

            indicatorPlaceables.moveAllRight()
            return Offsets(indicatorPlaceables, leftChevronWidth, rightChevronWidth, tapeWidth)
        }
    }
}

/**
 * Helper to keep track of indexes during and between placing the left and right indicators
 */
private data class PlaceCounters(
        var leftIndex: Int = 0,
        var rightIndex: Int = 0,
        var verticalIndex: Int = 0,
)

@Composable
private fun SightMarkIndicator(sightMark: SightMark) {
    val distanceUnit = stringResource(
            if (sightMark.isMetric) R.string.units_meters_short else R.string.units_yards_short
    )
    val text = listOf(
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

data class SightMarkIndicatorImpl(
        val sightMark: SightMark,
        override val placeable: Placeable,
        override val originalCentreOffset: Float,
) : SightMarkIndicator {
    override val width: Int = placeable.width
    override val height: Int = placeable.height
    override fun isLeft() = !sightMark.isMetric
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
                            SightMark(10, true, Calendar.getInstance(), 3.35f),
                            SightMark(10, true, Calendar.getInstance(), 3.3f),
                            SightMark(10, true, Calendar.getInstance(), 3.25f),
                            SightMark(20, true, Calendar.getInstance(), 3.2f),
                            SightMark(30, true, Calendar.getInstance(), 3.15f),
                            SightMark(50, false, Calendar.getInstance(), 4f),
                            SightMark(50, false, Calendar.getInstance(), 4f),
                            SightMark(50, false, Calendar.getInstance(), 2f),
                            SightMark(50, false, Calendar.getInstance(), 2f),
                            SightMark(20, false, Calendar.getInstance(), 2.55f),
                            SightMark(30, false, Calendar.getInstance(), 2.5f),
                            SightMark(40, false, Calendar.getInstance(), 2.45f),
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
                            SightMark(50, false, Calendar.getInstance(), 2f),
                    ),
            ),
    )
}
