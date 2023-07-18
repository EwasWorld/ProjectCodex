package eywa.projectcodex.common.sharedUi

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpState
import eywa.projectcodex.common.helpShowcase.updateHelpDialogPosition
import eywa.projectcodex.common.sharedUi.ComposeUtils.modifierIf

@Deprecated("Old")
@Composable
fun DataRow(
        @StringRes title: Int,
        text: String,
        helpListener: ((HelpShowcaseIntent) -> Unit)? = null,
        @StringRes helpTitle: Int? = null,
        @StringRes helpBody: Int? = null,
        modifier: Modifier = Modifier,
        textModifier: Modifier = Modifier,
) = DataRow(
        title = title,
        helpListener = helpListener,
        helpTitle = helpTitle,
        helpBody = helpBody,
        modifier = modifier,
) {
    Text(
            text = text,
            modifier = textModifier,
    )
}

@Deprecated("Old")
@Composable
fun DataRow(
        @StringRes title: Int,
        helpListener: ((HelpShowcaseIntent) -> Unit)? = null,
        @StringRes helpTitle: Int? = null,
        @StringRes helpBody: Int? = null,
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit,
) {
    val helpCount = listOfNotNull(helpListener, helpBody, helpTitle).count()
    require(helpCount == 0 || helpCount == 3) { "If a title is given, a map and body must be given too" }

    DataRow(
            title = stringResource(title),
            helpState = helpTitle?.let {
                HelpState(helpListener!!, stringResource(helpTitle), stringResource(helpBody!!))
            },
            content = content,
            modifier = modifier,
    )
}

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
        style: TextStyle = LocalTextStyle.current,
) = DataRow(
        title = title,
        helpState = helpState,
        modifier = modifier.clearAndSetSemantics {
            contentDescription = "$text $title"
            onClick?.let { onClick(onClickLabel) { onClick(); true } }
            accessibilityRole?.let { role = accessibilityRole }
        },
) {
    Text(
            text = text,
            style = style,
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
        content: @Composable () -> Unit,
) {
    helpState?.add()

    FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier.updateHelpDialogPosition(helpState)
    ) {
        Text(
                text = title,
                modifier = titleModifier
        )
        content()
    }
}
