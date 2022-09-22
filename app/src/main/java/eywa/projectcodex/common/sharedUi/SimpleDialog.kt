package eywa.projectcodex.common.sharedUi

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import eywa.projectcodex.R
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography

data class ButtonState(
        @StringRes val text: Int,
        val onClick: () -> Unit
)

@Composable
fun SimpleDialog(
        isShown: Boolean,
        onDismissListener: () -> Unit,
        content: @Composable () -> Unit,
) {
    // TODO Show/hide animations
    if (isShown) {
        Dialog(
                onDismissRequest = onDismissListener,
        ) {
            Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.sizeIn(maxHeight = 560.dp, maxWidth = 560.dp)
            ) {
                content()
            }
        }
    }
}

/**
 * @param content extra content placed between the message and the buttons
 */
@Composable
fun SimpleDialogContent(
        @StringRes title: Int,
        @StringRes message: Int,
        positiveButton: ButtonState,
        negativeButton: ButtonState? = null,
        modifier: Modifier = Modifier,
        content: (@Composable () -> Unit)? = null
) {
    Surface(
            shape = RoundedCornerShape(28.dp),
            modifier = modifier.fillMaxWidth()
    ) {
        Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                    text = stringResource(id = title),
                    style = CodexTypography.DIALOG_TITLE,
                    modifier = Modifier.testTag(SimpleDialogTestTag.TITLE),
            )
            Text(
                    text = stringResource(id = message),
                    style = CodexTypography.DIALOG_TEXT,
            )

            content?.invoke()

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                    modifier = Modifier.align(Alignment.End),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                negativeButton?.let {
                    CodexButton(
                            text = stringResource(id = negativeButton.text),
                            buttonStyle = CodexButtonDefaults.AlertDialogNegativeButton,
                            onClick = negativeButton.onClick,
                            modifier = Modifier.testTag(SimpleDialogTestTag.NEGATIVE_BUTTON),
                    )
                }
                CodexButton(
                        text = stringResource(id = positiveButton.text),
                        buttonStyle = CodexButtonDefaults.AlertDialogPositiveButton,
                        onClick = positiveButton.onClick,
                        modifier = Modifier.testTag(SimpleDialogTestTag.POSITIVE_BUTTON),
                )
            }
        }
    }
}

object SimpleDialogTestTag {
    const val TITLE = "SIMPLE_ALERT_DIALOG_TITLE"
    const val POSITIVE_BUTTON = "SIMPLE_ALERT_DIALOG_POSITIVE_BUTTON"
    const val NEGATIVE_BUTTON = "SIMPLE_ALERT_DIALOG_NEGATIVE_BUTTON"
}

@Composable
fun DialogPreviewHelper(
        dialogContent: @Composable () -> Unit
) {
    CodexTheme {
        Dialog(onDismissRequest = {}) {
            dialogContent()
        }
        Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
        ) {
            Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CodexColors.COLOR_PRIMARY,
            ) {
                Text(
                        text = LoremIpsum(300).values.joinToString(" ")
                )
            }
        }
    }
}

@Preview
@Composable
fun TwoButton_SimpleDialog_Preview() {
    DialogPreviewHelper {
        SimpleDialogContent(
                title = R.string.main_menu__exit_app_dialog_title,
                message = R.string.main_menu__exit_app_dialog_body,
                positiveButton = ButtonState(R.string.main_menu__exit_app_dialog_exit) {},
                negativeButton = ButtonState(R.string.general_cancel) {},
        )
    }
}

@Preview
@Composable
fun SingleButton_SimpleDialog_Preview() {
    DialogPreviewHelper {
        SimpleDialogContent(
                title = R.string.err_table_view__no_data,
                message = R.string.err_view_score__no_rounds,
                positiveButton = ButtonState(R.string.err_view_score__return_to_main_menu) {},
        )
    }
}


@Preview(
        device = Devices.NEXUS_10
)
@Composable
fun TwoButton_Tablet_SimpleDialog_Preview() {
    DialogPreviewHelper {
        SimpleDialogContent(
                title = R.string.main_menu__exit_app_dialog_title,
                message = R.string.main_menu__exit_app_dialog_body,
                positiveButton = ButtonState(R.string.main_menu__exit_app_dialog_exit) {},
                negativeButton = ButtonState(R.string.general_cancel) {},
        )
    }
}

@Preview(
        device = Devices.NEXUS_10
)
@Composable
fun SingleButton_Tablet_SimpleDialog_Preview() {
    DialogPreviewHelper {
        SimpleDialogContent(
                title = R.string.err_table_view__no_data,
                message = R.string.err_view_score__no_rounds,
                positiveButton = ButtonState(R.string.err_view_score__return_to_main_menu) {},
        )
    }
}

