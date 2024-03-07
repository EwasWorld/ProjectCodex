package eywa.projectcodex.components.viewScores.actionBar.filters

import eywa.projectcodex.common.sharedUi.UpdateCalendarInfo
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogIntent
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogIntent.RoundIntent
import eywa.projectcodex.common.utils.DateTimeFormat
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsState
import java.util.Calendar

sealed class ViewScoresFiltersIntent {
    object CloseFilters : ViewScoresFiltersIntent()
    object CloseFiltersHandled : ViewScoresFiltersIntent()
    object ClearAllFilters : ViewScoresFiltersIntent()
    data class SetUpdateRoundsState(val updateState: UpdateDefaultRoundsState) : ViewScoresFiltersIntent()

    data class UpdateFromFilter(val info: UpdateCalendarInfo) : ViewScoresFiltersIntent()
    object ClearFromFilter : ViewScoresFiltersIntent()

    data class UpdateUntilFilter(val info: UpdateCalendarInfo) : ViewScoresFiltersIntent()
    object ClearUntilFilter : ViewScoresFiltersIntent()

    data class UpdateScoreMaxFilter(val value: String?) : ViewScoresFiltersIntent()
    object ClearScoreMaxFilter : ViewScoresFiltersIntent()

    data class UpdateScoreMinFilter(val value: String?) : ViewScoresFiltersIntent()
    object ClearScoreMinFilter : ViewScoresFiltersIntent()

    data class UpdateRoundsFilter(val action: SelectRoundDialogIntent) : ViewScoresFiltersIntent()
    object ClearRoundsFilter : ViewScoresFiltersIntent()
    object ClearSubtypeFilter : ViewScoresFiltersIntent()

    object ClickTypeFilter : ViewScoresFiltersIntent()
    object ClickPbFilter : ViewScoresFiltersIntent()
    object ClickFirstOfDayFilter : ViewScoresFiltersIntent()
    object ClickCompleteFilter : ViewScoresFiltersIntent()

    fun handle(state: ViewScoresFiltersState): ViewScoresFiltersState = when (this) {
        CloseFilters -> state.copy(shouldCloseDialog = true)
        CloseFiltersHandled -> state.copy(shouldCloseDialog = false)
        ClearFromFilter -> state.copy(fromDate = null)
        ClearUntilFilter -> state.copy(untilDate = null)
        ClearRoundsFilter -> state.copy(roundFilter = false)
        ClearSubtypeFilter ->
            state.copy(selectRoundDialogState = state.selectRoundDialogState.copy(selectedSubTypeId = null))

        ClickPbFilter -> state.copy(personalBestsFilter = !state.personalBestsFilter)
        ClickCompleteFilter -> state.copy(completedRoundsFilter = !state.completedRoundsFilter)
        ClickFirstOfDayFilter -> state.copy(firstRoundOfDayFilter = !state.firstRoundOfDayFilter)
        ClickTypeFilter -> {
            val all = ViewScoresFiltersTypes.values()
            state.copy(typeFilter = all[(state.typeFilter.ordinal + 1) % all.size])
        }

        is UpdateFromFilter -> state.copy(fromDate = state.fromDate.update(info, "00:00"))
        is UpdateUntilFilter -> state.copy(untilDate = state.untilDate.update(info, "23:59"))
        is UpdateRoundsFilter -> {
            val newState = state.copy(selectRoundDialogState = action.handle(state.selectRoundDialogState).first)
            val roundSelectedAction = action is RoundIntent.RoundSelected || action is RoundIntent.NoRoundSelected
            newState.copy(roundFilter = roundSelectedAction || state.roundFilter)
        }

        ClearScoreMaxFilter -> state.copy(maxScore = state.maxScore.onTextChanged(null))
        ClearScoreMinFilter -> state.copy(minScore = state.minScore.onTextChanged(null))
        is UpdateScoreMaxFilter -> state.copy(maxScore = state.maxScore.onTextChanged(value))
        is UpdateScoreMinFilter -> state.copy(minScore = state.minScore.onTextChanged(value))
        ClearAllFilters -> ViewScoresFiltersState(
                selectRoundDialogState = state.selectRoundDialogState,
                roundFilter = false,
                updateDefaultRoundsState = state.updateDefaultRoundsState,
        )

        is SetUpdateRoundsState -> state.copy(updateDefaultRoundsState = updateState)
    }

    fun Calendar?.update(info: UpdateCalendarInfo, time: String): Calendar {
        var calendar = this
        if (calendar == null) {
            val todaysDate = DateTimeFormat.SHORT_DATE.format(Calendar.getInstance())
            calendar = DateTimeFormat.SHORT_DATE_TIME.parse("$todaysDate $time")
        }
        return info.updateCalendar(calendar)
    }
}
