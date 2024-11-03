package eywa.projectcodex.components.shootDetails.headToHeadEnd.grid

import eywa.projectcodex.common.sharedUi.grid.CodexGridRowMetadata
import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadArcherType
import eywa.projectcodex.model.Arrow

sealed class HeadToHeadGridRowData : CodexGridRowMetadata {
    abstract val type: HeadToHeadArcherType

    data class Arrows(override val type: HeadToHeadArcherType, val arrows: List<Arrow>) : HeadToHeadGridRowData() {
        override fun totalScore(): Int = arrows.sumOf { it.score }
        override fun isComplete(teamSize: Int, endSize: Int): Boolean =
                arrows.size == type.expectedArrowCount(endSize, teamSize)
    }

    data class Total(override val type: HeadToHeadArcherType, val total: Int?) : HeadToHeadGridRowData() {
        override fun totalScore(): Int = total ?: 0
        override fun isComplete(teamSize: Int, endSize: Int): Boolean = total != null
    }

    abstract fun totalScore(): Int
    abstract fun isComplete(teamSize: Int, endSize: Int): Boolean
}
