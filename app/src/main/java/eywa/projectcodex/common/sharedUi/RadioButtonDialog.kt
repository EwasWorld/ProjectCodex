package eywa.projectcodex.common.sharedUi

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography

@Composable
fun RadioButtonDialogContent(
        @StringRes title: Int,
        @StringRes message: Int,
        @StringRes radioButtonText: List<Int>,
        @StringRes positiveButtonText: Int,
        @StringRes negativeButtonText: Int? = null,
        onDialogActionClicked: (action: Boolean, selectedIndex: Int) -> Unit,
        currentlySelectedIndex: Int,
        selectionChangedListener: (Int) -> Unit,
) {
    Surface(
            // TODO_CURRENT Add this to codex theme
            shape = MaterialTheme.shapes.medium,
    ) {
        Column(
                modifier = Modifier.padding(10.dp)
        ) {
            Column(
                    modifier = Modifier.padding(20.dp),
            ) {
                Text(
                        text = stringResource(id = title),
                        style = CodexTypography.NORMAL,
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                        text = stringResource(id = message),
                        style = CodexTypography.SMALL_DIMMED,
                )
                Spacer(modifier = Modifier.height(15.dp))
                radioButtonText.forEachIndexed { index, textId ->
                    Row(
                            modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(selected = index == currentlySelectedIndex) {
                                        selectionChangedListener(index)
                                    },
                            verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                                selected = index == currentlySelectedIndex,
                                onClick = { selectionChangedListener(index) }
                        )
                        Text(
                                text = stringResource(id = textId),
                                style = CodexTypography.SMALL,
                        )
                    }
                }
            }
            Row(
                    modifier = Modifier.align(Alignment.End)
            ) {
                negativeButtonText?.let {
                    CodexButton(
                            text = stringResource(id = negativeButtonText),
                            buttonStyle = CodexButtonDefaults.AlertDialogNegativeButton,
                            onClick = { onDialogActionClicked(false, currentlySelectedIndex) }
                    )
                }
                CodexButton(
                        text = stringResource(id = positiveButtonText),
                        buttonStyle = CodexButtonDefaults.AlertDialogPositiveButton,
                        onClick = { onDialogActionClicked(true, currentlySelectedIndex) }
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewRadioButtonDialog() {
    CodexTheme {
        RadioButtonDialogContent(
                title = R.string.view_score__convert_score_dialog_title,
                message = R.string.view_score__convert_score_dialog_body,
                radioButtonText = listOf(
                        R.string.view_scores__convert_xs_to_tens,
                        R.string.view_scores__convert_to_five_zone
                ),
                positiveButtonText = R.string.general_ok,
                negativeButtonText = R.string.general_cancel,
                onDialogActionClicked = { _, _ -> },
                currentlySelectedIndex = 0,
                selectionChangedListener = {}
        )
    }
}

