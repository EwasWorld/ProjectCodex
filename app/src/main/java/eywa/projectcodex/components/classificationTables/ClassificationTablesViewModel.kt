package eywa.projectcodex.components.classificationTables

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogIntent
import eywa.projectcodex.common.utils.classificationTables.ClassificationTablesUseCase
import eywa.projectcodex.components.classificationTables.ClassificationTablesIntent.AgeClicked
import eywa.projectcodex.components.classificationTables.ClassificationTablesIntent.AgeSelected
import eywa.projectcodex.components.classificationTables.ClassificationTablesIntent.BowClicked
import eywa.projectcodex.components.classificationTables.ClassificationTablesIntent.BowSelected
import eywa.projectcodex.components.classificationTables.ClassificationTablesIntent.CloseDropdown
import eywa.projectcodex.components.classificationTables.ClassificationTablesIntent.HelpShowcaseAction
import eywa.projectcodex.components.classificationTables.ClassificationTablesIntent.SelectRoundDialogAction
import eywa.projectcodex.components.classificationTables.ClassificationTablesIntent.ToggleIsGent
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.datastore.CodexDatastore
import eywa.projectcodex.datastore.DatastoreKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClassificationTablesViewModel @Inject constructor(
        val db: ScoresRoomDatabase,
        private val helpShowcase: HelpShowcaseUseCase,
        private val tables: ClassificationTablesUseCase,
        private val datastore: CodexDatastore,
) : ViewModel() {
    private val _state = MutableStateFlow(ClassificationTablesState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            state.map { it.selectRoundDialogState.filters }.distinctUntilChanged().collectLatest { filters ->
                db.roundsRepo().fullRoundsInfo(filters).collectLatest { rounds ->
                    handle(SelectRoundDialogAction(SelectRoundDialogIntent.SetRounds(rounds)))
                }
            }
        }
        viewModelScope.launch {
            datastore.get(DatastoreKey.Use2023HandicapSystem).collect { use2023 ->
                _state.update { it.copy(use2023Handicaps = use2023) }
            }
        }
    }

    fun handle(action: ClassificationTablesIntent) {
        when (action) {
            ToggleIsGent -> _state.update { it.copy(isGent = !it.isGent).addScores() }
            is AgeSelected -> _state.update { it.copy(age = action.age, expanded = null).addScores() }
            is BowSelected -> _state.update { it.copy(bow = action.bow, expanded = null).addScores() }
            AgeClicked -> _state.update { it.copy(expanded = ClassificationTablesState.Dropdown.AGE) }
            BowClicked -> _state.update { it.copy(expanded = ClassificationTablesState.Dropdown.BOW) }
            CloseDropdown -> _state.update { it.copy(expanded = null) }
            is SelectRoundDialogAction -> {
                _state.update {
                    val (selectRoundDialogState, _) = action.action.handle(it.selectRoundDialogState)
                    it.copy(selectRoundDialogState = selectRoundDialogState).addScores()
                }
            }

            is HelpShowcaseAction -> helpShowcase.handle(action.action, CodexNavRoute.CLASSIFICATION_TABLES::class)
        }
    }

    private fun ClassificationTablesState.addScores(): ClassificationTablesState {
        val round = selectRoundDialogState.selectedRound
                ?: return clearScores()
        val scores = tables.get(isGent, age, bow, round, selectRoundDialogState.selectedSubTypeId, use2023Handicaps)
                ?: return clearScores()
        return copy(scores = scores)
    }

    private fun ClassificationTablesState.clearScores() = copy(scores = emptyList())
}
