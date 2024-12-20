package eywa.projectcodex.components.shootDetails.commonUi.arrowInputs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.helpShowcase.asHelpState
import eywa.projectcodex.common.helpShowcase.updateHelpDialogPosition
import eywa.projectcodex.common.sharedUi.CodexButton
import eywa.projectcodex.common.sharedUi.CodexButtonDefaults
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.testTag
import eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.ArrowInputsIntent.*
import eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.arrowButton.ArrowButtonGroup
import eywa.projectcodex.database.RoundFace
import eywa.projectcodex.model.Arrow
import eywa.projectcodex.model.endAsAccessibilityString

@Composable
fun ArrowInputs(
        state: ArrowInputsState,
        showResetButton: Boolean,
        verticalPadding: Dp = 0.dp,
        horizontalPadding: Dp = 0.dp,
        helpListener: (HelpShowcaseIntent) -> Unit,
        listener: (ArrowInputsIntent) -> Unit,
) {
    val resources = LocalContext.current.resources
    val endTotal = state.enteredArrows.sumOf { it.score }.toString()
    val enteredArrowStrings = state.enteredArrows.map { it.asString().get() }

    Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = verticalPadding)
    ) {
        Text(
                text = endTotal,
                style = CodexTypography.X_LARGE,
                color = CodexTheme.colors.onAppBackground,
                modifier = Modifier
                        .testTag(ArrowInputsTestTag.END_TOTAL_TEXT)
                        .updateHelpDialogPosition(
                                HelpShowcaseItem(
                                        helpTitle = stringResource(R.string.help_input_end__end_inputs_total_title),
                                        helpBody = stringResource(R.string.help_input_end__end_inputs_total_body),
                                ).asHelpState(helpListener),
                        )
                        .semantics {
                            contentDescription =
                                    resources.getString(R.string.input_end__end_total_accessibility, endTotal)
                        }
        )
        Text(
                text = enteredArrowStrings
                        .plus(
                                List(state.endSize - state.enteredArrows.size) {
                                    stringResource(R.string.end_to_string_arrow_placeholder)
                                },
                        )
                        .joinToString(stringResource(R.string.end_to_string_arrow_deliminator)),
                style = CodexTypography.X_LARGE,
                textAlign = TextAlign.Center,
                color = CodexTheme.colors.onAppBackground,
                modifier = Modifier
                        .padding(bottom = 15.dp)
                        .testTag(ArrowInputsTestTag.END_ARROWS_TEXT)
                        .updateHelpDialogPosition(
                                HelpShowcaseItem(
                                        helpTitle = stringResource(R.string.help_input_end__end_inputs_arrows_title),
                                        helpBody = stringResource(R.string.help_input_end__end_inputs_arrows_body),
                                ).asHelpState(helpListener),
                        )
                        .semantics {
                            contentDescription = resources.getString(
                                    R.string.input_end__end_arrows_accessibility,
                                    enteredArrowStrings.endAsAccessibilityString(),
                            )
                        }
        )

        ArrowButtonGroup(
                round = state.round,
                roundFace = state.face,
                onClick = { listener(ArrowInputted(it)) },
                horizontalPadding = horizontalPadding,
                modifier = Modifier
                        .updateHelpDialogPosition(
                                HelpShowcaseItem(
                                        helpTitle = stringResource(R.string.help_input_end__arrow_inputs_title),
                                        helpBody = stringResource(R.string.help_input_end__arrow_inputs_body),
                                ).asHelpState(helpListener),
                        )
                        .testTag(ArrowInputsTestTag.ARROW_SCORE_BUTTON_GROUP)
        )

        ArrowInputEditButtons(showResetButton, helpListener, listener)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ArrowInputEditButtons(
        showResetButton: Boolean,
        helpListener: (HelpShowcaseIntent) -> Unit,
        listener: (ArrowInputsIntent) -> Unit,
) {
    FlowRow {
        if (showResetButton) {
            CodexButton(
                    text = stringResource(R.string.general__reset_edits),
                    buttonStyle = CodexButtonDefaults.DefaultTextButton,
                    onClick = { listener(ResetArrowsInputted) },
                    modifier = Modifier
                            .testTag(ArrowInputsTestTag.RESET_BUTTON)
                            .updateHelpDialogPosition(
                                    HelpShowcaseItem(
                                            helpTitle = stringResource(R.string.help_input_end__end_inputs_reset_title),
                                            helpBody = stringResource(R.string.help_input_end__end_inputs_reset_body),
                                    ).asHelpState(helpListener),
                            )
                            .align(Alignment.CenterVertically)
            )
        }
        CodexButton(
                text = stringResource(R.string.input_end__clear),
                buttonStyle = CodexButtonDefaults.DefaultTextButton,
                onClick = { listener(ClearArrowsInputted) },
                modifier = Modifier
                        .testTag(ArrowInputsTestTag.CLEAR_BUTTON)
                        .updateHelpDialogPosition(
                                HelpShowcaseItem(
                                        helpTitle = stringResource(R.string.help_input_end__end_inputs_clear_title),
                                        helpBody = stringResource(R.string.help_input_end__end_inputs_clear_body),
                                ).asHelpState(helpListener),
                        )
                        .align(Alignment.CenterVertically)
        )
        CodexButton(
                text = stringResource(R.string.input_end__backspace),
                buttonStyle = CodexButtonDefaults.DefaultTextButton,
                onClick = { listener(BackspaceArrowsInputted) },
                modifier = Modifier
                        .testTag(ArrowInputsTestTag.BACKSPACE_BUTTON)
                        .updateHelpDialogPosition(
                                HelpShowcaseItem(
                                        helpTitle = stringResource(R.string.help_input_end__end_inputs_backspace_title),
                                        helpBody = stringResource(R.string.help_input_end__end_inputs_backspace_body),
                                ).asHelpState(helpListener),
                        )
                        .align(Alignment.CenterVertically)
        )
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
                    override val round = RoundPreviewHelper.outdoorImperialRoundData.round
                    override val face = RoundFace.FULL
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
