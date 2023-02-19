package eywa.projectcodex.components.viewScores

import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.components.viewScores.ui.multiSelectBar.MultiSelectBarIntent
import eywa.projectcodex.database.archerRound.ArcherRound
import eywa.projectcodex.database.arrowValue.ArrowValue

sealed class ViewScoresIntent {
    data class ToggleEntrySelected(val entryIndex: Int) : ViewScoresIntent()

    /**
     * Deletes the specified [ArcherRound] and all its [ArrowValue]s
     */
    data class DeleteRound(val archerRoundId: Int) : ViewScoresIntent()

    data class UpdateArrowValues(val arrows: List<ArrowValue>) : ViewScoresIntent()

    data class HelpShowcaseAction(val action: HelpShowcaseIntent) : ViewScoresIntent()

    data class MultiSelectAction(val action: MultiSelectBarIntent) : ViewScoresIntent()

    object HandledEmailClicked : EffectComplete()
    object HandledEmailNoSelection : EffectComplete()

    sealed class EffectComplete : ViewScoresIntent()
}
