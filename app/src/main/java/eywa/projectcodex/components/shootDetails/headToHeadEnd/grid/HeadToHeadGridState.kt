package eywa.projectcodex.components.shootDetails.headToHeadEnd.grid

import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadArcherType
import eywa.projectcodex.model.FullHeadToHeadSet

data class HeadToHeadGridState(
        val enteredArrows: List<FullHeadToHeadSet>,
        /**
         * Only used when [isSingleEditableSet] is true, marks the currently selected row
         */
        val selected: HeadToHeadArcherType?,
        /**
         * Show selectable indicators on arrows/totals
         */
        val isSingleEditableSet: Boolean,
) {
    val showExtraTotalColumn = enteredArrows.any { it.showExtraColumnTotal() }
}
