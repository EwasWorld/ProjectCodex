package eywa.projectcodex.components.archerRoundScore.scorePad

import eywa.projectcodex.components.archerRoundScore.scorePad.infoTable.ScorePadDataNew

// TODO Half way total
interface ScorePadState {
    val isRoundFull: Boolean
    val displayDeleteEndConfirmationDialog: Boolean
    val dropdownMenuOpenForEndNumber: Int?
    val scorePadData: ScorePadDataNew
}
