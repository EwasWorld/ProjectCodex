package eywa.projectcodex.components.viewScores.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.ComposeHelpShowcaseItem
import eywa.projectcodex.common.helpShowcase.ComposeHelpShowcaseMap
import eywa.projectcodex.common.helpShowcase.updateHelpDialogPosition
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.utils.DateTimeFormat
import eywa.projectcodex.components.viewScores.data.ViewScoresEntry
import java.util.*

internal val columnVerticalArrangement = Arrangement.spacedBy(2.dp)

/**
 * Displays a [ViewScoresEntry]
 */
@Composable
internal fun ViewScoresEntryRow(
        entry: ViewScoresEntry,
        helpInfo: ComposeHelpShowcaseMap,
        isInMultiSelectMode: Boolean,
        modifier: Modifier = Modifier,
) {
    val semanticsString = viewScoresEntryRowAccessibilityString(entry, isInMultiSelectMode)

    helpInfo.add(
            ComposeHelpShowcaseItem(
                    helpTitle = R.string.help_view_score__hsg_title,
                    helpBody = R.string.help_view_score__hsg_body,
                    priority = ViewScoresScreen.HelpItemPriority.SPECIFIC_ROW_ACTION.ordinal
            )
    )
    helpInfo.add(
            ComposeHelpShowcaseItem(
                    helpTitle = R.string.help_view_score__handicap_title,
                    helpBody = R.string.help_view_score__handicap_body,
                    priority = ViewScoresScreen.HelpItemPriority.SPECIFIC_ROW_ACTION.ordinal
            )
    )

    Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
            modifier = modifier
                    .padding(
                            start = 8.dp,
                            end = 15.dp,
                            top = 5.dp,
                            bottom = 5.dp,
                    )
                    .semantics { contentDescription = semanticsString }
    ) {
        DateAndRoundNameColumn(entry, Modifier.weight(1f))
        HsgColumn(entry, helpInfo)
        HandicapColumn(entry, helpInfo)
    }
}

@Composable
private fun DateAndRoundNameColumn(entry: ViewScoresEntry, modifier: Modifier = Modifier) {
    Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = columnVerticalArrangement,
            modifier = modifier
    ) {
        Text(
                text = DateTimeFormat.SHORT_DATE_TIME.format(entry.archerRound.dateShot),
                style = CodexTypography.SMALL_DIMMED,
        )
        Text(
                text = entry.displayName
                        ?: stringResource(R.string.create_round__no_round),
                style = CodexTypography.NORMAL,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 6.dp)
        )
    }
}


@Composable
private fun HsgColumn(
        entry: ViewScoresEntry,
        helpInfo: ComposeHelpShowcaseMap,
) {
    Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = columnVerticalArrangement,
    ) {
        Text(
                text = stringResource(id = R.string.view_score__hsg),
                style = CodexTypography.SMALL_DIMMED,
        )
        @Suppress("RemoveRedundantQualifierName")
        Text(
                text = entry.hitsScoreGolds
                        ?: stringResource(id = R.string.view_score__hsg_placeholder),
                style = CodexTypography.NORMAL,
                modifier = Modifier.updateHelpDialogPosition(helpInfo, R.string.help_view_score__hsg_title)
        )
    }
}

@Composable
private fun HandicapColumn(
        entry: ViewScoresEntry,
        helpInfo: ComposeHelpShowcaseMap,
) {
    Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = columnVerticalArrangement,
    ) {
        Text(
                text = stringResource(id = R.string.view_score__handicap),
                style = CodexTypography.SMALL_DIMMED,
        )
        Box(
                contentAlignment = Alignment.Center
        ) {
            @Suppress("RemoveRedundantQualifierName")
            Text(
                    text = entry.handicap?.toString()
                            ?: stringResource(id = R.string.view_score__handicap_placeholder),
                    style = CodexTypography.NORMAL,
                    modifier = Modifier.updateHelpDialogPosition(helpInfo, R.string.help_view_score__handicap_title)
            )
            // Force width to always accommodate "00" - this will forces columns into alignment
            Text(
                    text = "00",
                    style = CodexTypography.NORMAL.copy(color = Color.Transparent),
            )
        }
    }
}

@Composable
private fun viewScoresEntryRowAccessibilityString(
        entry: ViewScoresEntry,
        isInMultiSelectMode: Boolean,
): String {
    @Composable
    fun accessibilityString(@StringRes title: Int, value: Int?, @StringRes alt: Int? = null) =
            value?.let { stringResource(title) + " $it" } ?: alt?.let { stringResource(it) }

    val dateFormat = Calendar.getInstance().apply {
        set(
                // y/m/d
                get(Calendar.YEAR), 0, 1,
                // hr/min/s
                1, 1, 1
        )
    }.time.before(entry.archerRound.dateShot).let { wasThisYear ->
        if (wasThisYear) DateTimeFormat.LONG_DAY_MONTH else DateTimeFormat.LONG_DATE
    }

    return listOfNotNull(
            dateFormat.format(entry.archerRound.dateShot),
            entry.displayName,
            if (entry.isSelected && isInMultiSelectMode) stringResource(id = R.string.view_scores__selected) else null,
            accessibilityString(
                    title = R.string.view_score__score, value = entry.score, alt = R.string.view_score__no_arrows_shot
            ),
            accessibilityString(title = R.string.view_score__handicap_full, value = entry.handicap),
            entry.golds?.let {
                accessibilityString(title = R.string.view_score__golds, value = entry.golds)
            },
            entry.hits?.let {
                accessibilityString(title = R.string.view_score__hits, value = entry.hits)
            }
    ).joinToString()
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_LIGHT_ACCENT
)
@Composable
fun ViewScoresEntryRow_Preview() {
    CodexTheme {
        ViewScoresEntryRow(
                entry = ViewScoresEntryPreviewProvider.generateEntries(1).first(),
                helpInfo = ComposeHelpShowcaseMap(),
                isInMultiSelectMode = false,
        )
    }
}
