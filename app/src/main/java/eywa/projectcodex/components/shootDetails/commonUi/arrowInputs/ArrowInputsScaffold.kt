package eywa.projectcodex.components.shootDetails.commonUi.arrowInputs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.helpShowcase.asHelpState
import eywa.projectcodex.common.helpShowcase.updateHelpDialogPosition
import eywa.projectcodex.common.sharedUi.CodexButton
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.testTag
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.ArrowInputsIntent.CancelClicked
import eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.ArrowInputsIntent.SubmitClicked

@Composable
fun ArrowInputsScaffold(
        state: ArrowInputsState,
        showCancelButton: Boolean,
        showResetButton: Boolean,
        contentText: String,
        modifier: Modifier = Modifier,
        submitButtonText: String = stringResource(R.string.general_complete),
        helpListener: (HelpShowcaseIntent) -> Unit,
        cancelHelpInfo: HelpShowcaseItem? = null,
        submitHelpInfo: HelpShowcaseItem,
        testTag: CodexTestTag,
        listener: (ArrowInputsIntent) -> Unit,
) = ArrowInputsScaffold(
        state = state,
        showCancelButton = showCancelButton,
        showResetButton = showResetButton,
        submitButtonText = submitButtonText,
        modifier = modifier,
        helpListener = helpListener,
        cancelHelpInfo = cancelHelpInfo,
        submitHelpInfo = submitHelpInfo,
        testTag = testTag,
        listener = listener,
) {
    Text(
            text = contentText,
            style = CodexTypography.LARGE,
            color = CodexTheme.colors.onAppBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier
                    .testTag(ArrowInputsTestTag.CONTENT_TEXT)
                    .padding(horizontal = CodexTheme.dimens.screenPadding)
    )
}

@Composable
fun ArrowInputsScaffold(
        state: ArrowInputsState,
        showCancelButton: Boolean,
        showResetButton: Boolean,
        modifier: Modifier = Modifier,
        submitButtonText: String = stringResource(R.string.general_complete),
        helpListener: (HelpShowcaseIntent) -> Unit,
        cancelHelpInfo: HelpShowcaseItem? = null,
        submitHelpInfo: HelpShowcaseItem,
        testTag: CodexTestTag,
        listener: (ArrowInputsIntent) -> Unit,
        content: @Composable () -> Unit,
) {
    Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                    .padding(vertical = CodexTheme.dimens.screenPadding)
                    .testTag(testTag)
    ) {
        Spacer(modifier = Modifier.weight(1f))
        content()
        Spacer(modifier = Modifier.weight(1f))

        ArrowInputs(
                state = state,
                showResetButton = showResetButton,
                horizontalPadding = CodexTheme.dimens.screenPadding,
                verticalPadding = 10.dp,
                helpListener = helpListener,
                listener = listener,
        )

        Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.padding(horizontal = CodexTheme.dimens.screenPadding)
        ) {
            if (showCancelButton) {
                CodexButton(
                        text = stringResource(R.string.general_cancel),
                        onClick = { listener(CancelClicked) },
                        modifier = Modifier
                                .testTag(ArrowInputsTestTag.CANCEL_BUTTON)
                                .updateHelpDialogPosition(cancelHelpInfo!!.asHelpState(helpListener))
                )
            }

            CodexButton(
                    text = submitButtonText,
                    onClick = { listener(SubmitClicked) },
                    modifier = Modifier
                            .testTag(ArrowInputsTestTag.SUBMIT_BUTTON)
                            .updateHelpDialogPosition(submitHelpInfo.asHelpState(helpListener))
            )
        }
    }
}
