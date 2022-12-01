package eywa.projectcodex.components.archerRoundScore.inputEnd

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import eywa.projectcodex.R
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.components.archerRoundScore.ArcherRoundIntent
import eywa.projectcodex.components.archerRoundScore.ArcherRoundPreviewHelper
import eywa.projectcodex.components.archerRoundScore.ArcherRoundState

@Composable
fun EditEndScreen(
        state: ArcherRoundState.Loaded,
        listener: (ArcherRoundIntent) -> Unit,
) {
    InputEndScaffold(
            showReset = true,
            inputArrows = state.currentScreenInputArrows,
            round = state.fullArcherRoundInfo.round,
            endSize = state.currentScreenEndSize,
            contentText = stringResource(R.string.edit_end__edit_info, state.scorePadSelectedRow!! + 1),
            showCancelButton = true,
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
        EditEndScreen(ArcherRoundPreviewHelper.SIMPLE.copy(scorePadSelectedRow = 0)) {}
    }
}
