package eywa.projectcodex.components.mainMenu

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import eywa.projectcodex.R

@Composable
fun SimpleAlertDialog(
        isOpen: Boolean,
        @StringRes title: Int,
        @StringRes message: Int,
        @StringRes positiveButton: Int,
        @StringRes negativeButton: Int,
        onDialogActionClicked: (Boolean) -> Unit,
) {
    SimpleAlertDialog(
            isOpen = isOpen,
            title = title,
            message = message,
            positiveButton = positiveButton,
            negativeButton = negativeButton,
            onPositiveButtonClickListener = { onDialogActionClicked(true) },
            onNegativeButtonClickListener = { onDialogActionClicked(false) },
            onDismissListener = { onDialogActionClicked(false) }
    )
}

@Composable
fun SimpleAlertDialog(
        isOpen: Boolean,
        @StringRes title: Int,
        @StringRes message: Int,
        @StringRes positiveButton: Int,
        @StringRes negativeButton: Int,
        onPositiveButtonClickListener: () -> Unit,
        onNegativeButtonClickListener: () -> Unit,
        onDismissListener: () -> Unit,
) {
    if (isOpen) {
        AlertDialog(
                onDismissRequest = onDismissListener,
                title = {
                    Text(
                            text = stringResource(id = title),
                            modifier = Modifier.testTag(SimpleAlertDialogTestTag.POSITIVE_TITLE)
                    )
                },
                text = { Text(stringResource(id = message)) },
                confirmButton = {
                    Button(
                            onClick = onPositiveButtonClickListener,
                            modifier = Modifier.testTag(SimpleAlertDialogTestTag.POSITIVE_BUTTON)
                    ) {
                        Text(stringResource(id = positiveButton))
                    }
                },
                dismissButton = {
                    Button(
                            onClick = onNegativeButtonClickListener,
                            modifier = Modifier.testTag(SimpleAlertDialogTestTag.NEGATIVE_BUTTON)
                    ) {
                        Text(stringResource(id = negativeButton))
                    }
                }
        )
    }
}

object SimpleAlertDialogTestTag {
    const val POSITIVE_TITLE = "SIMPLE_ALERT_DIALOG_TITLE"
    const val POSITIVE_BUTTON = "SIMPLE_ALERT_DIALOG_POSITIVE_BUTTON"
    const val NEGATIVE_BUTTON = "SIMPLE_ALERT_DIALOG_NEGATIVE_BUTTON"
}

@Preview(showBackground = true)
@Composable
fun PreviewSimpleAlertDialog() {
    Box(modifier = Modifier.fillMaxSize()) {
        SimpleAlertDialog(
                isOpen = true,
                title = R.string.main_menu__exit_app_dialog_title,
                message = R.string.main_menu__exit_app_dialog_body,
                positiveButton = R.string.main_menu__exit_app_dialog_exit,
                negativeButton = R.string.general_cancel,
                onDialogActionClicked = { }
        )
    }
}