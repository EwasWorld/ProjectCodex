package eywa.projectcodex.components.shootDetails.commonUi.arrowInputs

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
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpState
import eywa.projectcodex.common.helpShowcase.updateHelpDialogPosition
import eywa.projectcodex.common.sharedUi.CodexButton
import eywa.projectcodex.common.sharedUi.CodexButtonDefaults
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelper.addRound
import eywa.projectcodex.common.utils.get
import eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.ArrowInputsIntent.*
import eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.arrowButton.ArrowButtonGroup
import eywa.projectcodex.model.Arrow

@Composable
fun ArrowInputs(
        state: ArrowInputsState,
        showResetButton: Boolean,
        verticalPadding: Dp = 0.dp,
        horizontalPadding: Dp = 0.dp,
        helpListener: (HelpShowcaseIntent) -> Unit,
        listener: (ArrowInputsIntent) -> Unit,
) {
    Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = verticalPadding)
    ) {
        Text(
                text = state.enteredArrows.sumOf { it.score }.toString(),
                style = CodexTypography.X_LARGE,
                color = CodexTheme.colors.onAppBackground,
                modifier = Modifier
                        .testTag(ArrowInputsTestTag.END_TOTAL_TEXT.getTestTag())
                        .updateHelpDialogPosition(
                                HelpState(
                                        helpListener = helpListener,
                                        helpTitle = stringResource(R.string.help_input_end__end_inputs_total_title),
                                        helpBody = stringResource(R.string.help_input_end__end_inputs_total_body),
                                )
                        )
        )
        Text(
                text = state.enteredArrows
                        .map { it.asString().get() }
                        .plus(
                                List(state.endSize - state.enteredArrows.size) {
                                    stringResource(R.string.end_to_string_arrow_placeholder)
                                }
                        )
                        .joinToString(stringResource(R.string.end_to_string_arrow_deliminator)),
                style = CodexTypography.X_LARGE,
                textAlign = TextAlign.Center,
                color = CodexTheme.colors.onAppBackground,
                modifier = Modifier
                        .padding(bottom = 15.dp)
                        .testTag(ArrowInputsTestTag.END_ARROWS_TEXT.getTestTag())
                        .updateHelpDialogPosition(
                                HelpState(
                                        helpListener = helpListener,
                                        helpTitle = stringResource(R.string.help_input_end__end_inputs_arrows_title),
                                        helpBody = stringResource(R.string.help_input_end__end_inputs_arrows_body),
                                )
                        )
        )

        ArrowButtonGroup(
                round = state.fullShootInfo.round,
                roundFace = state.fullShootInfo.currentFace,
                onClick = { listener(ArrowInputted(it)) },
                horizontalPadding = horizontalPadding,
                modifier = Modifier.updateHelpDialogPosition(
                        HelpState(
                                helpListener = helpListener,
                                helpTitle = stringResource(R.string.help_input_end__arrow_inputs_title),
                                helpBody = stringResource(R.string.help_input_end__arrow_inputs_body),
                        )
                )
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
                                .testTag(ArrowInputsTestTag.RESET_BUTTON.getTestTag())
                                .updateHelpDialogPosition(
                                        HelpState(
                                                helpListener = helpListener,
                                                helpTitle = stringResource(R.string.help_input_end__end_inputs_reset_title),
                                                helpBody = stringResource(R.string.help_input_end__end_inputs_reset_body),
                                        )
                                )
                )
            }
            CodexButton(
                    text = stringResource(R.string.input_end__clear),
                    buttonStyle = CodexButtonDefaults.DefaultTextButton,
                    onClick = { listener(ClearArrowsInputted) },
                    modifier = Modifier
                            .testTag(ArrowInputsTestTag.CLEAR_BUTTON.getTestTag())
                            .updateHelpDialogPosition(
                                    HelpState(
                                            helpListener = helpListener,
                                            helpTitle = stringResource(R.string.help_input_end__end_inputs_clear_title),
                                            helpBody = stringResource(R.string.help_input_end__end_inputs_clear_body),
                                    )
                            )
            )
            CodexButton(
                    text = stringResource(R.string.input_end__backspace),
                    buttonStyle = CodexButtonDefaults.DefaultTextButton,
                    onClick = { listener(BackspaceArrowsInputted) },
                    modifier = Modifier
                            .testTag(ArrowInputsTestTag.BACKSPACE_BUTTON.getTestTag())
                            .updateHelpDialogPosition(
                                    HelpState(
                                            helpListener = helpListener,
                                            helpTitle = stringResource(R.string.help_input_end__end_inputs_backspace_title),
                                            helpBody = stringResource(R.string.help_input_end__end_inputs_backspace_body),
                                    )
                            )
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
                state = object : ArrowInputsState {
                    override val fullShootInfo = ShootPreviewHelper.newFullShootInfo()
                            .addRound(RoundPreviewHelper.outdoorImperialRoundData)
                    override val enteredArrows = listOf(
                            Arrow(10, true),
                            Arrow(10, false),
                            Arrow(3, false),
                            Arrow(0, false),
                    )
                    override val endSize = 6
                },
                showResetButton = true,
                helpListener = {},
        ) {}
    }
}
