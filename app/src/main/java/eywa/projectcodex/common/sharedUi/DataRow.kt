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
import eywa.projectcodex.common.helpShowcase.HelpState
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
        helpState: HelpState?,
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit,
) {
    helpState?.add()

    Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier.updateHelpDialogPosition(helpState)
    ) {
        Text(
                text = title,
        )
        content()
    }
}
