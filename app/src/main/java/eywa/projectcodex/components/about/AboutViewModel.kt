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
    private val _state: MutableStateFlow<UpdateDefaultRoundsState?> = MutableStateFlow(null)
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            updateDefaultRoundsTask.state.collectLatest { defautRoundsState ->
                _state.update { defautRoundsState }
            }
        }
    }
}