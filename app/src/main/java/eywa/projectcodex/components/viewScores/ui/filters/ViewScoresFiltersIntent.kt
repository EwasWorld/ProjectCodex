package eywa.projectcodex.components.viewScores.ui.filters

import eywa.projectcodex.common.sharedUi.UpdateCalendarInfo
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogIntent
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogIntent.RoundIntent
import eywa.projectcodex.common.utils.DateTimeFormat
import java.util.Calendar

sealed class ViewScoresFiltersIntent {
    object OpenFilters : ViewScoresFiltersIntent()
    object CloseFilters : ViewScoresFiltersIntent()

    data class UpdateFromFilter(val info: UpdateCalendarInfo) : ViewScoresFiltersIntent()
    object ClearFromFilter : ViewScoresFiltersIntent()

    data class UpdateUntilFilter(val info: UpdateCalendarInfo) : ViewScoresFiltersIntent()
    object ClearUntilFilter : ViewScoresFiltersIntent()

    data class UpdateRoundsFilter(val action: SelectRoundDialogIntent) : ViewScoresFiltersIntent()
    object ClearRoundsFilter : ViewScoresFiltersIntent()

    object ClickTypeFilter : ViewScoresFiltersIntent()
    object ClickPbFilter : ViewScoresFiltersIntent()

    fun handle(state: ViewScoresFiltersState): ViewScoresFiltersState = when (this) {
        OpenFilters -> state.copy(isExpanded = true)
        CloseFilters -> state.copy(isExpanded = false)
        ClearFromFilter -> state.copy(from = null)
        ClearUntilFilter -> state.copy(until = null)
        ClearRoundsFilter -> state.copy(roundFilter = false)
        ClickPbFilter -> state.copy(personalBestsFilter = !state.personalBestsFilter)
        ClickTypeFilter -> {
            val all = ViewScoresFiltersTypes.values()
            state.copy(typeFilter = all[(state.typeFilter.ordinal + 1) % all.size])
        }

        is UpdateFromFilter -> state.copy(from = state.from.update(info, "00:00"))
        is UpdateUntilFilter -> state.copy(until = state.until.update(info, "23:59"))
        is UpdateRoundsFilter -> {
            val newState = state.copy(selectRoundDialogState = action.handle(state.selectRoundDialogState).first)
            val roundSelectedAction = action is RoundIntent.RoundSelected || action is RoundIntent.NoRoundSelected
            newState.copy(roundFilter = roundSelectedAction || state.roundFilter)
        }
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
