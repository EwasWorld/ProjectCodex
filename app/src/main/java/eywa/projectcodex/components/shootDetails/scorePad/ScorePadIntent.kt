package eywa.projectcodex.components.shootDetails.scorePad

import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.components.shootDetails.ShootDetailsIntent

sealed class ScorePadIntent {
    /**
     * @param endNumber 1-indexed
     */
    data class RowClicked(val endNumber: Int) : ScorePadIntent()
    object CloseDropdownMenu : ScorePadIntent()
    object EditEndClicked : ScorePadIntent()
    object EditEndHandled : ScorePadIntent()
    object InsertEndClicked : ScorePadIntent()
    object InsertEndHandled : ScorePadIntent()
    object DeleteEndClicked : ScorePadIntent()

    object DeleteEndDialogOkClicked : ScorePadIntent()
    object DeleteEndDialogCancelClicked : ScorePadIntent()

    object NoArrowsDialogOkClicked : ScorePadIntent()

    data class HelpShowcaseAction(val action: HelpShowcaseIntent) : ScorePadIntent()

    data class ShootDetailsAction(val action: ShootDetailsIntent) : ScorePadIntent()
}
