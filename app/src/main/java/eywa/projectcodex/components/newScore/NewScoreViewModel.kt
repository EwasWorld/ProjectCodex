package eywa.projectcodex.components.newScore

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.navigation.DEFAULT_INT_NAV_ARG
import eywa.projectcodex.common.navigation.NavArgument
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogIntent
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundEnabledFilters
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsTask
import eywa.projectcodex.database.ScoresRoomDatabase
import eywa.projectcodex.database.archerRound.ArcherRoundsRepo
import eywa.projectcodex.database.arrowValue.ArrowValuesRepo
import eywa.projectcodex.database.rounds.RoundRepo
import kotlinx.coroutines.Job
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
        private val helpShowcase: HelpShowcaseUseCase,
        savedStateHandle: SavedStateHandle,
) : ViewModel() {

    var state by mutableStateOf(NewScoreState())
        private set

    private val archerRoundsRepo: ArcherRoundsRepo = ArcherRoundsRepo(db.archerRoundDao())
    private val arrowValuesRepo: ArrowValuesRepo = ArrowValuesRepo(db.arrowValueDao())

    private var editingRoundJob: Job? = null

    init {
        initialiseRoundBeingEdited(
                savedStateHandle.get<Int>(NavArgument.ARCHER_ROUND_ID.toArgName())
                        ?.takeIf { it != DEFAULT_INT_NAV_ARG }
        )

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
            is NewScoreIntent.DateChanged -> state = state.copy(dateShot = action.info.updateCalendar(state.dateShot))
            is NewScoreIntent.SelectRoundDialogAction -> handleSelectRoundDialogIntent(action.action)
            is NewScoreIntent.HelpShowcaseAction -> helpShowcase.handle(action.action, CodexNavRoute.NEW_SCORE::class)

            /*
             * Final actions
             */
            NewScoreIntent.CancelEditInfo -> state = state.copy(popBackstack = true)
            NewScoreIntent.ResetEditInfo -> state = state.resetEditInfo()
            NewScoreIntent.Submit -> {
                viewModelScope.launch {
                    if (state.isEditing) {
                        archerRoundsRepo.update(state.asArcherRound())
                        state = state.copy(popBackstack = true)
                    }
                    else {
                        val newId = archerRoundsRepo.insert(state.asArcherRound())
                        state = state.copy(navigateToInputEnd = newId.toInt())
                    }
                }
            }
            NewScoreIntent.HandleNavigate -> state = state.copy(navigateToInputEnd = null)
            NewScoreIntent.HandlePopBackstack -> state = state.copy(popBackstack = false)
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
                dateShot = roundBeingEdited.dateShot,
                selectedRound = selectedRoundFullInfo?.round,
                selectedSubtype = roundBeingEdited.roundSubTypeId?.let { subType ->
                    selectedRoundFullInfo?.roundSubTypes?.find { it.subTypeId == subType }
                },
        )
    }
}

