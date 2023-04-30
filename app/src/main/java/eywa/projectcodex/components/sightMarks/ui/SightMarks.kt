package eywa.projectcodex.components.sightMarks.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxSize
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
import eywa.projectcodex.components.sightMarks.SightMark
import eywa.projectcodex.components.sightMarks.SightMarksState
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt


/*
 * TODO & IDEAS
 *  Fix small screen cutoff
 *  Help labels
 */

internal val START_ALIGNMENT_LINE = HorizontalAlignmentLine(::max)
internal val END_ALIGNMENT_LINE = HorizontalAlignmentLine(::max)

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
