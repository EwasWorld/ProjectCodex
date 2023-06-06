package eywa.projectcodex.components.classificationTables

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogIntent
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundEnabledFilters
import eywa.projectcodex.common.utils.classificationTables.ClassificationTables
import eywa.projectcodex.components.classificationTables.ClassificationTablesIntent.*
import eywa.projectcodex.database.ScoresRoomDatabase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClassificationTablesViewModel @Inject constructor(
        val db: ScoresRoomDatabase,
        private val helpShowcase: HelpShowcaseUseCase,
        private val tables: ClassificationTables,
) : ViewModel() {
    private val _state = MutableStateFlow(ClassificationTablesState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            state.map { it.roundFilters }.distinctUntilChanged().collectLatest { filters ->
                db.roundsRepo().fullRoundsInfo(filters).collectLatest { rounds ->
                    _state.update { it.copy(allRounds = rounds) }
                }
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
            is SelectRoundDialogAction -> handleSelectRoundDialogIntent(action.action)
            is HelpShowcaseAction -> helpShowcase.handle(action.action, ClassificationFragment::class)
        }
    }

    private fun handleSelectRoundDialogIntent(action: SelectRoundDialogIntent) {
        when (action) {
            SelectRoundDialogIntent.OpenRoundSelectDialog -> _state.update { it.copy(isSelectRoundDialogOpen = true) }
            SelectRoundDialogIntent.CloseRoundSelectDialog -> _state.update { it.copy(isSelectRoundDialogOpen = false) }
            SelectRoundDialogIntent.NoRoundSelected ->
                _state.update { it.copy(isSelectRoundDialogOpen = false, round = null).addScores() }
            is SelectRoundDialogIntent.RoundSelected ->
                _state.update {
                    val round = it.allRounds
                            ?.find { round -> round.round.roundId == action.round.roundId }
                    it.copy(isSelectRoundDialogOpen = false, round = round).addScores()
                }
            SelectRoundDialogIntent.SelectRoundDialogClearFilters ->
                _state.update { it.copy(roundFilters = SelectRoundEnabledFilters()) }
            is SelectRoundDialogIntent.SelectRoundDialogFilterClicked ->
                _state.update { it.copy(roundFilters = it.roundFilters.toggle(action.filter)) }

            SelectRoundDialogIntent.OpenSubTypeSelectDialog ->
                _state.update { it.copy(isSelectSubtypeDialogOpen = true) }
            SelectRoundDialogIntent.CloseSubTypeSelectDialog ->
                _state.update { it.copy(isSelectSubtypeDialogOpen = false) }
            is SelectRoundDialogIntent.SubTypeSelected ->
                _state.update {
                    it.copy(isSelectSubtypeDialogOpen = false, subType = action.subType.subTypeId).addScores()
                }
        }
    }

    private fun ClassificationTablesState.addScores(): ClassificationTablesState {
        val searchRound =
                if (round == null) return clearScores()
                else round.round.defaultRoundId ?: return clearScores()
        return copy(scores = tables.get(isGent, age, bow, searchRound, subType))
    }

    private fun ClassificationTablesState.clearScores() = copy(scores = emptyList())
}
