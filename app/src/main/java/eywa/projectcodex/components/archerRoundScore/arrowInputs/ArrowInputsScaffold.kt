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
        listener: (ArcherRoundIntent) -> Unit,
) = ArrowInputsScaffold(
        state = state,
        showCancelButton = showCancelButton,
        showResetButton = showResetButton,
        submitButtonText = submitButtonText,
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

        ArrowInputs(state, showResetButton, listener)

        Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (showCancelButton) {
                CodexButton(
                        text = stringResource(R.string.general_cancel),
                        buttonStyle = CodexButtonDefaults.DefaultButton(),
                        onClick = { listener(ArcherRoundIntent.ScreenCancelClicked) },
                        modifier = Modifier.testTag(ArrowInputsTestTag.CANCEL_BUTTON)
                )
            }
            CodexButton(
                    text = submitButtonText,
                    buttonStyle = CodexButtonDefaults.DefaultButton(),
                    onClick = { listener(ArcherRoundIntent.ScreenSubmitClicked) },
                    modifier = Modifier.testTag(ArrowInputsTestTag.SUBMIT_BUTTON)
            )
        }
    }
}
