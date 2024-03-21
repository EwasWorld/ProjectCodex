package eywa.projectcodex.components.viewScores.actionBar.filters

import eywa.projectcodex.common.helpShowcase.ActionBarHelp
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.sharedUi.UpdateCalendarInfo
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogIntent
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsState
import kotlin.reflect.KClass

sealed class ViewScoresFiltersIntent {
    object CloseFilters : ViewScoresFiltersIntent()
    object CloseFiltersHandled : ViewScoresFiltersIntent()
    object ClearAllFilters : ViewScoresFiltersIntent()
    object StartHelpShowcase : ViewScoresFiltersIntent()
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

    data class HelpShowcaseAction(
            val action: HelpShowcaseIntent,
            val screen: KClass<out ActionBarHelp>
    ) : ViewScoresFiltersIntent()
}
