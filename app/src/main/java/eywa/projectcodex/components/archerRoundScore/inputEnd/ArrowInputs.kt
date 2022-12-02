package eywa.projectcodex.components.archerRoundScore.inputEnd

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import eywa.projectcodex.database.rounds.Round

@Composable
fun ArrowInputs(
        showReset: Boolean,
        inputArrows: List<Arrow>,
        round: Round?,
        endSize: Int,
        listener: (ArcherRoundIntent.ArrowInputsIntent) -> Unit,
) {
    Column(
            horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
                text = inputArrows.sumOf { it.score }.toString(),
                style = CodexTypography.X_LARGE,
                color = CodexTheme.colors.onAppBackground,
        )
        @Suppress("SimplifiableCallChain")
        Text(
                text = inputArrows
                        .map { it.asString().get() }
                        .plus(
                                List(endSize - inputArrows.size) {
                                    stringResource(R.string.end_to_string_arrow_placeholder)
                                }
                        )
                        .joinToString(stringResource(R.string.end_to_string_arrow_deliminator)),
                style = CodexTypography.X_LARGE,
                color = CodexTheme.colors.onAppBackground,
                modifier = Modifier.padding(bottom = 15.dp)
        )

        ArrowButtonGroup(round = round, onClick = { listener(ArrowInputted(it)) })

        Row {
            if (showReset) {
                CodexButton(
                        text = stringResource(R.string.general__reset_edits),
                        buttonStyle = CodexButtonDefaults.DefaultTextButton,
                        onClick = { listener(ResetArrowsInputted) },
                )
            }
            CodexButton(
                    text = stringResource(R.string.input_end__clear),
                    buttonStyle = CodexButtonDefaults.DefaultTextButton,
                    onClick = { listener(ClearArrowsInputted) },
            )
            CodexButton(
                    text = stringResource(R.string.input_end__backspace),
                    buttonStyle = CodexButtonDefaults.DefaultTextButton,
                    onClick = { listener(BackspaceArrowsInputted) },
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
                true,
                ArcherRoundsPreviewHelper.inputArrows,
                ArcherRoundsPreviewHelper.round.round,
                6,
        ) {}
    }
}