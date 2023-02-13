package eywa.projectcodex.common.sharedUi

import androidx.annotation.StringRes
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun NumberSetting(
        @StringRes title: Int,
        currentValue: Int?,
        testTag: String,
        helpListener: ((HelpShowcaseIntent) -> Unit)? = null,
        @StringRes helpTitle: Int? = null,
        @StringRes helpBody: Int? = null,
        placeholder: Int = 6,
        onValueChanged: (Int?) -> Unit,
        modifier: Modifier = Modifier,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    DataRow(
            title = title,
            helpListener = helpListener,
            helpTitle = helpTitle,
            helpBody = helpBody,
            modifier = modifier,
    ) {
        Surface(
                color = CodexTheme.colors.surfaceOnBackground,
                shape = RoundedCornerShape(5.dp),
        ) {
            CodexTextField(
                    state = CodexTextFieldState(
                            text = currentValue?.toString() ?: "",
                            onValueChange = {
                                onValueChanged(it.takeIf { it.isNotBlank() && it.isDigitsOnly() }?.toInt())
                            },
                            testTag = "",
                    ),
                    placeholderText = placeholder.toString(),
                    textStyle = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onSurfaceOnBackground),
                    keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                            onDone = { keyboardController?.hide() },
                    ),
                    modifier = Modifier.testTag(testTag)
            )
        }
    }
}
