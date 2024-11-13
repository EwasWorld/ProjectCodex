package eywa.projectcodex.components.shootDetails.headToHeadEnd.grid

import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadArcherType
import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadResult
import eywa.projectcodex.model.Either
import eywa.projectcodex.model.headToHead.FullHeadToHeadSet
import eywa.projectcodex.model.headToHead.HeadToHeadNoResult

data class HeadToHeadGridState(
        val enteredArrows: List<FullHeadToHeadSet>,

        /**
         * Show selectable indicators on arrows/totals
         */
        val isSingleEditableSet: Boolean,

        /**
         * Only used when [isSingleEditableSet] is true, marks the currently selected row
         */
        val selected: HeadToHeadArcherType? = null,

        /**
         * Only used when [isSingleEditableSet] is false
         */
        val runningTotals: List<Either<Pair<Int, Int>, HeadToHeadNoResult>>?,

        /**
         * Only used when [isSingleEditableSet] is false
         */
        val finalResult: HeadToHeadResult?,
) {
    val showExtraTotalColumn = enteredArrows.any { it.showExtraColumnTotal() }
}
