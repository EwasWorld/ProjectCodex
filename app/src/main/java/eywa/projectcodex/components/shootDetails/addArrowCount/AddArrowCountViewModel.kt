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
import eywa.projectcodex.components.referenceTables.headToHead.HeadToHeadUseCase
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
    private val h2hMatchNumber = savedStateHandle.get<Int>(NavArgument.MATCH_NUMBER)
    private val screen = CodexNavRoute.SHOOT_DETAILS_ADD_COUNT
    private val extraState = MutableStateFlow(AddArrowCountExtras(h2hMatchNumber = h2hMatchNumber))
    private val isEditingSighters = savedStateHandle.get<Boolean>(NavArgument.IS_SIGHTERS) ?: false
    private var endSizeInitComplete = false

    val state = repo.getState(extraState) { main, extras ->
        if (!endSizeInitComplete && main.fullShootInfo?.h2h != null) {
            endSizeInitComplete = true

            val endSize = HeadToHeadUseCase.endSize(main.fullShootInfo.h2h.headToHead.teamSize, false)
            extraState.update { it.copy(endSize = it.endSize.copy(text = endSize.toString())) }
        }
        AddArrowCountState(main, extras, isEditingSighters)
    }
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
                if (response.getData()?.fullShootInfo?.h2h != null) return@collect
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
                val toAdd = currentState.endSize.parsed

                if (toAdd == null) {
                    extraState.update { it.copy(endSize = it.endSize.markDirty()) }
                    return
                }

                if (isEditingSighters) {
                    if (h2hMatchNumber == null) {
                        val shootRound = currentState.fullShootInfo.shootRound ?: return
                        val currentCount = shootRound.sightersCount
                        viewModelScope.launch {
                            repo.db.shootsRepo()
                                    .updateShootRound(shootRound.copy(sightersCount = currentCount + toAdd))
                        }
                    }
                    else {
                        val heat =
                                currentState.fullShootInfo.h2h?.matches
                                        ?.find { it.match.matchNumber == h2hMatchNumber }?.match
                                        ?: return
                        val currentCount = heat.sightersCount
                        viewModelScope.launch {
                            repo.db.h2hRepo().update(heat.copy(sightersCount = currentCount + toAdd))
                        }
                    }
                }
                else {
                    val currentCount = currentState.fullShootInfo.arrowCounter ?: return
                    viewModelScope.launch {
                        repo.db.arrowCounterRepo()
                                .update(currentCount.copy(shotCount = currentCount.shotCount + toAdd))
                    }
                }
            }

            EditSightMarkClicked -> extraState.update { it.copy(openEditSightMark = true) }
            EditSightMarkHandled -> extraState.update { it.copy(openEditSightMark = false) }
            FullSightMarksClicked -> extraState.update { it.copy(openFullSightMarks = true) }
            FullSightMarksHandled -> extraState.update { it.copy(openFullSightMarks = false) }
            EditSightersClicked -> extraState.update { it.copy(openEditSighters = true) }
            EditSightersHandled -> extraState.update { it.copy(openEditSighters = false) }
        }
    }

    private fun increaseEndSize(increase: Int) {
        val oldSize = state.value.getData()?.endSize?.parsed ?: return
        extraState.update { it.copy(endSize = it.endSize.onTextChanged((oldSize + increase).toString())) }
    }
}
