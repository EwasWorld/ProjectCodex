package eywa.projectcodex.components.archerRoundScore

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.ActionBarHelp
import eywa.projectcodex.common.sharedUi.ButtonState
import eywa.projectcodex.common.sharedUi.SimpleDialog
import eywa.projectcodex.common.sharedUi.SimpleDialogContent
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.components.archerRoundScore.state.ArcherRoundScreen
import eywa.projectcodex.components.archerRoundScore.state.ArcherRoundState
import eywa.projectcodex.components.archerRoundScore.state.ArcherRoundState.Loaded
import eywa.projectcodex.components.archerRoundScore.state.ArcherRoundState.Loading

abstract class ArcherRoundSubScreen : ActionBarHelp {
    @Composable
    abstract fun ComposeContent(state: Loaded, listener: (ArcherRoundIntent) -> Unit)
}

class ArcherRoundMainScreen : ActionBarHelp {
    private var currentScreen: ArcherRoundSubScreen? = null

    @Composable
    fun ComposeContent(
            state: ArcherRoundState,
            listener: (ArcherRoundIntent) -> Unit,
    ) {
        currentScreen = when (state) {
            is Loading -> null
            is Loaded -> state.currentScreen.getScreen()
        }

        SimpleDialog(
                isShown = (state as? Loaded)?.displayRoundCompletedDialog == true,
                onDismissListener = { listener(ArcherRoundIntent.RoundCompleteDialogOkClicked) },
        ) {
            SimpleDialogContent(
                    title = stringResource(R.string.input_end__round_complete),
                    positiveButton = ButtonState(
                            text = stringResource(R.string.input_end__go_to_summary),
                            onClick = { listener(ArcherRoundIntent.RoundCompleteDialogOkClicked) }
                    ),
            )
        }
        SimpleDialog(
                isShown = (state as? Loaded)?.displayCannotInputEndDialog == true,
                onDismissListener = { listener(ArcherRoundIntent.CannotInputEndDialogOkClicked) },
        ) {
            SimpleDialogContent(
                    title = stringResource(R.string.input_end__cannot_open_input_end_title),
                    message = stringResource(R.string.input_end__cannot_open_input_end_body),
                    positiveButton = ButtonState(
                            text = stringResource(R.string.general_ok),
                            onClick = { listener(ArcherRoundIntent.CannotInputEndDialogOkClicked) }
                    ),
            )
        }

        Column {
            Box(
                    modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .background(CodexTheme.colors.appBackground)
            ) {
                currentScreen?.ComposeContent(state as Loaded, listener)
                        ?: CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            if (state.showNavBar) {
                ArcherRoundBottomNavBar(
                        currentScreen = (state as? Loaded)?.currentScreen,
                        listener = { listener(ArcherRoundIntent.NavBarClicked(it)) },
                )
            }
        }
    }

    override fun getHelpShowcases() = currentScreen?.getHelpShowcases() ?: listOf()
    override fun getHelpPriority() = currentScreen?.getHelpPriority()

    object TestTag {
        private const val PREFIX = "ARCHER_ROUND_SCREEN_"

        fun bottomNavBarItem(screen: ArcherRoundScreen): String {
            require(screen.bottomNavItemInfo != null) { "${screen.name} isn't on the nav bar" }
            return "${PREFIX}BOTTOM_NAV_BAR_" + screen.name
        }
    }
}
