package eywa.projectcodex.components.archerRoundScore.arrowInputs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import eywa.projectcodex.R
import eywa.projectcodex.common.archeryObjects.Arrow
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.helpShowcase.updateHelpDialogPosition
import eywa.projectcodex.common.sharedUi.CodexButton
import eywa.projectcodex.common.sharedUi.CodexButtonDefaults
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.utils.get
import eywa.projectcodex.components.archerRoundScore.ArcherRoundIntent
import eywa.projectcodex.components.archerRoundScore.ArcherRoundIntent.ArrowInputsIntent.*
import eywa.projectcodex.components.archerRoundScore.arrowInputs.arrowButton.ArrowButtonGroup
import eywa.projectcodex.components.archerRoundScore.state.ArcherRoundStatePreviewHelper
import eywa.projectcodex.database.rounds.Round

@Composable
fun ArrowInputs(
        state: ArrowInputsState,
        showResetButton: Boolean,
        verticalPadding: Dp = 0.dp,
        horizontalPadding: Dp = 0.dp,
        helpListener: (HelpShowcaseIntent) -> Unit,
        listener: (ArcherRoundIntent.ArrowInputsIntent) -> Unit,
) {
    HelpInfoItems(showResetButton, helpListener)

    Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = verticalPadding)
    ) {
        Text(
                text = state.getEnteredArrows().sumOf { it.score }.toString(),
                style = CodexTypography.X_LARGE,
                color = CodexTheme.colors.onAppBackground,
                modifier = Modifier
                        .testTag(ArrowInputsTestTag.END_TOTAL_TEXT)
                        .updateHelpDialogPosition(helpListener, R.string.help_input_end__end_inputs_total_title)
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
                textAlign = TextAlign.Center,
                color = CodexTheme.colors.onAppBackground,
                modifier = Modifier
                        .padding(bottom = 15.dp)
                        .testTag(ArrowInputsTestTag.END_ARROWS_TEXT)
                        .updateHelpDialogPosition(helpListener, R.string.help_input_end__end_inputs_arrows_title)
        )

        ArrowButtonGroup(
                round = state.getRound(),
                onClick = { listener(ArrowInputted(it)) },
                horizontalPadding = horizontalPadding,
                modifier = Modifier.updateHelpDialogPosition(helpListener, R.string.help_input_end__arrow_inputs_title)
        )

        FlowRow(
                mainAxisAlignment = FlowMainAxisAlignment.Center,
        ) {
            if (showResetButton) {
                CodexButton(
                        text = stringResource(R.string.general__reset_edits),
                        buttonStyle = CodexButtonDefaults.DefaultTextButton,
                        onClick = { listener(ResetArrowsInputted) },
                        modifier = Modifier
                                .testTag(ArrowInputsTestTag.RESET_BUTTON)
                                .updateHelpDialogPosition(helpListener, R.string.help_input_end__end_inputs_reset_title)
                )
            }
            CodexButton(
                    text = stringResource(R.string.input_end__clear),
                    buttonStyle = CodexButtonDefaults.DefaultTextButton,
                    onClick = { listener(ClearArrowsInputted) },
                    modifier = Modifier
                            .testTag(ArrowInputsTestTag.CLEAR_BUTTON)
                            .updateHelpDialogPosition(helpListener, R.string.help_input_end__end_inputs_clear_title)
            )
            CodexButton(
                    text = stringResource(R.string.input_end__backspace),
                    buttonStyle = CodexButtonDefaults.DefaultTextButton,
                    onClick = { listener(BackspaceArrowsInputted) },
                    modifier = Modifier
                            .testTag(ArrowInputsTestTag.BACKSPACE_BUTTON)
                            .updateHelpDialogPosition(helpListener, R.string.help_input_end__end_inputs_backspace_title)
            )
        }
    }
}

@Composable
private fun HelpInfoItems(
        showResetButton: Boolean,
        helpListener: (HelpShowcaseIntent) -> Unit,
) {
    helpListener(
            HelpShowcaseIntent.Add(
                    HelpShowcaseItem(
                            helpTitle = R.string.help_input_end__end_inputs_total_title,
                            helpBody = R.string.help_input_end__end_inputs_total_body,
                    )
            )
    )
    helpListener(
            HelpShowcaseIntent.Add(
                    HelpShowcaseItem(
                            helpTitle = R.string.help_input_end__end_inputs_arrows_title,
                            helpBody = R.string.help_input_end__end_inputs_arrows_body,
                    )
            )
    )
    helpListener(
            HelpShowcaseIntent.Add(
                    HelpShowcaseItem(
                            helpTitle = R.string.help_input_end__arrow_inputs_title,
                            helpBody = R.string.help_input_end__arrow_inputs_body,
                    )
            )
    )

    if (showResetButton) {
        helpListener(
                HelpShowcaseIntent.Add(
                        HelpShowcaseItem(
                                helpTitle = R.string.help_input_end__end_inputs_reset_title,
                                helpBody = R.string.help_input_end__end_inputs_reset_body,
                        )
                )
        )
    }

    helpListener(
            HelpShowcaseIntent.Add(
                    HelpShowcaseItem(
                            helpTitle = R.string.help_input_end__end_inputs_clear_title,
                            helpBody = R.string.help_input_end__end_inputs_clear_body,
                    )
            )
    )
    helpListener(
            HelpShowcaseIntent.Add(
                    HelpShowcaseItem(
                            helpTitle = R.string.help_input_end__end_inputs_backspace_title,
                            helpBody = R.string.help_input_end__end_inputs_backspace_body,
                    )
            )
    )
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
                state = object : ArrowInputsState {
                    override fun getRound(): Round = RoundPreviewHelper.outdoorImperialRoundData.round
                    override fun getEnteredArrows(): List<Arrow> = ArcherRoundStatePreviewHelper.inputArrows
                    override fun getEndSize(): Int = 6
                },
                showResetButton = true,
                helpListener = {},
        ) {}
    }
}
