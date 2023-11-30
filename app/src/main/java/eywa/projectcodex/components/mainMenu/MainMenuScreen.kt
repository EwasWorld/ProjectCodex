package eywa.projectcodex.components.mainMenu

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import eywa.projectcodex.common.sharedUi.ButtonState
import eywa.projectcodex.common.sharedUi.CodexButton
import eywa.projectcodex.common.sharedUi.CodexButtonDefaults
import eywa.projectcodex.common.sharedUi.CodexIconButton
import eywa.projectcodex.common.sharedUi.SimpleDialog
import eywa.projectcodex.common.sharedUi.SimpleDialogContent
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexThemeColors
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.components.mainMenu.MainMenuIntent.ExitDialogCloseClicked
import eywa.projectcodex.components.mainMenu.MainMenuIntent.ExitDialogOkClicked
import eywa.projectcodex.components.mainMenu.MainMenuIntent.HelpShowcaseAction
import eywa.projectcodex.components.mainMenu.MainMenuIntent.Navigate
import eywa.projectcodex.components.mainMenu.MainMenuIntent.NavigateHandled
import eywa.projectcodex.components.mainMenu.MainMenuIntent.OpenExitDialog
import eywa.projectcodex.components.mainMenu.MainMenuIntent.WhatsNewClose
import eywa.projectcodex.components.mainMenu.MainMenuIntent.WhatsNewOpen
import kotlin.system.exitProcess

@Composable
fun MainMenuScreen(
        navController: NavController,
        viewModel: MainMenuViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    MainMenuScreen(options = viewModel.options, state = state, listener = { viewModel.handle(it) })

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

@Composable
private fun MainMenuScreen(
        options: Set<MainMenuOption>,
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
        val (iconButtons, defaultButtons) = options.partition { it.buttonTitle == null }

        defaultButtons
                .sortedBy { it.order }
                .forEach {
                    if (it.shouldShow(state)) {
                        val style = object : CodexButtonDefaults.DefaultButton() {
                            override fun getBackgroundColor(themeColors: CodexThemeColors): Color =
                                    if (it is MainMenuDefaultOptions) super.getBackgroundColor(themeColors)
                                    else Color.Magenta
                        }

                        CodexButton(
                                text = it.buttonTitle!!.get(),
                                onClick = { listener(Navigate(it.navRoute)) },
                                buttonStyle = style,
                                helpState = HelpState(
                                        helpListener = helpListener,
                                        helpTitle = it.helpTitle.get(),
                                        helpBody = it.helpBody.get(),
                                ),
                                modifier = Modifier.testTag(it.testTag.getTestTag())
                        )
                    }
                }

        Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 10.dp)
        ) {
            iconButtons
                    .sortedBy { it.order }
                    .forEach {
                        CodexIconButton(
                                onClick = { listener(Navigate(it.navRoute)) },
                                icon = it.icon!!,
                                helpState = HelpState(
                                        helpListener = helpListener,
                                        helpTitle = it.helpTitle.get(),
                                        helpBody = it.helpBody.get(),
                                ),
                                modifier = Modifier.testTag(it.testTag.getTestTag())
                        )
                    }
        }

        WhatsNewButtonAndDialog(
                isDialogShown = state.whatsNewDialogOpen,
                lastShownAppVersion = state.whatsNewDialogLastSeenAppVersion,
                onDialogDismiss = { listener(WhatsNewClose(it)) },
                helpListener = helpListener,
                buttonOnClick = { listener(WhatsNewOpen) },
        )

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
    }
}

enum class MainMenuTestTag : CodexTestTag {
    SCREEN,
    NEW_SCORE_BUTTON,
    VIEW_SCORE_BUTTON,
    REFERENCE_TABLES_BUTTON,
    CALENDAR_BUTTON,
    SIGHT_MARKS_BUTTON,
    SETTINGS_BUTTON,
    ABOUT_BUTTON,
    ARCHER_INFO_BUTTON,
    ;

    override val screenName: String
        get() = "MAIN_MENU"

    override fun getElement(): String = name
}

@Preview
@Composable
fun PreviewMainMenuScreen() {
    MainMenuScreen(
            options = MainMenuDefaultOptions.values().toSet(),
            state = MainMenuState(),
            listener = {},
    )
}
