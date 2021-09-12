package eywa.projectcodex.components.mainActivity

import android.app.Application
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.lifecycle.AndroidViewModel
import eywa.projectcodex.common.utils.UpdateDefaultRounds
import eywa.projectcodex.database.ScoresRoomDatabase

class MainActivityViewModel(application: Application, private val db: ScoresRoomDatabase) :
        AndroidViewModel(application) {
    /**
     * @see UpdateDefaultRounds.runUpdate
     */
    fun updateDefaultRounds(resources: Resources, sharedPreferences: SharedPreferences) {
        UpdateDefaultRounds.runUpdate(db, resources, sharedPreferences)
    }
}