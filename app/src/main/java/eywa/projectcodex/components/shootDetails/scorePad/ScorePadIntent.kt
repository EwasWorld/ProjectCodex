package eywa.projectcodex.components.shootDetails.scorePad

import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.components.shootDetails.ShootDetailsIntent

sealed class ScorePadIntent {
    /**
     * @param endNumber 1-indexed
     */
    data class RowClicked(val endNumber: Int) : ScorePadIntent()
    object CloseDropdownMenu : ScorePadIntent()
    data class EditEndClicked(val endNumber: Int) : ScorePadIntent()
    object EditEndHandled : ScorePadIntent()
    data class InsertEndClicked(val endNumber: Int) : ScorePadIntent()
    object InsertEndHandled : ScorePadIntent()
    data class DeleteEndClicked(val endNumber: Int) : ScorePadIntent()

    object DeleteEndDialogOkClicked : ScorePadIntent()
    object DeleteEndDialogCancelClicked : ScorePadIntent()

    object NoArrowsDialogOkClicked : ScorePadIntent()

    data class HelpShowcaseAction(val action: HelpShowcaseIntent) : ScorePadIntent()

    data class ShootDetailsAction(val action: ShootDetailsIntent) : ScorePadIntent()
}
