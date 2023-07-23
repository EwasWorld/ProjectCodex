package eywa.projectcodex.common.sharedUi.selectRoundFaceDialog

import eywa.projectcodex.database.RoundFace
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundDistance

sealed class SelectRoundFaceDialogIntent {
    object Open : SelectRoundFaceDialogIntent()
    object Close : SelectRoundFaceDialogIntent()
    object FaceTypeHelpClicked : SelectRoundFaceDialogIntent()

    object ToggleSingleMode : SelectRoundFaceDialogIntent()
    object CloseDropdown : SelectRoundFaceDialogIntent()
    data class OpenDropdown(val index: Int = 0) : SelectRoundFaceDialogIntent()
    data class DropdownItemClicked(val face: RoundFace) : SelectRoundFaceDialogIntent()
    data class SingleFaceClicked(val face: RoundFace) : SelectRoundFaceDialogIntent()

    data class SetRound(val round: Round, val distances: List<RoundDistance>) : SelectRoundFaceDialogIntent()
    data class SetDistances(val distances: List<RoundDistance>) : SelectRoundFaceDialogIntent()
    object SetNoRound : SelectRoundFaceDialogIntent()

    fun handle(state: SelectRoundFaceDialogState) =
            when (this) {
                Open -> state.copy(isShown = true)
                Close -> state.copy(isShown = false)
                CloseDropdown -> state.copy(dropdownExpandedFor = null)
                ToggleSingleMode ->
                    if (!state.isShown) state
                    else state.copy(isSingleMode = !state.isSingleMode)
                is DropdownItemClicked ->
                    if (
                        !state.isShown
                        || state.dropdownExpandedFor == null
                        || state.isSingleMode
                        || state.distances == null
                        || state.distances.size <= 1
                        || state.dropdownExpandedFor !in state.distances.indices
                    ) {
                        state.copy(dropdownExpandedFor = null)
                    }
                    else {
                        val newFaces = List(state.distances.size) { i ->
                            if (i == state.dropdownExpandedFor) face
                            else state.selectedFaces?.getOrNull(i) ?: state.selectedFaces?.firstOrNull()
                            ?: RoundFace.FULL
                        }
                        state.copy(dropdownExpandedFor = null, selectedFaces = newFaces)
                    }
                is OpenDropdown -> {
                    val size = state.distances.orEmpty().size
                    if (!state.isShown || state.isSingleMode || size <= 1 || index >= size) state
                    else state.copy(dropdownExpandedFor = index)
                }
                is SingleFaceClicked ->
                    if (!state.isShown || !state.isSingleMode) state
                    else state.copy(isShown = false, selectedFaces = listOf(face))
                FaceTypeHelpClicked -> TODO()
                is SetRound ->
                    if (distances.any { it.roundId != round.roundId }) state
                    else state.copy(
                            round = round,
                            distances = distances.map { it.distance },
                            selectedFaces = state.firstFaceAsSingleton,
                    )
                is SetDistances ->
                    if (state.round == null || distances.any { it.roundId != state.round.roundId }) state
                    else state.copy(distances = distances.map { it.distance })
                SetNoRound ->
                    state.copy(
                            round = null,
                            distances = null,
                            selectedFaces = state.firstFaceAsSingleton,
                    )
            }
}
