package eywa.projectcodex.components.viewScores.ui.filters

import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogState
import eywa.projectcodex.database.Filters
import eywa.projectcodex.database.shootData.ShootFilter
import java.util.Calendar

data class ViewScoresFiltersState(
        val isExpanded: Boolean = false,
        val from: Calendar? = null,
        val until: Calendar? = null,
        val selectRoundDialogState: SelectRoundDialogState = SelectRoundDialogState(),
        /**
         * True if round filter indicated by [selectRoundDialogState] should be applied.
         * Cannot use null in [selectRoundDialogState] as 'No Round' is a valid round filter.
         * TODO Implement no round filter in DB
         */
        val roundFilter: Boolean = false,
        val personalBestsFilter: Boolean = false,
        // TODO Implement in DB
        val typeFilter: ViewScoresFiltersTypes = ViewScoresFiltersTypes.ALL,
) {
    val dateRangeIsValid
        get() = from == null || until == null || from.before(until)

    /**
     * Filters to pass to db.
     * Note: if [dateRangeIsValid] is false, [until] will be ignored
     */
    val filters: Filters<ShootFilter>

    init {
        var activeFilters = Filters<ShootFilter>()

        if (from != null || until != null) {
            activeFilters = activeFilters.plus(ShootFilter.DateRange(from, until.takeIf { dateRangeIsValid }))
        }
        if (roundFilter) {
            activeFilters = activeFilters.plus(
                    ShootFilter.Round(
                            selectRoundDialogState.selectedRoundId,
                            selectRoundDialogState.selectedSubTypeId,
                    )
            )
        }
        if (personalBestsFilter) {
            activeFilters = activeFilters.plus(ShootFilter.PersonalBests)
        }
        typeFilter.filter?.let {
            activeFilters = activeFilters.plus(it)
        }

        filters = activeFilters
    }

    val activeFilterCount
        get() = filters.size
}
