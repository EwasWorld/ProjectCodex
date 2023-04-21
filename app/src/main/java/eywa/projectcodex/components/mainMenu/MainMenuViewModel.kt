package eywa.projectcodex.components.mainMenu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.common.helpShowcase.HelpShowcase
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
        private val helpShowcase: HelpShowcase,
        private val datastore: CodexDatastore,
) : ViewModel() {
    private val _state = MutableStateFlow(MainMenuState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            datastore.get(DatastoreKey.DisplayHandicapNotice).collectLatest { showHandicapNotice ->
                _state.update { it.copy(isHandicapNoticeDialogOpen = showHandicapNotice) }
            }
        }
    }

    fun handle(action: MainMenuIntent) {
        when (action) {
            is MainMenuIntent.HelpShowcaseAction -> helpShowcase.handle(action.action, MainMenuFragment::class)
            is MainMenuIntent.HandicapDialogClicked ->
                viewModelScope.launch { datastore.set(DatastoreKey.DisplayHandicapNotice, false) }
            else -> throw NotImplementedError()
        }
    }
}
