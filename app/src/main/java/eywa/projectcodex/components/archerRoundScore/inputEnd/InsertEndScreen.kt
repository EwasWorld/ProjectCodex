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
fun InsertEndScreen(
        state: ArcherRoundState.Loaded,
        listener: (ArcherRoundIntent) -> Unit,
) {
    val endNumber = state.scorePadSelectedRow!!

    val insertLocationString = if (endNumber == 0) {
        stringResource(R.string.insert_end__info_at_start)
    }
    else {
        stringResource(R.string.insert_end__info, endNumber, endNumber + 1)
    }

    InputEndScaffold(
            showReset = false,
            inputArrows = state.currentScreenInputArrows,
            round = state.fullArcherRoundInfo.round,
            endSize = state.currentScreenEndSize,
            contentText = insertLocationString,
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
fun InsertEndScreen_Preview() {
    CodexTheme {
        InsertEndScreen(ArcherRoundPreviewHelper.SIMPLE.copy(scorePadSelectedRow = 1)) {}
    }
}
