package eywa.projectcodex.common.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import eywa.projectcodex.common.helpShowcase.ActionBarHelp

interface NavRoute : ActionBarHelp {
    val routeBase: String

    /**
     * true if arg is required,
     * false if it's optional
     */
    val args: Map<NavArgument, Boolean>

    @Composable
    fun getMenuBarTitle(entry: NavBackStackEntry?): String

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

    fun asRoute(argValues: Map<NavArgument, String>? = null): String {
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

    fun navGraphBuilderArgs() = args.map { (arg, required) ->
        navArgument(arg.toArgName()) {
            nullable = !required && arg.type == NavType.StringType
            arg.defaultValue?.let {
                defaultValue = it
            }
            type = arg.type
        }
    }
}
