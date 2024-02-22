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
    data class FiltersAction(val action: ViewScoresFiltersIntent) : ViewScoresIntent()
    data class ConvertScoreAction(val action: ConvertScoreIntent) : ViewScoresIntent()

    data class DropdownMenuClicked(val item: ViewScoresDropdownMenuItem, val shootId: Int) : ViewScoresIntent()
    object DropdownMenuClosed : ViewScoresIntent()

    object NoRoundsDialogOkClicked : ViewScoresIntent()

    object DeleteDialogOkClicked : ViewScoresIntent()
    object DeleteDialogCancelClicked : ViewScoresIntent()

    object HandledNoRoundsDialogOkClicked : EffectComplete()
    object HandledEmailNoSelection : EffectComplete()
    object HandledScorePadOpened : EffectComplete()
    object HandledAddCountOpened : EffectComplete()
    object HandledAddEndOpened : EffectComplete()
    object HandledAddEndOnCompletedRound : EffectComplete()
    object HandledEmailOpened : EffectComplete()
    object HandledEditInfoOpened : EffectComplete()

    sealed class EffectComplete : ViewScoresIntent()
}
