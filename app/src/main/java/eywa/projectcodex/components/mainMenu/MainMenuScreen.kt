package eywa.projectcodex.components.mainMenu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.*
import eywa.projectcodex.common.sharedUi.*
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme

class MainMenuScreen : ActionBarHelp {
    private val helpInfo = ComposeHelpShowcaseMap().apply {
        add(
                ComposeHelpShowcaseItem(
                        R.string.help_main_menu__new_score_title,
                        R.string.help_main_menu__new_score_body
                )
        )
        add(
                ComposeHelpShowcaseItem(
                        R.string.help_main_menu__view_scores_title,
                        R.string.help_main_menu__view_scores_body
                )
        )
    }

    // TODO Fix button focus order - currently app starts focussed on the help icon in the action bar
    @Composable
    fun ComposeContent(
            isExitDialogOpen: Boolean,
            onExitAlertClicked: (Boolean) -> Unit,
            onStartNewScoreClicked: () -> Unit,
            onViewScoresClicked: () -> Unit,
    ) {
        Column(
                modifier = Modifier
                        .fillMaxSize()
                        .background(CodexTheme.colors.appBackground),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
        ) {

            CodexButton(
                    text = stringResource(id = R.string.main_menu__new_score),
                    buttonStyle = CodexButtonDefaults.ButtonOnAppBackground,
                    onClick = onStartNewScoreClicked,
                    modifier = Modifier
                            .updateHelpDialogPosition(helpInfo, R.string.help_main_menu__new_score_title)
                            .testTag(TestTag.NEW_SCORE),
            )
            CodexButton(
                    text = stringResource(id = R.string.main_menu__view_scores),
                    buttonStyle = CodexButtonDefaults.ButtonOnAppBackground,
                    onClick = onViewScoresClicked,
                    modifier = Modifier
                            .updateHelpDialogPosition(helpInfo, R.string.help_main_menu__view_scores_title)
                            .testTag(TestTag.VIEW_SCORES),
            )

            SimpleDialog(
                    isShown = isExitDialogOpen,
                    onDismissListener = { onExitAlertClicked(false) }
            ) {
                SimpleDialogContent(
                        title = R.string.main_menu__exit_app_dialog_title,
                        message = R.string.main_menu__exit_app_dialog_body,
                        positiveButton = ButtonState(
                                text = stringResource(R.string.main_menu__exit_app_dialog_exit),
                                onClick = { onExitAlertClicked(true) },
                        ),
                        negativeButton = ButtonState(
                                text = stringResource(R.string.general_cancel),
                                onClick = { onExitAlertClicked(false) },
                        ),
                )
            }
        }
    }

    override fun getHelpShowcases(): List<HelpShowcaseItem> = helpInfo.getItems()
    override fun getHelpPriority(): Int? = null

    object TestTag {
        const val NEW_SCORE = "MAIN_MENU_NEW_SCORE_BUTTON"
        const val VIEW_SCORES = "MAIN_MENU_VIEW_SCORE_BUTTON"
    }

    @Preview
    @Composable
    fun PreviewMainMenuScreen() {
        ComposeContent(
                isExitDialogOpen = false,
                onExitAlertClicked = {},
                onStartNewScoreClicked = { },
                onViewScoresClicked = {}
        )
    }
}