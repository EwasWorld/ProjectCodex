package eywa.projectcodex.components.shootDetails.headToHeadEnd.grid

import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadArcherType

data class HeadToHeadGridState(
        val enteredArrows: List<List<HeadToHeadGridRowData>>,
        val endSize: Int,
        val teamSize: Int,
        /**
         * Only used when [isSingleEditableSet] is true, marks the currently selected row
         */
        val selected: HeadToHeadArcherType?,
        /**
         * Show selectable indicators on arrows/totals
         */
        val isSingleEditableSet: Boolean,
        val hasShootOff: Boolean,
        val isShootOffWin: Boolean,
) {
    val showExtraTotalColumn = enteredArrows.any { it.showExtraColumnTotal() }
}
