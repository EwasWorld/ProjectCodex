package eywa.projectcodex.components.archerRoundScore.arrowInputs.editEnd

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.components.archerRoundScore.ArcherRoundIntent
import eywa.projectcodex.components.archerRoundScore.ArcherRoundSubScreen
import eywa.projectcodex.components.archerRoundScore.ArcherRoundsPreviewHelper
import eywa.projectcodex.components.archerRoundScore.arrowInputs.ArrowInputsScaffold
import eywa.projectcodex.components.archerRoundScore.state.ArcherRoundState

class EditEndScreen : ArcherRoundSubScreen() {
    @Composable
    override fun ComposeContent(
            state: ArcherRoundState.Loaded,
            listener: (ArcherRoundIntent) -> Unit,
    ) {
        ScreenContent(state, listener)
    }

    override fun getHelpShowcases(): List<HelpShowcaseItem> {
        // TODO_CURRENT Help info
        return listOf()
    }

    override fun getHelpPriority(): Int? = null

    @Composable
    private fun ScreenContent(
            state: EditEndState,
            listener: (ArcherRoundIntent) -> Unit,
    ) {
        ArrowInputsScaffold(
                state = state,
                showCancelButton = true,
                showResetButton = true,
                contentText = stringResource(R.string.edit_end__edit_info, state.getSelectedEndNumber()),
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
            ScreenContent(ArcherRoundsPreviewHelper.SIMPLE.copy(scorePadSelectedEnd = 0)) {}
        }
    }

}
