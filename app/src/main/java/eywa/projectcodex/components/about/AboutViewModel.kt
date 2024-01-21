package eywa.projectcodex.components.about

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsState
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsTask
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @see ArcherRoundScoreViewModel
 */
@HiltViewModel
class AboutViewModel @Inject constructor(
        val updateDefaultRoundsTask: UpdateDefaultRoundsTask,
) : ViewModel() {
    private val _state = MutableStateFlow<UpdateDefaultRoundsState>(UpdateDefaultRoundsState.NotStarted)
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            updateDefaultRoundsTask.state.collectLatest { defaultRoundsState ->
                _state.update { defaultRoundsState }
            }
        }
    }

    companion object {
        const val PRIVACY_POLICY_URL = "https://github.com/EwasWorld/ProjectCodex/blob/master/PRIVACY_POLICY.md"
    }
}
