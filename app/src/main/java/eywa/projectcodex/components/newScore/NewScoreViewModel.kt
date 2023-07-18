package eywa.projectcodex.components.newScore

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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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
            roundRepo.fullRoundsInfo.collect { data ->
                _state.update { it.copy(roundsData = data).resetEditInfo() }
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
            archerRoundsRepo.getArcherRound(roundBeingEditedId).asFlow()
                    .combine(arrowValuesRepo.getArrowValuesForRound(roundBeingEditedId).asFlow()) { a, b -> a to b }
                    .collect { (archerRound, arrowValues) ->
                        _state.update {
                            it.copy(
                                    roundBeingEdited = archerRound,
                                    roundBeingEditedArrowsShot = arrowValues.count()
                            ).resetEditInfo()
                        }
                    }
        }
    }

    fun handle(action: NewScoreIntent) {
        when (action) {
            is NewScoreIntent.DateChanged ->
                _state.update { it.copy(dateShot = action.info.updateCalendar(it.dateShot)) }
            is NewScoreIntent.SelectRoundDialogAction -> handleSelectRoundDialogIntent(action.action)
            is NewScoreIntent.HelpShowcaseAction -> helpShowcase.handle(action.action, CodexNavRoute.NEW_SCORE::class)

            /*
             * Final actions
             */
            NewScoreIntent.CancelEditInfo -> _state.update { it.copy(popBackstack = true) }
            NewScoreIntent.ResetEditInfo -> _state.update { it.resetEditInfo() }
            NewScoreIntent.Submit -> {
                val currentState = state.value
                viewModelScope.launch {
                    if (currentState.isEditing) {
                        archerRoundsRepo.update(currentState.asArcherRound())
                        _state.update { it.copy(popBackstack = true) }
                    }
                    else {
                        val newId = archerRoundsRepo.insert(currentState.asArcherRound())
                        _state.update { it.copy(navigateToInputEnd = newId.toInt()) }
                    }
                }
            }
            NewScoreIntent.HandleNavigate -> _state.update { it.copy(navigateToInputEnd = null) }
            NewScoreIntent.HandlePopBackstack -> _state.update { it.copy(popBackstack = false) }
        }
    }

    private fun handleSelectRoundDialogIntent(action: SelectRoundDialogIntent) {
        when (action) {
            /*
             * Select round dialog
             */
            SelectRoundDialogIntent.OpenRoundSelectDialog ->
                _state.update {
                    it.copy(
                            isSelectRoundDialogOpen = true,
                            enabledRoundFilters = SelectRoundEnabledFilters(),
                    )
                }
            SelectRoundDialogIntent.CloseRoundSelectDialog -> _state.update { it.copy(isSelectRoundDialogOpen = false) }
            SelectRoundDialogIntent.NoRoundSelected ->
                _state.update {
                    it.copy(
                            isSelectRoundDialogOpen = false,
                            selectedRound = null,
                            selectedSubtype = null,
                    )
                }
            is SelectRoundDialogIntent.RoundSelected ->
                _state.update {
                    val new = it.copy(isSelectRoundDialogOpen = false, selectedRound = action.round)
                    // Select the furthest distance if subtypes are available
                    new.copy(
                            selectedSubtype = new.selectedRoundInfo?.roundSubTypes?.maxByOrNull { subType ->
                                new.getFurthestDistance(subType).distance
                            }
                    )
                }
            is SelectRoundDialogIntent.SelectRoundDialogFilterClicked ->
                _state.update { it.copy(enabledRoundFilters = it.enabledRoundFilters.toggle(action.filter)) }
            SelectRoundDialogIntent.SelectRoundDialogClearFilters ->
                _state.update { it.copy(enabledRoundFilters = SelectRoundEnabledFilters()) }

            /*
             * Select sub type dialog
             */
            SelectRoundDialogIntent.OpenSubTypeSelectDialog ->
                _state.update { it.copy(isSelectSubTypeDialogOpen = true) }
            SelectRoundDialogIntent.CloseSubTypeSelectDialog ->
                _state.update { it.copy(isSelectSubTypeDialogOpen = false) }
            is SelectRoundDialogIntent.SubTypeSelected ->
                _state.update { it.copy(isSelectSubTypeDialogOpen = false, selectedSubtype = action.subType) }
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

