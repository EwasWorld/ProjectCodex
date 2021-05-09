package eywa.projectcodex.components.mainMenu

import android.app.Application
import android.content.res.Resources
import androidx.lifecycle.AndroidViewModel
import eywa.projectcodex.database.ScoresRoomDatabase

/**
 * @see InputEndViewModel
 */
class MainMenuViewModel(application: Application) : AndroidViewModel(application) {
    private val db = ScoresRoomDatabase.getDatabase(application)
    val updateDefaultRoundsState = UpdateDefaultRounds.getState()
    val updateDefaultRoundsProgressMessage = UpdateDefaultRounds.getProgressMessage()

    /**
     * @see UpdateDefaultRounds.runUpdate
     */
    fun updateDefaultRounds(resources: Resources) {
        UpdateDefaultRounds.runUpdate(db, resources)
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