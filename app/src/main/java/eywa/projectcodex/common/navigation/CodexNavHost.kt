package eywa.projectcodex.common.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import eywa.projectcodex.common.utils.ListUtils.containsDuplicates
import eywa.projectcodex.common.utils.ListUtils.getDuplicates

@Composable
fun CodexNavHost(
        navRoutes: Set<ScreenNavRoute>,
        navHostController: NavHostController,
        modifier: Modifier = Modifier,
) {
    val bottomSheetsRoutes = navRoutes
            .flatMap { it.bottomSheets.orEmpty() }
            .distinctBy { sheet -> sheet::class }
            .map { it.asRoute() }
    navRoutes
            .map { it.routeBase }
            .plus(bottomSheetsRoutes)
            .getDuplicates()
            .takeIf { it.isNotEmpty() }
            ?.let { throw IllegalStateException("Duplicate navRoutes found: " + it.joinToString(",")) }

    val tabSwitcherGroups = navRoutes
            .groupBy { it.tabSwitcherItem?.group }
            .minus(null)
            .mapValues { (_, value) -> value.mapNotNull { it.tabSwitcherItem } }
    tabSwitcherGroups
            .filter { it.value.size < 2 }
            .keys
            .takeIf { it.isNotEmpty() }
            ?.let { throw IllegalStateException("Tab groups with size < 2 are forbidden: " + it.joinToString()) }
    tabSwitcherGroups
            .filter { (_, value) -> value.map { it.position }.containsDuplicates() }
            .keys
            .takeIf { it.isNotEmpty() }
            ?.let { throw IllegalStateException("Duplicate tab group order value found: " + it.joinToString()) }

    NavHost(
            navController = navHostController,
            startDestination = CodexNavRoute.MAIN_MENU.routeBase,
            modifier = modifier,
    ) {
        navRoutes.forEach { route ->
            val groupItems = route.tabSwitcherItem?.group?.let { tabSwitcherGroups[it]!! }?.sortedBy { it.position }
            route.create(this, navHostController, groupItems)
        }
    }
}
