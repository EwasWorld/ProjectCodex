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
    if (duplicateRoutes.isNotEmpty()) {
        throw IllegalStateException("Duplicate navRoutes found: " + duplicateRoutes.joinToString(","))
    }

    NavHost(
            navController = navHostController,
            startDestination = CodexNavRoute.MAIN_MENU.routeBase,
            modifier = modifier,
    ) {
        navRoutes.forEach { route ->
            route.create(this, navHostController)
        }
    }
}
