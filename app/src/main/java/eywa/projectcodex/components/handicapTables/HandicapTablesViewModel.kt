package eywa.projectcodex.components.handicapTables

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogIntent
import eywa.projectcodex.components.handicapTables.HandicapTablesIntent.HelpShowcaseAction
import eywa.projectcodex.components.handicapTables.HandicapTablesIntent.InputChanged
import eywa.projectcodex.components.handicapTables.HandicapTablesIntent.SelectFaceDialogAction
import eywa.projectcodex.components.handicapTables.HandicapTablesIntent.SelectRoundDialogAction
import eywa.projectcodex.components.handicapTables.HandicapTablesIntent.ToggleHandicapSystem
import eywa.projectcodex.components.handicapTables.HandicapTablesIntent.ToggleInput
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.datastore.CodexDatastore
import eywa.projectcodex.datastore.DatastoreKey
import eywa.projectcodex.model.Handicap
import eywa.projectcodex.model.roundHandicap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
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

    fun handle(action: HandicapTablesIntent) {
        when (action) {
            is InputChanged -> _state.update { it.copy(input = it.input.onTextChanged(action.newSize)).addHandicaps() }
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

    private fun HandicapTablesState.addHandicaps(): HandicapTablesState {
        val round = selectRoundDialogState.selectedRound
        val inputParsed = inputFull.parsed
        if (
            inputParsed == null
            || round == null
            || round.roundArrowCounts.isNullOrEmpty()
            || selectRoundDialogState.roundSubTypeDistances.isNullOrEmpty()
        ) {
            return copy(handicaps = emptyList(), highlightedHandicap = null)
        }

        val initial =
                if (inputType == InputType.HANDICAP) getHandicapScore(inputParsed)
                else getHandicapScore(getHandicapForScore(inputParsed))

        /*
         * Add up to the next 5 handicaps better than [initial]
         */
        val handicaps = mutableListOf<HandicapScore>()
        var previous = initial
        var checkHandicap = initial.handicap
        while (handicaps.size < 5 && checkHandicap > Handicap.MIN_HANDICAP) {
            checkHandicap--
            val newEntry = getHandicapScore(checkHandicap)

            // Ignore duplicate scores, as we're checking better handicaps with each loop, we can just ignore duplicates
            // This will leave us with the worse handicap for each score and eliminate duplicate scores
            if (newEntry.score != previous.score || checkHandicap == Handicap.MIN_HANDICAP) {
                handicaps.add(newEntry)
            }
            previous = newEntry
        }

        /*
         * Add up to the next 6 handicaps at or worse than [initial]
         * (1 more to include [initial])
         */
        val maxHandicap = Handicap.maxHandicap(use2023System)
        checkHandicap = initial.handicap
        for (i in 0..5) {
            val handicapScore = getHandicapScore(checkHandicap)

            // Ensure we have the worst handicap for the found score
            val newItem = HandicapScore(getHandicapForScore(handicapScore.score), handicapScore.score)
            handicaps.add(newItem)

            checkHandicap = newItem.handicap + 1
            if (checkHandicap > maxHandicap) break
        }

        handicaps.sortBy { it.handicap }
        return copy(
                handicaps = handicaps,
                highlightedHandicap = handicaps.first { it.handicap >= initial.handicap },
        )
    }

    private fun HandicapTablesState.getHandicapForScore(score: Int) =
            Handicap.getHandicapForRound(
                    round = selectRoundDialogState.selectedRound!!,
                    subType = selectRoundDialogState.selectedSubTypeId,
                    score = score,
                    innerTenArcher = false,
                    arrows = null,
                    use2023Handicaps = use2023System,
                    faces = selectFaceDialogState.selectedFaces,
            )!!.roundHandicap()

    private fun HandicapTablesState.getHandicapScore(handicap: Int) = HandicapScore(
            handicap,
            Handicap.getScoreForRound(
                    round = selectRoundDialogState.selectedRound!!,
                    subType = selectRoundDialogState.selectedSubTypeId,
                    handicap = handicap.toDouble(),
                    innerTenArcher = false,
                    arrows = null,
                    use2023Handicaps = use2023System,
                    faces = selectFaceDialogState.selectedFaces,
            )!!,
    )
}
