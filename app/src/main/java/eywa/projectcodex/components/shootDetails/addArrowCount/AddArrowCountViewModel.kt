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
import eywa.projectcodex.components.shootDetails.addArrowCount.AddArrowCountIntent.ClickDecrease
import eywa.projectcodex.components.shootDetails.addArrowCount.AddArrowCountIntent.ClickEditShootInfo
import eywa.projectcodex.components.shootDetails.addArrowCount.AddArrowCountIntent.ClickIncrease
import eywa.projectcodex.components.shootDetails.addArrowCount.AddArrowCountIntent.ClickSubmit
import eywa.projectcodex.components.shootDetails.addArrowCount.AddArrowCountIntent.EditShootInfoHandled
import eywa.projectcodex.components.shootDetails.addArrowCount.AddArrowCountIntent.HelpShowcaseAction
import eywa.projectcodex.components.shootDetails.addArrowCount.AddArrowCountIntent.OnValueChanged
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
    private val screen = CodexNavRoute.SHOOT_DETAILS_EDIT_END
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
         * Monitor for add count resulting in the round being completed
         */
        viewModelScope.launch {
            var previousIsComplete: Boolean? = null
            state.collect { response ->
                val data = response.getData()
                if (data == null || data.fullShootInfo.round == null) {
                    previousIsComplete = null
                    return@collect
                }
                val isComplete = data.fullShootInfo.isRoundComplete
                if (previousIsComplete == false && isComplete) {
                    extraState.update { it.copy(endSize = PartialNumberFieldState("0")) }
                }
                previousIsComplete = isComplete
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
        }
    }

    private fun increaseEndSize(increase: Int) {
        val oldSize = state.value.getData()?.endSize?.parsed ?: return
        extraState.update { it.copy(endSize = it.endSize.onTextChanged((oldSize + increase).toString())) }
    }
}
