package eywa.projectcodex.components.viewScores.actionBar.filters

import eywa.projectcodex.common.sharedUi.numberField.NumberFieldState
import eywa.projectcodex.common.sharedUi.numberField.NumberValidator
import eywa.projectcodex.common.sharedUi.numberField.TypeValidator
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogState
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsState
import eywa.projectcodex.database.Filters
import eywa.projectcodex.database.shootData.ShootFilter
import eywa.projectcodex.database.shootData.ShootFilter.*
import java.util.Calendar

data class ViewScoresFiltersState(
        val shouldCloseDialog: Boolean = false,
        val fromDate: Calendar? = null,
        val untilDate: Calendar? = null,
        val minScore: NumberFieldState<Int> = NumberFieldState(
                TypeValidator.IntValidator,
                NumberValidator.InRange(0..10_000),
                NumberValidator.NotRequired,
        ),
        val maxScore: NumberFieldState<Int> = NumberFieldState(
                TypeValidator.IntValidator,
                NumberValidator.InRange(0..10_000),
                NumberValidator.NotRequired,
        ),
        val selectRoundDialogState: SelectRoundDialogState = SelectRoundDialogState(defaultNullSubtype = true),
        val updateDefaultRoundsState: UpdateDefaultRoundsState = UpdateDefaultRoundsState.NotStarted,
        /**
         * True if round filter indicated by [selectRoundDialogState] should be applied.
         * Cannot use null in [selectRoundDialogState] as 'No Round' is a valid round filter.
         */
        val roundFilter: Boolean = false,
        val personalBestsFilter: Boolean = false,
        val firstRoundOfDayFilter: Boolean = false,
        val completedRoundsFilter: Boolean = false,
        val typeFilter: ViewScoresFiltersTypes = ViewScoresFiltersTypes.ALL,
) {
    val dateRangeIsValid
        get() = fromDate == null || untilDate == null || fromDate.before(untilDate)
    val scoreRangeIsValid
        get() = minScore.parsed == null || maxScore.parsed == null || minScore.parsed < maxScore.parsed

    /**
     * Filters to pass to db.
     * Note: if [dateRangeIsValid] is false, [untilDate] will be ignored
     */
    val filters: Filters<ShootFilter>

    init {
        var activeFilters = Filters<ShootFilter>()

        if (fromDate != null || untilDate != null) {
            activeFilters = activeFilters.plus(DateRange(fromDate, untilDate.takeIf { dateRangeIsValid }))
        }
        if (roundFilter) {
            activeFilters = activeFilters.plus(
                    Round(
                            selectRoundDialogState.selectedRoundId,
                            selectRoundDialogState.selectedSubTypeId,
                    ),
            )
        }
        if (personalBestsFilter) {
            activeFilters = activeFilters.plus(PersonalBests)
        }
        if (completedRoundsFilter) {
            activeFilters = activeFilters.plus(CompleteRounds)
        }
        if (firstRoundOfDayFilter) {
            activeFilters = activeFilters.plus(FirstRoundOfDay)
        }
        if (minScore.parsed != null || maxScore.parsed != null) {
            activeFilters = activeFilters
                    .plus(ScoreRange(minScore.parsed, maxScore.parsed.takeIf { scoreRangeIsValid }))
        }
        if (typeFilter != ViewScoresFiltersTypes.ALL) {
            activeFilters = activeFilters.plus(ShootFilter.Type(typeFilter))
        }

        filters = activeFilters
    }

    val activeFilterCount
        get() = filters.size
}
