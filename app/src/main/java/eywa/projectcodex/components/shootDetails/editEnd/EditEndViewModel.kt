package eywa.projectcodex.components.shootDetails.editEnd

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.navigation.NavArgument
import eywa.projectcodex.common.navigation.get
import eywa.projectcodex.components.shootDetails.ShootDetailsRepo
import eywa.projectcodex.components.shootDetails.ShootDetailsResponse
import eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.ArrowInputsIntent
import eywa.projectcodex.components.shootDetails.editEnd.EditEndIntent.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditEndViewModel @Inject constructor(
        private val repo: ShootDetailsRepo,
        savedStateHandle: SavedStateHandle,
        private val helpShowcase: HelpShowcaseUseCase,
) : ViewModel() {
    private val screen = CodexNavRoute.SHOOT_DETAILS_EDIT_END
    private val extraState = MutableStateFlow(EditEndExtras())

    @Suppress("UNCHECKED_CAST")
    val state = repo.getState(
            savedStateHandle.get<Int>(NavArgument.SHOOT_ID),
            extraState,
    ) { main, extras -> EditEndState(main, extras) }
            .stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(),
                    ShootDetailsResponse.Loading as ShootDetailsResponse<EditEndState>,
            )

    init {
        /*
         * Monitor for db changes
         */
        viewModelScope.launch {
            state.map { it.data?.originalEnd }.distinctUntilChanged().collect {
                handleArrowInputIntent(ArrowInputsIntent.ResetArrowsInputted)
            }
        }
    }

    fun handle(action: EditEndIntent) {
        when (action) {
            is HelpShowcaseAction -> helpShowcase.handle(action.action, screen::class)
            is ShootDetailsAction -> repo.handle(action.action, screen)
            is ArrowInputsAction -> handleArrowInputIntent(action.action)
            is ErrorHandled -> extraState.update { it.copy(errors = it.errors.minus(action.error)) }
            CloseHandled -> extraState.update { it.copy(closeScreen = false) }
        }
    }

    private fun handleArrowInputIntent(action: ArrowInputsIntent) {
        val currentState = state.value.data ?: return
        action.handle(
                enteredArrows = currentState.enteredArrows,
                endSize = currentState.endSize,
                dbArrows = currentState.originalEnd,
                setEnteredArrows = { arrows, error ->
                    extraState.update {
                        it.copy(
                                enteredArrows = arrows,
                                errors = if (error == null) it.errors else it.errors.plus(error)
                        )
                    }
                },
                onCancel = { extraState.update { it.copy(closeScreen = true) } },
                onSubmit = {
                    var arrowNumber = currentState.firstArrowNumber
                    val arrows = currentState.enteredArrows.map {
                        it.toArrowScore(currentState.fullShootInfo.id, arrowNumber++)
                    }
                    check(arrows.size == currentState.endSize) { "Incorrect number of arrows inputted" }

                    viewModelScope.launch { repo.db.arrowScoresRepo().update(*arrows.toTypedArray()) }
                    handleArrowInputIntent(ArrowInputsIntent.CancelClicked)
                },
                helpListener = { handle(HelpShowcaseAction(it)) },
        )
    }
}
