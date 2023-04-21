package eywa.projectcodex.components.mainMenu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.ActionBarHelp
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.helpShowcase.updateHelpDialogPosition
import eywa.projectcodex.common.sharedUi.*
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme

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
                                R.string.help_main_menu__settings_title,
                                R.string.help_main_menu__settings_body
                        ),
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
            onHandicapTablesClicked: () -> Unit,
            onSettingsClicked: () -> Unit,
            helpListener: (HelpShowcaseIntent) -> Unit,
    ) {
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
                    onClick = onStartNewScoreClicked,
                    modifier = Modifier
                            .updateHelpDialogPosition(helpListener, R.string.help_main_menu__new_score_title)
                            .testTag(TestTag.NEW_SCORE),
            )
            CodexButton(
                    text = stringResource(id = R.string.main_menu__view_scores),
                    buttonStyle = CodexButtonDefaults.DefaultButton(),
                    onClick = onViewScoresClicked,
                    modifier = Modifier
                            .updateHelpDialogPosition(helpListener, R.string.help_main_menu__view_scores_title)
                            .testTag(TestTag.VIEW_SCORES),
            )
            CodexButton(
                    text = stringResource(id = R.string.main_menu__handicap_tables),
                    buttonStyle = CodexButtonDefaults.DefaultButton(),
                    onClick = onHandicapTablesClicked,
                    modifier = Modifier
                            .updateHelpDialogPosition(helpListener, R.string.help_main_menu__handicap_tables_title)
                            .testTag(TestTag.HANDICAP_TABLES),
            )

            Spacer(modifier = Modifier.height(1.dp))

            IconButton(
                    onClick = onSettingsClicked,
                    modifier = Modifier
                            .updateHelpDialogPosition(helpListener, R.string.help_main_menu__settings_title)
                            .testTag(TestTag.SETTINGS)
                            .scale(1.2f),
            ) {
                Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.main_menu__settings),
                        tint = CodexTheme.colors.iconButtonOnPrimary,
                )
            }

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

    object TestTag {
        private const val PREFIX = "MAIN_MENU_"

        const val SCREEN = "${PREFIX}SCREEN"
        const val NEW_SCORE = "${PREFIX}NEW_SCORE_BUTTON"
        const val VIEW_SCORES = "${PREFIX}VIEW_SCORE_BUTTON"
        const val HANDICAP_TABLES = "${PREFIX}HANDICAP_TABLES_BUTTON"
        const val SETTINGS = "${PREFIX}SETTINGS_BUTTON"
    }

    @Preview
    @Composable
    fun PreviewMainMenuScreen() {
        ComposeContent(
                isExitDialogOpen = false,
                onExitAlertClicked = {},
                onStartNewScoreClicked = { },
                onViewScoresClicked = {},
                onHandicapTablesClicked = {},
                onSettingsClicked = {},
                helpListener = {},
        )
    }
}
