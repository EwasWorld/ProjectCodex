package eywa.projectcodex.components.mainMenu

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.ActionBarHelp
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.helpShowcase.updateHelpDialogPosition
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.sharedUi.*
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.components.mainMenu.MainMenuIntent.*
import kotlin.system.exitProcess

class MainMenuScreen : ActionBarHelp {
    private fun addHelpInfo(
            helpListener: (HelpShowcaseIntent) -> Unit,
    ) {
        helpListener(
                HelpShowcaseIntent.Add(
                        HelpShowcaseItem(
                                R.string.help_main_menu__new_score_title,
                                R.string.help_main_menu__new_score_body
                        ),
                )
        )
        helpListener(
                HelpShowcaseIntent.Add(
                        HelpShowcaseItem(
                                R.string.help_main_menu__view_scores_title,
                                R.string.help_main_menu__view_scores_body
                        ),
                )
        )
        helpListener(
                HelpShowcaseIntent.Add(
                        HelpShowcaseItem(
                                R.string.help_main_menu__handicap_tables_title,
                                R.string.help_main_menu__handicap_tables_body
                        ),
                )
        )
        helpListener(
                HelpShowcaseIntent.Add(
                        HelpShowcaseItem(
                                R.string.help_main_menu__sight_marks_title,
                                R.string.help_main_menu__sight_marks_body
                        ),
                )
        )
        helpListener(
                HelpShowcaseIntent.Add(
                        HelpShowcaseItem(
                                R.string.help_main_menu__settings_title,
                                R.string.help_main_menu__settings_body
                        ),
                )
        )
        helpListener(
                HelpShowcaseIntent.Add(
                        HelpShowcaseItem(
                                R.string.help_main_menu__about_title,
                                R.string.help_main_menu__about_body
                        ),
                )
        )
    }

    @Composable
    fun ComposeContent(
            viewModel: MainMenuViewModel = hiltViewModel(),
            navController: NavController,
    ) {
        val state by viewModel.state.collectAsState()
        ComposeContent(state = state, listener = { viewModel.handle(it) })

        BackHandler(!state.isHelpShowcaseInProgress && !state.isExitDialogOpen) {
            viewModel.handle(OpenExitDialog)
        }

        val activity = LocalContext.current as Activity
        LaunchedEffect(state.navigateTo, state.closeApplication) {
            state.navigateTo?.let {
                it.navigate(navController)
                viewModel.handle(NavigateHandled)
            }
            if (state.closeApplication) {
                activity.finish()
                exitProcess(0)
            }
        }
    }

