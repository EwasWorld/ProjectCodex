package eywa.projectcodex.common.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.ActionBarHelp
import eywa.projectcodex.components.about.AboutScreen
import eywa.projectcodex.components.classificationTables.ClassificationTablesScreen
import eywa.projectcodex.components.emailScores.EmailScoresScreen
import eywa.projectcodex.components.handicapTables.HandicapTablesScreen
import eywa.projectcodex.components.mainMenu.MainMenuScreen
import eywa.projectcodex.components.newScore.NewScoreScreen
import eywa.projectcodex.components.settings.SettingsScreen
import eywa.projectcodex.components.shootDetails.addEnd.AddEndScreen
import eywa.projectcodex.components.shootDetails.editEnd.EditEndScreen
import eywa.projectcodex.components.shootDetails.insertEnd.InsertEndScreen
import eywa.projectcodex.components.shootDetails.scorePad.ScorePadScreen
import eywa.projectcodex.components.sightMarks.SightMarksScreen
import eywa.projectcodex.components.sightMarks.detail.SightMarkDetailScreen
import eywa.projectcodex.components.viewScores.ui.ViewScoresScreen

enum class CodexNavRoute : NavRoute, ActionBarHelp {
    ABOUT {
        @Composable
        override fun getMenuBarTitle(entry: NavBackStackEntry?): String = stringResource(R.string.about__title)

        @Composable
        override fun Screen(navController: NavController) {
            AboutScreen()
        }
    },
    ARCHER_ROUND_SCORE {
        override val args: Map<NavArgument, Boolean>
            get() = mapOf(NavArgument.SHOOT_ID to true, NavArgument.SCREEN to true)

        @Composable
        override fun getMenuBarTitle(entry: NavBackStackEntry?): String = stringResource(R.string.archer_round_title)

        @Composable
        override fun Screen(navController: NavController) {
            TODO()
        }
    },
    CLASSIFICATION_TABLES {
        @Composable
        override fun getMenuBarTitle(entry: NavBackStackEntry?): String =
                stringResource(R.string.classification_tables__title)

        @Composable
        override fun Screen(navController: NavController) {
            ClassificationTablesScreen()
        }
    },
    EMAIL_SCORE {
        @Composable
        override fun getMenuBarTitle(entry: NavBackStackEntry?): String = stringResource(R.string.email_scores__title)

        @Composable
        override fun Screen(navController: NavController) {
            EmailScoresScreen(navController)
        }
    },
    HANDICAP_TABLES {
        @Composable
        override fun getMenuBarTitle(entry: NavBackStackEntry?): String =
                stringResource(R.string.handicap_tables__title)

        @Composable
        override fun Screen(navController: NavController) {
            HandicapTablesScreen()
        }
    },
    MAIN_MENU {
        @Composable
        override fun getMenuBarTitle(entry: NavBackStackEntry?): String = stringResource(R.string.main_menu__title)

        @Composable
        override fun Screen(navController: NavController) {
            MainMenuScreen(navController)
        }
    },
    NEW_SCORE {
        override val args: Map<NavArgument, Boolean>
            get() = mapOf(NavArgument.SHOOT_ID to false)

        @Composable
        override fun getMenuBarTitle(entry: NavBackStackEntry?): String {
            val id = entry?.arguments
                    ?.getInt(NavArgument.SHOOT_ID.toArgName())
                    ?.takeIf { it != DEFAULT_INT_NAV_ARG }
            return stringResource(
                    if (id == null) R.string.create_round__title
                    else R.string.create_round__edit_title
            )
        }

        @Composable
        override fun Screen(navController: NavController) {
            NewScoreScreen(navController)
        }
    },
    SETTINGS {
        @Composable
        override fun getMenuBarTitle(entry: NavBackStackEntry?): String = stringResource(R.string.settings__title)

        @Composable
        override fun Screen(navController: NavController) {
            SettingsScreen()
        }
    },
    SHOOT_DETAILS_EDIT_END {
        override val args: Map<NavArgument, Boolean>
            get() = mapOf(NavArgument.SHOOT_ID to true)

        @Composable
        override fun getMenuBarTitle(entry: NavBackStackEntry?): String = stringResource(R.string.insert_end__title)

        @Composable
        override fun Screen(navController: NavController) {
            EditEndScreen(navController)
        }
    },
    SHOOT_DETAILS_ADD_END {
        override val args: Map<NavArgument, Boolean>
            get() = mapOf(NavArgument.SHOOT_ID to true)

        @Composable
        override fun getMenuBarTitle(entry: NavBackStackEntry?): String = stringResource(R.string.input_end__title)

        @Composable
        override fun Screen(navController: NavController) {
            AddEndScreen(navController)
        }
    },
    SHOOT_DETAILS_INSERT_END {
        override val args: Map<NavArgument, Boolean>
            get() = mapOf(NavArgument.SHOOT_ID to true)

        @Composable
        override fun getMenuBarTitle(entry: NavBackStackEntry?): String = stringResource(R.string.insert_end__title)

        @Composable
        override fun Screen(navController: NavController) {
            InsertEndScreen(navController)
        }
    },
    SHOOT_DETAILS_SCORE_PAD {
        override val args: Map<NavArgument, Boolean>
            get() = mapOf(NavArgument.SHOOT_ID to true)

        @Composable
        override fun getMenuBarTitle(entry: NavBackStackEntry?): String = stringResource(R.string.score_pad__title)

        @Composable
        override fun Screen(navController: NavController) {
            ScorePadScreen(navController)
        }
    },
    SHOOT_DETAILS_SETTINGS {
        override val args: Map<NavArgument, Boolean>
            get() = mapOf(NavArgument.SHOOT_ID to true)

        @Composable
        override fun getMenuBarTitle(entry: NavBackStackEntry?): String =
                stringResource(R.string.archer_round_settings__title)

        @Composable
        override fun Screen(navController: NavController) {
            TODO()
        }
    },
    SHOOT_DETAILS_STATS {
        override val args: Map<NavArgument, Boolean>
            get() = mapOf(NavArgument.SHOOT_ID to true)

        @Composable
        override fun getMenuBarTitle(entry: NavBackStackEntry?): String =
                stringResource(R.string.archer_round_stats__title)

        @Composable
        override fun Screen(navController: NavController) {
            TODO()
        }
    },
    SIGHT_MARKS {
        @Composable
        override fun getMenuBarTitle(entry: NavBackStackEntry?): String = stringResource(R.string.sight_marks__title)

        @Composable
        override fun Screen(navController: NavController) {
            SightMarksScreen(navController)
        }
    },
    SIGHT_MARK_DETAIL {
        override val args: Map<NavArgument, Boolean>
            get() = mapOf(NavArgument.SIGHT_MARK_ID to false)

        @Composable
        override fun getMenuBarTitle(entry: NavBackStackEntry?): String =
                stringResource(R.string.sight_marks__detail_title)

        @Composable
        override fun Screen(navController: NavController) {
            SightMarkDetailScreen(navController)
        }
    },
    VIEW_SCORES {
        @Composable
        override fun getMenuBarTitle(entry: NavBackStackEntry?): String = stringResource(R.string.view_score__title)

        @Composable
        override fun Screen(navController: NavController) {
            ViewScoresScreen(navController)
        }
    },
    ;

    override val routeBase = "main_" + name.lowercase()
    override val args: Map<NavArgument, Boolean> = emptyMap()

    companion object {
        val reverseMap = values().associateBy { it.routeBase }
    }
}
