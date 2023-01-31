package eywa.projectcodex.components.archerRoundScore.arrowInputs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.archeryObjects.Arrow
import eywa.projectcodex.common.sharedUi.CodexButton
import eywa.projectcodex.common.sharedUi.CodexButtonDefaults
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.utils.get
import eywa.projectcodex.components.archerRoundScore.ArcherRoundIntent
import eywa.projectcodex.components.archerRoundScore.ArcherRoundIntent.ArrowInputsIntent.*
import eywa.projectcodex.components.archerRoundScore.ArcherRoundsPreviewHelper
import eywa.projectcodex.components.archerRoundScore.arrowInputs.arrowButton.ArrowButtonGroup
import eywa.projectcodex.database.rounds.Round

@Composable
fun ArrowInputs(
        state: ArrowInputsState,
        showResetButton: Boolean,
        listener: (ArcherRoundIntent.ArrowInputsIntent) -> Unit,
) {
    Column(
            horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
                text = state.getEnteredArrows().sumOf { it.score }.toString(),
                style = CodexTypography.X_LARGE,
                color = CodexTheme.colors.onAppBackground,
                modifier = Modifier.testTag(ArrowInputsTestTag.END_TOTAL_TEXT)
        )
        @Suppress("SimplifiableCallChain")
        Text(
                text = state.getEnteredArrows()
                        .map { it.asString().get() }
                        .plus(
                                List(state.getEndSize() - state.getEnteredArrows().size) {
                                    stringResource(R.string.end_to_string_arrow_placeholder)
                                }
                        )
                        .joinToString(stringResource(R.string.end_to_string_arrow_deliminator)),
                style = CodexTypography.X_LARGE,
                color = CodexTheme.colors.onAppBackground,
                modifier = Modifier
                        .padding(bottom = 15.dp)
                        .testTag(ArrowInputsTestTag.END_ARROWS_TEXT)
        )

        ArrowButtonGroup(round = state.getRound(), onClick = { listener(ArrowInputted(it)) })

        Row {
            if (showResetButton) {
                CodexButton(
                        text = stringResource(R.string.general__reset_edits),
                        buttonStyle = CodexButtonDefaults.DefaultTextButton,
                        onClick = { listener(ResetArrowsInputted) },
                        modifier = Modifier.testTag(ArrowInputsTestTag.RESET_BUTTON)
                )
            }
            CodexButton(
                    text = stringResource(R.string.input_end__clear),
                    buttonStyle = CodexButtonDefaults.DefaultTextButton,
                    onClick = { listener(ClearArrowsInputted) },
                    modifier = Modifier.testTag(ArrowInputsTestTag.CLEAR_BUTTON)
            )
            CodexButton(
                    text = stringResource(R.string.input_end__backspace),
                    buttonStyle = CodexButtonDefaults.DefaultTextButton,
                    onClick = { listener(BackspaceArrowsInputted) },
                    modifier = Modifier.testTag(ArrowInputsTestTag.BACKSPACE_BUTTON)
            )
        }
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun ArrowInputs_Preview(
) {
    CodexTheme {
        ArrowInputs(
                object : ArrowInputsState {
                    override fun getRound(): Round = ArcherRoundsPreviewHelper.round.round
                    override fun getEnteredArrows(): List<Arrow> = ArcherRoundsPreviewHelper.inputArrows
                    override fun getEndSize(): Int = 6
                },
                true
        ) {}
    }
}
