package eywa.projectcodex.components.archerRoundScore.arrowInputs.editEnd

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import eywa.projectcodex.R
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.components.archerRoundScore.ArcherRoundIntent
import eywa.projectcodex.components.archerRoundScore.ArcherRoundsPreviewHelper
import eywa.projectcodex.components.archerRoundScore.arrowInputs.ArrowInputsScaffold

@Composable
fun EditEndScreen(
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
        EditEndScreen(ArcherRoundsPreviewHelper.SIMPLE.copy(scorePadSelectedEnd = 0)) {}
    }
}
