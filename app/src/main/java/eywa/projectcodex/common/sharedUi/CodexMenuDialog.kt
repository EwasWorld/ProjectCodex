package eywa.projectcodex.common.sharedUi

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography

@Composable
fun CodexMenuDialog(
        isShown: Boolean,
        items: List<CodexMenuDialogItem>,
        onDismiss: () -> Unit,
) {
    SimpleDialog(
            isShown = isShown,
            onDismissListener = onDismiss,
    ) {
        SimpleDialogContent(
                title = stringResource(R.string.sight_marks__menu_title),
                negativeButton = ButtonState(stringResource(R.string.general_cancel), onDismiss),
        ) {
            Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
            ) {
                items.forEach {
                    Text(
                            text = it.displayName,
                            style = CodexTypography.DIALOG_TEXT,
                            color = CodexTheme.colors.onDialogBackground,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { it.onClick() }
                                    .padding(8.dp)
                                    .testTag(it.itemTestTag)
                    )
                }
            }
        }
    }
}

interface CodexMenuDialogItem {
    val displayName: String
    val itemTestTag: String
    val onClick: () -> Unit
}
