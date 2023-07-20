package eywa.projectcodex.common.sharedUi.selectRoundFaceDialog

import eywa.projectcodex.database.RoundFace

sealed class SelectRoundFaceDialogIntent {
    object Open : SelectRoundFaceDialogIntent()
    object Close : SelectRoundFaceDialogIntent()
    object FaceTypeHelpClicked : SelectRoundFaceDialogIntent()

    object ToggleAllDifferentAllSame : SelectRoundFaceDialogIntent()
    object CloseDropdown : SelectRoundFaceDialogIntent()
    data class OpenDropdown(val index: Int = 0) : SelectRoundFaceDialogIntent()
    data class DropdownItemClicked(val face: RoundFace, val index: Int = 0) : SelectRoundFaceDialogIntent()
    data class SingleFaceClicked(val face: RoundFace) : SelectRoundFaceDialogIntent()
}
