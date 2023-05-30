package eywa.projectcodex.datastore

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey

sealed class DatastoreKey<T : Any>(val key: Preferences.Key<T>, val defaultValue: T) {
    object Use2023HandicapSystem : DatastoreKey<Boolean>(booleanPreferencesKey("use_2023_handicaps"), true)
    object DisplayHandicapNotice : DatastoreKey<Boolean>(booleanPreferencesKey("display_handicap_notice"), true)
    object UseBetaFeatures : DatastoreKey<Boolean>(booleanPreferencesKey("use_beta_features"), false)
}
