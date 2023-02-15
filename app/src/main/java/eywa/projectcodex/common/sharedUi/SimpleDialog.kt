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
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography

data class ButtonState(
        val text: String,
        val onClick: () -> Unit
)

/**
 * Used to display a single dialog from a set of dialogs.
 * Passes true to the first Dialog whose IsShown is true and false to all others.
 *
 * @param dialogs List<Pair<IsShown, Dialog>>
 */
@Composable
fun SetOfDialogs(vararg dialogs: Pair<Boolean, @Composable (isShown: Boolean) -> Unit>) {
    dialogs.fold(false) { isAnyDialogShown, (showCondition, content) ->
        content(showCondition && !isAnyDialogShown)
        return@fold isAnyDialogShown || showCondition
    }
}

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
 * @see SimpleDialogContent(String, String, ButtonState, ButtonState, Modifier, (@Composable () -> Unit)?)
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
    SimpleDialogContent(
            title = stringResource(id = title),
            message = stringResource(id = message),
            positiveButton = positiveButton,
            negativeButton = negativeButton,
            modifier = modifier,
            content = content
    )
}

/**
 * @param content extra content placed between the message and the buttons
 */
@Composable
fun SimpleDialogContent(
        title: String,
        message: String? = null,
        positiveButton: ButtonState? = null,
        negativeButton: ButtonState? = null,
        modifier: Modifier = Modifier,
        content: (@Composable () -> Unit)? = null
) {
    Surface(
            shape = RoundedCornerShape(28.dp),
            modifier = modifier.fillMaxWidth(),
            color = CodexTheme.colors.dialogBackground
    ) {
        Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                    text = title,
                    style = CodexTypography.DIALOG_TITLE.copy(color = CodexTheme.colors.onDialogBackground),
                    modifier = Modifier.testTag(SimpleDialogTestTag.TITLE),
            )
            message?.let {
                Text(
                        text = message,
                        style = CodexTypography.DIALOG_TEXT.copy(color = CodexTheme.colors.onDialogBackground),
                        modifier = Modifier.testTag(SimpleDialogTestTag.MESSAGE),
                )
            }

            content?.invoke()

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                    modifier = Modifier.align(Alignment.End),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                negativeButton?.let {
                    CodexButton(
                            text = negativeButton.text,
                            buttonStyle = CodexButtonDefaults.DialogNegativeButton,
                            onClick = negativeButton.onClick,
                            modifier = Modifier.testTag(SimpleDialogTestTag.NEGATIVE_BUTTON),
                    )
                }
                positiveButton?.let {
                    CodexButton(
                            text = positiveButton.text,
                            buttonStyle = CodexButtonDefaults.DialogPositiveButton,
                            onClick = positiveButton.onClick,
                            modifier = Modifier.testTag(SimpleDialogTestTag.POSITIVE_BUTTON),
                    )
                }
            }
        }
    }
}

object SimpleDialogTestTag {
    const val TITLE = "SIMPLE_ALERT_DIALOG_TITLE"
    const val MESSAGE = "SIMPLE_ALERT_DIALOG_MESSAGE"
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
                    color = CodexTheme.colors.appBackground,
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
                positiveButton = ButtonState(stringResource(R.string.main_menu__exit_app_dialog_exit)) {},
                negativeButton = ButtonState(stringResource(R.string.general_cancel)) {},
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
                positiveButton = ButtonState(stringResource(R.string.err_view_score__return_to_main_menu)) {},
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
                positiveButton = ButtonState(stringResource(R.string.main_menu__exit_app_dialog_exit)) {},
                negativeButton = ButtonState(stringResource(R.string.general_cancel)) {},
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
                positiveButton = ButtonState(stringResource(R.string.err_view_score__return_to_main_menu)) {},
        )
    }
}

