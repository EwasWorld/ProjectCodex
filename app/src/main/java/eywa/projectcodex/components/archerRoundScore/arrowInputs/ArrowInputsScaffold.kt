package eywa.projectcodex.components.archerRoundScore.arrowInputs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
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
        helpListener: (HelpShowcaseIntent) -> Unit,
        cancelHelpInfoTitle: String? = null,
        cancelHelpInfoBody: String? = null,
        submitHelpInfoTitle: String,
        submitHelpInfoBody: String,
        testTag: String,
        listener: (ArcherRoundIntent) -> Unit,
) = ArrowInputsScaffold(
        state = state,
        showCancelButton = showCancelButton,
        showResetButton = showResetButton,
        submitButtonText = submitButtonText,
        helpListener = helpListener,
        cancelHelpInfoTitle = cancelHelpInfoTitle,
        cancelHelpInfoBody = cancelHelpInfoBody,
        submitHelpInfoTitle = submitHelpInfoTitle,
        submitHelpInfoBody = submitHelpInfoBody,
        testTag = testTag,
        listener = listener,
) {
    Text(
            text = contentText,
            style = CodexTypography.LARGE,
            color = CodexTheme.colors.onAppBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag(ArrowInputsTestTag.CONTENT_TEXT)
    )
}

@Composable
fun ArrowInputsScaffold(
        state: ArrowInputsState,
        showCancelButton: Boolean,
        showResetButton: Boolean,
        submitButtonText: String = stringResource(R.string.general_complete),
        helpListener: (HelpShowcaseIntent) -> Unit,
        cancelHelpInfoTitle: String? = null,
        cancelHelpInfoBody: String? = null,
        submitHelpInfoTitle: String,
        submitHelpInfoBody: String,
        testTag: String,
        listener: (ArcherRoundIntent) -> Unit,
        content: @Composable () -> Unit,
) {
    Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 20.dp)
                    .testTag(testTag)
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Column(
                verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            content()
        }
        Spacer(modifier = Modifier.weight(1f))

        ArrowInputs(
                state = state,
                showResetButton = showResetButton,
                horizontalPadding = 20.dp,
                verticalPadding = 10.dp,
                helpListener = helpListener,
                listener = listener,
        )

        Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            if (showCancelButton) {
                helpListener(
                        HelpShowcaseIntent.Add(
                                HelpShowcaseItem(
                                        helpTitle = cancelHelpInfoTitle!!,
                                        helpBody = cancelHelpInfoBody!!,
                                )
                        )
                )
                CodexButton(
                        text = stringResource(R.string.general_cancel),
                        buttonStyle = CodexButtonDefaults.DefaultButton(),
                        onClick = { listener(ArcherRoundIntent.ScreenCancelClicked) },
                        modifier = Modifier
                                .testTag(ArrowInputsTestTag.CANCEL_BUTTON)
                                .updateHelpDialogPosition(helpListener, cancelHelpInfoTitle)
                )
            }

            helpListener(
                    HelpShowcaseIntent.Add(
                            HelpShowcaseItem(
                                    helpTitle = submitHelpInfoTitle,
                                    helpBody = submitHelpInfoBody,
                            )
                    )
            )
            CodexButton(
                    text = submitButtonText,
                    buttonStyle = CodexButtonDefaults.DefaultButton(),
                    onClick = { listener(ArcherRoundIntent.ScreenSubmitClicked) },
                    modifier = Modifier
                            .testTag(ArrowInputsTestTag.SUBMIT_BUTTON)
                            .updateHelpDialogPosition(helpListener, submitHelpInfoTitle)
            )
        }
    }
}
