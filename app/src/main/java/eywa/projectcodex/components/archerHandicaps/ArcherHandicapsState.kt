package eywa.projectcodex.components.archerHandicaps

import eywa.projectcodex.common.utils.ListUtils.plusAtIndex
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationBow
import eywa.projectcodex.database.archer.DatabaseArcherHandicap

data class ArcherHandicapsState(
        /**
         * Most recent handicap of each type
         */
        val currentHandicaps: List<DatabaseArcherHandicap>? = null,
        /**
         * All past handicaps, may include those in [currentHandicaps]
         */
        val allHandicaps: List<DatabaseArcherHandicap>? = null,
        val selectedBowStyle: ClassificationBow = ClassificationBow.RECURVE,
        val lastClickedId: Int? = null,
        val isDropdownMenuShown: Boolean = false,
        val openAddDialog: Boolean = false,
        val deleteDialogOpen: Boolean = false,
        val selectHandicapTypeDialogOpen: Boolean = false,
) {
    val isLoaded = currentHandicaps != null && allHandicaps != null

    val handicapsForDisplay = allHandicaps
            .orEmpty()
            .minus(currentHandicaps.orEmpty().toSet())
            .sortedByDescending { it.dateSet }
            .plusAtIndex(currentHandicaps.orEmpty().sortedByDescending { it.dateSet }, 0)

    val handicapForDeletion
        get() = lastClickedId
                ?.takeIf { deleteDialogOpen }
                ?.let { id -> handicapsForDisplay.find { it.archerHandicapId == id } }
}
