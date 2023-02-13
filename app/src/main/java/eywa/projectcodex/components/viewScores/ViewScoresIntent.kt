package eywa.projectcodex.components.viewScores

import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.database.archerRound.ArcherRound
import eywa.projectcodex.database.arrowValue.ArrowValue

sealed class ViewScoresIntent {
    object ToggleMultiSelectMode : ViewScoresIntent()

    data class ToggleEntrySelected(val entryIndex: Int) : ViewScoresIntent()

    /**
     * @param forceIsSelectedTo If non-null, forces all item's isSelected to be this value.
     *      Otherwise, if all items are selected, deselect all items.
     *      Otherwise, select all items
     */
    data class SelectAllOrNone(val forceIsSelectedTo: Boolean? = null) : ViewScoresIntent()

    /**
     * Deletes the specified [ArcherRound] and all its [ArrowValue]s
     */
    data class DeleteRound(val archerRoundId: Int) : ViewScoresIntent()

    data class UpdateArrowValues(val arrows: List<ArrowValue>) : ViewScoresIntent()

    data class HelpShowcaseAction(val action: HelpShowcaseIntent) : ViewScoresIntent()
}
