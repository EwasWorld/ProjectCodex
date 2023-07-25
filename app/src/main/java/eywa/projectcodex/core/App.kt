package eywa.projectcodex.core

import android.os.Build
import android.util.Log
import androidx.multidex.MultiDexApplication
import dagger.hilt.android.HiltAndroidApp
import java.io.File

@HiltAndroidApp
open class App : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        deleteSharedPrefs()
    }

    /**
     * TODO Remove when all users have moved off of shared prefs?
     *
     * @since 2.3.0
     */
    private fun deleteSharedPrefs(): Boolean {
        val sharedPrefsName = "Codex Archery Aide"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val deletePrefs = applicationContext.deleteSharedPreferences(sharedPrefsName)
            Log.i(LOG_TAG, "Delete shared prefs result: $deletePrefs")
            return deletePrefs
        }
        else {
            val clearPrefs = applicationContext.getSharedPreferences(sharedPrefsName, MODE_PRIVATE)
                    .edit().clear().commit()
            Log.i(LOG_TAG, "Clear shared prefs result: $clearPrefs")
            if (clearPrefs) {
                val dir = File(applicationContext.applicationInfo.dataDir, "shared_prefs")
                val deletePrefs = File(dir, "$sharedPrefsName.xml").delete()
                Log.i(LOG_TAG, "Delete shared prefs result: $deletePrefs")
                return deletePrefs
            }
        }
        return false
    }

    companion object {
        private const val LOG_TAG = "MainApplication"
    }
}
