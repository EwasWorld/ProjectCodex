package eywa.projectcodex.components.viewScores

import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.components.viewScores.ui.convertScoreDialog.ConvertScoreIntent
import eywa.projectcodex.components.viewScores.ui.multiSelectBar.MultiSelectBarIntent
import eywa.projectcodex.components.viewScores.utils.ViewScoresDropdownMenuItem
import eywa.projectcodex.database.shootData.ShootFilter

sealed class ViewScoresIntent {
    data class EntryClicked(val shootId: Int) : ViewScoresIntent()
    data class EntryLongClicked(val shootId: Int) : ViewScoresIntent()

    data class HelpShowcaseAction(val action: HelpShowcaseIntent) : ViewScoresIntent()
    data class MultiSelectAction(val action: MultiSelectBarIntent) : ViewScoresIntent()
    data class ConvertScoreAction(val action: ConvertScoreIntent) : ViewScoresIntent()

    data class DropdownMenuClicked(val item: ViewScoresDropdownMenuItem, val shootId: Int) : ViewScoresIntent()
    object DropdownMenuClosed : ViewScoresIntent()

    object NoRoundsDialogOkClicked : ViewScoresIntent()

    object DeleteDialogOkClicked : ViewScoresIntent()
    object DeleteDialogCancelClicked : ViewScoresIntent()

    data class AddFilter(val filter: ShootFilter) : ViewScoresIntent()

    object HandledNoRoundsDialogOkClicked : EffectComplete()
    object HandledEmailNoSelection : EffectComplete()
    object HandledScorePadOpened : EffectComplete()
    object HandledInputEndOpened : EffectComplete()
    object HandledInputEndOnCompletedRound : EffectComplete()
    object HandledEmailOpened : EffectComplete()
    object HandledEditInfoOpened : EffectComplete()

    sealed class EffectComplete : ViewScoresIntent()
}
