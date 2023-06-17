package eywa.projectcodex.components.archerRoundScore

import eywa.projectcodex.common.archeryObjects.Arrow
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.components.archerRoundScore.state.ArcherRoundScreen

sealed class ArcherRoundIntent {
    sealed class ArrowInputsIntent : ArcherRoundIntent() {
        data class ArrowInputted(val arrow: Arrow) : ArrowInputsIntent()
        object ResetArrowsInputted : ArrowInputsIntent()
        object ClearArrowsInputted : ArrowInputsIntent()
        object BackspaceArrowsInputted : ArrowInputsIntent()

        object SubmitClicked : ArrowInputsIntent()
        object CancelClicked : ArrowInputsIntent()

        data class HelpShowcaseAction(val action: HelpShowcaseIntent) : ArrowInputsIntent()
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

        object DeleteEndDialogOkClicked : ScorePadIntent()
        object DeleteEndDialogCancelClicked : ScorePadIntent()

        object NoArrowsDialogOkClicked : ScorePadIntent()

        data class HelpShowcaseAction(val action: HelpShowcaseIntent) : ScorePadIntent()
    }

    sealed class SettingsIntent : ArcherRoundIntent() {
        data class InputEndSizeChanged(val endSize: Int?) : SettingsIntent()
        data class ScorePadEndSizeChanged(val endSize: Int?) : SettingsIntent()

        data class HelpShowcaseAction(val action: HelpShowcaseIntent) : SettingsIntent()
    }

    data class NavBarClicked(val screen: ArcherRoundScreen) : ArcherRoundIntent()

    sealed class InvalidArcherRoundIntent : ArcherRoundIntent() {
        object ReturnToMenuClicked : InvalidArcherRoundIntent()
        object ReturnToMenuHandled : InvalidArcherRoundIntent()
    }

    object CannotInputEndDialogOkClicked : ArcherRoundIntent()
    object RoundCompleteDialogOkClicked : ArcherRoundIntent()
    data class ErrorHandled(val error: ArcherRoundError) : ArcherRoundIntent()

    data class HelpShowcaseAction(val action: HelpShowcaseIntent) : ArcherRoundIntent()
}
