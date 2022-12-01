package eywa.projectcodex.components.archerRoundScore

import eywa.projectcodex.common.archeryObjects.Arrow

sealed class ArcherRoundIntent {
    data class Initialise(val screen: ArcherRoundScreen, val archerRoundId: Int) : ArcherRoundIntent()

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
        data class RowLongClicked(val endNumber: Int) : ScorePadIntent()

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
        data class InputEndSizeChanged(val endSize: Int?) : SettingsIntent()
        data class ScorePadEndSizeChanged(val endSize: Int?) : SettingsIntent()
    }

    data class NavBarClicked(val screen: ArcherRoundScreen) : ArcherRoundIntent()
    object ScreenSubmitClicked : ArcherRoundIntent()
    object ScreenCancelClicked : ArcherRoundIntent()

    object RoundCompleteDialogOkClicked : ArcherRoundIntent()
    object NoArrowsDialogOkClicked : ArcherRoundIntent()
    object DeleteEndDialogOkClicked : ArcherRoundIntent()
    object DeleteEndDialogCancelClicked : ArcherRoundIntent()
}