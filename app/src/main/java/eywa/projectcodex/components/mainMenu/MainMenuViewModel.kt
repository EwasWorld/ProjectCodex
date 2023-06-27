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
        private val exitDialogRepo: ExitDialogRepo,
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
            exitDialogRepo.state.collectLatest { exitDialogState ->
                _state.update { it.copy(isExitDialogOpen = exitDialogState.isOpen) }
            }
        }
    }

    fun handle(action: MainMenuIntent) {
        when (action) {
            is HelpShowcaseAction -> helpShowcase.handle(action.action, CodexNavRoute.MAIN_MENU::class)
            is HandicapDialogClicked ->
                viewModelScope.launch { datastore.set(DatastoreKey.DisplayHandicapNotice, false) }
            is ExitDialogOkClicked -> exitDialogRepo.reduce(ExitDialogState(closeApplicationClicked = true))
            is ExitDialogCloseClicked -> exitDialogRepo.reduce(ExitDialogState())
            is Navigate -> _state.update { it.copy(navigateTo = action.route) }
            is NavigateHandled -> _state.update { it.copy(navigateTo = null) }
        }
    }
}
