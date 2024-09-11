package eywa.projectcodex.common.sharedUi

import android.annotation.SuppressLint
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import eywa.projectcodex.R
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.utils.CodexTestTag

data class ButtonState(
        val text: String,
        val onClick: () -> Unit,
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

    // TODO Compose has a bug where it doesn't recompose the dialog when the content height changes.
    //  This will be fixed in 1.5 (not stable yet). https://issuetracker.google.com/issues/221643630
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
        @SuppressLint("ModifierParameter") modifier: Modifier = Modifier,
        content: (@Composable () -> Unit)? = null
) {
    SimpleDialogContent(
            title = stringResource(id = title),
            message = stringResource(id = message),
            positiveButton = positiveButton,
            negativeButton = negativeButton,
            content = content,
            modifier = modifier
    )
}

/**
 * @param content extra content placed between the message and the buttons
 */
@Composable
fun SimpleDialogContent(
        title: String,
        modifier: Modifier = Modifier,
        message: String? = null,
        positiveButton: ButtonState? = null,
        negativeButton: ButtonState? = null,
        content: (@Composable () -> Unit)? = null
) {
    Surface(
            shape = RoundedCornerShape(28.dp),
            color = CodexTheme.colors.dialogBackground,
            modifier = modifier.fillMaxWidth()
    ) {
        Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(24.dp)
        ) {
            Text(
                    text = title,
                    style = CodexTypography.DIALOG_TITLE.copy(color = CodexTheme.colors.onDialogBackground),
                    modifier = Modifier.testTag(SimpleDialogTestTag.TITLE)
            )
            message?.let {
                Text(
                        text = message,
                        style = CodexTypography.DIALOG_TEXT.copy(color = CodexTheme.colors.onDialogBackground),
                        modifier = Modifier.testTag(SimpleDialogTestTag.MESSAGE)
                )
            }

            if (content != null) {
                Box(
                        content = { content() },
                        modifier = Modifier.weight(weight = 1f, fill = false)
                )
            }
            else {
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.align(Alignment.End)
            ) {
                negativeButton?.let {
                    CodexButton(
                            text = negativeButton.text,
                            buttonStyle = CodexButtonDefaults.DialogNegativeButton,
                            onClick = negativeButton.onClick,
                            modifier = Modifier.testTag(SimpleDialogTestTag.NEGATIVE_BUTTON)
                    )
                }
                positiveButton?.let {
                    CodexButton(
                            text = positiveButton.text,
                            buttonStyle = CodexButtonDefaults.DialogPositiveButton,
                            onClick = positiveButton.onClick,
                            modifier = Modifier.testTag(SimpleDialogTestTag.POSITIVE_BUTTON)
                    )
                }
            }
        }
    }
}

enum class SimpleDialogTestTag : CodexTestTag {
    TITLE,
    MESSAGE,
    POSITIVE_BUTTON,
    NEGATIVE_BUTTON,
    ;

    override val screenName: String
        get() = "SIMPLE_ALERT_DIALOG"

    override fun getElement(): String = name
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
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
        ) {
            Surface(
                    color = CodexTheme.colors.appBackground,
                    modifier = Modifier.fillMaxSize()
            ) {
                Text(
                        text = LoremIpsum(300).values.joinToString(" "),
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

