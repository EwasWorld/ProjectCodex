package eywa.projectcodex.components.classificationTables

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogIntent
import eywa.projectcodex.common.utils.classificationTables.ClassificationTables
import eywa.projectcodex.components.classificationTables.ClassificationTablesIntent.*
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.datastore.CodexDatastore
import eywa.projectcodex.datastore.DatastoreKey
import eywa.projectcodex.model.Handicap
import eywa.projectcodex.model.roundHandicap
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClassificationTablesViewModel @Inject constructor(
        val db: ScoresRoomDatabase,
        private val helpShowcase: HelpShowcaseUseCase,
        private val tables: ClassificationTables,
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
        val searchRound =
                if (round == null) return clearScores()
                else round.round.defaultRoundId ?: return clearScores()
        return copy(
                scores = tables
                        .get(isGent, age, bow, searchRound, selectRoundDialogState.selectedSubTypeId)
                        .map {
                            it.copy(
                                    handicap = Handicap.getHandicapForRound(
                                            round = round,
                                            subType = selectRoundDialogState.selectedSubTypeId,
                                            score = it.score,
                                            use2023Handicaps = use2023Handicaps,
                                    )?.roundHandicap()
                            )
                        }
        )
    }

    private fun ClassificationTablesState.clearScores() = copy(scores = emptyList())
}
