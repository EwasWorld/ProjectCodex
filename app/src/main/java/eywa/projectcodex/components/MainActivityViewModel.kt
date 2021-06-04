package eywa.projectcodex.components

import android.app.Application
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.lifecycle.AndroidViewModel
import eywa.projectcodex.components.commonUtils.UpdateDefaultRounds
import eywa.projectcodex.database.ScoresRoomDatabase

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {
    /**
     * @see UpdateDefaultRounds.runUpdate
     */
    fun updateDefaultRounds(resources: Resources, sharedPreferences: SharedPreferences) {
        UpdateDefaultRounds.runUpdate(ScoresRoomDatabase.getDatabase(getApplication()), resources, sharedPreferences)
    }
}