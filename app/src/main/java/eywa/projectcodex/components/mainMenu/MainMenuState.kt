package eywa.projectcodex.components.mainMenu

import eywa.projectcodex.common.navigation.NavRoute
import eywa.projectcodex.datastore.DatastoreKey

data class MainMenuState(
        val isHelpShowcaseInProgress: Boolean = false,
        val isExitDialogOpen: Boolean = false,
        val closeApplication: Boolean = false,
        val navigateTo: NavRoute? = null,
        val whatsNewDialogLastSeenAppVersion: AppVersion? = null,
        val whatsNewDialogOpen: Boolean = false,
        val useBetaFeatures: Boolean = DatastoreKey.UseBetaFeatures.defaultValue,
)
