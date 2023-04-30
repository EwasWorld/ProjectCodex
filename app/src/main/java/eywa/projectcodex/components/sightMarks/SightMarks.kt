package eywa.projectcodex.components.sightMarks

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
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
import kotlin.math.*


/*
 * TODO & IDEAS
 *  Fix small screen cutoff
 *  Help labels
 */

private val START_ALIGNMENT_LINE = HorizontalAlignmentLine(::max)
private val END_ALIGNMENT_LINE = HorizontalAlignmentLine(::max)

@Composable
fun SightMarks(
        state: SightMarksState,
) {
    val screenWidth = with(LocalDensity.current) { LocalConfiguration.current.screenWidthDp.dp.toPx() }
    val totalSightMarks = state.sightMarks.size
    val horizontalLineFixedWidth = 30
    val indicatorPadding = (horizontalLineFixedWidth * 2 - 25).coerceAtLeast(0)
    val indentAmount = 20

    Layout(
            content = {
                SightTape(state = state)
                state.sightMarks.forEach { SightMarkIndicator(it) }
                repeat(totalSightMarks) { Icon(Icons.Default.ChevronLeft, null) }
                repeat(totalSightMarks) { Icon(Icons.Default.ChevronRight, null) }
                // Horizontal line from chevron & to text
                repeat(totalSightMarks * 2) { Divider(color = Color.Black, thickness = 2.dp) }
                // Vertical line up
                repeat(totalSightMarks) { Divider(color = Color.Black, modifier = Modifier.width(3.dp)) }
            },
            modifier = Modifier
                    .background(CodexTheme.colors.appBackground)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .horizontalScroll(rememberScrollState())
    ) { measurables, constraints ->
        var current = 1
        fun getNext(n: Int) = measurables.subList(current, current + n).apply { current += n }

        val tapePlaceable = measurables.first().measure(constraints.copy(minWidth = 0, minHeight = 0))
        val indicatorPlaceables = getNext(totalSightMarks)
                .map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }
        val rightChevronPlaceables = getNext(totalSightMarks)
                .map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }
        val leftChevronPlaceables = getNext(totalSightMarks)
                .map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }

        val horizontalMeasurables = getNext(totalSightMarks * 2)
        val verticalMeasurables = getNext(totalSightMarks)
        val horizontalLines = mutableListOf<Placeable>()
        val verticalLines = mutableListOf<Placeable>()

        val start = tapePlaceable[START_ALIGNMENT_LINE]
                .takeIf { it != AlignmentLine.Unspecified } ?: 0
        val end = tapePlaceable[END_ALIGNMENT_LINE]
                .takeIf { it != AlignmentLine.Unspecified } ?: tapePlaceable.height
        val diff = end - start

        var (left, right) = state.sightMarks
                .zip(indicatorPlaceables)
                .map { (s, p) ->
                    SightMarkIndicatorGroup(
                            SightMarkIndicatorImpl(
                                    sightMark = s,
                                    placeable = p,
                                    originalCentreOffset = state.getSightMarkAsPercentage(s) * diff,
                            )
                    )
                }
                .partition { it.indicators.first().isLeft() }

        fun List<SightMarkIndicatorGroup>.getMaxWidth(chevron: Placeable): Int {
            if (isEmpty()) return 0
            return maxOf { it.getMaxWidth(indentAmount) } + indicatorPadding + chevron.width
        }

        var tapeOffset = 0
        var rightIndicatorOffset = 0
        var totalWidth = 0

        fun calculateWidths() {
            left = left.sortedBy { it.topOffset }.resolve()
            right = right.sortedBy { it.topOffset }.resolve()

            tapeOffset = left.getMaxWidth(leftChevronPlaceables.first())
            rightIndicatorOffset = tapeOffset + tapePlaceable.width
            totalWidth = rightIndicatorOffset + right.getMaxWidth(rightChevronPlaceables.first())
        }

        calculateWidths()

        // If screen is too small for display on both sides, move all items to the right
        if (totalWidth * 0.8f > screenWidth) {
            right = left.plus(right)
            left = emptyList()
            calculateWidths()
        }

        val both = left.plus(right)
        var verticalIndexM = 0
        both.forEach { group ->
            var textOffset = group.topOffset
            group.indicators.forEachIndexed { index, indicator ->
                val chevronCentreY = start + indicator.originalCentreOffset
                val indicatorCentreY = (start + textOffset + indicator.height / 2f).roundToInt()
                val indentPaddingTotal = group.getIndentLevel(index) * indentAmount

                val horizontal1 = horizontalMeasurables[verticalIndexM * 2]
                        .measure(Constraints.fixedWidth(horizontalLineFixedWidth + indentPaddingTotal))
                val horizontal2 = horizontalMeasurables[verticalIndexM * 2 + 1]
                        .measure(Constraints.fixedWidth(horizontalLineFixedWidth))
                horizontalLines.add(horizontal1)
                horizontalLines.add(horizontal2)

                val height = abs(chevronCentreY - indicatorCentreY) + (horizontal1.height + horizontal2.height) / 2f
                verticalLines.add(
                        verticalMeasurables[verticalIndexM++].measure(Constraints.fixedHeight(height.roundToInt()))
                )

                textOffset += indicator.height
            }
        }

        val topOverhang = abs((both.minOf { it.topOffset } + start).roundToInt()
                .coerceAtMost(0))
        val bottomOverhang = (both.maxOf { it.bottomOffset } + start - tapePlaceable.height).roundToInt()
                .coerceAtLeast(0)
        layout(totalWidth, tapePlaceable.height + topOverhang + bottomOverhang) {
            tapePlaceable.place(x = tapeOffset, y = topOverhang)

            var leftIndex = 0
            var rightIndex = 0
            var verticalIndex = 0
            fun List<SightMarkIndicatorGroup>.place(isLeft: Boolean) {
                fun Placeable.offsetPlace(x: Float, y: Float) {
                    val startX = if (isLeft) tapeOffset else rightIndicatorOffset
                    val offset = (x * if (isLeft) -1 else 1).roundToInt()
                    val extra = if (isLeft) -width else 0
                    place(startX + offset + extra, y.roundToInt())
                }

                forEach { group ->
                    var textOffset = group.topOffset
                    group.indicators.forEachIndexed { index, indicator ->
                        val chevronCentreY = topOverhang + start + indicator.originalCentreOffset
                        val indicatorTop = topOverhang + start + textOffset
                        val indicatorCentreY = indicatorTop + indicator.height / 2f
                        val indentPaddingTotal = group.getIndentLevel(index) * indentAmount

                        val chevron =
                                if (isLeft) leftChevronPlaceables[leftIndex++]
                                else rightChevronPlaceables[rightIndex++]
                        chevron.offsetPlace(
                                x = 0f,
                                y = chevronCentreY - chevron.height / 2f,
                        )

                        val horizontal1 = horizontalLines[verticalIndex * 2]
                        val horizontal2 = horizontalLines[verticalIndex * 2 + 1]
                        val vertical = verticalLines[verticalIndex++]
                        val horizontal1Start = chevron.width / 2.3f
                        horizontal1.offsetPlace(
                                x = horizontal1Start,
                                y = chevronCentreY - horizontal1.height / 2f,
                        )
                        horizontal2.offsetPlace(
                                x = horizontal1Start + horizontal1.width,
                                y = indicatorCentreY - horizontal2.height / 2f,
                        )
                        if (abs(chevronCentreY - indicatorCentreY) > 0.5f) {
                            vertical.offsetPlace(
                                    x = horizontal1Start + horizontal1.width - vertical.width / 2f,
                                    y = min(
                                            chevronCentreY - horizontal1.height / 2f,
                                            indicatorCentreY - horizontal2.height / 2f,
                                    ),
                            )
                        }

                        indicator.placeable.offsetPlace(
                                x = 0f + chevron.width + indicatorPadding + indentPaddingTotal,
                                y = indicatorTop
                        )
                        textOffset += indicator.height
                    }
                }
            }

            left.place(true)
            right.place(false)
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
    val originalCentreOffset: Float
    val placeable: Placeable
    fun isLeft(): Boolean
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

class SightMarkIndicatorGroup @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE) constructor(
        val indicators: List<SightMarkIndicator>,
        private val centre: Float,
) {
    constructor(indicator: SightMarkIndicator) : this(
            indicators = listOf<SightMarkIndicator>(indicator),
            centre = indicator.originalCentreOffset,
    )

    init {
        require(indicators.isNotEmpty()) { "Indicators cannot be empty" }
    }

    private val height: Int = indicators.sumOf { it.height }
    val topOffset: Float = centre - height / 2f
    val bottomOffset = topOffset + height
    fun getMaxWidth(indentAmount: Int) = indicators.withIndex()
            .maxOf { (i, it) -> it.width + getIndentLevel(i) * indentAmount }

    fun isOverlapping(group: SightMarkIndicatorGroup): Boolean {
        val exclusiveRange = (topOffset.nextUp())..(bottomOffset.nextDown())
        return group.bottomOffset in exclusiveRange
                || group.topOffset in exclusiveRange
                || group.centre in exclusiveRange
    }

    fun mergeWith(group: SightMarkIndicatorGroup): SightMarkIndicatorGroup {
        val top = if (topOffset < group.topOffset) this else group
        val bottom = if (topOffset < group.topOffset) group else this

        val centreDiff = abs(bottom.centre - top.centre)
        val ratio = bottom.height.toFloat() / (top.height + bottom.height).toFloat()

        return SightMarkIndicatorGroup(
                indicators = top.indicators.plus(bottom.indicators),
                centre = top.centre + centreDiff * ratio,
        )
    }

    /**
     * First and last indicators have an indent level of 0,
     * second and penultimate have an indent level of 1,
     * etc.
     */
    fun getIndentLevel(index: Int): Int {
        check(index in indicators.indices) { "Index out of bounds" }
        return min(index, indicators.size - 1 - index)
    }

    fun maxIndentLevel() = (indicators.size - 1) / 2
}
