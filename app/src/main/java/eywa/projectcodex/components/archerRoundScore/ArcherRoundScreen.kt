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
import eywa.projectcodex.common.sharedUi.ButtonState
import eywa.projectcodex.common.sharedUi.SimpleDialog
import eywa.projectcodex.common.sharedUi.SimpleDialogContent
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.components.archerRoundScore.archerRoundStats.ArcherRoundStatsScreen
import eywa.projectcodex.components.archerRoundScore.arrowInputs.editEnd.EditEndScreen
import eywa.projectcodex.components.archerRoundScore.arrowInputs.inputEnd.InputEndScreen
import eywa.projectcodex.components.archerRoundScore.arrowInputs.insertEnd.InsertEndScreen
import eywa.projectcodex.components.archerRoundScore.scorePad.ScorePadScreen
import eywa.projectcodex.components.archerRoundScore.settings.ArcherRoundSettingsScreen
import eywa.projectcodex.components.archerRoundScore.state.ArcherRoundScreen
import eywa.projectcodex.components.archerRoundScore.state.ArcherRoundState
import eywa.projectcodex.components.archerRoundScore.state.ArcherRoundState.Loaded
import eywa.projectcodex.components.archerRoundScore.state.ArcherRoundState.Loading

@Composable
fun ArcherRoundScreen(
        state: ArcherRoundState,
        listener: (ArcherRoundIntent) -> Unit,
) {
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

    Column {
        Box(
                modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .background(CodexTheme.colors.appBackground)
        ) {
            when (state) {
                is Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is Loaded -> when (state.currentScreen) {
                    ArcherRoundScreen.INPUT_END -> InputEndScreen(state, listener)
                    ArcherRoundScreen.SCORE_PAD -> ScorePadScreen(state = state, listener = listener)
                    ArcherRoundScreen.STATS -> ArcherRoundStatsScreen(
                            state = state,
                            noArrowsListener = { listener(ArcherRoundIntent.NoArrowsDialogOkClicked) },
                    )
                    ArcherRoundScreen.SETTINGS -> ArcherRoundSettingsScreen(state, listener)
                    ArcherRoundScreen.INSERT_END -> InsertEndScreen(state, listener)
                    ArcherRoundScreen.EDIT_END -> EditEndScreen(state, listener)
                }
            }
        }
        if (state.showNavBar) {
            ArcherRoundBottomNavBar(
                    currentScreen = (state as? Loaded)?.currentScreen,
                    listener = { listener(ArcherRoundIntent.NavBarClicked(it)) },
            )
        }
    }
}
