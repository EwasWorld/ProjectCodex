package eywa.projectcodex.components.mainMenu

import android.app.Application
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.lifecycle.AndroidViewModel
import eywa.projectcodex.database.ScoresRoomDatabase

/**
 * @see InputEndViewModel
 */
class MainMenuViewModel(application: Application) : AndroidViewModel(application) {
    private val db = ScoresRoomDatabase.getDatabase(application)
    val updateDefaultRoundsState = UpdateDefaultRounds.taskProgress.getState()
    val updateDefaultRoundsProgressMessage = UpdateDefaultRounds.taskProgress.getMessage()

    /**
     * @see UpdateDefaultRounds.runUpdate
     */
    fun updateDefaultRounds(resources: Resources, sharedPreferences: SharedPreferences) {
        UpdateDefaultRounds.runUpdate(db, resources, sharedPreferences)
    }

    /**
     * @see UpdateDefaultRounds.cancelUpdateDefaultRounds
     */
    fun cancelUpdateDefaultRounds(resources: Resources) {
        UpdateDefaultRounds.cancelUpdateDefaultRounds(resources)
    }

    /**
     * @see UpdateDefaultRounds.resetState
     */
    fun resetUpdateDefaultRoundsStateIfComplete() {
        UpdateDefaultRounds.resetState()
    }
}