package eywa.projectcodex.components.mainMenu

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.*

class MainMenuScreen : ActionBarHelp {
    private val mainMenuHelpInfo = ComposeHelpShowcaseMap().apply {
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

    @SuppressLint("NotConstructor")
    @Composable
    fun MainMenuScreen(
            isAlertDialogOpen: Boolean,
            onStartNewScoreClicked: () -> Unit,
            onViewScoresClicked: () -> Unit,
            onDialogActionClicked: (Boolean) -> Unit,
    ) {
        Column(
                modifier = Modifier
                        .fillMaxSize()
                        .background(colorResource(id = R.color.colorPrimary)),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                    onClick = onStartNewScoreClicked,
                    modifier = Modifier
                            .updateHelpDialogPosition(mainMenuHelpInfo, R.string.help_main_menu__new_score_title)
            ) {
                Text(stringResource(id = R.string.main_menu__new_score))
            }
            Button(
                    onClick = onViewScoresClicked,
                    modifier = Modifier
                            .updateHelpDialogPosition(mainMenuHelpInfo, R.string.help_main_menu__view_scores_title)
            ) {
                Text(stringResource(id = R.string.main_menu__view_scores))
            }

            SimpleAlertDialog(
                    isOpen = isAlertDialogOpen,
                    title = R.string.main_menu__exit_app_dialog_title,
                    message = R.string.main_menu__exit_app_dialog_body,
                    positiveButton = R.string.main_menu__exit_app_dialog_exit,
                    negativeButton = R.string.general_cancel,
                    onDialogActionClicked = onDialogActionClicked
            )
        }
    }

    override fun getHelpShowcases(): List<HelpShowcaseItem> = mainMenuHelpInfo.getItems()
    override fun getHelpPriority(): Int? = null
}