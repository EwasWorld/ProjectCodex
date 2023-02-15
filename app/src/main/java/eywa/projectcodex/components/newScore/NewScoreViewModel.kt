package eywa.projectcodex.components.newScore

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.common.helpShowcase.HelpShowcase
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogIntent
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundEnabledFilters
import eywa.projectcodex.common.utils.asCalendar
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsTask
import eywa.projectcodex.components.newScore.NewScoreEffect.PopBackstack
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.archerRound.ArcherRoundsRepo
import eywa.projectcodex.database.arrowValue.ArrowValuesRepo
import eywa.projectcodex.database.rounds.RoundRepo
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
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
        private val helpShowcase: HelpShowcase,
) : ViewModel() {

    var state by mutableStateOf(NewScoreState())
        private set

    private val _effects: MutableSharedFlow<NewScoreEffect> =
            MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val effects: Flow<NewScoreEffect> = _effects

    private val archerRoundsRepo: ArcherRoundsRepo = ArcherRoundsRepo(db.archerRoundDao())
    private val arrowValuesRepo: ArrowValuesRepo = ArrowValuesRepo(db.arrowValueDao())

    private var editingRoundJob: Job? = null

    init {
        val roundRepo = RoundRepo(db)

        viewModelScope.launch {
            roundRepo.fullRoundsInfo.collect { state = state.copy(roundsData = it).resetEditInfo() }
        }
        viewModelScope.launch {
            updateDefaultRoundsTask.state.collect {
                state = state.copy(updateDefaultRoundsState = it).resetEditInfo()
            }
        }
    }

    private fun initialiseRoundBeingEdited(roundBeingEditedId: Int?) {
        if (roundBeingEditedId == null) {
            state = state.copy(
                    roundBeingEdited = null,
                    roundBeingEditedArrowsShot = null,
            )
            return
        }
        if (roundBeingEditedId == state.roundBeingEdited?.archerRoundId) return

        editingRoundJob?.cancel()
        editingRoundJob = viewModelScope.launch {
            archerRoundsRepo.getArcherRound(roundBeingEditedId).asFlow()
                    .combine(arrowValuesRepo.getArrowValuesForRound(roundBeingEditedId).asFlow()) { a, b -> a to b }
                    .collect { (archerRound, arrowValues) ->
                        state = state.copy(
                                roundBeingEdited = archerRound,
                                roundBeingEditedArrowsShot = arrowValues.count()
                        ).resetEditInfo()
                    }
        }
    }

    fun handle(action: NewScoreIntent) {
        when (action) {
            is NewScoreIntent.Initialise -> initialiseRoundBeingEdited(action.roundBeingEditedId)
            is NewScoreIntent.DateChanged -> state = state.copy(dateShot = action.info.updateCalendar(state.dateShot))
            is NewScoreIntent.SelectRoundDialogAction -> handleSelectRoundDialogIntent(action.action)
            is NewScoreIntent.HelpShowcaseAction -> helpShowcase.handle(action.action, NewScoreFragment::class)

            /*
             * Final actions
             */
            NewScoreIntent.CancelEditInfo -> viewModelScope.launch { _effects.emit(PopBackstack) }
            NewScoreIntent.ResetEditInfo -> state = state.resetEditInfo()
            NewScoreIntent.Submit -> {
                viewModelScope.launch {
                    if (state.isEditing) {
                        archerRoundsRepo.update(state.asArcherRound())
                        _effects.emit(PopBackstack)
                    }
                    else {
                        val newId = archerRoundsRepo.insert(state.asArcherRound())
                        _effects.emit(NewScoreEffect.NavigateToInputEnd(newId.toInt()))
                    }
                }
            }
        }
    }

    private fun handleSelectRoundDialogIntent(action: SelectRoundDialogIntent) {
        when (action) {
            /*
             * Select round dialog
             */
            SelectRoundDialogIntent.OpenRoundSelectDialog -> {
                state = state.copy(
                        isSelectRoundDialogOpen = true,
                        enabledRoundFilters = SelectRoundEnabledFilters(),
                )
            }
            SelectRoundDialogIntent.CloseRoundSelectDialog -> {
                state = state.copy(isSelectRoundDialogOpen = false)
            }
            SelectRoundDialogIntent.NoRoundSelected -> {
                state = state.copy(
                        isSelectRoundDialogOpen = false,
                        selectedRound = null,
                        selectedSubtype = null,
                )
            }
            is SelectRoundDialogIntent.RoundSelected -> {
                state = state.copy(
                        isSelectRoundDialogOpen = false,
                        selectedRound = action.round,
                ).let {
                    // Select the furthest distance if subtypes are available
                    it.copy(selectedSubtype = it.selectedRoundInfo?.roundSubTypes?.maxByOrNull { subType ->
                        state.getFurthestDistance(subType).distance
                    })
                }
            }
            is SelectRoundDialogIntent.SelectRoundDialogFilterClicked -> {
                state = state.copy(enabledRoundFilters = state.enabledRoundFilters.toggle(action.filter))
            }
            SelectRoundDialogIntent.SelectRoundDialogClearFilters -> {
                state = state.copy(enabledRoundFilters = SelectRoundEnabledFilters())
            }

            /*
             * Select sub type dialog
             */
            SelectRoundDialogIntent.OpenSubTypeSelectDialog -> {
                state = state.copy(isSelectSubTypeDialogOpen = true)
            }
            SelectRoundDialogIntent.CloseSubTypeSelectDialog -> {
                state = state.copy(isSelectSubTypeDialogOpen = false)
            }
            is SelectRoundDialogIntent.SubTypeSelected -> {
                state = state.copy(
                        isSelectSubTypeDialogOpen = false,
                        selectedSubtype = action.subType,
                )
            }
        }
    }

    private fun NewScoreState.resetEditInfo(): NewScoreState {
        if (roundBeingEdited == null) return this

        val selectedRoundFullInfo = roundsData?.find { it.round.roundId == roundBeingEdited.roundId }
        return copy(
                dateShot = roundBeingEdited.dateShot.asCalendar(),
                selectedRound = selectedRoundFullInfo?.round,
                selectedSubtype = roundBeingEdited.roundSubTypeId?.let { subType ->
                    selectedRoundFullInfo?.roundSubTypes?.find { it.subTypeId == subType }
                },
        )
    }
}

