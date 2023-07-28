package eywa.projectcodex.components.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.components.settings.SettingsIntent.*
import eywa.projectcodex.datastore.CodexDatastore
import eywa.projectcodex.datastore.DatastoreKey.Use2023HandicapSystem
import eywa.projectcodex.datastore.DatastoreKey.UseBetaFeatures
import eywa.projectcodex.datastore.retrieve
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
        val datastore: CodexDatastore,
        val helpShowcaseUseCase: HelpShowcaseUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            datastore.get(
                    listOf(
                            Use2023HandicapSystem,
                            UseBetaFeatures,
                    )
            ).collect { values ->
                _state.update {
                    it.copy(
                            use2023System = values.retrieve(Use2023HandicapSystem),
                            useBetaFeatures = values.retrieve(UseBetaFeatures),
                    )
                }
            }
        }
    }

    fun handle(action: SettingsIntent) {
        when (action) {
            ToggleUse2023System -> viewModelScope.launch { datastore.toggle(Use2023HandicapSystem) }
            ToggleUseBetaFeatures -> viewModelScope.launch { datastore.toggle(UseBetaFeatures) }
            is HelpShowcaseAction -> helpShowcaseUseCase.handle(action.action, CodexNavRoute.SETTINGS::class)
        }
    }
}
