package eywa.projectcodex.datastore

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsTask

sealed class DatastoreKey<T : Any>(val key: Preferences.Key<T>, val defaultValue: T) {
    object Use2023HandicapSystem : DatastoreKey<Boolean>(booleanPreferencesKey("use_2023_handicaps"), true)
    object DisplayHandicapNotice : DatastoreKey<Boolean>(booleanPreferencesKey("display_handicap_notice"), true)
    object UseBetaFeatures : DatastoreKey<Boolean>(booleanPreferencesKey("use_beta_features"), false)

    /**
     * The app version of the last time [UpdateDefaultRoundsTask] was successfully run
     */
    object AppVersionAtLastDefaultRoundsUpdate :
            DatastoreKey<Int>(intPreferencesKey("app_version_at_last_default_rounds_update"), -1)

    /**
     * The version of the default_rounds_data.json file currently loaded into the database
     */
    object CurrentDefaultRoundsVersion : DatastoreKey<Int>(intPreferencesKey("current_default_rounds_version"), -1)
    object WhatsNewLastOpenedAppVersion :
            DatastoreKey<String>(stringPreferencesKey("whats_new_last_opened_app_version"), "")
}
