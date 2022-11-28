package eywa.projectcodex.components.about

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsState
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsTask
import eywa.projectcodex.components.archerRoundScore.ArcherRoundScoreViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @see ArcherRoundScoreViewModel
 */
@HiltViewModel
class AboutViewModel @Inject constructor(
        val updateDefaultRoundsTask: UpdateDefaultRoundsTask,
) : ViewModel() {
    var state: UpdateDefaultRoundsState? by mutableStateOf(null)

    init {
        viewModelScope.launch {
            updateDefaultRoundsTask.state.collectLatest {
                state = it
            }
        }
    }
}