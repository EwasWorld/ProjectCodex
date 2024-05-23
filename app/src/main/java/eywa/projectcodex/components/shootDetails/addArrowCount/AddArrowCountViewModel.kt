package eywa.projectcodex.components.shootDetails.addArrowCount

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.navigation.NavArgument
import eywa.projectcodex.common.navigation.get
import eywa.projectcodex.common.sharedUi.numberField.PartialNumberFieldState
import eywa.projectcodex.components.shootDetails.ShootDetailsRepo
import eywa.projectcodex.components.shootDetails.ShootDetailsResponse
import eywa.projectcodex.components.shootDetails.addArrowCount.AddArrowCountIntent.*
import eywa.projectcodex.components.shootDetails.getData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddArrowCountViewModel @Inject constructor(
        private val repo: ShootDetailsRepo,
        savedStateHandle: SavedStateHandle,
        private val helpShowcase: HelpShowcaseUseCase,
) : ViewModel() {
    private val screen = CodexNavRoute.SHOOT_DETAILS_ADD_COUNT
    private val extraState = MutableStateFlow(AddArrowCountExtras())

    val state = repo.getState(
            savedStateHandle.get<Int>(NavArgument.SHOOT_ID),
            extraState,
    ) { main, extras -> AddArrowCountState(main, extras) }
            .stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(),
                    ShootDetailsResponse.Loading as ShootDetailsResponse<AddArrowCountState>,
            )

    init {
        /*
         * If there are fewer than the end size worth of arrows left in the round, return null
         */
        viewModelScope.launch {
            var previousRemainingArrows: Int? = null
            state.collect { response ->
                val data = response.getData()
                val remaining = data?.fullShootInfo?.remainingArrows ?: return@collect

                val size = data.endSize.parsed
                if (previousRemainingArrows != remaining && (size == null || size > remaining)) {
                    extraState.update { it.copy(endSize = PartialNumberFieldState("$remaining")) }
                }
                previousRemainingArrows = remaining
            }
        }
    }

    fun handle(action: AddArrowCountIntent) {
        when (action) {
            is HelpShowcaseAction -> helpShowcase.handle(action.action, screen::class)

            ClickEditShootInfo -> extraState.update { it.copy(editShootInfoClicked = true) }
            EditShootInfoHandled -> extraState.update { it.copy(editShootInfoClicked = false) }

            ClickIncrease -> increaseEndSize(1)
            ClickDecrease -> increaseEndSize(-1)

            is OnValueChanged -> extraState.update { it.copy(endSize = it.endSize.onTextChanged(action.value)) }

            ClickSubmit -> {
                val currentState = state.value.getData() ?: return
                val currentCount = currentState.fullShootInfo.arrowCounter ?: return
                val toAdd = currentState.endSize.parsed

                if (toAdd == null) {
                    // Can only happen i
                    extraState.update { it.copy(endSize = it.endSize.markDirty()) }
                    return
                }

                viewModelScope.launch {
                    repo.db.arrowCounterRepo().update(currentCount.copy(shotCount = currentCount.shotCount + toAdd))
                }
            }

            EditSightMarkClicked -> extraState.update { it.copy(openEditSightMark = true) }
            EditSightMarkHandled -> extraState.update { it.copy(openEditSightMark = false) }
            FullSightMarksClicked -> extraState.update { it.copy(openFullSightMarks = true) }
            FullSightMarksHandled -> extraState.update { it.copy(openFullSightMarks = false) }
        }
    }

    private fun increaseEndSize(increase: Int) {
        val oldSize = state.value.getData()?.endSize?.parsed ?: return
        extraState.update { it.copy(endSize = it.endSize.onTextChanged((oldSize + increase).toString())) }
    }
}
