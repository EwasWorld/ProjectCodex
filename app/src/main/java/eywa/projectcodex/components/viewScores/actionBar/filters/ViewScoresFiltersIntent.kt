package eywa.projectcodex.components.viewScores.actionBar.filters

import eywa.projectcodex.common.helpShowcase.ActionBarHelp
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.sharedUi.UpdateCalendarInfo
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogIntent
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsState
import kotlin.reflect.KClass

sealed class ViewScoresFiltersIntent {
    data object CloseFilters : ViewScoresFiltersIntent()
    data object CloseFiltersHandled : ViewScoresFiltersIntent()
    data object ClearAllFilters : ViewScoresFiltersIntent()
    data object StartHelpShowcase : ViewScoresFiltersIntent()
    data class SetUpdateRoundsState(val updateState: UpdateDefaultRoundsState) : ViewScoresFiltersIntent()

    data class UpdateFromFilter(val info: UpdateCalendarInfo) : ViewScoresFiltersIntent()
    data object ClearFromFilter : ViewScoresFiltersIntent()

    data class UpdateUntilFilter(val info: UpdateCalendarInfo) : ViewScoresFiltersIntent()
    data object ClearUntilFilter : ViewScoresFiltersIntent()

    data class UpdateScoreMaxFilter(val value: String?) : ViewScoresFiltersIntent()
    data object ClearScoreMaxFilter : ViewScoresFiltersIntent()

    data class UpdateScoreMinFilter(val value: String?) : ViewScoresFiltersIntent()
    data object ClearScoreMinFilter : ViewScoresFiltersIntent()

    data class UpdateRoundsFilter(val action: SelectRoundDialogIntent) : ViewScoresFiltersIntent()
    data object ClearRoundsFilter : ViewScoresFiltersIntent()
    data object ClearSubtypeFilter : ViewScoresFiltersIntent()

    data object ClickTypeFilter : ViewScoresFiltersIntent()
    data object ClickPbFilter : ViewScoresFiltersIntent()
    data object ClickFirstOfDayFilter : ViewScoresFiltersIntent()
    data object ClickCompleteFilter : ViewScoresFiltersIntent()

    data class HelpShowcaseAction(
            val action: HelpShowcaseIntent,
            val screen: KClass<out ActionBarHelp>
    ) : ViewScoresFiltersIntent()
}
