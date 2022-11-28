package eywa.projectcodex.common.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import eywa.projectcodex.R

/**
 * Used to keep track of the keys used in SharedPreferences
 */
enum class SharedPrefs(val key: String) {
    DEFAULT_ROUNDS_VERSION("default_rounds_version");

    companion object {
        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        var sharedPreferencesCustomName: String? = null

        fun Context.getSharedPreferences(): SharedPreferences = this.getSharedPreferences(
                sharedPreferencesCustomName ?: this.resources.getString(R.string.app_name),
                Context.MODE_PRIVATE
        )
    }
}