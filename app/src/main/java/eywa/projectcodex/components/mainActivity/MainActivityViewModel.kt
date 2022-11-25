package eywa.projectcodex.components.mainActivity

import android.content.SharedPreferences
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import eywa.projectcodex.common.helpShowcase.ActionBarHelp
import eywa.projectcodex.common.utils.UpdateDefaultRounds
import eywa.projectcodex.database.ScoresRoomDatabase
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(val db: ScoresRoomDatabase) : ViewModel() {
    private val mutableState: MutableState<MainActivityState> = mutableStateOf(MainActivityState())
    val state: MainActivityState by mutableState

    /**
     * @see UpdateDefaultRounds.runUpdate
     */
    fun updateDefaultRounds(resources: Resources, sharedPreferences: SharedPreferences) {
        UpdateDefaultRounds.runUpdate(db, resources, sharedPreferences)
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

