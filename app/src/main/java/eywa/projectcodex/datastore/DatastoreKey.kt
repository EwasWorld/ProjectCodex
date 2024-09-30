package eywa.projectcodex.datastore

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsTask

sealed class DatastoreKey<T : Any>(val key: Preferences.Key<T>, val defaultValue: T) {
    data object Use2023HandicapSystem : BooleanKey("use_2023_handicaps", true)
    data object DisplayHandicapNotice : BooleanKey("display_handicap_notice", true)
    data object UseBetaFeatures : BooleanKey("use_beta_features", false)
    data object UseSimpleStatsView : BooleanKey("use_simple_stats_view", true)
    data object UseSimpleHandicapView : BooleanKey("use_simple_handicap_view", true)

    /**
     * The app version of the last time [UpdateDefaultRoundsTask] was successfully run
     */
    data object AppVersionAtLastDefaultRoundsUpdate : IntKey("app_version_at_last_default_rounds_update", -1)

    /**
     * The version of the default_rounds_data.json file currently loaded into the database
     */
    data object CurrentDefaultRoundsVersion : IntKey("current_default_rounds_version", -1)
    data object WhatsNewLastOpenedAppVersion : StringKey("whats_new_last_opened_app_version", "")
    data object SavedEmails : StringKey("saved_emails", "") {
        const val DELIM = ":"
    }
}

open class BooleanKey internal constructor(val name: String, val default: Boolean) :
        DatastoreKey<Boolean>(booleanPreferencesKey(name), default)

open class IntKey internal constructor(val name: String, val default: Int) :
        DatastoreKey<Int>(intPreferencesKey(name), default)

open class StringKey internal constructor(val name: String, val default: String) :
        DatastoreKey<String>(stringPreferencesKey(name), default)
