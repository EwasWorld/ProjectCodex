package eywa.projectcodex.components.archerRoundScore

import eywa.projectcodex.common.archeryObjects.Arrow

sealed class ArcherRoundIntent {
    sealed class ArrowInputsIntent : ArcherRoundIntent() {
        data class ArrowInputted(val arrow: Arrow) : ArrowInputsIntent()
        object ResetArrowsInputted : ArrowInputsIntent()
        object ClearArrowsInputted : ArrowInputsIntent()
        object BackspaceArrowsInputted : ArrowInputsIntent()
    }

    sealed class ScorePadIntent : ArcherRoundIntent() {
        /**
         * @param endNumber 1-indexed
         */
        data class RowClicked(val endNumber: Int) : ScorePadIntent()
        object CloseDropdownMenu : ScorePadIntent()
        object EditEndClicked : ScorePadIntent()
        object InsertEndClicked : ScorePadIntent()
        object DeleteEndClicked : ScorePadIntent()
    }

    sealed class SettingsIntent : ArcherRoundIntent() {
        data class InputEndSizeChanged(val endSize: Int) : SettingsIntent()
        data class ScorePadEndSizeChanged(val endSize: Int) : SettingsIntent()
    }

    data class NavBarClicked(val item: ArcherRoundScreen) : ArcherRoundIntent()
    object ScreenSubmitClicked : ArcherRoundIntent()
    object ScreenCancelClicked : ArcherRoundIntent()
}