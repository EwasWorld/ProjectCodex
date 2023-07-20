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
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpState
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.sharedUi.*
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.components.mainMenu.MainMenuIntent.*
import kotlin.system.exitProcess

@Composable
fun MainMenuScreen(
        navController: NavController,
        viewModel: MainMenuViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    MainMenuScreen(state = state, listener = { viewModel.handle(it) })

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
private fun MainMenuScreen(
        state: MainMenuState,
        listener: (MainMenuIntent) -> Unit,
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(HelpShowcaseAction(it)) }

    Column(
            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                    .fillMaxSize()
                    .background(CodexTheme.colors.appBackground)
                    .testTag(MainMenuTestTag.SCREEN.getTestTag())
    ) {

        CodexButton(
                text = stringResource(id = R.string.main_menu__new_score),
                onClick = { listener(Navigate(CodexNavRoute.NEW_SCORE)) },
                helpState = HelpState(
                        helpListener = helpListener,
                        helpTitle = stringResource(R.string.help_main_menu__new_score_title),
                        helpBody = stringResource(R.string.help_main_menu__new_score_body),
                ),
                modifier = Modifier.testTag(MainMenuTestTag.NEW_SCORE_BUTTON.getTestTag())
        )
        CodexButton(
                text = stringResource(id = R.string.main_menu__view_scores),
                onClick = { listener(Navigate(CodexNavRoute.VIEW_SCORES)) },
                helpState = HelpState(
                        helpListener = helpListener,
                        helpTitle = stringResource(R.string.help_main_menu__view_scores_title),
                        helpBody = stringResource(R.string.help_main_menu__view_scores_body),
                ),
                modifier = Modifier.testTag(MainMenuTestTag.VIEW_SCORE_BUTTON.getTestTag())
        )
        CodexButton(
                text = stringResource(id = R.string.main_menu__handicap_tables),
                onClick = { listener(Navigate(CodexNavRoute.HANDICAP_TABLES)) },
                helpState = HelpState(
                        helpListener = helpListener,
                        helpTitle = stringResource(R.string.help_main_menu__handicap_tables_title),
                        helpBody = stringResource(R.string.help_main_menu__handicap_tables_body),
                ),
                modifier = Modifier.testTag(MainMenuTestTag.HANDICAP_TABLES_BUTTON.getTestTag())
        )
        if (state.useBetaFeatures) {
            CodexButton(
                    text = stringResource(id = R.string.main_menu__classification_tables),
                    onClick = { listener(Navigate(CodexNavRoute.CLASSIFICATION_TABLES)) },
                    helpState = HelpState(
                            helpListener = helpListener,
                            helpTitle = stringResource(R.string.help_main_menu__classification_tables_title),
                            helpBody = stringResource(R.string.help_main_menu__classification_tables_body),
                    ),
                    modifier = Modifier.testTag(MainMenuTestTag.CLASSIFICATION_TABLES_BUTTON.getTestTag())
            )
        }
        CodexButton(
                text = stringResource(id = R.string.main_menu__sight_marks),
                onClick = { listener(Navigate(CodexNavRoute.SIGHT_MARKS)) },
                helpState = HelpState(
                        helpListener = helpListener,
                        helpTitle = stringResource(R.string.help_main_menu__sight_marks_title),
                        helpBody = stringResource(R.string.help_main_menu__sight_marks_body),
                ),
                modifier = Modifier.testTag(MainMenuTestTag.SIGHT_MARKS_BUTTON.getTestTag())
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
                    helpState = HelpState(
                            helpListener = helpListener,
                            helpTitle = stringResource(R.string.help_main_menu__settings_title),
                            helpBody = stringResource(R.string.help_main_menu__settings_body),
                    ),
                    modifier = Modifier.testTag(MainMenuTestTag.SETTINGS_BUTTON.getTestTag())
            )
            CodexIconButton(
                    onClick = { listener(Navigate(CodexNavRoute.ABOUT)) },
                    icon = Icons.Outlined.Info,
                    contentDescription = stringResource(R.string.main_menu__about),
                    helpState = HelpState(
                            helpListener = helpListener,
                            helpTitle = stringResource(R.string.help_main_menu__about_title),
                            helpBody = stringResource(R.string.help_main_menu__about_body),
                    ),
                    modifier = Modifier.testTag(MainMenuTestTag.ABOUT_BUTTON.getTestTag())
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

enum class MainMenuTestTag : CodexTestTag {
    SCREEN,
    NEW_SCORE_BUTTON,
    VIEW_SCORE_BUTTON,
    HANDICAP_TABLES_BUTTON,
    CLASSIFICATION_TABLES_BUTTON,
    SIGHT_MARKS_BUTTON,
    SETTINGS_BUTTON,
    ABOUT_BUTTON,
    ;

    override val screenName: String
        get() = "MAIN_MENU"

    override fun getElement(): String = name
}

@Preview
@Composable
fun PreviewMainMenuScreen() {
    MainMenuScreen(
            state = MainMenuState(),
            listener = {},
    )
}
