package eywa.projectcodex.common.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.*
import androidx.navigation.compose.composable

interface NavRoute {
    val routeBase: String
    val args: Map<NavArgument, Boolean>

    @Composable
    fun getMenuBarTitle(): String

    @Composable
    fun Screen(navController: NavController)

    fun navigate(
            navController: NavController,
            argValues: Map<NavArgument, String> = emptyMap(),
            options: (NavOptionsBuilder.() -> Unit)? = null,
    ) {
        val route = asRoute(argValues)

        if (options == null) {
            navController.navigate(route)
        }
        else {
            navController.navigate(route, options)
        }
    }

    fun create(navGraphBuilder: NavGraphBuilder, navController: NavController) {
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
            Screen(navController = navController)
        }
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
