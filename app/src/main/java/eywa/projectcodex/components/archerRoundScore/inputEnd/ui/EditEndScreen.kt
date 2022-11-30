package eywa.projectcodex.components.archerRoundScore.inputEnd.ui

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
        state: ArcherRoundState,
        listener: (ArcherRoundIntent) -> Unit,
) {
    InputEndScaffold(
            showReset = true,
            inputArrows = state.inputArrows,
            round = state.fullArcherRoundInfo.round,
            endSize = state.inputEndSize,
            contentText = stringResource(R.string.edit_end__edit_info, state.isEditingEndNumber!! + 1),
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
        EditEndScreen(ArcherRoundPreviewHelper.SIMPLE.copy(isEditingEndNumber = 0)) {}
    }
}
