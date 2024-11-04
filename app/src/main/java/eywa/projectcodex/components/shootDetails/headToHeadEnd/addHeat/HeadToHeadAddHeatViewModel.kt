package eywa.projectcodex.components.shootDetails.headToHeadEnd.addHeat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.common.navigation.NavArgument
import eywa.projectcodex.common.navigation.get
import eywa.projectcodex.components.shootDetails.headToHeadEnd.addHeat.HeadToHeadAddHeatIntent.*
import eywa.projectcodex.database.ScoresRoomDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HeadToHeadAddHeatViewModel @Inject constructor(
        db: ScoresRoomDatabase,
        savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _state = MutableStateFlow(HeadToHeadAddHeatState())
    val state = _state.asStateFlow()

    private val h2hRepo = db.h2hRepo()
    private val shootId = savedStateHandle.get<Int>(NavArgument.SHOOT_ID)!!

    fun handle(action: HeadToHeadAddHeatIntent) {
        when (action) {
            is HelpShowcaseAction -> Unit

            is OpponentUpdated -> _state.update { it.copy(opponent = action.opponent) }

            is OpponentQualiRankUpdated ->
                _state.update { it.copy(opponentQualiRank = it.opponentQualiRank.onTextChanged(action.rank)) }

            is SelectHeatDialogItemClicked ->
                _state.update {
                    it.copy(
                            heat = action.heat,
                            showSelectHeatDialog = false,
                            showHeatRequiredError = false,
                    )
                }

            ToggleIsBye -> _state.update { it.copy(isBye = !it.isBye) }

            HeatClicked -> _state.update { it.copy(showSelectHeatDialog = true) }
            CloseSelectHeatDialog -> _state.update { it.copy(showSelectHeatDialog = false) }

            ShouldCloseScreenHandled -> _state.update { it.copy(shouldCloseScreen = false) }

            SubmitClicked -> submit(state.value)
        }
    }

    private fun submit(state: HeadToHeadAddHeatState) {
        if (state.heat == null) {
            _state.update { it.copy(showHeatRequiredError = true) }
            return
        }

        viewModelScope.launch {
            h2hRepo.insert(state.asHeatToHeatHeat(shootId)!!)
        }
        _state.update { it.copy(shouldCloseScreen = true) }
    }
}
