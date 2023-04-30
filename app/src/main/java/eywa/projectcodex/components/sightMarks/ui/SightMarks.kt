package eywa.projectcodex.components.sightMarks.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.sharedUi.ComposeUtils.modifierIf
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
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
 *  Marked on top
 *  Fix 2.01 showing below 2
 *  Help labels
 */

internal val START_ALIGNMENT_LINE = HorizontalAlignmentLine(::max)
internal val END_ALIGNMENT_LINE = HorizontalAlignmentLine(::max)

private const val HORIZONTAL_LINE_FIXED_WIDTH = 30
private const val INDICATOR_PADDING = 10
private const val INDENT_AMOUNT = 20
internal const val CHEVRON_WIDTH_MODIFIER = 2.3f

@Composable
fun SightMarks(state: SightMarksState) {
    val totalSightMarks = state.sightMarks.size
    val marked = state.sightMarks.map {
        if (it.marked) CodexTheme.colors.sightMarksMarkedBackground else CodexTheme.colors.sightMarksIndicator
    }

    Layout(
            content = {
                SightTape(state = state)
                state.sightMarks.forEach { SightMarkIndicator(it) }
                repeat(totalSightMarks) { index ->
                    Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = marked[index]
                    )
                }
                repeat(totalSightMarks) { index ->
                    Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = null,
                            tint = marked[index]
                    )
                }
                // Horizontal line from chevron & to text
                repeat(totalSightMarks) { index ->
                    repeat(2) {
                        Divider(color = marked[index], thickness = 2.dp)
                    }
                }
                // Vertical line up
                repeat(totalSightMarks) { index ->
                    Divider(color = marked[index], modifier = Modifier.width(3.dp))
                }
            },
    ) { measurables, constraints ->
        /*
         * Separate measurables
         */
        val noMinConstraints = constraints.copy(minWidth = 0, minHeight = 0)
        var current = 1
        fun getNext(n: Int) = measurables.subList(current, current + n).apply { current += n }

        val tape = Tape(measurables.first().measure(noMinConstraints))
        val indicatorMeasureables = getNext(totalSightMarks)
        val leftChevronMeasureables = getNext(totalSightMarks)
        val rightChevronMeasureables = getNext(totalSightMarks)
        val horizontalMeasurables = getNext(totalSightMarks * 2)
        val verticalMeasurables = getNext(totalSightMarks)

        /*
         * Run measure calculations
         */
        val placeables = IndicatorPlaceables(
                indicatorPlaceables = indicatorMeasureables.map { it.measure(noMinConstraints) },
                sightMarks = state.sightMarks,
                getSightMarkAsPercentage = { state.getSightMarkAsPercentage(it) },
                totalHeight = tape.tickHeight,
                leftChevronPlaceables = leftChevronMeasureables.map { it.measure(noMinConstraints) },
                rightChevronPlaceables = rightChevronMeasureables.map { it.measure(noMinConstraints) },
                horizontalMeasurables = horizontalMeasurables,
                verticalMeasurables = verticalMeasurables,
        )
        val offsets = Offsets.getOffsetsAndAdjustForScreenSize(
                indicatorPlaceables = placeables,
                tapeWidth = tape.width,
                singleSideThreshold = constraints.maxWidth / 0.9f
        )
        placeables.calculateAndSetLinePlaceables(tape)
        tape.calculateAndSetOverhangs(placeables)

        /*
         * Place
         */
        layout(offsets.totalWidth, tape.heightWithOverhang.roundToInt()) {
            fun List<SightMarkIndicatorGroup>.placeIndicators(isLeft: Boolean) =
                    place(isLeft, offsets, tape) { x, y -> place(x, y) }

            tape.placeable.place(x = offsets.tapeOffset, y = tape.topOverhang.roundToInt())
            placeables.left.placeIndicators(isLeft = true)
            placeables.right.placeIndicators(isLeft = false)
        }
    }
}

