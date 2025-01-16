package eywa.projectcodex.components.shootDetails.headToHeadEnd.addHeat

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
import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadResult
import eywa.projectcodex.components.shootDetails.headToHeadEnd.addEnd.HeadToHeadRoundInfo
import eywa.projectcodex.components.shootDetails.headToHeadEnd.addHeat.HeadToHeadAddHeatIntent.*
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHeadHeat
import eywa.projectcodex.database.shootData.headToHead.Opponent
import eywa.projectcodex.model.headToHead.FullHeadToHeadHeat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.pow

@HiltViewModel
class HeadToHeadAddHeatViewModel @Inject constructor(
        db: ScoresRoomDatabase,
        private val repo: ShootDetailsRepo,
        savedStateHandle: SavedStateHandle,
        private val helpShowcaseUseCase: HelpShowcaseUseCase,
) : ViewModel() {
    private val screen = CodexNavRoute.HEAD_TO_HEAD_ADD_HEAT
    private val extraState = MutableStateFlow<HeadToHeadAddHeatExtras?>(null)

    val state = repo.getStateNullableExtra(extraState, ::stateConverter)
            .stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(),
                    ShootDetailsResponse.Loading as ShootDetailsResponse<HeadToHeadAddHeatState>,
            )

    private val h2hRepo = db.h2hRepo()
    val shootId = savedStateHandle.get<Int>(NavArgument.SHOOT_ID)!!
    private val editingMatchNumber = savedStateHandle.get<Int>(NavArgument.MATCH_NUMBER)

    private fun stateConverter(
            main: ShootDetailsState,
            extras: HeadToHeadAddHeatExtras?,
    ): HeadToHeadAddHeatState {
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
        val editingMatch = fullH2hInfo.heats.find { it.heat.matchNumber == editingMatchNumber }?.heat

        fun estimatedHeat(): Int? {
            val totalMatches = HeadToHeadUseCase.getOpponents(
                    rank = fullH2hInfo.headToHead.qualificationRank ?: return null,
                    totalArchers = fullH2hInfo.headToHead.totalArchers ?: return null,
            ).count()
            return (totalMatches - (editingMatchNumber?.minus(1) ?: 0) - 1).coerceAtLeast(0)
        }

        fun FullHeadToHeadHeat.asPreviousHeat() =
                HeadToHeadAddHeatState.PreviousHeat(
                        matchNumber = heat.matchNumber,
                        heat = heat.heat,
                        result = sets.lastOrNull()?.result ?: HeadToHeadResult.INCOMPLETE,
                        runningTotal = runningTotals.lastOrNull()?.left,
                        isBye = heat.isBye,
                )

        if (editingMatchNumber != null) {
            val previous = fullH2hInfo.heats.find { it.heat.matchNumber == editingMatchNumber - 1 }

            // Edit heat from database
            if (editingMatch != null) {
                if (extraState.value == null) {
                    extraState.update { (it ?: HeadToHeadAddHeatExtras()).resetEditInfo(editingMatch) }
                }

                return HeadToHeadAddHeatState(
                        matchNumber = editingMatchNumber,
                        roundInfo = roundInfo,
                        extras = extras ?: HeadToHeadAddHeatExtras(),
                        previousHeat = previous?.asPreviousHeat(),
                        editing = editingMatch,
                )
            }

            // Create new heat with fixed match number
            if (extraState.value == null) {
                extraState.update {
                    HeadToHeadAddHeatExtras(
                            heat = previous?.heat?.heat?.minus(1)?.coerceAtLeast(0) ?: estimatedHeat(),
                    )
                            .setMaxRank(previous)
                            .setOpponentQualiRank(fullH2hInfo.headToHead.getOpponentRank(editingMatchNumber))
                }
            }

            return HeadToHeadAddHeatState(
                    matchNumber = editingMatchNumber,
                    roundInfo = roundInfo,
                    extras = (extras ?: HeadToHeadAddHeatExtras()),
                    previousHeat = previous?.asPreviousHeat(),
                    editing = null,
            )
        }

        val maxHeat = fullH2hInfo.heats.maxByOrNull { it.heat.matchNumber }

        // Create first heat
        if (maxHeat == null) {

            if (extraState.value == null) {
                extraState.update {
                    HeadToHeadAddHeatExtras(heat = estimatedHeat())
                            .setOpponentQualiRank(fullH2hInfo.headToHead.getOpponentRank(1))
                }
            }

            return HeadToHeadAddHeatState(
                    matchNumber = 1,
                    roundInfo = roundInfo,
                    extras = extras ?: HeadToHeadAddHeatExtras(),
                    previousHeat = null,
                    editing = null,
            )
        }

        // Create next new heat
        if (maxHeat.isComplete && !maxHeat.heat.isBye) {
            val previousHeat = maxHeat.asPreviousHeat()

            if (extraState.value == null || maxHeat.heat.heat == extraState.value?.heat) {
                extraState.update {
                    HeadToHeadAddHeatExtras(heat = maxHeat.heat.heat?.minus(1)?.coerceAtLeast(0))
                            .setOpponentQualiRank(fullH2hInfo.headToHead.getOpponentRank(previousHeat.matchNumber + 1))
                            .setMaxRank(maxHeat)
                }
            }

            return HeadToHeadAddHeatState(
                    matchNumber = previousHeat.matchNumber + 1,
                    roundInfo = roundInfo,
                    extras = extras ?: HeadToHeadAddHeatExtras(),
                    previousHeat = previousHeat,
                    editing = null,
            )
        }

        // Previous heat not completed, jump to add end screen
        if (extras?.openAddEndScreenForMatch == null) {
            extraState.update { (it ?: HeadToHeadAddHeatExtras()).copy(openAddEndScreenForMatch = DEFAULT_INT_NAV_ARG) }
        }

        return HeadToHeadAddHeatState(
                matchNumber = maxHeat.heat.matchNumber,
                roundInfo = roundInfo,
                extras = (extras ?: HeadToHeadAddHeatExtras()).resetEditInfo(maxHeat.heat),
                previousHeat = null,
                editing = maxHeat.heat,
        )
    }

    private fun HeadToHeadAddHeatExtras.resetEditInfo(editing: DatabaseHeadToHeadHeat) = copy(
            heat = editing.heat,
            opponent = editing.opponent ?: "",
            opponentQualiRank = opponentQualiRank.copy(text = editing.opponentQualificationRank?.toString() ?: ""),
            isBye = editing.isBye,
    )

    private fun HeadToHeadAddHeatExtras.setMaxRank(previous: FullHeadToHeadHeat?): HeadToHeadAddHeatExtras {
        fun HeadToHeadAddHeatExtras.set(rank: Int?) =
                copy(maxPossibleRank = maxPossibleRank.copy(text = rank?.toString() ?: ""))

        return set(
                if (previous?.heat?.maxPossibleRank == null) null
                else if (previous.result == HeadToHeadResult.WIN) previous.heat.maxPossibleRank
                else if (previous.heat.heat != null) previous.heat.maxPossibleRank + 2.0.pow(previous.heat.heat).toInt()
                else null
        )
    }

    private fun HeadToHeadAddHeatExtras.setOpponentQualiRank(opponentQualiRank: Opponent?) =
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

    fun handle(action: HeadToHeadAddHeatIntent) {
        fun updateState(block: (HeadToHeadAddHeatExtras) -> HeadToHeadAddHeatExtras) =
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
                val newHeat = state.asHeadToHeadHeat(shootId)
                viewModelScope.launch {
                    if (state.editing != null) {
                        h2hRepo.update(newHeat)
                        extraState.update { it!!.copy(pressBack = true) }
                    }
                    else {
                        h2hRepo.insert(newHeat)
                        extraState.update { it!!.copy(openAddEndScreenForMatch = newHeat.matchNumber) }
                    }
                }
            }

            DeleteClicked -> state.value.getData()?.let { state ->
                if (state.editing == null) return
                viewModelScope.launch {
                    h2hRepo.delete(shootId = state.editing.shootId, matchNumber = state.editing.matchNumber)
                }
            }

            ResetClicked -> updateState { s ->
                state.value.getData()?.editing?.let { s.resetEditInfo(it) } ?: s
            }
        }
    }
}
