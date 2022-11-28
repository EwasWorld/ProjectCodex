package eywa.projectcodex.components.mainActivity

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.common.helpShowcase.ActionBarHelp
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsTask
import eywa.projectcodex.database.ScoresRoomDatabase
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
        val db: ScoresRoomDatabase,
        private val updateDefaultRoundsTask: UpdateDefaultRoundsTask,
) : ViewModel() {
    private val mutableState: MutableState<MainActivityState> = mutableStateOf(MainActivityState())
    val state: MainActivityState by mutableState

    fun updateDefaultRounds() = viewModelScope.launch {
        updateDefaultRoundsTask.runTask()
    }

    // TODO Move this to a MainActivityIntent and remove the activity param
    fun openHelpDialogs(activity: AppCompatActivity, visibleScreens: List<ActionBarHelp>) {
        val composeHelpItems = ActionBarHelp.executeHelpPressed(visibleScreens, activity) ?: return
        mutableState.value = mutableState.value.copy(
                helpItems = composeHelpItems,
                currentHelpItemIndex = 0,
        )
    }

    fun handle(action: MainActivityIntent) {
        when (action) {
            MainActivityIntent.GoToNextHelpShowcaseItem -> mutableState.value = mutableState.value.nextHelpItem()
            MainActivityIntent.CloseHelpShowcase -> mutableState.value = mutableState.value.clearHelpItems()
        }
    }
}

