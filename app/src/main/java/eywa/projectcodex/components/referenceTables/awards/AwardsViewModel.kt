package eywa.projectcodex.components.referenceTables.awards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsTask
import eywa.projectcodex.database.ScoresRoomDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AwardsViewModel @Inject constructor(
        val db: ScoresRoomDatabase,
        private val helpShowcase: HelpShowcaseUseCase,
        private val updateDefaultRoundsTask: UpdateDefaultRoundsTask,
) : ViewModel() {
    private val _state = MutableStateFlow(AwardsState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            updateDefaultRoundsTask.state.collect { updateState ->
                _state.update {
                    it.copy(updateDefaultRoundsState = updateState)
                }
            }
        }

        viewModelScope.launch {
            db.bowRepo().defaultBow.collect { bow ->
                _state.update { it.copy(bow = bow?.type ?: it.bow) }
            }
        }

        viewModelScope.launch {
            db.roundsRepo().allRounds.collect { rounds ->
                _state.update { it.copy(allRounds = rounds) }
            }
        }
    }

    fun handleEvent(action: AwardsIntent) {
        when (action) {
            AwardsIntent.BowClicked -> _state.update { it.copy(bowDropdownExpanded = true) }
            is AwardsIntent.BowSelected -> _state.update { it.copy(bow = action.bow, bowDropdownExpanded = false) }
            AwardsIntent.CloseDropdown -> _state.update { it.copy(bowDropdownExpanded = false) }
            is AwardsIntent.HelpShowcaseAction -> helpShowcase.handle(action.action, CodexNavRoute.AWARDS::class)
        }
    }
}
