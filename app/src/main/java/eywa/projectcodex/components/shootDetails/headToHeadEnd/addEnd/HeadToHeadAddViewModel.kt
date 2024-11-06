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
import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadArcherType
import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadResult
import eywa.projectcodex.components.shootDetails.headToHeadEnd.addEnd.HeadToHeadAddEndIntent.*
import eywa.projectcodex.components.shootDetails.headToHeadEnd.addEnd.HeadToHeadAddHeatIntent.*
import eywa.projectcodex.components.shootDetails.headToHeadEnd.addEnd.HeadToHeadAddIntent.*
import eywa.projectcodex.components.shootDetails.headToHeadEnd.addEnd.HeadToHeadAddState.*
import eywa.projectcodex.components.shootDetails.headToHeadEnd.grid.HeadToHeadGridRowData.Arrows
import eywa.projectcodex.components.shootDetails.headToHeadEnd.grid.HeadToHeadGridRowData.EditableTotal
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.model.FullHeadToHeadSet
import eywa.projectcodex.model.FullShootInfo
import eywa.projectcodex.model.SightMark
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HeadToHeadAddViewModel @Inject constructor(
        private val db: ScoresRoomDatabase,
        savedStateHandle: SavedStateHandle,
        private val helpShowcaseUseCase: HelpShowcaseUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow<HeadToHeadAddState>(Loading())
    val state = _state.asStateFlow()

    private val h2hRepo = db.h2hRepo()
    private val shootId = savedStateHandle.get<Int>(NavArgument.SHOOT_ID)!!

    init {
        viewModelScope.launch {
            h2hRepo.get(shootId).collectLatest { fullH2hInfo ->
                val heat = fullH2hInfo.heats.minByOrNull { it.heat.heat }
                if (heat == null) {
                    _state.update {
                        AddHeat(roundCommon = it.roundCommon, effects = it.effects)
                    }
                    return@collectLatest
                }

                val scores = heat.results.last()
                if (heat.isComplete()) {
                    _state.update {
                        AddHeat(
                                roundCommon = it.roundCommon,
                                effects = it.effects,
                                previousHeat = AddHeat.PreviousHeat(
                                        heat = heat.heat.heat,
                                        result = heat.sets.last().result,
                                        teamRunningTotal = scores!!.first,
                                        opponentRunningTotal = scores.second,
                                ),
                        )
                    }
                    return@collectLatest
                }

                val lastSet = heat.sets.maxByOrNull { it.setNumber }
                if (lastSet == null || lastSet.result != HeadToHeadResult.INCOMPLETE) {
                    val defaultData = if (fullH2hInfo.headToHead.teamSize == 1) {
                        listOf(
                                Arrows(type = HeadToHeadArcherType.SELF, expectedArrowCount = 0),
                                EditableTotal(type = HeadToHeadArcherType.OPPONENT, expectedArrowCount = 0),
                        )
                    }
                    else {
                        listOf(
                                Arrows(type = HeadToHeadArcherType.SELF, expectedArrowCount = 0),
                                EditableTotal(type = HeadToHeadArcherType.TEAM, expectedArrowCount = 0),
                                EditableTotal(type = HeadToHeadArcherType.OPPONENT, expectedArrowCount = 0),
                        )
                    }

                    _state.update { s ->
                        val setNumber = (lastSet?.setNumber?.plus(1)) ?: 1
                        val isShootOff = HeadToHeadUseCase.shootOffSet(fullH2hInfo.headToHead.teamSize) == setNumber
                        val endSize = if (isShootOff) 1 else HeadToHeadUseCase.END_SIZE

                        val set = lastSet ?: FullHeadToHeadSet(
                                setNumber = setNumber,
                                data = defaultData.map {
                                    it.setEditableTotal(teamSize = fullH2hInfo.headToHead.teamSize, endSize = endSize)
                                },
                                isShootOff = HeadToHeadUseCase.shootOffSet(fullH2hInfo.headToHead.teamSize) == setNumber,
                                teamSize = fullH2hInfo.headToHead.teamSize,
                                isShootOffWin = false,
                        )

                        AddEnd(
                                roundCommon = s.roundCommon,
                                effects = s.effects,
                                heat = heat.heat,
                                isRecurveStyle = fullH2hInfo.headToHead.isRecurveStyle,
                                teamRunningTotal = scores?.first ?: 0,
                                opponentRunningTotal = scores?.second ?: 0,
                                set = set,
                                selected = HeadToHeadArcherType.SELF,
                        )
                    }
                    return@collectLatest
                }

                _state.update { s ->
                    AddEnd(
                            roundCommon = s.roundCommon,
                            effects = s.effects,
                            heat = heat.heat,
                            isRecurveStyle = fullH2hInfo.headToHead.isRecurveStyle,
                            teamRunningTotal = scores?.first ?: 0,
                            opponentRunningTotal = scores?.second ?: 0,
                            set = lastSet,
                            selected = lastSet
                                    .data
                                    .sortedBy { it.type.ordinal }
                                    .first { !it.isComplete }
                                    .type,
                    )
                }
            }
        }

        viewModelScope.launch {
            db.shootsRepo().getFullShootInfo(shootId)
                    .flatMapLatest { dbShootInfo ->
                        val shoot = dbShootInfo?.let { FullShootInfo(it, true) }

                        val common = RoundCommon(
                                round = shoot?.fullRoundInfo?.round,
                                face = shoot?.faces?.first(),
                                distance = shoot?.fullRoundInfo?.roundDistances?.maxOfOrNull { it.distance }
                                        ?: shoot?.shootDetail?.distance,
                                sightMark = null,
                                isMetric = shoot?.fullRoundInfo?.round?.isMetric
                                        ?: shoot?.shootDetail?.isDistanceInMeters,
                        )

                        if (common.distance != null && common.isMetric != null) {
                            db.sightMarkRepo().getSightMarkForDistance(common.distance, common.isMetric)
                                    .map { common.copy(sightMark = it?.let { SightMark(it) }) }
                        }
                        else {
                            flowOf()
                        }
                    }
                    .collectLatest { common ->
                        _state.update {
                            when (it) {
                                is AddEnd -> it.copy(roundCommon = common)
                                is AddHeat -> it.copy(roundCommon = common)
                                is Loading -> Loading(roundCommon = common)
                            }
                        }
                    }
        }
    }

    fun handle(action: HeadToHeadAddIntent) {
        when (action) {
            is HeadToHeadAddIntent.HelpShowcaseAction ->
                helpShowcaseUseCase.handle(action.action, CodexNavRoute.HEAD_TO_HEAD_ADD_END::class)

            is AddEndAction -> handle(action.action)
            is AddHeatAction -> handle(action.action)

            EditSightMarkClicked -> updateEffects { it.copy(openEditSightMark = true) }
            ExpandSightMarkClicked -> updateEffects { it.copy(openAllSightMarks = true) }
            EditSightMarkHandled -> updateEffects { it.copy(openEditSightMark = false) }
            ExpandSightMarkHandled -> updateEffects { it.copy(openAllSightMarks = false) }
        }
    }

    private fun updateEffects(block: (Effects) -> Effects) {
        _state.update {
            when (it) {
                is AddEnd -> it.copy(effects = block(it.effects))
                is AddHeat -> it.copy(effects = block(it.effects))
                else -> Loading(effects = block(it.effects))
            }
        }
    }

    fun handle(action: HeadToHeadAddEndIntent) {
        fun updateState(block: (AddEnd) -> AddEnd) = _state.update { if (it !is AddEnd) it else block(it) }

        when (action) {
            is HeadToHeadAddEndIntent.HelpShowcaseAction ->
                helpShowcaseUseCase.handle(action.action, CodexNavRoute.HEAD_TO_HEAD_ADD_END::class)

            is ArrowInputAction -> {
                val currentState = state.value as AddEnd
                val row = currentState.set.data.find { it.type == currentState.selected } ?: return
                if (row !is Arrows) return

                action.action.handle(
                        enteredArrows = row.arrows,
                        endSize = currentState.set.endSize,
                        dbArrows = null,
                        setEnteredArrows = { arrows, error ->
                            updateState { s ->
                                val data = s.set.data
                                        .filter { it.type != currentState.selected }
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
            HeadToHeadAddEndIntent.SubmitClicked -> state.value.let { state ->
                if (state !is AddEnd) return

                if (state.set.result == HeadToHeadResult.INCOMPLETE) {
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
        fun updateState(block: (AddHeat) -> AddHeat) = _state.update { if (it !is AddHeat) it else block(it) }

        when (action) {
            is HeadToHeadAddHeatIntent.HelpShowcaseAction ->
                helpShowcaseUseCase.handle(action.action, CodexNavRoute.HEAD_TO_HEAD_ADD_END::class)

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

            HeadToHeadAddHeatIntent.SubmitClicked -> state.value.let { state ->
                if (state !is AddHeat) return

                if (state.heat == null) {
                    updateState { it.copy(showHeatRequiredError = true) }
                    return
                }

                viewModelScope.launch { h2hRepo.insert(state.asHeadToHeadHeat(shootId)!!) }
            }
        }
    }
}
