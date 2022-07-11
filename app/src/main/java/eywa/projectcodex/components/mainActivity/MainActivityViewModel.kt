package eywa.projectcodex.components.mainActivity

import android.app.Application
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import eywa.projectcodex.common.helpShowcase.ActionBarHelp
import eywa.projectcodex.common.helpShowcase.ComposeHelpShowcaseItem
import eywa.projectcodex.common.utils.UpdateDefaultRounds
import eywa.projectcodex.components.app.App
import eywa.projectcodex.database.ScoresRoomDatabase
import javax.inject.Inject

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {
    @Inject
    lateinit var db: ScoresRoomDatabase

    private val mutableState: MutableState<MainActivityState> = mutableStateOf(MainActivityState())
    val state: MainActivityState by mutableState

    init {
        (application as App).appComponent.inject(this)
    }

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

data class MainActivityState(
        internal val helpItems: List<ComposeHelpShowcaseItem>? = null,
        internal val currentHelpItemIndex: Int? = null,
) {
    val isHelpShowcaseInProgress: Boolean
        get() = !helpItems.isNullOrEmpty() && currentHelpItemIndex in helpItems.indices
    val currentHelpItem: ComposeHelpShowcaseItem?
        get() = currentHelpItemIndex?.let { helpItems?.get(it) }
    val hasNextItem: Boolean
        get() = currentHelpItemIndex != null && currentHelpItemIndex != helpItems?.lastIndex

    fun nextHelpItem(): MainActivityState {
        if (!hasNextItem) return clearHelpItems()
        return copy(currentHelpItemIndex = currentHelpItemIndex?.plus(1))
    }

    fun clearHelpItems(): MainActivityState {
        ActionBarHelp.markShowcaseComplete()
        return copy(helpItems = null, currentHelpItemIndex = null)
    }
}