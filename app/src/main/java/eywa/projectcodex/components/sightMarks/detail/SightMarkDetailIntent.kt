package eywa.projectcodex.components.sightMarks.detail

import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent

sealed class SightMarkDetailIntent {
    data class HelpShowcaseAction(val action: HelpShowcaseIntent) : SightMarkDetailIntent()

    data class SightMarkUpdated(val value: String) : SightMarkDetailIntent()
    data class DistanceUpdated(val value: String) : SightMarkDetailIntent()
    data class NoteUpdated(val value: String) : SightMarkDetailIntent()

    object ToggleIsMetric : SightMarkDetailIntent()
    object ToggleIsMarked : SightMarkDetailIntent()
    object ToggleIsArchived : SightMarkDetailIntent()
    object ToggleUpdateDateSet : SightMarkDetailIntent()

    object DeleteClicked : SightMarkDetailIntent()
    object ResetClicked : SightMarkDetailIntent()
    object SaveClicked : SightMarkDetailIntent()

    object CloseHandled : SightMarkDetailIntent()
}
