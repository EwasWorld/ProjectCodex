package eywa.projectcodex.common.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.*
import androidx.navigation.compose.composable
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.ActionBarHelp
import eywa.projectcodex.components.mainMenu.MainMenuScreen

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
                if (argValues == null) toArgName() else argValues[this] ?: defaultValue,
        )

        val required = req.joinToString("") { (arg, _) ->
            val value = arg.asNameAndValue().second
                    ?: throw IllegalStateException("Required argument not provided")
            "/{$value}"
        }
        val optional = opt
                .mapNotNull { (arg, _) ->
                    val (name, value) = arg.asNameAndValue()
                    if (value == null) return@mapNotNull null
                    name to value
                }
                .takeIf { it.isNotEmpty() }
                ?.joinToString("&") { (name, value) -> "$name={$value}" }
                ?.let { "?$it" }
                ?: ""
        return routeBase + required + optional
    }
}

// TODO_CURRENT Check help clears between screens
enum class CodexNavRoute : NavRoute, ActionBarHelp {
    ABOUT,
    ARCHER_ROUND_SCORE {
        override val args: Map<NavArgument, Boolean>
            get() = mapOf(NavArgument.ARCHER_ROUND_ID to false, NavArgument.SCREEN to true)
    },
    CLASSIFICATION_TABLES,
    HANDICAP_TABLES,
    MAIN_MENU {
        @Composable
        override fun getMenuBarTitle(): String = stringResource(R.string.main_menu__title)

        @Composable
        override fun Screen(navController: NavController) {
            MainMenuScreen().ComposeContent(navController = navController)
        }
    },
    NEW_SCORE {
        override val args: Map<NavArgument, Boolean>
            get() = mapOf(NavArgument.ARCHER_ROUND_ID to false)
    },
    SETTINGS,
    SIGHT_MARKS {
        override val args: Map<NavArgument, Boolean>
            get() = mapOf(NavArgument.SIGHT_MARK_ID to false)
    },
    VIEW_SCORES,
    ;

    override val routeBase = "main_" + name.lowercase()
    override val args: Map<NavArgument, Boolean> = emptyMap()

    @Composable
    override fun getMenuBarTitle(): String {
        TODO()
    }

    @Composable
    override fun Screen(navController: NavController) {
        TODO()
    }

    companion object {
        val reverseMap = values().associateBy { it.routeBase }
    }
}
