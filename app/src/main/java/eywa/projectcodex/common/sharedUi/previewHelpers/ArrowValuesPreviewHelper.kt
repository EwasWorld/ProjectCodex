package eywa.projectcodex.common.sharedUi.previewHelpers

import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.model.Arrow

object ArrowValuesPreviewHelper {
    val ARROWS = arrayOf(
            Arrow(0, false),
            Arrow(1, false),
            Arrow(2, false),
            Arrow(3, false),
            Arrow(4, false),
            Arrow(5, false),
            Arrow(6, false),
            Arrow(7, false),
            Arrow(8, false),
            Arrow(9, false),
            Arrow(10, false),
            Arrow(10, true),
    )

    fun getArrows(
            archerRoundId: Int = 1,
            size: Int = 6,
            firstArrowNumber: Int = 1,
            score: Int,
            isX: Boolean,
    ) = List(size) { ArrowValue(archerRoundId, firstArrowNumber + it, score, isX) }

    fun getArrowsInOrder(
            archerRoundId: Int = 1,
            size: Int = 6,
            firstArrowNumber: Int = 1,
            ascending: Boolean = true,
    ) = List(size) {
        var index = it % ARROWS.size
        if (!ascending) index -= ARROWS.size - 1
        ARROWS[index].toArrowValue(archerRoundId, firstArrowNumber + it)
    }

    fun getArrowsInOrderFullSet(
            archerRoundId: Int = 1,
            firstArrowNumber: Int = 1,
            ascending: Boolean = true,
    ) = ARROWS
            .mapIndexed { index, arrow -> arrow.toArrowValue(archerRoundId, index + firstArrowNumber) }
            .let { if (ascending) it else it.reversed() }
}
