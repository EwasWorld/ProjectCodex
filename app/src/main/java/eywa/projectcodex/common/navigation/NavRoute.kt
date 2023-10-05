package eywa.projectcodex.common.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import eywa.projectcodex.common.helpShowcase.ActionBarHelp
import eywa.projectcodex.common.sharedUi.CodexTabSwitcher

interface NavRoute : ActionBarHelp {
    val routeBase: String
    val args: Map<NavArgument, Boolean>
    val tabSwitcherItem: TabSwitcherItem?
    val bottomSheets: List<BottomSheetNavRoute>?

    @Composable
    fun getMenuBarTitle(entry: NavBackStackEntry?): String

    @Composable
    fun Screen(navController: NavController)

    fun navigate(
            navController: NavController,
            argValues: Map<NavArgument, String> = emptyMap(),
            popCurrentRoute: Boolean = false,
            options: (NavOptionsBuilder.() -> Unit)? = null,
    ) {
        val route = asRoute(argValues)

        if (options == null && !popCurrentRoute) {
            navController.navigate(route)
        }
        else {
            navController.navigate(route) {
                if (popCurrentRoute) {
                    val currentRoute = navController.currentDestination?.route
                    if (currentRoute != null) {
                        popUpTo(currentRoute) { inclusive = true }
                    }
                }
                options?.invoke(this)
            }
        }
    }

    fun create(
            navGraphBuilder: NavGraphBuilder,
            navController: NavController,
            tabSwitcherItems: List<TabSwitcherItem>?,
    ) {
        navGraphBuilder.composable(
                route = asRoute(null),
                arguments = args.map { (arg, required) ->
                    navArgument(arg.toArgName()) {
                        nullable = !required && arg.type == NavType.StringType
                        arg.defaultValue?.let {
                            defaultValue = it
                        }
                        type = arg.type
                    }
                },
        ) {
            Column {
                if (tabSwitcherItems != null) {
                    CodexTabSwitcher(
                            items = tabSwitcherItems,
                            selectedItem = tabSwitcherItem!!,
                            itemClickedListener = { item ->
                                item.navRoute.navigate(navController, popCurrentRoute = true)
                            },
                    )
                }
                Screen(navController = navController)
            }
        }

        bottomSheets?.forEach { it.create(navGraphBuilder, navController) }
    }

    private fun asRoute(argValues: Map<NavArgument, String>? = null): String {
        val (req, opt) = args.entries.partition { it.value }

        fun NavArgument.asNameAndValue() = Pair(
                toArgName(),
                if (argValues == null) "{${toArgName()}}" else argValues[this] ?: defaultValue,
        )

        val required = req.joinToString("") { (arg, _) ->
            val value = arg.asNameAndValue().second
                    ?: throw IllegalStateException("Required argument not provided")
            "/$value"
        }
        val optional = opt
                .mapNotNull { (arg, _) ->
                    val (name, value) = arg.asNameAndValue()
                    if (value == null) return@mapNotNull null
                    name to value
                }
                .takeIf { it.isNotEmpty() }
                ?.joinToString("&") { (name, value) -> "$name=$value" }
                ?.let { "?$it" }
                ?: ""
        return routeBase + required + optional
    }
}
