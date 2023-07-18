package eywa.projectcodex.components.viewScores

import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.components.viewScores.ui.convertScoreDialog.ConvertScoreIntent
import eywa.projectcodex.components.viewScores.ui.multiSelectBar.MultiSelectBarIntent
import eywa.projectcodex.components.viewScores.utils.ViewScoresDropdownMenuItem
import eywa.projectcodex.database.archerRound.ArcherRoundsFilter

sealed class ViewScoresIntent {
    data class EntryClicked(val archerRoundId: Int) : ViewScoresIntent()
    data class EntryLongClicked(val archerRoundId: Int) : ViewScoresIntent()

    data class HelpShowcaseAction(val action: HelpShowcaseIntent) : ViewScoresIntent()
    data class MultiSelectAction(val action: MultiSelectBarIntent) : ViewScoresIntent()
    data class ConvertScoreAction(val action: ConvertScoreIntent) : ViewScoresIntent()

    data class DropdownMenuClicked(val item: ViewScoresDropdownMenuItem, val archerRoundId: Int) : ViewScoresIntent()
    object DropdownMenuClosed : ViewScoresIntent()

    object NoRoundsDialogOkClicked : ViewScoresIntent()

    object DeleteDialogOkClicked : ViewScoresIntent()
    object DeleteDialogCancelClicked : ViewScoresIntent()

    data class AddFilter(val filter: ArcherRoundsFilter) : ViewScoresIntent()

    object HandledNoRoundsDialogOkClicked : EffectComplete()
    object HandledEmailClicked : EffectComplete()
    object HandledEmailNoSelection : EffectComplete()
    object HandledScorePadOpened : EffectComplete()
    object HandledInputEndOpened : EffectComplete()
    object HandledInputEndOnCompletedRound : EffectComplete()
    object HandledEmailOpened : EffectComplete()
    object HandledEditInfoOpened : EffectComplete()

    sealed class EffectComplete : ViewScoresIntent()
}
