package eywa.projectcodex.components.mainMenu

import eywa.projectcodex.datastore.DatastoreKey

data class MainMenuState(
        val isHandicapNoticeDialogOpen: Boolean = false,
        val useBetaFeatures: Boolean = DatastoreKey.UseBetaFeatures.defaultValue,
)
