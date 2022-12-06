package eywa.projectcodex.components.archerRoundScore

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import eywa.projectcodex.common.helpShowcase.ComposeHelpShowcaseItem
import eywa.projectcodex.common.helpShowcase.ComposeHelpShowcaseMap
import eywa.projectcodex.common.helpShowcase.updateHelpDialogPosition
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography

@Composable
private fun style(textAlign: TextAlign = TextAlign.Start) =
        CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground, textAlign = textAlign)

@Composable
fun DataRow(
        @StringRes title: Int,
        text: String,
        helpInfo: ComposeHelpShowcaseMap? = null,
        @StringRes helpTitle: Int? = null,
        @StringRes helpBody: Int? = null,
        modifier: Modifier = Modifier,
        textModifier: Modifier = Modifier,
) = DataRow(
        title = title,
        helpInfo = helpInfo,
        helpTitle = helpTitle,
        helpBody = helpBody,
        modifier = modifier,
) {
    Text(
            text = text,
            style = style(textAlign = TextAlign.Start),
            modifier = textModifier,
    )
}

@Composable
fun DataRow(
        @StringRes title: Int,
        helpInfo: ComposeHelpShowcaseMap? = null,
        @StringRes helpTitle: Int? = null,
        @StringRes helpBody: Int? = null,
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit,
) {
    require(helpTitle == null || (helpBody != null || helpInfo == null)) { "If a title is given, a map and body must be given too" }
    var rowModifier = modifier

    if (helpTitle != null) {
        helpInfo!!.add(ComposeHelpShowcaseItem(helpTitle = helpTitle, helpBody = helpBody!!))
        rowModifier = rowModifier.then(Modifier.updateHelpDialogPosition(helpInfo, helpTitle))
    }

    Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            modifier = rowModifier
    ) {
        Text(
                text = stringResource(title),
                style = style(textAlign = TextAlign.End),
        )
        content()
    }
}