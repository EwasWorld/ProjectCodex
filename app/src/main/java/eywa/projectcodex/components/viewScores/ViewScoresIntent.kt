package eywa.projectcodex.components.viewScores

import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.components.viewScores.actionBar.filters.ViewScoresFiltersIntent
import eywa.projectcodex.components.viewScores.actionBar.multiSelectBar.MultiSelectBarIntent
import eywa.projectcodex.components.viewScores.dialogs.convertScoreDialog.ConvertScoreIntent
import eywa.projectcodex.components.viewScores.screenUi.ViewScoresDropdownMenuItem

sealed class ViewScoresIntent {
    data class EntryClicked(val shootId: Int) : ViewScoresIntent()
    data class EntryLongClicked(val shootId: Int) : ViewScoresIntent()

    data class HelpShowcaseAction(val action: HelpShowcaseIntent) : ViewScoresIntent()
    data class MultiSelectAction(val action: MultiSelectBarIntent) : ViewScoresIntent()
    data class ConvertScoreAction(val action: ConvertScoreIntent) : ViewScoresIntent()

    data class FiltersAction(val action: ViewScoresFiltersIntent) : ViewScoresIntent()
    data object OpenFilters : ViewScoresIntent()
    data object HandledOpenFilters : ViewScoresIntent()

    data class DropdownMenuClicked(val item: ViewScoresDropdownMenuItem, val shootId: Int) : ViewScoresIntent()
    data object DropdownMenuClosed : ViewScoresIntent()

    data object NoRoundsDialogOkClicked : ViewScoresIntent()

    data object DeleteDialogOkClicked : ViewScoresIntent()
    data object DeleteDialogCancelClicked : ViewScoresIntent()

    data object HandledNoRoundsDialogOkClicked : EffectComplete()
    data object HandledEmailNoSelection : EffectComplete()
    data object HandledScorePadOpened : EffectComplete()
    data object HandledH2hScorePadOpened : EffectComplete()
    data object HandledH2hAddOpened : EffectComplete()
    data object HandledAddCountOpened : EffectComplete()
    data object HandledAddEndOpened : EffectComplete()
    data object HandledAddEndOnCompletedRound : EffectComplete()
    data object HandledEmailOpened : EffectComplete()
    data object HandledEditInfoOpened : EffectComplete()

    sealed class EffectComplete : ViewScoresIntent()
}
