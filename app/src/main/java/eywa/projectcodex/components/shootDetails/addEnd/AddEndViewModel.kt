package eywa.projectcodex.components.shootDetails.addEnd

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.navigation.NavArgument
import eywa.projectcodex.common.navigation.get
import eywa.projectcodex.components.shootDetails.ShootDetailsIntent.NavBarClicked
import eywa.projectcodex.components.shootDetails.ShootDetailsIntent.SetInputtedArrows
import eywa.projectcodex.components.shootDetails.ShootDetailsRepo
import eywa.projectcodex.components.shootDetails.ShootDetailsResponse
import eywa.projectcodex.components.shootDetails.addEnd.AddEndIntent.*
import eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.ArrowInputsIntent
import eywa.projectcodex.components.shootDetails.getData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEndViewModel @Inject constructor(
        private val repo: ShootDetailsRepo,
        savedStateHandle: SavedStateHandle,
        private val helpShowcase: HelpShowcaseUseCase,
) : ViewModel() {
    private val screen = CodexNavRoute.SHOOT_DETAILS_ADD_END
    private val extraState = MutableStateFlow(AddEndExtras())

    val state = repo.getState(
            savedStateHandle.get<Int>(NavArgument.SHOOT_ID),
            extraState,
    ) { main, extras -> AddEndState(main, extras) }
            .stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(),
                    ShootDetailsResponse.Loading as ShootDetailsResponse<AddEndState>,
            )

    init {
        /*
         * Monitor for add end resulting in the round being completed
         */
        viewModelScope.launch {
            var previousIsComplete: Boolean? = null
            state.collect { response ->
                val data = response.getData()
                if (data == null || data.fullShootInfo.arrows.isNullOrEmpty()) {
                    previousIsComplete = null
                    return@collect
                }
                val isComplete = data.fullShootInfo.isRoundComplete
                if (previousIsComplete == false && isComplete) {
                    extraState.update { it.copy(roundCompleted = true) }
                }
                previousIsComplete = isComplete
            }
        }
    }

    fun handle(action: AddEndIntent) {
        when (action) {
            is HelpShowcaseAction -> helpShowcase.handle(action.action, screen::class)
            is ShootDetailsAction -> repo.handle(action.action, screen)
            is ArrowInputsAction -> handleArrowInputIntent(action.action)
            is ErrorHandled -> extraState.update { it.copy(errors = it.errors.minus(action.error)) }
            RoundFullDialogOkClicked -> repo.handle(NavBarClicked(CodexNavRoute.SHOOT_DETAILS_STATS), screen)
            /*
             * roundCompleted flag is not cleared as this causes dialogs to display incorrectly
             *      between button press and the navigation happening (flicks up round full dialog).
             * This navigation will pop the AddEnd screen is popped off the backstack
             *      causing this ViewModel to be destroyed.
             * When returning to the screen, roundCompleted flag will be back on the default and the corresponding
             *      dialog won't be displayed
             */
            RoundCompleteDialogOkClicked -> repo.handle(NavBarClicked(CodexNavRoute.SHOOT_DETAILS_STATS), screen)
            EditSightMarkClicked -> extraState.update { it.copy(openEditSightMark = true) }
            EditSightMarkHandled -> extraState.update { it.copy(openEditSightMark = false) }
            FullSightMarksClicked -> extraState.update { it.copy(openFullSightMarks = true) }
            FullSightMarksHandled -> extraState.update { it.copy(openFullSightMarks = false) }
            SightersClicked -> extraState.update { it.copy(openSighters = true) }
            SightersHandled -> extraState.update { it.copy(openSighters = false) }
        }
    }

    private fun handleArrowInputIntent(action: ArrowInputsIntent) {
        val currentState = state.value.getData() ?: return
        action.handle(
                enteredArrows = currentState.enteredArrows,
                endSize = currentState.endSize,
                dbArrows = null,
                setEnteredArrows = { arrows, error ->
                    repo.handle(SetInputtedArrows(arrows), screen)
                    error?.let { e ->
                        extraState.update { it.copy(errors = it.errors.plus(e)) }
                    }
                },
                onSubmit = {
                    var arrowNumber = currentState.fullShootInfo.arrows?.maxOfOrNull { it.arrowNumber } ?: 0
                    val arrows = currentState.enteredArrows.map {
                        it.asArrowScore(currentState.fullShootInfo.id, ++arrowNumber)
                    }
                    check(arrows.size == currentState.endSize) { "Incorrect number of arrows inputted" }

                    viewModelScope.launch { repo.db.arrowScoresRepo().insert(*arrows.toTypedArray()) }
                    handleArrowInputIntent(ArrowInputsIntent.ClearArrowsInputted)
                },
                helpListener = { handle(HelpShowcaseAction(it)) },
        )
    }
}
