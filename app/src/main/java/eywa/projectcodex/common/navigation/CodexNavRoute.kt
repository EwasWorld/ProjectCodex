package eywa.projectcodex.common.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.ActionBarHelp
import eywa.projectcodex.components.handicapTables.HandicapTablesScreen
import eywa.projectcodex.components.mainMenu.MainMenuScreen

// TODO_CURRENT Check help clears between screens
enum class CodexNavRoute : NavRoute, ActionBarHelp {
    ABOUT,
    ARCHER_ROUND_SCORE {
        override val args: Map<NavArgument, Boolean>
            get() = mapOf(NavArgument.ARCHER_ROUND_ID to false, NavArgument.SCREEN to true)
    },
    CLASSIFICATION_TABLES,
    HANDICAP_TABLES {
        @Composable
        override fun getMenuBarTitle(): String = stringResource(R.string.handicap_tables__title)

        @Composable
        override fun Screen(navController: NavController) {
            HandicapTablesScreen()
        }
    },
    MAIN_MENU {
        @Composable
        override fun getMenuBarTitle(): String = stringResource(R.string.main_menu__title)

        @Composable
        override fun Screen(navController: NavController) {
            MainMenuScreen(navController = navController)
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
