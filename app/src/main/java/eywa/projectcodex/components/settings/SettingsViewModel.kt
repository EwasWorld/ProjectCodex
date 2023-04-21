package eywa.projectcodex.components.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.datastore.CodexDatastore
import eywa.projectcodex.datastore.DatastoreKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
        val datastore: CodexDatastore,
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            datastore.get(DatastoreKey.Use2023HandicapSystem).collect { system ->
                _state.update { it.copy(use2023System = system) }
            }
        }
    }

    fun handle(action: SettingsIntent) {
        when (action) {
            SettingsIntent.ToggleUse2023System ->
                viewModelScope.launch { datastore.toggle(DatastoreKey.Use2023HandicapSystem) }
        }
    }
}
