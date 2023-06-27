package eywa.projectcodex.components.mainMenu

import eywa.projectcodex.common.navigation.NavRoute
import eywa.projectcodex.datastore.DatastoreKey

data class MainMenuState(
        val isExitDialogOpen: Boolean = false,
        val navigateTo: NavRoute? = null,
        val isHandicapNoticeDialogOpen: Boolean = false,
        val useBetaFeatures: Boolean = DatastoreKey.UseBetaFeatures.defaultValue,
)
