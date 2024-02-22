package eywa.projectcodex.components.viewScores.dialogs.convertScoreDialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import eywa.projectcodex.R
import eywa.projectcodex.common.sharedUi.ButtonState
import eywa.projectcodex.common.sharedUi.RadioButtonDialogContent
import eywa.projectcodex.common.sharedUi.SimpleDialog
import eywa.projectcodex.common.sharedUi.rememberRadioButtonDialogState

@Composable
fun ConvertScoreDialog(
        isShown: Boolean,
        listener: (ConvertScoreIntent) -> Unit,
) {
    SimpleDialog(
            isShown = isShown,
            onDismissListener = { listener(ConvertScoreIntent.Close) }
    ) {
        RadioButtonDialogContent(
                title = R.string.view_score__convert_score_dialog_title,
                message = R.string.view_score__convert_score_dialog_body,
                positiveButtonText = R.string.general_ok,
                onPositiveButtonPressed = { listener(ConvertScoreIntent.Ok(it)) },
                negativeButton = ButtonState(
                        text = stringResource(R.string.general_cancel),
                        onClick = { listener(ConvertScoreIntent.Close) }
                ),
                state = rememberRadioButtonDialogState(items = ConvertScoreType.values().toList()),
        )
    }
}
