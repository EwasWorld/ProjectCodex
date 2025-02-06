package eywa.projectcodex.components.shootDetails.headToHead.grid

import eywa.projectcodex.R
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.components.shootDetails.headToHead.HeadToHeadArcherType
import eywa.projectcodex.components.shootDetails.headToHead.HeadToHeadResult
import eywa.projectcodex.model.Either
import eywa.projectcodex.model.headToHead.FullHeadToHeadSet
import eywa.projectcodex.model.headToHead.HeadToHeadNoResult

sealed class HeadToHeadGridState {
    abstract val matchNumber: Int
    abstract val enteredArrows: List<FullHeadToHeadSet>

    /**
     * Allows entry of arrows and totals.
     * Can only represent one set.
     * Shows selectable indicators on arrows/totals.
     */
    data class SingleEditable(
            override val enteredArrows: List<FullHeadToHeadSet>,

            /**
             * Currently selected row
             */
            val selected: HeadToHeadArcherType? = null,
    ) : HeadToHeadGridState() {
        init {
            require(enteredArrows.isEmpty() || enteredArrows.size == 1)
        }

        override val matchNumber: Int = 1
    }

    data class NonEditable(
            override val matchNumber: Int,
            override val enteredArrows: List<FullHeadToHeadSet>,
            val runningTotals: List<Either<Pair<Int, Int>, HeadToHeadNoResult>>?,
            val finalResult: HeadToHeadResult?,

            /**
             * <matchNumber, setNumber, dropdownMenuItems>
             */
            val dropdownMenuExpandedFor: Triple<Int, Int, List<DropdownMenuItem>>? = null,
    ) : HeadToHeadGridState()

    val showExtraTotalColumn
        get() = enteredArrows.any { it.showExtraColumnTotal() }
}

enum class DropdownMenuItem(val title: ResOrActual<String>) {
    INSERT(title = ResOrActual.StringResource(R.string.head_to_head_grid__dropdown_insert)),
    EDIT(title = ResOrActual.StringResource(R.string.head_to_head_grid__dropdown_edit)),
    DELETE(title = ResOrActual.StringResource(R.string.head_to_head_grid__dropdown_delete)),
}
