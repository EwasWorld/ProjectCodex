package eywa.projectcodex.common.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import eywa.projectcodex.common.sharedUi.CodexTabSwitcher

interface ScreenNavRoute : NavRoute {
    val tabSwitcherItem: TabSwitcherItem?
    val bottomSheets: List<BottomSheetNavRoute>?

    @Composable
    fun Screen(navController: NavController)

    fun create(
            navGraphBuilder: NavGraphBuilder,
            navController: NavController,
            tabSwitcherItems: List<TabSwitcherItem>?,
    ) {
        navGraphBuilder.composable(
                route = asRoute(null),
                arguments = navGraphBuilderArgs(),
        ) {
            Column {
                if (!tabSwitcherItems.isNullOrEmpty()) {
                    val shouldSaveState = tabSwitcherItems.first().group.saveState
                    CodexTabSwitcher(
                            items = tabSwitcherItems,
                            selectedItem = tabSwitcherItem!!,
                            itemClickedListener = { item ->
                                item.navRoute.navigate(navController) {
                                    val currentRoute = navController.currentDestination?.route
                                    if (currentRoute != null) {
                                        popUpTo(currentRoute) {
                                            inclusive = true
                                            if (shouldSaveState) saveState = true
                                        }
                                    }
                                    if (shouldSaveState) {
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                    )
                }
                Screen(navController = navController)
            }
        }

        bottomSheets?.forEach { it.create(navGraphBuilder, navController) }
    }
}
