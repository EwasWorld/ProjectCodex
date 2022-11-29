package eywa.projectcodex.components.archerRoundScore

import eywa.projectcodex.common.archeryObjects.Arrow

sealed class ArcherRoundIntent {
    sealed class ArrowInputsIntent : ArcherRoundIntent() {
        data class ArrowInputted(val arrow: Arrow) : ArrowInputsIntent()
        object ResetArrowsInputted : ArrowInputsIntent()
        object ClearArrowsInputted : ArrowInputsIntent()
        object BackspaceArrowsInputted : ArrowInputsIntent()
        object ChangeEndSizeClicked : ArrowInputsIntent()
    }

    object ScreenSubmitClicked : ArcherRoundIntent()
    object ScreenCancelClicked : ArcherRoundIntent()
}