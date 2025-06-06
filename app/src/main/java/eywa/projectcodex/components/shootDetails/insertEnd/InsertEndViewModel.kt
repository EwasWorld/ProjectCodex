package eywa.projectcodex.components.shootDetails.insertEnd

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.components.shootDetails.ShootDetailsRepo
import eywa.projectcodex.components.shootDetails.ShootDetailsResponse
import eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.ArrowInputsIntent
import eywa.projectcodex.components.shootDetails.getData
import eywa.projectcodex.components.shootDetails.insertEnd.InsertEndIntent.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InsertEndViewModel @Inject constructor(
        private val repo: ShootDetailsRepo,
        private val helpShowcase: HelpShowcaseUseCase,
) : ViewModel() {
    private val screen = CodexNavRoute.SHOOT_DETAILS_INSERT_END
    private val extraState = MutableStateFlow(InsertEndExtras())

    val state = repo.getState(extraState) { main, extras -> InsertEndState(main, extras) }
            .stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(),
                    ShootDetailsResponse.Loading as ShootDetailsResponse<InsertEndState>,
            )

    fun handle(action: InsertEndIntent) {
        when (action) {
            is HelpShowcaseAction -> helpShowcase.handle(action.action, screen::class)
            is ShootDetailsAction -> repo.handle(action.action, screen)
            is ArrowInputsAction -> handleArrowInputIntent(action.action)
            is ErrorHandled -> extraState.update { it.copy(errors = it.errors.minus(action.error)) }
            CloseHandled -> extraState.update { it.copy(closeScreen = false) }
        }
    }

    private fun handleArrowInputIntent(action: ArrowInputsIntent) {
        val currentState = state.value.getData() ?: return
        action.handle(
                enteredArrows = currentState.enteredArrows,
                endSize = currentState.endSize,
                dbArrows = null,
                setEnteredArrows = { arrows, error ->
                    extraState.update {
                        it.copy(
                                enteredArrows = arrows,
                                errors = if (error == null) it.errors else it.errors.plus(error),
                        )
                    }
                },
                onCancel = { extraState.update { it.copy(closeScreen = true) } },
                onSubmit = {
                    var arrowNumber = currentState.firstArrowNumber
                    val arrows = currentState.enteredArrows.map {
                        it.asArrowScore(currentState.fullShootInfo.id, arrowNumber++)
                    }
                    check(arrows.size == currentState.endSize) { "Incorrect number of arrows inputted" }

                    viewModelScope.launch {
                        repo.db.arrowScoresRepo().insertEnd(currentState.fullShootInfo.arrows!!, arrows)
                        handleArrowInputIntent(ArrowInputsIntent.CancelClicked)
                    }
                },
                helpListener = { handle(HelpShowcaseAction(it)) },
        )
    }
}
