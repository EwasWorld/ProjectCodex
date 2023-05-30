package eywa.projectcodex.components.settings

import eywa.projectcodex.datastore.DatastoreKey

data class SettingsState(
        val use2023System: Boolean = DatastoreKey.Use2023HandicapSystem.defaultValue,
        val useBetaFeatures: Boolean = DatastoreKey.UseBetaFeatures.defaultValue,
)
