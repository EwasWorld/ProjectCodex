package eywa.projectcodex.components.newScore

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.navigation.DEFAULT_INT_NAV_ARG
import eywa.projectcodex.common.navigation.NavArgument
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogIntent
import eywa.projectcodex.common.sharedUi.selectRoundFaceDialog.SelectRoundFaceDialogIntent
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsTask
import eywa.projectcodex.components.newScore.NewScoreIntent.*
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.rounds.RoundRepo
import eywa.projectcodex.model.FullArcherRoundInfo
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

/**
 * @see ArcherRoundScoreViewModel
 */
@HiltViewModel
class NewScoreViewModel @Inject constructor(
        val db: ScoresRoomDatabase,
        updateDefaultRoundsTask: UpdateDefaultRoundsTask,
        private val helpShowcase: HelpShowcaseUseCase,
        savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _state = MutableStateFlow(NewScoreState())
    val state = _state.asStateFlow()

    private val archerRoundsRepo = db.archerRoundsRepo()

    private var editingRoundJob: Job? = null

    init {
        initialiseRoundBeingEdited(
                savedStateHandle.get<Int>(NavArgument.ARCHER_ROUND_ID.toArgName())
                        ?.takeIf { it != DEFAULT_INT_NAV_ARG }
        )

        val roundRepo = RoundRepo(db)
        viewModelScope.launch {
            roundRepo.fullRoundsInfo.collect { data ->
                handle(SelectRoundDialogAction(SelectRoundDialogIntent.SetRounds(data)))
            }
        }
        viewModelScope.launch {
            updateDefaultRoundsTask.state.collect { updateState ->
                _state.update { it.copy(updateDefaultRoundsState = updateState).resetEditInfo() }
            }
        }
    }

    private fun initialiseRoundBeingEdited(roundBeingEditedId: Int?) {
        if (roundBeingEditedId == null) {
            _state.update { it.copy(roundBeingEdited = null, roundBeingEditedArrowsShot = null) }
            return
        }

        editingRoundJob?.cancel()
        editingRoundJob = viewModelScope.launch {
            archerRoundsRepo.getFullArcherRoundInfo(roundBeingEditedId)
                    .collect { info ->
                        _state.update {
                            if (info == null) return@update it.copy(roundNotFoundError = true)
                            it.copy(
                                    roundBeingEdited = FullArcherRoundInfo(info, true),
                                    roundBeingEditedArrowsShot = info.arrows.orEmpty().count()
                            ).resetEditInfo()
                        }
                    }
        }
    }

    fun handle(action: NewScoreIntent) {
        when (action) {
            is DateChanged ->
                _state.update { it.copy(dateShot = action.info.updateCalendar(it.dateShot)) }
            is SelectRoundDialogAction -> {
                _state.update {
                    val (selectRoundDialogState, faceIntent) = action.action.handle(it.selectRoundDialogState)
                    val selectFaceDialogState = faceIntent?.handle(it.selectFaceDialogState)
                            ?: it.selectFaceDialogState
                    val newState = it.copy(
                            selectRoundDialogState = selectRoundDialogState,
                            selectFaceDialogState = selectFaceDialogState,
                    )
                    if (action.action is SelectRoundDialogIntent.SetRounds) newState.resetEditInfo()
                    else newState
                }
            }
            is SelectFaceDialogAction ->
                _state.update { it.copy(selectFaceDialogState = action.action.handle(it.selectFaceDialogState)) }
            is HelpShowcaseAction -> helpShowcase.handle(action.action, CodexNavRoute.NEW_SCORE::class)

            /*
             * Final actions
             */
            CancelEditInfo -> _state.update { it.copy(popBackstack = true) }
            ResetEditInfo -> _state.update { it.resetEditInfo() }
            Submit -> {
                val currentState = state.value
                viewModelScope.launch {
                    if (currentState.isEditing) {
                        archerRoundsRepo.update(
                                original = currentState.roundBeingEdited!!,
                                shoot = currentState.asArcherRound(),
                                shootRound = currentState.asShootRound(),
                                shootDetail = currentState.asShootDetail(),
                        )
                        _state.update { it.copy(popBackstack = true) }
                    }
                    else {
                        val newId = archerRoundsRepo.insert(
                                shoot = currentState.asArcherRound(),
                                shootRound = currentState.asShootRound(),
                                shootDetail = currentState.asShootDetail(),
                        )
                        _state.update { it.copy(navigateToInputEnd = newId.toInt()) }
                    }
                }
            }
            HandleNavigate -> _state.update { it.copy(navigateToInputEnd = null) }
            HandlePopBackstack -> _state.update { it.copy(popBackstack = false) }
        }
    }

    private fun NewScoreState.resetEditInfo(): NewScoreState {
        if (roundBeingEdited == null) return this
        val roundsState = selectRoundDialogState.copy(
                selectedRoundId = roundBeingEdited.shootRound?.roundId,
                selectedSubTypeId = roundBeingEdited.shootRound?.roundSubTypeId,
        )
        val faceAction =
                if (roundsState.selectedRound == null) SelectRoundFaceDialogIntent.SetNoRound
                else SelectRoundFaceDialogIntent.SetRound(
                        roundsState.selectedRound!!.round,
                        roundsState.roundSubTypeDistances!!,
                )
        // Sometimes roundsState.allRounds loads late so need ignore multi-faces if selected round doesn't come up yet
        val faces =
                if (roundsState.selectedRound != null) roundBeingEdited.faces
                else roundBeingEdited.faces?.firstOrNull()?.let { listOf(it) }

        return copy(
                dateShot = roundBeingEdited.shoot.dateShot,
                selectRoundDialogState = roundsState,
                selectFaceDialogState = faceAction.handle(selectFaceDialogState)
                        .copy(selectedFaces = faces),
        )
    }
}

