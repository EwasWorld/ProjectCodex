package eywa.projectcodex.common.sharedUi.selectRoundFaceDialog

import eywa.projectcodex.database.RoundFace
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundDistance

sealed class SelectRoundFaceDialogIntent {
    object Open : SelectRoundFaceDialogIntent()
    object Close : SelectRoundFaceDialogIntent()
    object FaceTypeHelpClicked : SelectRoundFaceDialogIntent()

    object ToggleAllDifferentAllSame : SelectRoundFaceDialogIntent()
    object CloseDropdown : SelectRoundFaceDialogIntent()
    data class OpenDropdown(val index: Int = 0) : SelectRoundFaceDialogIntent()
    data class DropdownItemClicked(val face: RoundFace, val index: Int = 0) : SelectRoundFaceDialogIntent()
    data class SingleFaceClicked(val face: RoundFace) : SelectRoundFaceDialogIntent()

    data class SetRound(val round: Round, val distances: List<RoundDistance>) : SelectRoundFaceDialogIntent()
    data class SetDistances(val distances: List<RoundDistance>) : SelectRoundFaceDialogIntent()
    object SetNoRound : SelectRoundFaceDialogIntent()

    fun handle(state: SelectRoundFaceDialogState) =
            when (this) {
                Open -> state.copy(isShown = true)
                Close -> state.copy(isShown = false)
                CloseDropdown -> state.copy(dropdownExpandedFor = null)
                ToggleAllDifferentAllSame -> state.copy(isSingleMode = !state.isSingleMode)
                is DropdownItemClicked ->
                    if (state.distances.isNullOrEmpty()) {
                        state.copy(dropdownExpandedFor = null)
                    }
                    else {
                        val newFaces = List(state.distances.size) { i ->
                            if (i == index) face
                            else state.selectedFaces?.getOrNull(i) ?: state.selectedFaces?.firstOrNull()
                            ?: RoundFace.FULL
                        }
                        state.copy(dropdownExpandedFor = null, selectedFaces = newFaces)
                    }
                is OpenDropdown -> state.copy(dropdownExpandedFor = index)
                is SingleFaceClicked -> state.copy(isShown = false, selectedFaces = listOf(face))
                FaceTypeHelpClicked -> TODO()
                is SetRound ->
                    state.copy(
                            round = round,
                            distances = distances.map { it.distance },
                            selectedFaces = state.finalFaces?.firstOrNull()?.let { face -> listOf(face) },
                    )
                is SetDistances -> state.copy(distances = distances.map { it.distance })
                SetNoRound ->
                    state.copy(
                            round = null,
                            distances = null,
                            selectedFaces = state.selectedFaces?.firstOrNull()?.let { listOf(it) },
                    )
            }
}
