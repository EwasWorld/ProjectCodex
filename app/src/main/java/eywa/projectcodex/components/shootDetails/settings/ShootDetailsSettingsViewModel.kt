package eywa.projectcodex.components.shootDetails.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.components.shootDetails.ShootDetailsIntent
import eywa.projectcodex.components.shootDetails.ShootDetailsRepo
import eywa.projectcodex.components.shootDetails.ShootDetailsResponse
import eywa.projectcodex.components.shootDetails.getData
import eywa.projectcodex.components.shootDetails.settings.ShootDetailsSettingsIntent.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShootDetailsSettingsViewModel @Inject constructor(
        private val repo: ShootDetailsRepo,
        private val helpShowcase: HelpShowcaseUseCase,
) : ViewModel() {
    private val screen = CodexNavRoute.SHOOT_DETAILS_SETTINGS
    private val extraState = MutableStateFlow(ShootDetailsSettingsExtras())

    val state = repo.getState(extraState) { main, extras -> ShootDetailsSettingsState(main, extras) }
            .stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(),
                    ShootDetailsResponse.Loading as ShootDetailsResponse<ShootDetailsSettingsState>,
            )

    init {
        viewModelScope.launch {
            state.collect {
                val data = it.getData() ?: return@collect
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

    fun handle(action: ShootDetailsSettingsIntent) {
        when (action) {
            is HelpShowcaseAction -> helpShowcase.handle(action.action, screen::class)
            is ShootDetailsAction -> repo.handle(action.action, screen)
            is AddEndSizeChanged -> {
                val currentState = state.value.getData()?.addEndSizePartial ?: return
                val new = currentState.onTextChanged(action.endSize)
                if (new.parsed != null) {
                    repo.handle(ShootDetailsIntent.SetAddEndEndSize(new.parsed), screen)
                }
                extraState.update { it.copy(addEndSizePartial = new) }
            }

            is ScorePadEndSizeChanged -> {
                val currentState = state.value.getData()?.scorePadEndSizePartial ?: return
                val new = currentState.onTextChanged(action.endSize)
                if (new.parsed != null) {
                    repo.handle(ShootDetailsIntent.SetScorePadEndSize(new.parsed), screen)
                }
                extraState.update { it.copy(scorePadEndSizePartial = new) }
            }
        }
    }
}
