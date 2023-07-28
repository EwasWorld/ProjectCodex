package eywa.projectcodex.components.mainMenu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.components.mainMenu.MainMenuIntent.*
import eywa.projectcodex.datastore.CodexDatastore
import eywa.projectcodex.datastore.DatastoreKey.*
import eywa.projectcodex.datastore.retrieve
import kotlinx.coroutines.flow.*
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
            datastore.get(
                    listOf(DisplayHandicapNotice, UseBetaFeatures, WhatsNewLastOpenedAppVersion)
            ).collectLatest { values ->
                _state.update { state ->
                    val version = values.retrieve(WhatsNewLastOpenedAppVersion)
                            .takeIf { it.isNotBlank() }
                            ?.let { AppVersion(it) }
                            ?: SEEN_HANDICAP_NOTICE_LATEST_APP_VERSION
                                    .takeIf { !values.retrieve(DisplayHandicapNotice) }
                    state.copy(
                            whatsNewDialogOpen = hasUpdates(version),
                            whatsNewDialogLastSeenAppVersion = version,
                            useBetaFeatures = values.retrieve(UseBetaFeatures),
                    )
                }
            }
        }
        viewModelScope.launch {
            helpShowcase.state.map { it.isInProgress }.distinctUntilChanged().collectLatest { isInProgress ->
                _state.update { it.copy(isHelpShowcaseInProgress = isInProgress) }
            }
        }
    }

    fun handle(action: MainMenuIntent) {
        when (action) {
            is HelpShowcaseAction -> helpShowcase.handle(action.action, CodexNavRoute.MAIN_MENU::class)
            OpenExitDialog -> _state.update { it.copy(isExitDialogOpen = true) }
            is ExitDialogCloseClicked -> _state.update { it.copy(isExitDialogOpen = false) }
            is ExitDialogOkClicked -> _state.update { it.copy(isExitDialogOpen = false, closeApplication = true) }
            CloseApplicationHandled -> _state.update { it.copy(closeApplication = false) }
            is Navigate -> _state.update { it.copy(navigateTo = action.route) }
            is NavigateHandled -> _state.update { it.copy(navigateTo = null) }
            is WhatsNewOpen -> _state.update { it.copy(whatsNewDialogOpen = true) }
            is WhatsNewClose -> {
                viewModelScope.launch {
                    datastore.set(WhatsNewLastOpenedAppVersion, action.latestUpdateAppVersion.toString())
                }
                _state.update {
                    it.copy(
                            whatsNewDialogLastSeenAppVersion = action.latestUpdateAppVersion,
                            whatsNewDialogOpen = false,
                    )
                }
            }
        }
    }
}
