package eywa.projectcodex.components.mainMenu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.components.mainMenu.MainMenuIntent.*
import eywa.projectcodex.datastore.CodexDatastore
import eywa.projectcodex.datastore.DatastoreKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainMenuViewModel @Inject constructor(
        private val helpShowcase: HelpShowcaseUseCase,
        private val datastore: CodexDatastore,
) : ViewModel() {
    private val _state = MutableStateFlow(MainMenuState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            datastore.get(listOf(DatastoreKey.DisplayHandicapNotice, DatastoreKey.UseBetaFeatures))
                    .collectLatest { values ->
                        _state.update {
                            it.copy(
                                    isHandicapNoticeDialogOpen = values[DatastoreKey.DisplayHandicapNotice]!!,
                                    useBetaFeatures = values[DatastoreKey.UseBetaFeatures]!!,
                            )
                        }
                    }
        }
        viewModelScope.launch {
            helpShowcase.state.collectLatest { helpShowcaseState ->
                _state.update { it.copy(isHelpShowcaseInProgress = helpShowcaseState.isInProgress) }
            }
        }
    }

    fun handle(action: MainMenuIntent) {
        when (action) {
            is HelpShowcaseAction -> helpShowcase.handle(action.action, CodexNavRoute.MAIN_MENU::class)
            is HandicapDialogClicked ->
                viewModelScope.launch { datastore.set(DatastoreKey.DisplayHandicapNotice, false) }
            OpenExitDialog -> _state.update { it.copy(isExitDialogOpen = true) }
            is ExitDialogCloseClicked -> _state.update { it.copy(isExitDialogOpen = false) }
            is ExitDialogOkClicked -> _state.update { it.copy(isExitDialogOpen = false, closeApplication = true) }
            CloseApplicationHandled -> _state.update { it.copy(closeApplication = false) }
            is Navigate -> _state.update { it.copy(navigateTo = action.route) }
            is NavigateHandled -> _state.update { it.copy(navigateTo = null) }
        }
    }
}
