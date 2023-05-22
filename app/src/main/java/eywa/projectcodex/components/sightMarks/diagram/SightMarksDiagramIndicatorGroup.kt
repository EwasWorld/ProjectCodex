package eywa.projectcodex.components.sightMarks.diagram

import androidx.annotation.VisibleForTesting
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.nextDown
import kotlin.math.nextUp

tailrec fun List<SightMarkIndicatorGroup>.resolve(highestAtTop: Boolean): List<SightMarkIndicatorGroup> {
    if (size < 2) return this
    val (i, overlappingGroups) = zipWithNext().withIndex()
            .find { (_, pair) -> pair.first.isOverlapping(pair.second) }
            ?: return this
    val newGroup = overlappingGroups.first.mergeWith(overlappingGroups.second, highestAtTop)
    return take(i).plus(newGroup).plus(drop(i + 2)).resolve(highestAtTop)
}

class SightMarkIndicatorGroup @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE) constructor(
        val indicators: List<SightMarksDiagramIndicator>,
        private val centre: Float,
) {
    constructor(indicator: SightMarksDiagramIndicator) : this(
            indicators = listOf<SightMarksDiagramIndicator>(indicator),
            centre = indicator.originalCentreOffset,
    )

    init {
        require(indicators.isNotEmpty()) { "Indicators cannot be empty" }
    }

    private val height: Int = indicators.sumOf { it.height() }
    val firstSightMark: Float = indicators.first().sightMark
    val topOffset: Float = centre - height / 2f
    val bottomOffset = topOffset + height
    fun getMaxWidth(indentAmount: Int, isLeft: Boolean) = indicators.withIndex()
            .maxOf { (i, it) ->
                it.width() +
                        getIndentLevel(i) * indentAmount +
                        it.getChevron(isLeft).width / CHEVRON_WIDTH_MODIFIER +
                        it.getPadding()
            }

    fun isOverlapping(group: SightMarkIndicatorGroup): Boolean {
        val exclusiveRange = (topOffset.nextUp())..(bottomOffset.nextDown())
        return group.bottomOffset in exclusiveRange
                || group.topOffset in exclusiveRange
                || group.centre in exclusiveRange
    }

    fun mergeWith(group: SightMarkIndicatorGroup, highestAtTop: Boolean): SightMarkIndicatorGroup {
        val outcome = (firstSightMark - group.firstSightMark) * (if (highestAtTop) 1 else -1)
        val thisIsTop = (outcome == 0f && topOffset < group.topOffset) || outcome > 0f
        val top = if (thisIsTop) this else group
        val bottom = if (thisIsTop) group else this

        val centreDiff = abs(bottom.centre - top.centre)
        val ratio = bottom.height.toFloat() / (top.height + bottom.height).toFloat()

        return SightMarkIndicatorGroup(
                indicators = top.indicators.plus(bottom.indicators),
                centre = top.centre + centreDiff * ratio,
        )
    }

    fun breakApart() = indicators.map { SightMarkIndicatorGroup(it) }

    /**
     * First and last indicators have an indent level of 0,
     * second and penultimate have an indent level of 1,
     * etc.
     */
    fun getIndentLevel(index: Int): Int {
        check(index in indicators.indices) { "Index out of bounds" }
        return min(index, indicators.size - 1 - index)
    }

    fun getIndicatorTopOffset(index: Int): Float = topOffset + indicators.take(index).sumOf { it.height() }

    fun maxIndentLevel() = (indicators.size - 1) / 2
}
