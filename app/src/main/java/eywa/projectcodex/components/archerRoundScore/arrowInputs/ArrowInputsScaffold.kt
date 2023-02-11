package eywa.projectcodex.components.archerRoundScore.arrowInputs

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.ComposeHelpShowcaseItem
import eywa.projectcodex.common.helpShowcase.ComposeHelpShowcaseMap
import eywa.projectcodex.common.helpShowcase.updateHelpDialogPosition
import eywa.projectcodex.common.sharedUi.CodexButton
import eywa.projectcodex.common.sharedUi.CodexButtonDefaults
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.components.archerRoundScore.ArcherRoundIntent

@Composable
fun ArrowInputsScaffold(
        state: ArrowInputsState,
        showCancelButton: Boolean,
        showResetButton: Boolean,
        contentText: String,
        submitButtonText: String = stringResource(R.string.general_complete),
        helpInfo: ComposeHelpShowcaseMap,
        cancelHelpInfoTitle: String? = null,
        cancelHelpInfoBody: String? = null,
        submitHelpInfoTitle: String,
        submitHelpInfoBody: String,
        listener: (ArcherRoundIntent) -> Unit,
) = ArrowInputsScaffold(
        state = state,
        showCancelButton = showCancelButton,
        showResetButton = showResetButton,
        submitButtonText = submitButtonText,
        helpInfo = helpInfo,
        cancelHelpInfoTitle = cancelHelpInfoTitle,
        cancelHelpInfoBody = cancelHelpInfoBody,
        submitHelpInfoTitle = submitHelpInfoTitle,
        submitHelpInfoBody = submitHelpInfoBody,
        listener = listener,
) {
    Text(
            text = contentText,
            style = CodexTypography.LARGE,
            color = CodexTheme.colors.onAppBackground,
            textAlign = TextAlign.Center,
    )
}

@Composable
fun ArrowInputsScaffold(
        state: ArrowInputsState,
        showCancelButton: Boolean,
        showResetButton: Boolean,
        submitButtonText: String = stringResource(R.string.general_complete),
        helpInfo: ComposeHelpShowcaseMap,
        cancelHelpInfoTitle: String? = null,
        cancelHelpInfoBody: String? = null,
        submitHelpInfoTitle: String,
        submitHelpInfoBody: String,
        listener: (ArcherRoundIntent) -> Unit,
        content: @Composable () -> Unit,
) {
    Column(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
    ) {
        Spacer(modifier = Modifier.weight(1f))
        content()
        Spacer(modifier = Modifier.weight(1f))

        ArrowInputs(state, showResetButton, helpInfo, listener)

        Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (showCancelButton) {
                helpInfo.add(
                        ComposeHelpShowcaseItem(
                                helpTitle = cancelHelpInfoTitle!!,
                                helpBody = cancelHelpInfoBody!!,
                        )
                )
                CodexButton(
                        text = stringResource(R.string.general_cancel),
                        buttonStyle = CodexButtonDefaults.DefaultButton(),
                        onClick = { listener(ArcherRoundIntent.ScreenCancelClicked) },
                        modifier = Modifier
                                .testTag(ArrowInputsTestTag.CANCEL_BUTTON)
                                .updateHelpDialogPosition(helpInfo, cancelHelpInfoTitle)
                )
            }

            helpInfo.add(
                    ComposeHelpShowcaseItem(
                            helpTitle = submitHelpInfoTitle,
                            helpBody = submitHelpInfoBody,
                    )
            )
            CodexButton(
                    text = submitButtonText,
                    buttonStyle = CodexButtonDefaults.DefaultButton(),
                    onClick = { listener(ArcherRoundIntent.ScreenSubmitClicked) },
                    modifier = Modifier
                            .testTag(ArrowInputsTestTag.SUBMIT_BUTTON)
                            .updateHelpDialogPosition(helpInfo, submitHelpInfoTitle)
            )
        }
    }
}
