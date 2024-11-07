package eywa.projectcodex.components.shootDetails.headToHeadEnd.addEnd

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.navigation.NavArgument
import eywa.projectcodex.common.navigation.get
import eywa.projectcodex.components.referenceTables.headToHead.HeadToHeadUseCase
import eywa.projectcodex.components.shootDetails.ShootDetailsError
import eywa.projectcodex.components.shootDetails.ShootDetailsRepo
import eywa.projectcodex.components.shootDetails.ShootDetailsResponse
import eywa.projectcodex.components.shootDetails.ShootDetailsState
import eywa.projectcodex.components.shootDetails.getData
import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadArcherType
import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadArcherType.*
import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadResult
import eywa.projectcodex.components.shootDetails.headToHeadEnd.addEnd.HeadToHeadAddEndIntent.*
import eywa.projectcodex.components.shootDetails.headToHeadEnd.addEnd.HeadToHeadAddHeatIntent.*
import eywa.projectcodex.components.shootDetails.headToHeadEnd.addEnd.HeadToHeadAddIntent.*
import eywa.projectcodex.components.shootDetails.headToHeadEnd.addEnd.HeadToHeadAddState.*
import eywa.projectcodex.components.shootDetails.headToHeadEnd.grid.HeadToHeadGridRowData
import eywa.projectcodex.components.shootDetails.headToHeadEnd.grid.HeadToHeadGridRowData.Arrows
import eywa.projectcodex.components.shootDetails.headToHeadEnd.grid.HeadToHeadGridRowData.EditableTotal
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.model.FullHeadToHeadSet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HeadToHeadAddViewModel @Inject constructor(
        private val db: ScoresRoomDatabase,
        private val repo: ShootDetailsRepo,
        savedStateHandle: SavedStateHandle,
        private val helpShowcaseUseCase: HelpShowcaseUseCase,
) : ViewModel() {
    private val screen = CodexNavRoute.HEAD_TO_HEAD_ADD
    private val extraState = MutableStateFlow<HeadToHeadAddExtras?>(null)

    val state = repo.getStateNullableExtra(extraState, ::stateConverter)
            .stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(),
                    ShootDetailsResponse.Loading as ShootDetailsResponse<HeadToHeadAddState>,
            )

    private val h2hRepo = db.h2hRepo()
    private val shootId = savedStateHandle.get<Int>(NavArgument.SHOOT_ID)!!

    private fun stateConverter(
            main: ShootDetailsState,
            extras: HeadToHeadAddExtras?,
    ): HeadToHeadAddState {
        val shoot = main.fullShootInfo!!
        val fullH2hInfo = shoot.h2h ?: throw ShootDetailsError()

        val common = RoundCommon(
                round = shoot.fullRoundInfo?.round,
                face = shoot.faces?.first(),
                distance = shoot.fullRoundInfo?.roundDistances?.maxOfOrNull { it.distance }
                        ?: shoot.shootDetail?.distance,
                sightMark = null,
                isMetric = shoot.fullRoundInfo?.round?.isMetric
                        ?: shoot.shootDetail?.isDistanceInMeters,
        )

        val teamSize = fullH2hInfo.headToHead.teamSize
        val heat = fullH2hInfo.heats.minByOrNull { it.heat.heat }
        if (heat == null) {
            if (extraState.value !is HeadToHeadAddExtras.AddHeat) {
                extraState.update { HeadToHeadAddExtras.AddHeat() }
            }
            return AddHeat(
                    roundCommon = common,
                    extras = (extras as? HeadToHeadAddExtras.AddHeat) ?: HeadToHeadAddExtras.AddHeat(),
            )
        }

        val scores = heat.results.lastOrNull()
        if (heat.heatResult() != HeadToHeadResult.INCOMPLETE) {
            if (extraState.value !is HeadToHeadAddExtras.AddHeat) {
                extraState.update { HeadToHeadAddExtras.AddHeat(heat = (heat.heat.heat - 1).coerceAtLeast(0)) }
            }
            return AddHeat(
                    roundCommon = common,
                    extras = (extras as? HeadToHeadAddExtras.AddHeat) ?: HeadToHeadAddExtras.AddHeat(),
                    previousHeat = AddHeat.PreviousHeat(
                            heat = heat.heat.heat,
                            result = heat.sets.last().result,
                            teamRunningTotal = scores!!.first,
                            opponentRunningTotal = scores.second,
                    ),
            )
        }

        val lastSet = heat.sets.maxByOrNull { it.setNumber }
        if (lastSet == null || lastSet.result != HeadToHeadResult.INCOMPLETE) {
            val setNumber = (lastSet?.setNumber?.plus(1)) ?: 1
            val isShootOff = HeadToHeadUseCase.shootOffSet(teamSize) == setNumber
            val endSize = if (isShootOff) 1 else HeadToHeadUseCase.END_SIZE

            fun getRow(type: HeadToHeadArcherType, isTotal: Boolean): HeadToHeadGridRowData {
                val expectedArrowCount = type.expectedArrowCount(endSize = endSize, teamSize = teamSize)
                return if (isTotal) EditableTotal(type = type, expectedArrowCount = expectedArrowCount)
                else Arrows(type = type, expectedArrowCount = expectedArrowCount)
            }

            val defaultData =
                    if (teamSize == 1) listOf(getRow(SELF, false), getRow(OPPONENT, true))
                    else listOf(getRow(SELF, false), getRow(TEAM, true), getRow(OPPONENT, true))

            val set = FullHeadToHeadSet(
                    setNumber = setNumber,
                    data = defaultData,
                    isShootOff = HeadToHeadUseCase.shootOffSet(teamSize) == setNumber,
                    teamSize = fullH2hInfo.headToHead.teamSize,
                    isShootOffWin = false,
            )

            if (extraState.value !is HeadToHeadAddExtras.AddEnd) {
                extraState.update { HeadToHeadAddExtras.AddEnd(set = set, selected = SELF) }
            }
            return AddEnd(
                    roundCommon = common,
                    extras = (extras as? HeadToHeadAddExtras.AddEnd) ?: HeadToHeadAddExtras.AddEnd(),
                    heat = heat.heat,
                    isRecurveStyle = fullH2hInfo.headToHead.isRecurveStyle,
                    teamRunningTotal = scores?.first ?: 0,
                    opponentRunningTotal = scores?.second ?: 0,
            )
        }

        if (extraState.value !is HeadToHeadAddExtras.AddEnd) {
            extraState.update {
                HeadToHeadAddExtras.AddEnd(
                        set = lastSet,
                        selected = lastSet
                                .data
                                .sortedBy { it.type.ordinal }
                                .first { !it.isComplete }
                                .type,
                )
            }
        }
        return AddEnd(
                roundCommon = common,
                extras = (extras as? HeadToHeadAddExtras.AddEnd) ?: HeadToHeadAddExtras.AddEnd(),
                heat = heat.heat,
                isRecurveStyle = fullH2hInfo.headToHead.isRecurveStyle,
                teamRunningTotal = scores?.first ?: 0,
                opponentRunningTotal = scores?.second ?: 0,
        )
    }

    fun handle(action: HeadToHeadAddIntent) {
        when (action) {
            is HeadToHeadAddIntent.HelpShowcaseAction ->
                helpShowcaseUseCase.handle(action.action, screen::class)

            is AddEndAction -> handle(action.action)
            is AddHeatAction -> handle(action.action)
            is ShootDetailsAction -> repo.handle(action.action, screen)

            EditSightMarkClicked -> updateEffects { it.copy(openEditSightMark = true) }
            ExpandSightMarkClicked -> updateEffects { it.copy(openAllSightMarks = true) }
            EditSightMarkHandled -> updateEffects { it.copy(openEditSightMark = false) }
            ExpandSightMarkHandled -> updateEffects { it.copy(openAllSightMarks = false) }
        }
    }

    private fun updateEffects(block: (Effects) -> Effects) = extraState.update {
        when (it) {
            is HeadToHeadAddExtras.AddEnd -> it.copy(effects = block(it.effects))
            is HeadToHeadAddExtras.AddHeat -> it.copy(effects = block(it.effects))
            else -> it
        }
    }

    fun handle(action: HeadToHeadAddEndIntent) {
        fun updateState(block: (HeadToHeadAddExtras.AddEnd) -> HeadToHeadAddExtras.AddEnd) =
                extraState.update {
                    if (it !is HeadToHeadAddExtras.AddEnd) return@update it
                    block(it)
                }

        when (action) {
            is HeadToHeadAddEndIntent.HelpShowcaseAction ->
                helpShowcaseUseCase.handle(action.action, screen::class)

            is ArrowInputAction -> {
                val currentState = state.value.getData() as AddEnd
                val row = currentState.extras.set.data.find { it.type == currentState.extras.selected } ?: return
                if (row !is Arrows) return

                action.action.handle(
                        enteredArrows = row.arrows,
                        endSize = currentState.extras.set.endSize,
                        dbArrows = null,
                        setEnteredArrows = { arrows, error ->
                            updateState { s ->
                                val data = s.set.data
                                        .filter { it.type != currentState.extras.selected }
                                        .plus(row.copy(arrows = arrows))
                                s.copy(
                                        set = s.set.copy(data = data),
                                        arrowInputsError = error,
                                )
                            }
                        },
                        onSubmit = { throw NotImplementedError() },
                        helpListener = { handle(HeadToHeadAddEndIntent.HelpShowcaseAction(it)) }
                )
            }

            SightersClicked -> updateState { it.copy(openSighters = true) }
            SightersHandled -> updateState { it.copy(openSighters = false) }
            is GridRowClicked -> updateState { it.copy(selected = action.row) }
            ToggleShootOffWin -> updateState { it.copy(set = it.set.copy(isShootOffWin = !it.set.isShootOffWin)) }
            HeadToHeadAddEndIntent.SubmitClicked -> state.value.getData().let { state ->
                if (state !is AddEnd) return

                if (state.extras.set.result == HeadToHeadResult.INCOMPLETE) {
                    updateState { it.copy(incompleteError = true) }
                    return
                }

                viewModelScope.launch {
                    if (state.dbSet == null) h2hRepo.insert(*state.toDbDetails().toTypedArray())
                    else h2hRepo.update(*state.toDbDetails().toTypedArray())
                }
            }

            is GridTextValueChanged -> updateState { s ->
                val row = s.set.data.find { it.type == action.type } ?: return@updateState s
                if (row !is EditableTotal) return@updateState s

                val newData = s.set.data.minus(row).plus(row.copy(text = row.text.onTextChanged(action.text)))
                s.copy(set = s.set.copy(data = newData))
            }
        }
    }

    fun handle(action: HeadToHeadAddHeatIntent) {
        fun updateState(block: (HeadToHeadAddExtras.AddHeat) -> HeadToHeadAddExtras.AddHeat) =
                extraState.update {
                    if (it !is HeadToHeadAddExtras.AddHeat) return@update it
                    block(it)
                }

        when (action) {
            is HeadToHeadAddHeatIntent.HelpShowcaseAction ->
                helpShowcaseUseCase.handle(action.action, CodexNavRoute.HEAD_TO_HEAD_ADD::class)

            is OpponentUpdated -> updateState { it.copy(opponent = action.opponent) }

            is OpponentQualiRankUpdated ->
                updateState { it.copy(opponentQualiRank = it.opponentQualiRank.onTextChanged(action.rank)) }

            is SelectHeatDialogItemClicked ->
                updateState {
                    it.copy(heat = action.heat, showSelectHeatDialog = false, showHeatRequiredError = false)
                }

            ToggleIsBye -> updateState { it.copy(isBye = !it.isBye) }
            HeatClicked -> updateState { it.copy(showSelectHeatDialog = true) }
            CloseSelectHeatDialog -> updateState { it.copy(showSelectHeatDialog = false) }

            HeadToHeadAddHeatIntent.SubmitClicked -> state.value.getData().let { state ->
                if (state !is AddHeat) return

                if (state.extras.heat == null) {
                    updateState { it.copy(showHeatRequiredError = true) }
                    return
                }

                viewModelScope.launch { h2hRepo.insert(state.asHeadToHeadHeat(shootId)!!) }
            }
        }
    }
}
