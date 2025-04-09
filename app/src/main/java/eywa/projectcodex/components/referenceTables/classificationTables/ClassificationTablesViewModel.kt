package eywa.projectcodex.components.referenceTables.classificationTables

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.navigation.NavArgument
import eywa.projectcodex.common.navigation.get
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogIntent
import eywa.projectcodex.common.utils.classificationTables.ClassificationTablesUseCase
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationBow
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsTask
import eywa.projectcodex.components.referenceTables.classificationTables.ClassificationTablesIntent.*
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.datastore.CodexDatastore
import eywa.projectcodex.datastore.DatastoreKey
import eywa.projectcodex.model.Handicap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClassificationTablesViewModel @Inject constructor(
        private val db: ScoresRoomDatabase,
        private val helpShowcase: HelpShowcaseUseCase,
        private val tables: ClassificationTablesUseCase,
        private val datastore: CodexDatastore,
        private val updateDefaultRoundsTask: UpdateDefaultRoundsTask,
        savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private var argRoundId = savedStateHandle.get<Int>(NavArgument.ROUND_ID)
    private var argRoundSubTypeId = savedStateHandle.get<Int>(NavArgument.ROUND_SUB_TYPE_ID)

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
        viewModelScope.launch {
            updateDefaultRoundsTask.state.collect { updateState ->
                _state.update { it.copy(updateDefaultRoundsState = updateState) }
            }
        }
        viewModelScope.launch {
            db.archerRepo().defaultArcher
                    .combine(db.bowRepo().defaultBow) { a, b -> a to b }
                    .collect { (archer, bow) ->
                        if (archer != null || bow != null) {
                            _state.update {
                                it.copy(
                                        isGent = archer?.isGent ?: it.isGent,
                                        age = archer?.age ?: it.age,
                                        bow = bow?.type ?: it.bow,
                                ).addScores()
                            }
                        }
                    }
        }
        if (argRoundId == null) {
            viewModelScope.launch {
                db.shootsRepo().mostRecentRoundShot.collect { round ->
                    if (round != null) {
                        argRoundId = round.roundId
                        argRoundSubTypeId = round.roundSubTypeId
                        _state.update {
                            it.copy(
                                    selectRoundDialogState = it.selectRoundDialogState
                                            .copy(selectedRoundId = argRoundId, selectedSubTypeId = argRoundSubTypeId)
                                            .clearSelectedIfInvalid(),
                            ).addScores()
                        }
                    }
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
            is SelectRoundDialogAction -> {
                _state.update {
                    var (selectRoundDialogState, _) = action.action.handle(it.selectRoundDialogState)
                    if (action.action is SelectRoundDialogIntent.SetRounds) {
                        selectRoundDialogState = selectRoundDialogState
                                .copy(selectedRoundId = argRoundId, selectedSubTypeId = argRoundSubTypeId)
                                .clearSelectedIfInvalid()
                    }
                    it.copy(selectRoundDialogState = selectRoundDialogState).addScores()
                }
            }

            is HelpShowcaseAction -> helpShowcase.handle(action.action, CodexNavRoute.CLASSIFICATION_TABLES::class)
        }
    }

    private fun ClassificationTablesState.addScores(): ClassificationTablesState {
        val selectedRound = selectRoundDialogState.selectedRound
        val bestPossibleHandicap =
                if (selectedRound?.maxScore != null) {
                    Handicap.getHandicapForRound(
                            round = selectedRound,
                            score = selectedRound.maxScore,
                            subType = selectRoundDialogState.selectedSubTypeId,
                            innerTenArcher = bow == ClassificationBow.COMPOUND,
                            use2023Handicaps = use2023Handicaps,
                    )
                }
                else {
                    null
                }

        val isOutdoor = selectRoundDialogState.selectedRound?.round?.isOutdoor ?: true
        val rough = if (wa1440RoundInfo != null && wa18RoundInfo != null) {
            tables.getRoughHandicaps(
                    isGent = isGent,
                    age = age,
                    bow = bow,
                    wa1440RoundInfo = wa1440RoundInfo,
                    wa18RoundInfo = wa18RoundInfo,
                    isOutdoor = isOutdoor,
                    use2023Handicaps = use2023Handicaps,
            )
                    ?.map {
                        val score =
                                if (selectedRound == null) null
                                // Classification is impossible if required handicap is better than
                                // the highest possible for the round
                                else if (bestPossibleHandicap != null && it.handicap!! < bestPossibleHandicap) null
                                else Handicap.getScoreForRound(
                                        round = selectedRound,
                                        subType = selectRoundDialogState.selectedSubTypeId,
                                        handicap = it.handicap!!.toDouble(),
                                        innerTenArcher = bow == ClassificationBow.COMPOUND,
                                        use2023Handicaps = use2023Handicaps,
                                )
                        it.copy(score = score)
                    }.orEmpty()
        }
        else emptyList()

        fun ClassificationTablesState.clearScores() =
                copy(officialClassifications = emptyList(), roughHandicaps = rough)

        val round = selectRoundDialogState.selectedRound
                ?: return clearScores()
        val scores = tables.get(
                isGent = isGent,
                age = age,
                bow = bow,
                fullRoundInfo = round,
                roundSubTypeId = selectRoundDialogState.selectedSubTypeId,
                isTripleFace = false,
                use2023Handicaps = use2023Handicaps,
        ) ?: return clearScores()
        return copy(officialClassifications = scores, roughHandicaps = rough)
    }
}
