package eywa.projectcodex.common.sharedUi.selectRoundDialog

import eywa.projectcodex.common.sharedUi.selectRoundFaceDialog.SelectRoundFaceDialogIntent
import eywa.projectcodex.database.rounds.FullRoundInfo
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundSubType

sealed class SelectRoundDialogIntent {
    data class SetRounds(val rounds: List<FullRoundInfo>) : SelectRoundDialogIntent()

    sealed class RoundIntent : SelectRoundDialogIntent() {
        object OpenRoundDialog : RoundIntent()
        object CloseRoundDialog : RoundIntent()
        object NoRoundSelected : RoundIntent()
        data class RoundSelected(val round: Round) : RoundIntent()
        data class FilterClicked(val filter: SelectRoundFilter) : RoundIntent()
        object ClearFilters : RoundIntent()
    }

    sealed class SubTypeIntent : SelectRoundDialogIntent() {
        object OpenSubTypeDialog : SubTypeIntent()
        object CloseSubTypeDialog : SubTypeIntent()
        data class SubTypeSelected(val subType: RoundSubType) : SubTypeIntent()
    }


    fun handle(state: SelectRoundDialogState) =
            when (this) {
                is SetRounds -> SelectRoundDialogState(
                        allRounds = rounds,
                        isRoundDialogOpen = state.isRoundDialogOpen && rounds.isNotEmpty(),
                        filters = state.filters,
                ) to SelectRoundFaceDialogIntent.SetNoRound
                is RoundIntent -> handleRoundIntent(state)
                is SubTypeIntent -> handleSubTypeIntent(state)
            }

    private fun RoundIntent.handleRoundIntent(state: SelectRoundDialogState) =
            when (this) {
                RoundIntent.OpenRoundDialog ->
                    if (state.isSubtypeDialogOpen || state.allRounds.isNullOrEmpty()) state to null
                    else state.copy(isRoundDialogOpen = true, filters = SelectRoundEnabledFilters()) to null

                RoundIntent.CloseRoundDialog -> state.copy(isRoundDialogOpen = false) to null

                RoundIntent.NoRoundSelected ->
                    if (!state.isRoundDialogOpen) state to null
                    else state.copy(
                            isRoundDialogOpen = false,
                            selectedRoundId = null,
                            selectedSubTypeId = null,
                    ) to SelectRoundFaceDialogIntent.SetNoRound

                is RoundIntent.RoundSelected ->
                    if (!state.isRoundDialogOpen) {
                        state to null
                    }
                    else {
                        val new = state.copy(isRoundDialogOpen = false, selectedRoundId = round.roundId)
                        new.copy(selectedSubTypeId = new.furthestSubType?.subTypeId) to
                                SelectRoundFaceDialogIntent.SetRound(
                                        new.selectedRound!!.round,
                                        new.roundSubTypeDistances!!,
                                )
                    }

                is RoundIntent.FilterClicked ->
                    if (!state.isRoundDialogOpen) state to null
                    else state.copy(filters = state.filters.toggle(filter)) to null

                RoundIntent.ClearFilters -> state.copy(filters = SelectRoundEnabledFilters()) to null
            }

    private fun SubTypeIntent.handleSubTypeIntent(state: SelectRoundDialogState) =
            when (this) {
                SubTypeIntent.OpenSubTypeDialog -> {
                    val subTypes = state.selectedRound?.roundSubTypes.orEmpty()
                    if (state.isRoundDialogOpen || subTypes.size <= 1) state to null
                    else state.copy(isSubtypeDialogOpen = true) to null
                }

                SubTypeIntent.CloseSubTypeDialog -> state.copy(isSubtypeDialogOpen = false) to null

                is SubTypeIntent.SubTypeSelected -> {
                    if (!state.isSubtypeDialogOpen) {
                        state to null
                    }
                    else {
                        val new = state.copy(isSubtypeDialogOpen = false, selectedSubTypeId = subType.subTypeId)
                        new to SelectRoundFaceDialogIntent.SetDistances(new.roundSubTypeDistances!!)
                    }
                }
            }
}
