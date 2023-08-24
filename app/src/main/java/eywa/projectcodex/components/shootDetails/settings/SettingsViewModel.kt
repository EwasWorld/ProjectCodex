package eywa.projectcodex.components.shootDetails.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.navigation.NavArgument
import eywa.projectcodex.common.navigation.get
import eywa.projectcodex.components.shootDetails.ShootDetailsIntent
import eywa.projectcodex.components.shootDetails.ShootDetailsRepo
import eywa.projectcodex.components.shootDetails.ShootDetailsResponse
import eywa.projectcodex.components.shootDetails.settings.SettingsIntent.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
        private val repo: ShootDetailsRepo,
        savedStateHandle: SavedStateHandle,
        private val helpShowcase: HelpShowcaseUseCase,
) : ViewModel() {
    private val screen = CodexNavRoute.SHOOT_DETAILS_SETTINGS
    private val extraState = MutableStateFlow(SettingsExtras())

    @Suppress("UNCHECKED_CAST")
    val state = repo.getState(
            savedStateHandle.get<Int>(NavArgument.SHOOT_ID),
            extraState,
    ) { main, extras -> SettingsState(main, extras) }
            .stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(),
                    ShootDetailsResponse.Loading as ShootDetailsResponse<SettingsState>,
            )

    init {
        viewModelScope.launch {
            state.collect {
                val data = it.data ?: return@collect
                if (data.scorePadEndSizePartial.parsed != null
                    && data.scorePadEndSize != data.scorePadEndSizePartial.parsed
                ) {
                    handle(ScorePadEndSizeChanged(data.scorePadEndSize.toString()))
                }
                if (data.addEndSizePartial.parsed != null && data.addEndSize != data.addEndSizePartial.parsed) {
                    handle(AddEndSizeChanged(data.addEndSize.toString()))
                }
            }
        }
    }

    fun handle(action: SettingsIntent) {
        when (action) {
            is HelpShowcaseAction -> helpShowcase.handle(action.action, screen::class)
            is ShootDetailsAction -> repo.handle(action.action, screen)
            is AddEndSizeChanged -> {
                val currentState = state.value.data?.addEndSizePartial ?: return
                val new = currentState.onValueChanged(action.endSize)
                if (new.parsed != null) {
                    repo.handle(ShootDetailsIntent.SetAddEndEndSize(new.parsed), screen)
                }
                extraState.update { it.copy(addEndSizePartial = new) }
            }
            is ScorePadEndSizeChanged -> {
                val currentState = state.value.data?.scorePadEndSizePartial ?: return
                val new = currentState.onValueChanged(action.endSize)
                if (new.parsed != null) {
                    repo.handle(ShootDetailsIntent.SetScorePadEndSize(new.parsed), screen)
                }
                extraState.update { it.copy(scorePadEndSizePartial = new) }
            }
        }
    }
}
