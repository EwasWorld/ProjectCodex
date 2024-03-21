package eywa.projectcodex.core.mainActivity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.utils.GlobalTouchDetector
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsTask
import eywa.projectcodex.core.mainActivity.MainActivityIntent.ClearNoHelpShowcaseFlag
import eywa.projectcodex.core.mainActivity.MainActivityIntent.CloseHelpShowcase
import eywa.projectcodex.core.mainActivity.MainActivityIntent.GoToNextHelpShowcaseItem
import eywa.projectcodex.core.mainActivity.MainActivityIntent.PressDetected
import eywa.projectcodex.core.mainActivity.MainActivityIntent.SetScreenSize
import eywa.projectcodex.core.mainActivity.MainActivityIntent.StartHelpShowcase
import eywa.projectcodex.database.ScoresRoomDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
        val db: ScoresRoomDatabase,
        private val updateDefaultRoundsTask: UpdateDefaultRoundsTask,
        val helpShowcase: HelpShowcaseUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(MainActivityState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            helpShowcase.state.collect { showcaseState ->
                _state.update { it.copy(helpShowcaseState = showcaseState) }
            }
        }
    }

    fun updateDefaultRounds() = viewModelScope.launch {
        updateDefaultRoundsTask.runTask()
    }

    fun handle(action: MainActivityIntent) {
        when (action) {
            is StartHelpShowcase -> helpShowcase.startShowcase(action.screen?.let { it::class })
            GoToNextHelpShowcaseItem -> helpShowcase.nextShowcase()
            CloseHelpShowcase -> helpShowcase.endShowcase()
            ClearNoHelpShowcaseFlag -> helpShowcase.clearNoShowcaseFlag()
            PressDetected -> viewModelScope.launch { GlobalTouchDetector.pressDetected() }
            is SetScreenSize -> helpShowcase.handle(HelpShowcaseIntent.SetScreenSize(action.size))
        }
    }
}
