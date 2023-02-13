package eywa.projectcodex.common.sharedUi

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.helpShowcase.updateHelpDialogPosition

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

@Composable
fun DataRow(
        @StringRes title: Int,
        helpListener: ((HelpShowcaseIntent) -> Unit)? = null,
        @StringRes helpTitle: Int? = null,
        @StringRes helpBody: Int? = null,
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit,
) {
    require(helpTitle == null || (helpBody != null || helpListener == null)) { "If a title is given, a map and body must be given too" }
    var rowModifier = modifier

    if (helpTitle != null) {
        helpListener!!(HelpShowcaseIntent.Add(HelpShowcaseItem(helpTitle = helpTitle, helpBody = helpBody!!)))
        rowModifier = rowModifier.then(Modifier.updateHelpDialogPosition(helpListener!!, helpTitle))
    }

    Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            modifier = rowModifier
    ) {
        Text(
                text = stringResource(title),
        )
        content()
    }
}