    // TODO Fix button focus order - currently app starts focussed on the help icon in the action bar
    @Composable
    fun ComposeContent(
            state: MainMenuState,
            listener: (MainMenuIntent) -> Unit,
    ) {
        val helpListener = { it: HelpShowcaseIntent -> listener(HelpShowcaseAction(it)) }
        addHelpInfo(helpListener)

        Column(
                verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                        .fillMaxSize()
                        .background(CodexTheme.colors.appBackground)
                        .testTag(TestTag.SCREEN)
        ) {

            CodexButton(
                    text = stringResource(id = R.string.main_menu__new_score),
                    buttonStyle = CodexButtonDefaults.DefaultButton(),
                    onClick = { listener(Navigate(CodexNavRoute.NEW_SCORE)) },
                    modifier = Modifier
                            .updateHelpDialogPosition(helpListener, R.string.help_main_menu__new_score_title)
                            .testTag(TestTag.NEW_SCORE)
            )
            CodexButton(
                    text = stringResource(id = R.string.main_menu__view_scores),
                    buttonStyle = CodexButtonDefaults.DefaultButton(),
                    onClick = { listener(Navigate(CodexNavRoute.VIEW_SCORES)) },
                    modifier = Modifier
                            .updateHelpDialogPosition(helpListener, R.string.help_main_menu__view_scores_title)
                            .testTag(TestTag.VIEW_SCORES)
            )
            CodexButton(
                    text = stringResource(id = R.string.main_menu__handicap_tables),
                    buttonStyle = CodexButtonDefaults.DefaultButton(),
                    onClick = { listener(Navigate(CodexNavRoute.HANDICAP_TABLES)) },
                    modifier = Modifier
                            .updateHelpDialogPosition(helpListener, R.string.help_main_menu__handicap_tables_title)
                            .testTag(TestTag.HANDICAP_TABLES)
            )
            if (state.useBetaFeatures) {
                CodexButton(
                        text = stringResource(id = R.string.main_menu__classification_tables),
                        buttonStyle = CodexButtonDefaults.DefaultButton(),
                        onClick = { listener(Navigate(CodexNavRoute.CLASSIFICATION_TABLES)) },
                        modifier = Modifier
                )
            }
            CodexButton(
                    text = stringResource(id = R.string.main_menu__sight_marks),
                    buttonStyle = CodexButtonDefaults.DefaultButton(),
                    onClick = { listener(Navigate(CodexNavRoute.SIGHT_MARKS)) },
                    modifier = Modifier
                            .updateHelpDialogPosition(helpListener, R.string.help_main_menu__sight_marks_title)
                            .testTag(TestTag.SIGHT_MARKS)
            )

            Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 10.dp)
            ) {
                CodexIconButton(
                        onClick = { listener(Navigate(CodexNavRoute.SETTINGS)) },
                        icon = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.main_menu__settings),
                        modifier = Modifier
                                .updateHelpDialogPosition(helpListener, R.string.help_main_menu__settings_title)
                                .testTag(TestTag.SETTINGS)
                )
                CodexIconButton(
                        onClick = { listener(Navigate(CodexNavRoute.ABOUT)) },
                        icon = Icons.Outlined.Info,
                        contentDescription = stringResource(R.string.main_menu__about),
                        modifier = Modifier
                                .updateHelpDialogPosition(helpListener, R.string.help_main_menu__about_title)
                                .testTag(TestTag.ABOUT)
                )
            }

            SimpleDialog(
                    isShown = state.isExitDialogOpen,
                    onDismissListener = { listener(ExitDialogCloseClicked) }
            ) {
                SimpleDialogContent(
                        title = R.string.main_menu__exit_app_dialog_title,
                        message = R.string.main_menu__exit_app_dialog_body,
                        positiveButton = ButtonState(
                                text = stringResource(R.string.main_menu__exit_app_dialog_exit),
                                onClick = { listener(ExitDialogOkClicked) },
                        ),
                        negativeButton = ButtonState(
                                text = stringResource(R.string.general_cancel),
                                onClick = { listener(ExitDialogCloseClicked) },
                        ),
                )
            }
            SimpleDialog(
                    isShown = state.isHandicapNoticeDialogOpen,
                    onDismissListener = { listener(HandicapDialogClicked) }
            ) {
                SimpleDialogContent(
                        title = R.string.main_menu__handicap_dialog_title,
                        message = R.string.main_menu__handicap_dialog_body,
                        positiveButton = ButtonState(
                                text = stringResource(R.string.general_ok),
                                onClick = { listener(HandicapDialogClicked) },
                        ),
                )
            }
        }
    }

    object TestTag {
        private const val PREFIX = "MAIN_MENU_"

        const val SCREEN = "${PREFIX}SCREEN"
        const val NEW_SCORE = "${PREFIX}NEW_SCORE_BUTTON"
        const val VIEW_SCORES = "${PREFIX}VIEW_SCORE_BUTTON"
        const val HANDICAP_TABLES = "${PREFIX}HANDICAP_TABLES_BUTTON"
        const val SIGHT_MARKS = "${PREFIX}SIGHT_MARKS_BUTTON"
        const val SETTINGS = "${PREFIX}SETTINGS_BUTTON"
        const val ABOUT = "${PREFIX}ABOUT_BUTTON"
    }

    @Preview
    @Composable
    fun PreviewMainMenuScreen() {
        ComposeContent(
                state = MainMenuState(),
                listener = {},
        )
    }
}
