package eywa.projectcodex.common.sharedUi

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import eywa.projectcodex.R
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.components.viewScores.dialogs.convertScoreDialog.ConvertScoreType

interface HasDisplayTitle {
    val displayTitle: Int
}

@Stable
interface RadioButtonDialogState<T : HasDisplayTitle> {
    var items: List<T>
    var selectedIndex: Int

    val currentItem: T
        get() = items[selectedIndex]
}

private class RadioButtonDialogStateImpl<T : HasDisplayTitle>(
        items: List<T>,
        initiallySelectedIndex: Int = 0,
) : RadioButtonDialogState<T> {
    override var items by mutableStateOf(items)
    override var selectedIndex by mutableStateOf(initiallySelectedIndex)
}

@Composable
fun <T : HasDisplayTitle> rememberRadioButtonDialogState(
        items: List<T>,
        initiallySelectedIndex: Int = 0,
): RadioButtonDialogState<T> = remember { RadioButtonDialogStateImpl(items, initiallySelectedIndex) }

@Composable
fun <T : HasDisplayTitle> RadioButtonDialogContent(
        @StringRes title: Int,
        @StringRes message: Int,
        @StringRes positiveButtonText: Int,
        onPositiveButtonPressed: (selectedItem: T) -> Unit,
        negativeButton: ButtonState? = null,
        state: RadioButtonDialogState<T>,
) {
    SimpleDialogContent(
            title = title,
            message = message,
            negativeButton = negativeButton,
            positiveButton = ButtonState(
                    text = stringResource(positiveButtonText),
                    onClick = { onPositiveButtonPressed(state.currentItem) }
            ),
    ) {
        Column {
            state.items.forEachIndexed { index, item ->
                Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                                .fillMaxWidth()
                                .selectable(selected = index == state.selectedIndex) {
                                    state.selectedIndex = index
                                }
                                .testTag(RadioButtonDialogTestTag.RADIO_BUTTON)
                ) {
                    RadioButton(
                            selected = index == state.selectedIndex,
                            onClick = { state.selectedIndex = index },
                            colors = RadioButtonDefaults.colors(
                                    selectedColor = CodexTheme.colors.dialogRadioButton,
                            ),
                            modifier = Modifier.clearAndSetSemantics { }
                    )
                    Text(
                            text = stringResource(id = item.displayTitle),
                            style = CodexTypography.DIALOG_TEXT.copy(color = CodexTheme.colors.onDialogBackground),
                    )
                }
            }
        }
    }
}

enum class RadioButtonDialogTestTag : CodexTestTag {
    RADIO_BUTTON,
    ;

    override val screenName: String
        get() = "RADIO_BUTTON_DIALOG"

    override fun getElement(): String = name
}

@Preview
@Composable
fun RadioButtonDialog_Preview() {
    DialogPreviewHelper {
        RadioButtonDialogContent(
                title = R.string.view_score__convert_score_dialog_title,
                message = R.string.view_score__convert_score_dialog_body,
                positiveButtonText = R.string.general_ok,
                onPositiveButtonPressed = {},
                negativeButton = ButtonState(stringResource(R.string.general_cancel)) {},
                state = rememberRadioButtonDialogState(items = ConvertScoreType.values().toList())
        )
    }
}
