package eywa.projectcodex.components.commonUtils

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import eywa.projectcodex.R

/**
 * Used to keep track of the keys used in SharedPreferences
 */
enum class SharedPrefs(val key: String) {
    DEFAULT_ROUNDS_VERSION("default_rounds_version");

    companion object {
        fun Activity.getSharedPreferences(): SharedPreferences =
                this.getSharedPreferences(this.resources.getString(R.string.app_name), Context.MODE_PRIVATE)
    }
}