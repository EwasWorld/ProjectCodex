package eywa.projectcodex.components.mainActivity

import android.app.Application
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.lifecycle.AndroidViewModel
import eywa.projectcodex.common.utils.UpdateDefaultRounds
import eywa.projectcodex.components.app.App
import eywa.projectcodex.database.ScoresRoomDatabase
import javax.inject.Inject

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {
    @Inject
    lateinit var db: ScoresRoomDatabase

    init {
        (application as App).appComponent.inject(this)
    }

    /**
     * @see UpdateDefaultRounds.runUpdate
     */
    fun updateDefaultRounds(resources: Resources, sharedPreferences: SharedPreferences) {
        UpdateDefaultRounds.runUpdate(db, resources, sharedPreferences)
    }
}