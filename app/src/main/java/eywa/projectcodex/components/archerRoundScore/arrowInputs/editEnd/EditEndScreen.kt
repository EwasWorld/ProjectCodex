package eywa.projectcodex.components.archerRoundScore.arrowInputs.editEnd

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import eywa.projectcodex.R
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.components.archerRoundScore.ArcherRoundIntent
import eywa.projectcodex.components.archerRoundScore.ArcherRoundSubScreen
import eywa.projectcodex.components.archerRoundScore.arrowInputs.ArrowInputsScaffold
import eywa.projectcodex.components.archerRoundScore.arrowInputs.ArrowInputsTestTag
import eywa.projectcodex.components.archerRoundScore.state.ArcherRoundState
import eywa.projectcodex.components.archerRoundScore.state.ArcherRoundStatePreviewHelper

class EditEndScreen : ArcherRoundSubScreen() {
    @Composable
    override fun ComposeContent(
            state: ArcherRoundState.Loaded,
            modifier: Modifier,
            listener: (ArcherRoundIntent) -> Unit,
    ) {
        ScreenContent(state, modifier, listener)
    }

    @Composable
    private fun ScreenContent(
            state: EditEndState,
            modifier: Modifier = Modifier,
            listener: (ArcherRoundIntent) -> Unit,
    ) {
        ArrowInputsScaffold(
                state = state,
                showCancelButton = true,
                showResetButton = true,
                contentText = stringResource(R.string.edit_end__edit_info, state.selectedEndNumber),
                modifier = modifier,
                helpListener = { listener(ArcherRoundIntent.HelpShowcaseAction(it)) },
                submitHelpInfoTitle = stringResource(R.string.help_edit_end__complete_title),
                submitHelpInfoBody = stringResource(R.string.help_edit_end__complete_body),
                cancelHelpInfoTitle = stringResource(R.string.help_edit_end__cancel_title),
                cancelHelpInfoBody = stringResource(R.string.help_edit_end__cancel_body),
                testTag = ArrowInputsTestTag.EDIT_SCREEN,
                listener = listener,
        )
    }

    @Preview(
            showBackground = true,
            backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
            heightDp = 700,
    )
    @Composable
    fun EditEndScreen_Preview() {
        CodexTheme {
            ScreenContent(ArcherRoundStatePreviewHelper.SIMPLE.copy(scorePadSelectedEnd = 0)) {}
        }
    }

}
