package eywa.projectcodex.components.mainMenu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Flag
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpState
import eywa.projectcodex.common.sharedUi.*
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.utils.DateTimeFormat
import java.util.*

val SEEN_HANDICAP_NOTICE_LATEST_APP_VERSION
    get() = WhatsNewInfo.V2_1_0.appVersion

/**
 * @return true if there have been [WhatsNewInfo.importantUpdates] since [lastShownAppVersion]
 */
fun hasUpdates(lastShownAppVersion: AppVersion? = null, importantOnly: Boolean = true) =
        WhatsNewInfo.values()
                .sortedByDescending { it.releaseDate }
                .takeWhile { lastShownAppVersion == null || it.appVersion > lastShownAppVersion }
                .any { it.importantUpdates != null || (!importantOnly && it.updates != null) }

@Composable
private fun WhatsNewButton(
        lastShownAppVersion: AppVersion? = null,
        helpListener: (HelpShowcaseIntent) -> Unit,
        onClick: () -> Unit,
) {
    val icon =
            if (hasUpdates(lastShownAppVersion, false)) Icons.Default.Flag
            else Icons.Default.Check

    CodexIconButton(
            onClick = onClick,
            captionBelow = stringResource(R.string.whats_new__button_text),
            helpState = HelpState(
                    helpListener = helpListener,
                    helpTitle = stringResource(R.string.help_whats_new__title),
                    helpBody = stringResource(R.string.help_whats_new__body),
            ),
            icon = CodexIconInfo.VectorIcon(
                    imageVector = icon,
                    tint = CodexTheme.colors.onFilledButton,
            ),
            captionStyle = CodexTypography.SMALL
    )
}

@Composable
fun WhatsNewButtonAndDialog(
        isDialogShown: Boolean,
        lastShownAppVersion: AppVersion? = null,
        onDialogDismiss: (latestUpdateAppVersion: AppVersion) -> Unit,
        buttonOnClick: () -> Unit,
        helpListener: (HelpShowcaseIntent) -> Unit,
) {
    val onDismiss = { onDialogDismiss(WhatsNewInfo.values().first().appVersion) }

    WhatsNewButton(
            lastShownAppVersion = lastShownAppVersion,
            helpListener = helpListener,
            onClick = buttonOnClick,
    )

    SimpleDialog(
            isShown = isDialogShown,
            onDismissListener = onDismiss,
    ) {
        SimpleDialogContent(
                title = stringResource(R.string.whats_new__title),
                negativeButton = ButtonState(
                        text = stringResource(R.string.general_cancel),
                        onClick = onDismiss,
                ),
        ) {
            Column(
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
            ) {

                val allInfo = WhatsNewInfo.values().sortedByDescending { it.releaseDate }
                val firstSeen =
                        if (lastShownAppVersion == null) null
                        else allInfo.firstOrNull { it.appVersion <= lastShownAppVersion }
                allInfo.forEach { info ->

                    Divider(modifier = Modifier.fillMaxWidth())
                    if (firstSeen == info) {
                        Text(
                                text = stringResource(R.string.whats_new__seen_banner),
                                style = CodexTypography.DIALOG_TEXT,
                                color = CodexTheme.colors.dialogNegativeText,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                        .align(Alignment.CenterHorizontally)
                                        .padding(vertical = 10.dp)
                        )
                        Divider(modifier = Modifier.fillMaxWidth())
                    }

                    Spacer(modifier = Modifier.height(3.dp))
                    WhatsNewInfo(info)
                    Spacer(modifier = Modifier.height(15.dp))
                }
            }
        }
    }
}

@Composable
private fun WhatsNewInfo(info: WhatsNewInfo) {
    Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
                text = info.appVersion.toString(),
                style = CodexTypography.DIALOG_TITLE,
                fontWeight = FontWeight.Bold,
        )
        Text(
                text = DateTimeFormat.SHORT_DATE.format(info.releaseDate),
                style = CodexTypography.DIALOG_TEXT,
                fontStyle = FontStyle.Italic,
        )
    }
    if (info.importantUpdates != null) {
        Text(
                text = stringResource(info.importantUpdates),
                fontWeight = FontWeight.Bold,
                style = CodexTypography.DIALOG_TEXT,
                modifier = Modifier
                        // Between box and other things
                        .padding(horizontal = 5.dp, vertical = 7.dp)
                        .background(
                                color = CodexTheme.colors.dialogBackgroundAccent,
                                shape = RoundedCornerShape(15)
                        )
                        // Between box and text
                        .padding(horizontal = 10.dp, vertical = 7.dp)
        )
    }
    if (info.updates != null) {
        Text(
                text = stringResource(info.updates),
                style = CodexTypography.DIALOG_TEXT,
                modifier = Modifier.padding(horizontal = 10.dp)
        )
    }
}

private enum class WhatsNewInfo(
        val appVersion: AppVersion,
        val releaseDate: Calendar,
        val importantUpdates: Int? = null,
        val updates: Int? = null,
) {
    V2_3_0(
            appVersion = AppVersion("2.3.0"),
            releaseDate = DateTimeFormat.SHORT_DATE.parse("27/07/23"),
            importantUpdates = R.string.whats_new__2_3_0_important_update,
            updates = R.string.whats_new__2_3_0_update,
    ),
    V2_1_0(
            appVersion = AppVersion("2.1.0"),
            releaseDate = DateTimeFormat.SHORT_DATE.parse("21/03/23"),
            importantUpdates = R.string.whats_new__2_1_0_important_update,
    ),
    ;

    companion object {
        init {
            check(
                    values().sortedByDescending { it.releaseDate } == values().sortedByDescending { it.appVersion }
            ) { "Version/release date sorting discrepancy" }
            check(
                    values().distinctBy { it.releaseDate }.size == values().size
            ) { "Duplicate release dates" }
            check(
                    values().distinctBy { it.appVersion }.size == values().size
            ) { "Duplicate version codes" }
        }
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun Unseen_WhatsNewButton_Preview() {
    CodexTheme {
        WhatsNewButton(
                lastShownAppVersion = null,
                helpListener = {},
        ) {}
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun Seen_WhatsNewButton_Preview() {
    CodexTheme {
        WhatsNewButton(
                lastShownAppVersion = WhatsNewInfo.values().first().appVersion,
                helpListener = {},
        ) {}
    }
}

@Preview
@Composable
fun WhatsNewDialog_Preview() {
    DialogPreviewHelper {
        WhatsNewButtonAndDialog(
                isDialogShown = true,
                lastShownAppVersion = WhatsNewInfo.values()[1].appVersion,
                onDialogDismiss = {},
                helpListener = {},
                buttonOnClick = {},
        )
    }
}
