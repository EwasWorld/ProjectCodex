package eywa.projectcodex.components.shootDetails.headToHead.addMatch

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.navigation.DEFAULT_INT_NAV_ARG
import eywa.projectcodex.common.navigation.NavArgument
import eywa.projectcodex.common.navigation.get
import eywa.projectcodex.components.referenceTables.headToHead.HeadToHeadUseCase
import eywa.projectcodex.components.shootDetails.ShootDetailsError
import eywa.projectcodex.components.shootDetails.ShootDetailsRepo
import eywa.projectcodex.components.shootDetails.ShootDetailsResponse
import eywa.projectcodex.components.shootDetails.ShootDetailsState
import eywa.projectcodex.components.shootDetails.getData
import eywa.projectcodex.components.shootDetails.headToHead.HeadToHeadResult
import eywa.projectcodex.components.shootDetails.headToHead.addEnd.HeadToHeadRoundInfo
import eywa.projectcodex.components.shootDetails.headToHead.addMatch.HeadToHeadAddMatchIntent.*
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHeadMatch
import eywa.projectcodex.database.shootData.headToHead.Opponent
import eywa.projectcodex.model.headToHead.FullHeadToHeadMatch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.pow

@HiltViewModel
class HeadToHeadAddMatchViewModel @Inject constructor(
        db: ScoresRoomDatabase,
        private val repo: ShootDetailsRepo,
        savedStateHandle: SavedStateHandle,
        private val helpShowcaseUseCase: HelpShowcaseUseCase,
) : ViewModel() {
    private val screen = CodexNavRoute.HEAD_TO_HEAD_ADD_MATCH
    private val extraState = MutableStateFlow<HeadToHeadAddMatchExtras?>(null)

    val state = repo.getStateNullableExtra(extraState, ::stateConverter)
            .stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(),
                    ShootDetailsResponse.Loading as ShootDetailsResponse<HeadToHeadAddMatchState>,
            )

    private val h2hRepo = db.h2hRepo()
    val shootId = savedStateHandle.get<Int>(NavArgument.SHOOT_ID)!!
    private val editingMatchNumber = savedStateHandle.get<Int>(NavArgument.MATCH_NUMBER)

    private val isInserting = savedStateHandle.get<Boolean>(NavArgument.IS_INSERT) ?: false

    private fun stateConverter(
            main: ShootDetailsState,
            extras: HeadToHeadAddMatchExtras?,
    ): HeadToHeadAddMatchState {
        val shoot = main.fullShootInfo!!
        val fullH2hInfo = shoot.h2h ?: throw ShootDetailsError()

        val roundInfo = HeadToHeadRoundInfo(
                round = shoot.fullRoundInfo?.round,
                face = shoot.faces?.first(),
                distance = shoot.fullRoundInfo?.roundDistances?.maxOfOrNull { it.distance }
                        ?: shoot.shootDetail?.distance,
                sightMark = main.sightMark,
                isMetric = shoot.fullRoundInfo?.round?.isMetric ?: shoot.shootDetail?.isDistanceInMeters,
        )
        val editingMatch = fullH2hInfo.matches.find { it.match.matchNumber == editingMatchNumber }

        fun estimatedHeat(): Int? {
            val totalMatches = HeadToHeadUseCase.getOpponents(
                    rank = fullH2hInfo.headToHead.qualificationRank ?: return null,
                    totalArchers = fullH2hInfo.headToHead.totalArchers ?: return null,
            ).count()
            return (totalMatches - (editingMatchNumber?.minus(1) ?: 0) - 1).coerceAtLeast(0)
        }

        fun FullHeadToHeadMatch.asPreviousMatch() =
                HeadToHeadAddMatchState.PreviousMatch(
                        matchNumber = match.matchNumber,
                        heat = match.heat,
                        result = result,
                        runningTotal = runningTotals.lastOrNull()?.left,
                        isBye = match.isBye,
                )

        if (editingMatchNumber != null) {
            val previous = fullH2hInfo.matches.find { it.match.matchNumber == editingMatchNumber - 1 }

            // Edit match from database
            if (editingMatch != null && !isInserting) {
                if (extraState.value == null || extraState.value?.matchNumber != editingMatchNumber) {
                    extraState.update { (it ?: HeadToHeadAddMatchExtras()).resetEditInfo(editingMatch.match) }
                }

                return HeadToHeadAddMatchState(
                        roundInfo = roundInfo,
                        extras = extras ?: HeadToHeadAddMatchExtras(),
                        previousMatch = previous?.asPreviousMatch(),
                        editing = editingMatch.match,
                        editingMatchWithSetsToBye = editingMatch.sets.isNotEmpty() && extras?.isBye == true,
                )
            }

            // Create new match with fixed match number
            if (extraState.value == null || extraState.value?.matchNumber != editingMatchNumber) {
                extraState.update {
                    if (isInserting) {
                        HeadToHeadAddMatchExtras(matchNumber = editingMatchNumber)
                    }
                    else {
                        HeadToHeadAddMatchExtras(
                                matchNumber = editingMatchNumber,
                                heat = previous?.match?.heat?.minus(1)?.coerceAtLeast(0) ?: estimatedHeat(),
                        )
                                .setMaxRank(previous)
                                .setOpponentQualiRank(fullH2hInfo.headToHead.getExpectedOpponentRank(editingMatchNumber))
                    }
                }
            }

            return HeadToHeadAddMatchState(
                    roundInfo = roundInfo,
                    extras = (extras ?: HeadToHeadAddMatchExtras()),
                    previousMatch = previous?.asPreviousMatch(),
                    editing = null,
                    isInserting = isInserting,
            )
        }

        check(!isInserting) { "Must provide a match number when inserting" }
        val latestMatch = fullH2hInfo.matches.maxByOrNull { it.match.matchNumber }

        // Create first match
        if (latestMatch == null) {

            if (extraState.value == null || extraState.value?.matchNumber != 1) {
                extraState.update {
                    HeadToHeadAddMatchExtras(
                            matchNumber = 1,
                            heat = estimatedHeat(),
                    ).setOpponentQualiRank(fullH2hInfo.headToHead.getExpectedOpponentRank(1))
                }
            }

            return HeadToHeadAddMatchState(
                    roundInfo = roundInfo,
                    extras = extras ?: HeadToHeadAddMatchExtras(),
                    previousMatch = null,
                    editing = null,
            )
        }

        // Create next new match
        if (latestMatch.isComplete && !latestMatch.match.isBye) {
            val previousMatch = latestMatch.asPreviousMatch()
            val newMatchNumber = previousMatch.matchNumber + 1

            if (extraState.value == null || extraState.value?.matchNumber != newMatchNumber) {
                extraState.update {
                    HeadToHeadAddMatchExtras(
                            matchNumber = newMatchNumber,
                            heat = latestMatch.match.heat?.minus(1)?.coerceAtLeast(0),
                    )
                            .setOpponentQualiRank(fullH2hInfo.headToHead.getExpectedOpponentRank(newMatchNumber))
                            .setMaxRank(latestMatch)
                }
            }

            return HeadToHeadAddMatchState(
                    roundInfo = roundInfo,
                    extras = extras ?: HeadToHeadAddMatchExtras(),
                    previousMatch = previousMatch,
                    editing = null,
            )
        }

        // Previous match not completed, jump to add end screen
        if (extras?.openAddEndScreenForMatch == null) {
            extraState.update {
                (it ?: HeadToHeadAddMatchExtras()).copy(
                        openAddEndScreenForMatch = DEFAULT_INT_NAV_ARG,
                        matchNumber = latestMatch.match.matchNumber,
                )
            }
        }

        return HeadToHeadAddMatchState(
                roundInfo = roundInfo,
                extras = (extras ?: HeadToHeadAddMatchExtras()).resetEditInfo(latestMatch.match),
                previousMatch = null,
                editing = latestMatch.match,
        )
    }

    private fun HeadToHeadAddMatchExtras.resetEditInfo(editing: DatabaseHeadToHeadMatch) = copy(
            heat = editing.heat,
            opponent = editing.opponent ?: "",
            opponentQualiRank = opponentQualiRank.copy(text = editing.opponentQualificationRank?.toString() ?: ""),
            isBye = editing.isBye,
            matchNumber = editing.matchNumber,
    )

    private fun HeadToHeadAddMatchExtras.setMaxRank(previous: FullHeadToHeadMatch?): HeadToHeadAddMatchExtras {
        fun HeadToHeadAddMatchExtras.set(rank: Int?) =
                copy(maxPossibleRank = maxPossibleRank.copy(text = rank?.toString() ?: ""))

        return set(
                if (previous?.match?.maxPossibleRank == null) null
                else if (previous.result == HeadToHeadResult.WIN) previous.match.maxPossibleRank
                else if (previous.match.heat != null) previous.match.maxPossibleRank + 2.0.pow(previous.match.heat)
                        .toInt()
                else null
        )
    }

    private fun HeadToHeadAddMatchExtras.setOpponentQualiRank(opponentQualiRank: Opponent?) =
            when (opponentQualiRank) {
                null -> this
                Opponent.Bye -> {
                    copy(
                            isBye = true,
                            opponentQualiRank = this.opponentQualiRank.copy(text = ""),
                            opponent = "",
                    )
                }

                is Opponent.Rank -> {
                    copy(
                            isBye = false,
                            opponentQualiRank = this.opponentQualiRank.copy(text = opponentQualiRank.rank.toString()),
                            opponent = "",
                    )
                }
            }

    fun handle(action: HeadToHeadAddMatchIntent) {
        fun updateState(block: (HeadToHeadAddMatchExtras) -> HeadToHeadAddMatchExtras) =
                extraState.update { block(it!!) }

        when (action) {
            is ShootDetailsAction -> repo.handle(action.action, screen)

            EditSightMarkClicked -> updateState { it.copy(openEditSightMark = true) }
            ExpandSightMarkClicked -> updateState { it.copy(openAllSightMarks = true) }
            EditSightMarkHandled -> updateState { it.copy(openEditSightMark = false) }
            ExpandSightMarkHandled -> updateState { it.copy(openAllSightMarks = false) }
            OpenAddEndScreenHandled -> updateState { it.copy(openAddEndScreenForMatch = null) }

            is HelpShowcaseAction -> helpShowcaseUseCase.handle(action.action, screen::class)

            is OpponentUpdated -> updateState { it.copy(opponent = action.opponent) }

            is OpponentQualiRankUpdated ->
                updateState { it.copy(opponentQualiRank = it.opponentQualiRank.onTextChanged(action.rank)) }

            is MaxPossibleRankUpdated ->
                updateState { it.copy(maxPossibleRank = it.maxPossibleRank.onTextChanged(action.rank)) }

            is SelectHeatDialogItemClicked ->
                updateState {
                    it.copy(heat = action.heat, showSelectHeatDialog = false)
                }

            ToggleIsBye -> updateState { it.copy(isBye = !it.isBye) }
            HeatClicked -> updateState { it.copy(showSelectHeatDialog = true) }
            CloseSelectHeatDialog -> updateState { it.copy(showSelectHeatDialog = false) }

            SubmitClicked -> state.value.getData()?.let { state ->
                val newMatch = state.asHeadToHeadMatch(shootId)
                viewModelScope.launch {
                    if (state.editing != null) {
                        if (state.editingMatchWithSetsToBye) {
                            h2hRepo.deleteSets(shootId = newMatch.shootId, matchNumber = newMatch.matchNumber)
                        }
                        h2hRepo.update(newMatch.copy(sightersCount = state.editing.sightersCount))
                        extraState.update { it!!.copy(pressBack = true) }
                    }
                    else {
                        h2hRepo.insert(newMatch)
                        if (isInserting) {
                            extraState.update { it!!.copy(pressBack = true) }
                        }
                        else {
                            extraState.update { it!!.copy(openAddEndScreenForMatch = newMatch.matchNumber) }
                        }
                    }
                }
            }

            DeleteClicked -> state.value.getData()?.let { state ->
                if (state.editing == null) return
                viewModelScope.launch {
                    h2hRepo.delete(shootId = state.editing.shootId, matchNumber = state.editing.matchNumber)
                    extraState.update { it!!.copy(pressBack = true) }
                }
            }

            ResetClicked -> updateState { s ->
                state.value.getData()?.editing?.let { s.resetEditInfo(it) } ?: s
            }

            BackPressedHandled -> extraState.update { it!!.copy(pressBack = false) }
        }
    }
}
