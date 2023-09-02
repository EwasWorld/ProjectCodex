package eywa.projectcodex.components.handicapTables

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogIntent
import eywa.projectcodex.components.handicapTables.HandicapTablesIntent.*
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.datastore.CodexDatastore
import eywa.projectcodex.datastore.DatastoreKey
import eywa.projectcodex.model.Handicap
import eywa.projectcodex.model.roundHandicap
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HandicapTablesViewModel @Inject constructor(
        val db: ScoresRoomDatabase,
        private val helpShowcase: HelpShowcaseUseCase,
        private val datastore: CodexDatastore,
) : ViewModel() {
    private val _state = MutableStateFlow(HandicapTablesState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            datastore.get(DatastoreKey.Use2023HandicapSystem).firstOrNull()?.let { use2023 ->
                _state.update { it.copy(use2023System = use2023) }
            }
            state.map { it.selectRoundDialogState.filters }.distinctUntilChanged().collectLatest { filters ->
                db.roundsRepo().fullRoundsInfo(filters).collectLatest { rounds ->
                    handle(SelectRoundDialogAction(SelectRoundDialogIntent.SetRounds(rounds)))
                }
            }
        }
    }

    private fun HandicapTablesState.addHandicaps(): HandicapTablesState {
        val round = selectRoundDialogState.selectedRound
        val inputParsed = inputFull.parsed
        if (
            inputParsed == null
            || round == null
            || round.roundArrowCounts.isNullOrEmpty()
            || selectRoundDialogState.roundSubTypeDistances.isNullOrEmpty()
        )
            return copy(handicaps = emptyList(), highlightedHandicap = null)

        fun getHandicapScore(handicap: Int) = HandicapScore(
                handicap,
                Handicap.getScoreForRound(
                        round = round,
                        subType = selectRoundDialogState.selectedSubTypeId,
                        handicap = handicap.toDouble(),
                        innerTenArcher = false,
                        arrows = null,
                        use2023Handicaps = use2023System,
                        faces = selectFaceDialogState.selectedFaces,
                )!!,
        )

        val initial = if (inputType == InputType.HANDICAP) {
            getHandicapScore(inputParsed)
        }
        else {
            getHandicapScore(
                    Handicap.getHandicapForRound(
                            round = round.round,
                            roundArrowCounts = round.roundArrowCounts,
                            roundDistances = selectRoundDialogState.roundSubTypeDistances!!,
                            score = inputParsed,
                            innerTenArcher = false,
                            arrows = null,
                            use2023Handicaps = use2023System,
                            faces = selectFaceDialogState.selectedFaces,
                    ).roundHandicap(),
            )
        }

        // TODO Ignore same score handicaps
        val surrounding = 5
        val list = mutableListOf(initial)
        repeat(surrounding) { index ->
            (initial.handicap - index - 1).let {
                if (it >= 0) list.add(getHandicapScore(it))
            }
            (initial.handicap + index + 1).let {
                if (it <= 150) list.add(getHandicapScore(it))
            }
        }

        return copy(handicaps = list.sortedBy { it.handicap }, highlightedHandicap = initial)
    }

    fun handle(action: HandicapTablesIntent) {
        when (action) {
            is InputChanged -> _state.update { it.copy(input = it.input.onValueChanged(action.newSize)).addHandicaps() }
            is SelectRoundDialogAction -> {
                _state.update {
                    val (selectRoundDialogState, faceIntent) = action.action.handle(it.selectRoundDialogState)
                    val selectFaceDialogState = faceIntent?.handle(it.selectFaceDialogState)
                            ?: it.selectFaceDialogState
                    it.copy(
                            selectRoundDialogState = selectRoundDialogState,
                            selectFaceDialogState = selectFaceDialogState,
                    ).addHandicaps()
                }
            }
            is SelectFaceDialogAction ->
                _state.update {
                    it.copy(selectFaceDialogState = action.action.handle(it.selectFaceDialogState)).addHandicaps()
                }
            ToggleHandicapSystem -> _state.update { it.copy(use2023System = !it.use2023System).addHandicaps() }
            ToggleInput ->
                _state.update {
                    val newType = if (it.inputType == InputType.HANDICAP) InputType.SCORE else InputType.HANDICAP
                    it.copy(inputType = newType).addHandicaps()
                }
            is HelpShowcaseAction -> helpShowcase.handle(action.action, CodexNavRoute.HANDICAP_TABLES::class)
        }
    }
}
