package eywa.projectcodex.components.newScore

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import eywa.projectcodex.R
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.common.utils.UpdateDefaultRounds
import eywa.projectcodex.components.app.App
import eywa.projectcodex.components.archerRoundScore.ArcherRoundScoreViewModel
import eywa.projectcodex.components.newScore.NewScoreEffect.PopBackstack
import eywa.projectcodex.components.newScore.helpers.NewScoreRoundEnabledFilters
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.archerRound.ArcherRoundsRepo
import eywa.projectcodex.database.arrowValue.ArrowValuesRepo
import eywa.projectcodex.database.rounds.*
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
class NewScoreViewModel(application: Application) : AndroidViewModel(application) {
    @Inject
    lateinit var db: ScoresRoomDatabase

    var state by mutableStateOf(NewScoreState())
        private set

    private val _effects: MutableSharedFlow<NewScoreEffect> =
            MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val effects: Flow<NewScoreEffect> = _effects

    init {
        (application as App).appComponent.inject(this)
    }

    private val archerRoundsRepo: ArcherRoundsRepo = ArcherRoundsRepo(db.archerRoundDao())
    private val arrowValuesRepo: ArrowValuesRepo = ArrowValuesRepo(db.arrowValueDao())

    private var editingRoundJob: Job? = null

    init {
        val roundRepo = RoundRepo(db)

        viewModelScope.launch {
            roundRepo.rounds.asFlow()
                    .combine(roundRepo.roundSubTypes.asFlow()) { rounds, subTypes ->
                        NewScoreDbData(rounds = rounds, subTypes = subTypes)
                    }
                    .combine(roundRepo.roundArrowCounts.asFlow()) { flowData, arrowCounts ->
                        flowData.copy(arrowCounts = arrowCounts)
                    }
                    .combine(roundRepo.roundDistances.asFlow()) { flowData, distances ->
                        flowData.copy(distances = distances)
                    }
                    .collect { state = state.copy(roundsData = it).resetEditInfo() }
        }
        viewModelScope.launch {
            UpdateDefaultRounds.taskProgress.getState().asFlow()
                    .combine(UpdateDefaultRounds.taskProgress.getMessage().asFlow()) { a, b -> a to b }
                    .collect { (updateState, message) ->
                        state = state.copy(
                                databaseUpdatingProgress = updateState == UpdateDefaultRounds.UpdateTaskState.IN_PROGRESS,
                                databaseUpdatingMessage = when {
                                    message != null -> ResOrActual.fromActual(message)
                                    updateState == UpdateDefaultRounds.UpdateTaskState.IN_PROGRESS -> {
                                        ResOrActual.fromRes(R.string.about__update_default_rounds_in_progress)
                                    }
                                    else -> null
                                }
                        ).resetEditInfo()
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

            /*
             * Select round dialog
             */
            NewScoreIntent.OpenRoundSelectDialog -> {
                state = state.copy(
                        isSelectRoundDialogOpen = true,
                        enabledRoundFilters = NewScoreRoundEnabledFilters(),
                )
            }
            NewScoreIntent.CloseRoundSelectDialog -> {
                state = state.copy(isSelectRoundDialogOpen = false)
            }
            NewScoreIntent.NoRoundSelected -> {
                state = state.copy(
                        isSelectRoundDialogOpen = false,
                        selectedRound = null,
                        selectedSubtype = null,
                )
            }
            is NewScoreIntent.RoundSelected -> {
                state = state.copy(
                        isSelectRoundDialogOpen = false,
                        selectedRound = action.round,
                ).let {
                    // Select the furthest distance if subtypes are available
                    it.copy(selectedSubtype = it.roundSubTypes.maxByOrNull { subType ->
                        state.getFurthestDistance(subType).distance
                    })
                }
            }
            is NewScoreIntent.SelectRoundDialogFilterClicked -> {
                state = state.copy(enabledRoundFilters = state.enabledRoundFilters.toggle(action.filter))
            }
            NewScoreIntent.SelectRoundDialogClearFilters -> {
                state = state.copy(enabledRoundFilters = NewScoreRoundEnabledFilters())
            }

            /*
             * Select sub type dialog
             */
            NewScoreIntent.OpenSubTypeSelectDialog -> {
                state = state.copy(isSelectSubTypeDialogOpen = true)
            }
            NewScoreIntent.CloseSubTypeSelectDialog -> {
                state = state.copy(isSelectSubTypeDialogOpen = false)
            }
            is NewScoreIntent.SubTypeSelected -> {
                state = state.copy(
                        isSelectSubTypeDialogOpen = false,
                        selectedSubtype = action.subType,
                )
            }

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

    private fun NewScoreState.resetEditInfo(): NewScoreState {
        if (roundBeingEdited == null) return this

        return copy(
                dateShot = Calendar.Builder().setInstant(roundBeingEdited.dateShot).build(),
                selectedRound = roundsData.rounds?.find { it.roundId == roundBeingEdited.roundId },
                selectedSubtype = roundBeingEdited.roundSubTypeId?.let { subType ->
                    roundsData.subTypes?.find { it.roundId == roundBeingEdited.roundId && it.subTypeId == subType }
                },
        )
    }
}

data class NewScoreDbData(
        val rounds: List<Round>? = null,
        val subTypes: List<RoundSubType>? = null,
        val arrowCounts: List<RoundArrowCount>? = null,
        val distances: List<RoundDistance>? = null,
)
