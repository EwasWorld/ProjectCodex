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
import eywa.projectcodex.components.shootDetails.headToHeadEnd.grid.HeadToHeadGridRowData
import eywa.projectcodex.components.shootDetails.headToHeadEnd.grid.HeadToHeadGridRowData.Arrows
import eywa.projectcodex.components.shootDetails.headToHeadEnd.grid.HeadToHeadGridRowData.EditableTotal
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.model.headToHead.FullHeadToHeadSet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HeadToHeadAddEndViewModel @Inject constructor(
        db: ScoresRoomDatabase,
        private val repo: ShootDetailsRepo,
        savedStateHandle: SavedStateHandle,
        private val helpShowcaseUseCase: HelpShowcaseUseCase,
) : ViewModel() {
    private val screen = CodexNavRoute.HEAD_TO_HEAD_ADD_END
    private val extraState = MutableStateFlow<HeadToHeadAddEndExtras?>(null)

    val state = repo.getStateNullableExtra(extraState, ::stateConverter)
            .stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(),
                    ShootDetailsResponse.Loading as ShootDetailsResponse<HeadToHeadAddEndState>,
            )

    private val h2hRepo = db.h2hRepo()
    val shootId = savedStateHandle.get<Int>(NavArgument.SHOOT_ID)!!

    private fun stateConverter(
            main: ShootDetailsState,
            extras: HeadToHeadAddEndExtras?,
    ): HeadToHeadAddEndState {
        val shoot = main.fullShootInfo!!
        val fullH2hInfo = shoot.h2h ?: throw ShootDetailsError()

        val common = HeadToHeadRoundInfo(
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
            extraState.update { (it ?: HeadToHeadAddEndExtras()).copy(openAddHeatScreen = true) }
            return HeadToHeadAddEndState(extras = extras ?: HeadToHeadAddEndExtras())
        }

        if (heat.result() != HeadToHeadResult.INCOMPLETE) {
            extraState.update { (it ?: HeadToHeadAddEndExtras()).copy(openAddHeatScreen = true) }
            return HeadToHeadAddEndState(extras = extras ?: HeadToHeadAddEndExtras())
        }

        val scores = heat.runningTotals.lastOrNull()?.left
        val lastSet = heat.sets.maxByOrNull { it.setNumber }
        if (lastSet == null || lastSet.result != HeadToHeadResult.INCOMPLETE) {
            val setNumber = (lastSet?.setNumber?.plus(1)) ?: 1
            val isShootOff = HeadToHeadUseCase.shootOffSet(teamSize) == setNumber
            val endSize = HeadToHeadUseCase.endSize(teamSize, isShootOff)

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
                    isShootOff = isShootOff,
                    teamSize = fullH2hInfo.headToHead.teamSize,
                    isShootOffWin = false,
                    isRecurveStyle = fullH2hInfo.headToHead.isRecurveStyle,
            )

            if (extras == null || extras.set.setNumber <= (lastSet?.setNumber ?: 0)) {
                extraState.update { HeadToHeadAddEndExtras(set = set, selected = SELF) }
            }
            return HeadToHeadAddEndState(
                    headToHeadRoundInfo = common,
                    extras = extras ?: HeadToHeadAddEndExtras(),
                    heat = heat.heat,
                    isRecurveStyle = fullH2hInfo.headToHead.isRecurveStyle,
                    teamRunningTotal = scores?.first ?: 0,
                    opponentRunningTotal = scores?.second ?: 0,
            )
        }

        if (extras == null || extras.set.setNumber != lastSet.setNumber) {
            extraState.update {
                HeadToHeadAddEndExtras(
                        set = lastSet,
                        selected = lastSet
                                .data
                                .sortedBy { it.type.ordinal }
                                .first { !it.isComplete }
                                .type,
                )
            }
        }
        return HeadToHeadAddEndState(
                headToHeadRoundInfo = common,
                extras = extras ?: HeadToHeadAddEndExtras(),
                heat = heat.heat,
                isRecurveStyle = fullH2hInfo.headToHead.isRecurveStyle,
                teamRunningTotal = scores?.first ?: 0,
                opponentRunningTotal = scores?.second ?: 0,
        )
    }

    fun handle(action: HeadToHeadAddEndIntent) {
        fun updateState(block: (HeadToHeadAddEndExtras) -> HeadToHeadAddEndExtras) =
                extraState.update {
                    if (it !is HeadToHeadAddEndExtras) return@update it
                    block(it)
                }

        when (action) {
            is HelpShowcaseAction -> helpShowcaseUseCase.handle(action.action, screen::class)

            is ShootDetailsAction -> repo.handle(action.action, screen)

            EditSightMarkClicked -> updateState { it.copy(openEditSightMark = true) }
            ExpandSightMarkClicked -> updateState { it.copy(openAllSightMarks = true) }
            EditSightMarkHandled -> updateState { it.copy(openEditSightMark = false) }
            ExpandSightMarkHandled -> updateState { it.copy(openAllSightMarks = false) }
            OpenAddHeatScreenHandled -> updateState { it.copy(openAddHeatScreen = false) }
            is ArrowInputsErrorHandled ->
                updateState { it.copy(arrowInputsError = it.arrowInputsError.minus(action.error)) }

            is ArrowInputAction -> {
                val currentState = state.value.getData() ?: return
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
                                        arrowInputsError = error?.let { s.arrowInputsError.plus(error) }
                                                ?: s.arrowInputsError,
                                )
                            }
                        },
                        onSubmit = { throw NotImplementedError() },
                        helpListener = { handle(HelpShowcaseAction(it)) }
                )
            }

            SightersClicked -> updateState { it.copy(openSighters = true) }
            SightersHandled -> updateState { it.copy(openSighters = false) }
            is GridRowClicked -> updateState { it.copy(selected = action.row) }
            ToggleShootOffWin -> updateState { it.copy(set = it.set.copy(isShootOffWin = !it.set.isShootOffWin)) }
            SubmitClicked -> state.value.getData().let { state ->
                if (state == null) {
                    return
                }

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
}