private fun List<SightMarkIndicatorGroup>.place(
        isLeft: Boolean,
        offsets: Offsets,
        tape: Tape,
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

            val chevron = indicator.getChevron(isLeft)
            chevron.offsetPlace(
                    x = 0f,
                    y = chevronCentreY - chevron.height / 2f,
            )

            val horizontal1 = indicator.horizontalLine1Placeable!!
            val horizontal2 = indicator.horizontalLine2Placeable!!
            val vertical = indicator.verticalLinePlaceable!!
            val horizontal1Start = chevron.width / CHEVRON_WIDTH_MODIFIER
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

            indicator.indicatorPlaceable.offsetPlace(
                    x = 0f + horizontal2Start + horizontal2.width + indicator.getPadding(),
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

    fun calculateAndSetOverhangs(indicatorPlaceables: IndicatorPlaceables) {
        topOverhang = abs((indicatorPlaceables.minOffset + start).coerceAtMost(0f))
        bottomOverhang = (indicatorPlaceables.maxOffset + start - placeable.height).coerceAtLeast(0f)
    }
}

private class IndicatorPlaceables(
        indicatorPlaceables: List<Placeable>,
        sightMarks: List<SightMark>,
        getSightMarkAsPercentage: (SightMark) -> Float,
        totalHeight: Int,
        leftChevronPlaceables: List<Placeable>,
        rightChevronPlaceables: List<Placeable>,
        horizontalMeasurables: List<Measurable>,
        verticalMeasurables: List<Measurable>,
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
        val (l, r) = sightMarks.indices
                .map {
                    val sightMark = sightMarks[it]
                    SightMarkIndicatorGroup(
                            SightMarkIndicatorImpl(
                                    sightMark = sightMark,
                                    indicatorPlaceable = indicatorPlaceables[it],
                                    originalCentreOffset = getSightMarkAsPercentage(sightMark) * totalHeight,
                                    chevronLeft = leftChevronPlaceables[it],
                                    chevronRight = rightChevronPlaceables[it],
                                    horizontalLine1Measurable = horizontalMeasurables[it * 2],
                                    horizontalLine2Measurable = horizontalMeasurables[it * 2 + 1],
                                    verticalLineMeasurable = verticalMeasurables[it],
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
    fun calculateAndSetLinePlaceables(tape: Tape) {
        all.forEach { group ->
            var textOffset = group.topOffset
            group.indicators.forEachIndexed { index, indicator ->
                val chevronCentreY = tape.start + indicator.originalCentreOffset
                val indicatorCentreY = (tape.start + textOffset + indicator.height / 2f)
                val indentPaddingTotal = group.getIndentLevel(index) * INDENT_AMOUNT

                val horizontal1 = indicator.horizontalLine1Measurable!!
                        .measure(Constraints.fixedWidth(HORIZONTAL_LINE_FIXED_WIDTH + indentPaddingTotal))
                val horizontal2 = indicator.horizontalLine2Measurable!!
                        .measure(Constraints.fixedWidth(HORIZONTAL_LINE_FIXED_WIDTH))
                indicator.horizontalLine1Placeable = horizontal1
                indicator.horizontalLine2Placeable = horizontal2

                val height = abs(chevronCentreY - indicatorCentreY) + (horizontal1.height + horizontal2.height) / 2f
                indicator.verticalLinePlaceable =
                        indicator.verticalLineMeasurable!!.measure(Constraints.fixedHeight(height.roundToInt()))

                indicator.horizontalLine1Measurable = null
                indicator.horizontalLine2Measurable = null
                indicator.verticalLineMeasurable = null

                textOffset += indicator.height
            }
        }
    }

    companion object {
        fun List<SightMarkIndicatorGroup>.resolveList() = sortedBy { it.topOffset }.resolve()
    }
}

private class Offsets(
        indicatorPlaceables: IndicatorPlaceables,
        tapeWidth: Int,
) {
    val tapeOffset: Int = indicatorPlaceables.left.getMaxWidth(true)
    val rightIndicatorOffset: Int = tapeOffset + tapeWidth
    val totalWidth: Int = rightIndicatorOffset + indicatorPlaceables.right.getMaxWidth(false)

    companion object {
        private fun List<SightMarkIndicatorGroup>.getMaxWidth(isLeft: Boolean): Int {
            if (isEmpty()) return 0
            return HORIZONTAL_LINE_FIXED_WIDTH * 2 +
                    maxOf { it.getMaxWidth(INDENT_AMOUNT, isLeft) }.roundToInt()
        }

        /**
         * Calculate offsets and move all placables to right if necessary
         *
         * @param singleSideThreshold if the width of the layout is larger than this threshold,
         * shift all indicators to the right (rather than having them on both sides)
         */
        fun getOffsetsAndAdjustForScreenSize(
                indicatorPlaceables: IndicatorPlaceables,
                tapeWidth: Int,
                singleSideThreshold: Float,
        ): Offsets {
            val offsets = Offsets(indicatorPlaceables, tapeWidth)

            // If screen is too small for display on both sides, move all items to the right
            if (offsets.totalWidth < singleSideThreshold) return offsets

            indicatorPlaceables.moveAllRight()
            return Offsets(indicatorPlaceables, tapeWidth)
        }
    }
}

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
            style = CodexTypography.NORMAL
                    .copy(
                            fontStyle = if (sightMark.marked) FontStyle.Italic else FontStyle.Normal,
                            fontWeight = if (sightMark.marked) FontWeight.Bold else FontWeight.Normal,
                    ),
            color = CodexTheme.colors.sightMarksIndicator,
            textAlign = TextAlign.Center,
            modifier = Modifier.modifierIf(
                    sightMark.marked,
                    Modifier
                            .padding(vertical = 3.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CodexTheme.colors.sightMarksMarkedBackground)
                            .padding(horizontal = 10.dp)
            )
    )
}

data class SightMarkIndicatorImpl(
        val sightMark: SightMark,
        override val indicatorPlaceable: Placeable,
        override val originalCentreOffset: Float,
        private val chevronLeft: Placeable,
        private val chevronRight: Placeable,
        override var horizontalLine1Measurable: Measurable?,
        override var horizontalLine2Measurable: Measurable?,
        override var verticalLineMeasurable: Measurable?,
        override var horizontalLine1Placeable: Placeable? = null,
        override var horizontalLine2Placeable: Placeable? = null,
        override var verticalLinePlaceable: Placeable? = null,
) : SightMarkIndicator {
    override val width: Int = indicatorPlaceable.width
    override val height: Int = indicatorPlaceable.height
    override fun isLeft() = !sightMark.isMetric
    override fun getChevron(isLeft: Boolean): Placeable = if (isLeft) chevronLeft else chevronRight
    override fun getPadding(): Float = if (sightMark.marked) 0f else INDICATOR_PADDING.toFloat()
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
                            SightMark(50, false, Calendar.getInstance(), 2.01f),
                            SightMark(50, false, Calendar.getInstance(), 2f, marked = true),
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
