package eywa.projectcodex.common.sharedUi

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import eywa.projectcodex.common.helpShowcase.HelpState
import eywa.projectcodex.common.helpShowcase.updateHelpDialogPosition
import eywa.projectcodex.common.sharedUi.ComposeUtils.modifierIf
import eywa.projectcodex.common.sharedUi.codexTheme.asClickableStyle

@Composable
fun DataRow(
        title: String,
        text: String,
        modifier: Modifier = Modifier,
        textModifier: Modifier = Modifier,
        helpState: HelpState? = null,
        onClick: (() -> Unit)? = null,
        onClickLabel: String? = null,
        accessibilityRole: Role? = null,
        textStyle: TextStyle = LocalTextStyle.current,
        textClickableStyle: TextStyle = LocalTextStyle.current.asClickableStyle(),
        titleStyle: TextStyle = textStyle,
) = DataRow(
        title = title,
        helpState = helpState,
        style = titleStyle,
        modifier = modifier.clearAndSetSemantics {
            contentDescription = "$text $title"
            onClick?.let { onClick(onClickLabel) { onClick(); true } }
            accessibilityRole?.let { role = accessibilityRole }
        },
) {
    Text(
            text = text,
            style = if (onClick == null) textStyle else textClickableStyle,
            textAlign = TextAlign.Center,
            modifier = textModifier.modifierIf(
                    onClick != null,
                    Modifier.clickable { onClick!!.invoke() }
            ),
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DataRow(
        title: String,
        helpState: HelpState?,
        modifier: Modifier = Modifier,
        titleModifier: Modifier = Modifier,
        style: TextStyle = LocalTextStyle.current,
        content: @Composable () -> Unit,
) {
    FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier.updateHelpDialogPosition(helpState)
    ) {
        Text(
                text = title,
                style = style,
                modifier = titleModifier
        )
        content()
    }
}
