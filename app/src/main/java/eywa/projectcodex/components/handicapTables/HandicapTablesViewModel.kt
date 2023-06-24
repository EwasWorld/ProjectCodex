package eywa.projectcodex.components.handicapTables

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.common.archeryObjects.Handicap
import eywa.projectcodex.common.archeryObjects.roundHandicap
import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogIntent
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundEnabledFilters
import eywa.projectcodex.common.utils.getDistances
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.datastore.CodexDatastore
import eywa.projectcodex.datastore.DatastoreKey
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
                _state.update { it.copy(use2023Tables = use2023) }
            }
            state.map { it.roundFilters }.distinctUntilChanged().collectLatest { filters ->
                db.roundsRepo().fullRoundsInfo(filters).collectLatest { rounds ->
                    _state.update { it.copy(allRounds = rounds) }
                }
            }
        }
    }

    private fun HandicapTablesState.addHandicaps(): HandicapTablesState {
        val subtypeDistances = round?.info?.getDistances(subType ?: 1)
        if (
            input == null
            || (inputHandicap && (input < 0 || input > 150))
            || round == null || round.info.roundArrowCounts.isNullOrEmpty() || subtypeDistances.isNullOrEmpty()
        )
            return copy(handicaps = emptyList(), highlightedHandicap = null)

        fun getScore(handicap: Int) = HandicapScore(
                handicap,
                Handicap.getScoreForRound(
                        round = round.info.round,
                        roundArrowCounts = round.info.roundArrowCounts,
                        roundDistances = subtypeDistances,
                        handicap = handicap.toFloat(),
                        innerTenArcher = false,
                        arrows = null,
                        use2023Handicaps = use2023Tables,
                        // TODO_CURRENT Faces
                ),
        )

        val initial = if (inputHandicap) {
            getScore(input)
        }
        else {
            getScore(
                    Handicap.getHandicapForRound(
                            round = round.info.round,
                            roundArrowCounts = round.info.roundArrowCounts,
                            roundDistances = subtypeDistances,
                            score = input,
                            innerTenArcher = false,
                            arrows = null,
                            use2023Handicaps = use2023Tables,
                            // TODO_CURRENT Faces
                    ).roundHandicap(),
            )
        }

        // TODO Ignore same score handicaps
        val surrounding = 5
        val list = mutableListOf(initial)
        repeat(surrounding) { index ->
            (initial.handicap - index - 1).let {
                if (it >= 0) list.add(getScore(it))
            }
            (initial.handicap + index + 1).let {
                if (it <= 150) list.add(getScore(it))
            }
        }

        return copy(handicaps = list.sortedBy { it.handicap }, highlightedHandicap = initial)
    }

    fun handle(action: HandicapTablesIntent) {
        when (action) {
            is HandicapTablesIntent.InputChanged -> _state.update { it.copy(input = action.newSize).addHandicaps() }
            is HandicapTablesIntent.SelectRoundDialogAction -> handleSelectRoundDialogIntent(action.action)
            HandicapTablesIntent.ToggleHandicapSystem -> _state.update {
                it.copy(use2023Tables = !it.use2023Tables).addHandicaps()
            }
            HandicapTablesIntent.ToggleInput -> _state.update {
                it.copy(inputHandicap = !it.inputHandicap).addHandicaps()
            }
            is HandicapTablesIntent.HelpShowcaseAction ->
                helpShowcase.handle(action.action, HandicapTablesFragment::class)
        }
    }

    private fun handleSelectRoundDialogIntent(action: SelectRoundDialogIntent) {
        when (action) {
            SelectRoundDialogIntent.OpenRoundSelectDialog -> _state.update { it.copy(isSelectRoundDialogOpen = true) }
            SelectRoundDialogIntent.CloseRoundSelectDialog -> _state.update { it.copy(isSelectRoundDialogOpen = false) }
            SelectRoundDialogIntent.NoRoundSelected ->
                _state.update { it.copy(isSelectRoundDialogOpen = false, round = null).addHandicaps() }
            is SelectRoundDialogIntent.RoundSelected ->
                _state.update {
                    val round = it.allRounds
                            ?.find { round -> round.round.roundId == action.round.roundId }
                            ?.let { roundInfo -> HandicapTablesState.RoundInfo.Round(roundInfo) }
                    it.copy(isSelectRoundDialogOpen = false, round = round).addHandicaps()
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
                    it.copy(isSelectSubtypeDialogOpen = false, subType = action.subType.subTypeId).addHandicaps()
                }
        }
    }
}
