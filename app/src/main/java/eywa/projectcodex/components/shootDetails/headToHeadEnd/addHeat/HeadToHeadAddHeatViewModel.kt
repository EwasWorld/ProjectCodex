package eywa.projectcodex.components.shootDetails.headToHeadEnd.addHeat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.navigation.NavArgument
import eywa.projectcodex.common.navigation.get
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

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
    private val heatId = savedStateHandle.get<Int>(NavArgument.HEAT_ID)

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

        val editingHeat = fullH2hInfo.heats.find { it.heat.heat == heatId }?.heat
        val heat = fullH2hInfo.heats.minByOrNull { it.heat.heat }.takeIf { editingHeat == null }
        var existingHeats = fullH2hInfo.heats.map { it.heat.heat }

        // Edit heat from database
        if (heatId != null && editingHeat != null) {
            existingHeats = existingHeats.minus(editingHeat.heat)
            if (extraState.value == null) {
                extraState.update { (it ?: HeadToHeadAddHeatExtras()).resetEditInfo(editingHeat) }
            }
        }
        // Create new heat
        else if (heat == null || heatId != null) {
            if (extraState.value == null) {
                extraState.update { HeadToHeadAddHeatExtras(heat = heatId?.coerceAtLeast(0)) }
            }
        }
        // Create next new heat
        else if (heat.isComplete && !heat.heat.isBye) {
            if (extraState.value == null || heat.heat.heat == extraState.value?.heat) {
                extraState.update { HeadToHeadAddHeatExtras(heat = (heat.heat.heat - 1).coerceAtLeast(0)) }
            }
        }
        // Previous heat not completed, jump to add end screen
        else {
            extraState.update { (it ?: HeadToHeadAddHeatExtras()).copy(openAddEndScreen = true) }
        }

        return HeadToHeadAddHeatState(
                roundInfo = roundInfo,
                extras = extras ?: HeadToHeadAddHeatExtras(),
                previousHeat = heat?.let {
                    HeadToHeadAddHeatState.PreviousHeat(
                            heat = it.heat.heat,
                            result = it.sets.lastOrNull()?.result ?: HeadToHeadResult.INCOMPLETE,
                            runningTotal = heat.runningTotals.lastOrNull()?.left,
                    )
                },
                editing = editingHeat,
                existingHeats = existingHeats,
        )
    }

    private fun HeadToHeadAddHeatExtras.resetEditInfo(editing: DatabaseHeadToHeadHeat) = HeadToHeadAddHeatExtras(
            heat = editing.heat,
            opponent = editing.opponent ?: "",
            opponentQualiRank = opponentQualiRank.onTextChanged(editing.opponentQualificationRank?.toString() ?: ""),
            isBye = editing.isBye,
    )

    fun handle(action: HeadToHeadAddHeatIntent) {
        fun updateState(block: (HeadToHeadAddHeatExtras) -> HeadToHeadAddHeatExtras) =
                extraState.update { block(it!!) }

        when (action) {
            is ShootDetailsAction -> repo.handle(action.action, screen)

            EditSightMarkClicked -> updateState { it.copy(openEditSightMark = true) }
            ExpandSightMarkClicked -> updateState { it.copy(openAllSightMarks = true) }
            EditSightMarkHandled -> updateState { it.copy(openEditSightMark = false) }
            ExpandSightMarkHandled -> updateState { it.copy(openAllSightMarks = false) }
            OpenAddEndScreenHandled -> updateState { it.copy(openAddEndScreen = false) }

            is HelpShowcaseAction -> helpShowcaseUseCase.handle(action.action, screen::class)

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

            SubmitClicked -> state.value.getData()?.let { state ->
                if (state.extras.heat == null || state.extras.heat in state.existingHeats) {
                    updateState { it.copy(showHeatRequiredError = state.extras.heat == null) }
                    return
                }

                val newHeat = state.asHeadToHeadHeat(shootId)!!
                viewModelScope.launch {
                    if (state.editing != null) {
                        if (state.editing.heat == state.extras.heat) h2hRepo.update(newHeat)
                        else h2hRepo.updateWithHeatIdChange(state.editing.heat, newHeat)
                    }
                    else {
                        h2hRepo.insert(newHeat)
                    }
                }
            }

            DeleteClicked -> state.value.getData()?.let { state ->
                if (state.editing == null) return
                viewModelScope.launch {
                    h2hRepo.delete(shootId = state.editing.shootId, heatId = state.editing.heat)
                }
            }

            ResetClicked -> updateState { s ->
                state.value.getData()?.editing?.let { s.resetEditInfo(it) } ?: s
            }
        }
    }
}
