package eywa.projectcodex.components.mainMenu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.helpShowcase.asHelpState
import eywa.projectcodex.common.sharedUi.ButtonState
import eywa.projectcodex.common.sharedUi.CodexIconButton
import eywa.projectcodex.common.sharedUi.CodexIconInfo
import eywa.projectcodex.common.sharedUi.DialogPreviewHelper
import eywa.projectcodex.common.sharedUi.SimpleDialog
import eywa.projectcodex.common.sharedUi.SimpleDialogContent
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.utils.DateTimeFormat
import java.util.Calendar

val SEEN_HANDICAP_NOTICE_LATEST_APP_VERSION
    get() = WhatsNewInfo.V2_1_0.appVersion

/**
 * @return true if there have been [WhatsNewInfo.importantUpdates] since [lastShownAppVersion]
 */
fun hasUpdates(lastShownAppVersion: AppVersion? = null, importantOnly: Boolean = true) =
        WhatsNewInfo.entries
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
            helpState = HelpShowcaseItem(
                    helpTitle = stringResource(R.string.help_whats_new__title),
                    helpBody = stringResource(R.string.help_whats_new__body),
            ).asHelpState(helpListener = helpListener),
            icon = CodexIconInfo.VectorIcon(
                    imageVector = icon,
                    tint = CodexTheme.colors.onFilledButton,
            ),
            captionStyle = CodexTypography.SMALL,
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
    val onDismiss = { onDialogDismiss(WhatsNewInfo.entries.first().appVersion) }

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
                        text = stringResource(R.string.general_close),
                        onClick = onDismiss,
                ),
        ) {
            Column(
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
            ) {

                val allInfo = WhatsNewInfo.entries.sortedByDescending { it.releaseDate }
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
                                shape = RoundedCornerShape(10),
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
    V2_5_2(
            appVersion = AppVersion("2.5.2"),
            releaseDate = DateTimeFormat.SHORT_DATE.parse("21/04/25"),
            updates = R.string.whats_new__2_5_2_update,
    ),
    V2_5_0(
            appVersion = AppVersion("2.5.0"),
            releaseDate = DateTimeFormat.SHORT_DATE.parse("09/09/24"),
            updates = R.string.whats_new__2_5_0_update,
    ),
    V2_4_2(
            appVersion = AppVersion("2.4.2"),
            releaseDate = DateTimeFormat.SHORT_DATE.parse("25/04/24"),
            importantUpdates = R.string.whats_new__2_4_2_important_update,
            updates = R.string.whats_new__2_4_2_update,
    ),
    V2_3_0(
            appVersion = AppVersion("2.3.0"),
            releaseDate = DateTimeFormat.SHORT_DATE.parse("14/08/23"),
            importantUpdates = R.string.whats_new__2_3_0_important_update,
            updates = R.string.whats_new__2_3_0_update,
    ),
    V2_1_0(
            appVersion = AppVersion("2.1.0"),
            releaseDate = DateTimeFormat.SHORT_DATE.parse("21/03/23"),
            importantUpdates = R.string.whats_new__2_1_0_important_update,
            updates = R.string.whats_new__2_1_0_update,
    ),
    ;

    companion object {
        init {
            check(
                    entries.sortedByDescending { it.releaseDate } == entries.sortedByDescending { it.appVersion },
            ) { "Version/release date sorting discrepancy" }
            check(
                    entries.distinctBy { it.releaseDate }.size == entries.size,
            ) { "Duplicate release dates" }
            check(
                    entries.distinctBy { it.appVersion }.size == entries.size,
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
                lastShownAppVersion = WhatsNewInfo.entries.first().appVersion,
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
                lastShownAppVersion = WhatsNewInfo.entries[1].appVersion,
                onDialogDismiss = {},
                helpListener = {},
                buttonOnClick = {},
        )
    }
}
