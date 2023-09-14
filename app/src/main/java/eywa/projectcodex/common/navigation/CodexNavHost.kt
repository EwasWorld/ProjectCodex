package eywa.projectcodex.common.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost

@Composable
fun CodexNavHost(
        navRoutes: Set<NavRoute>,
        navHostController: NavHostController,
        modifier: Modifier = Modifier,
) {
    val duplicateRoutes = navRoutes
            .groupBy { it.routeBase.lowercase() }
            .filter { (_, v) -> v.size > 1 }
            .keys
    check(duplicateRoutes.isEmpty()) { "Duplicate navRoutes found: " + duplicateRoutes.joinToString(",") }

    val tabGroups = navRoutes
            .groupBy { it.tabSwitcherItem?.group }
            .minus(null)
            .mapValues { (_, value) -> value.mapNotNull { it.tabSwitcherItem } }
    val sizeViolations = tabGroups.filter { it.value.size < 2 }.keys
    val orderViolations = tabGroups.filter { (_, value) -> value.distinctBy { it.position }.size != value.size }.keys
    check(sizeViolations.isEmpty()) { "Tab groups with size < 2 are forbidden: " + sizeViolations.joinToString() }
    check(orderViolations.isEmpty()) { "Duplicate tab group order value found: " + orderViolations.joinToString() }

    NavHost(
            navController = navHostController,
            startDestination = CodexNavRoute.MAIN_MENU.routeBase,
            modifier = modifier,
    ) {
        navRoutes.forEach { route ->
            val groupItems = route.tabSwitcherItem?.group?.let { tabGroups[it]!! }?.sortedBy { it.position }
            route.create(this, navHostController, groupItems)
        }
    }
}
