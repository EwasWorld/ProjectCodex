package eywa.projectcodex.components.viewScores.ui

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcase
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.helpShowcase.updateHelpDialogPosition
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.utils.DateTimeFormat
import eywa.projectcodex.components.viewScores.ViewScoresFragment
import eywa.projectcodex.components.viewScores.data.ViewScoresEntry
import eywa.projectcodex.components.viewScores.ui.ViewScoresEntryPreviewProvider.setPersonalBests
import eywa.projectcodex.components.viewScores.ui.ViewScoresEntryPreviewProvider.setTiedPersonalBests
import java.util.*

internal val columnVerticalArrangement = Arrangement.spacedBy(2.dp)

// TODO PB labels help
/**
 * Displays a [ViewScoresEntry]
 */
@Composable
internal fun ViewScoresEntryRow(
        entry: ViewScoresEntry,
        helpInfo: HelpShowcase,
        modifier: Modifier = Modifier,
        showPbs: Boolean,
) {
    helpInfo.handle(
            HelpShowcaseIntent.Add(
                    HelpShowcaseItem(
                            helpTitle = R.string.help_view_score__hsg_title,
                            helpBody = R.string.help_view_score__hsg_body,
                            priority = ViewScoresScreen.HelpItemPriority.SPECIFIC_ROW_ACTION.ordinal
                    )
            ),
            ViewScoresFragment::class,
    )
    helpInfo.handle(
            HelpShowcaseIntent.Add(
                    HelpShowcaseItem(
                            helpTitle = R.string.help_view_score__handicap_title,
                            helpBody = (
                                    if (entry.info.use2023HandicapSystem) R.string.help_view_score__handicap_2023_body
                                    else R.string.help_view_score__handicap_old_body
                                    ),
                            priority = ViewScoresScreen.HelpItemPriority.SPECIFIC_ROW_ACTION.ordinal
                    )
            ),
            ViewScoresFragment::class,
    )
    helpInfo.handle(
            HelpShowcaseIntent.Add(
                    HelpShowcaseItem(
                            helpTitle = R.string.help_view_score__round_title,
                            helpBody = R.string.help_view_score__round_body,
                            priority = ViewScoresScreen.HelpItemPriority.SPECIFIC_ROW_ACTION.ordinal
                    )
            ),
            ViewScoresFragment::class,
    )

    Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier.padding(start = 8.dp, end = 15.dp, top = 5.dp, bottom = 8.dp)
    ) {
        if (entry.info.isPersonalBest && showPbs) {
            Surface(
                    color = CodexTheme.colors.targetFaceGold,
                    shape = RoundedCornerShape(100),
            ) {
                Text(
                        text = stringResource(
                                if (entry.info.isTiedPersonalBest) R.string.view_score__round_personal_best_tied
                                else R.string.view_score__round_personal_best
                        ),
                        style = CodexTypography.SMALL.copy(color = CodexTheme.colors.onListItemAppOnBackground),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
        Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
        ) {
            DateAndRoundNameColumn(entry, helpInfo, Modifier.weight(1f))
            HsgColumn(entry, helpInfo)
            HandicapColumn(entry, helpInfo)
        }
    }
}

@Composable
private fun DateAndRoundNameColumn(
        entry: ViewScoresEntry,
        helpInfo: HelpShowcase,
        modifier: Modifier = Modifier,
) {
    Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = columnVerticalArrangement,
            modifier = modifier
    ) {
        Text(
                text = DateTimeFormat.SHORT_DATE_TIME.format(entry.info.archerRound.dateShot),
                style = CodexTypography.SMALL.copy(
                        color = CodexTheme.colors.onListItemAppOnBackground.copy(alpha = 0.55f)
                ),
        )
        Text(
                text = entry.info.displayName
                        ?: stringResource(R.string.create_round__no_round),
                style = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onListItemAppOnBackground),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textDecoration = (
                        if (entry.info.round == null || entry.isRoundComplete()) TextDecoration.None
                        else TextDecoration.LineThrough
                        ),
                modifier = Modifier.updateHelpDialogPosition(helpInfo, R.string.help_view_score__round_title)
        )
    }
}


@Composable
private fun HsgColumn(
        entry: ViewScoresEntry,
        helpInfo: HelpShowcase,
) {
    Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = columnVerticalArrangement,
    ) {
        Text(
                text = stringResource(R.string.view_score__hsg),
                style = CodexTypography.SMALL.copy(
                        color = CodexTheme.colors.onListItemAppOnBackground.copy(alpha = 0.55f)
                ),
        )
        @Suppress("RemoveRedundantQualifierName")
        Text(
                text = entry.hitsScoreGolds ?: stringResource(R.string.view_score__hsg_placeholder),
                style = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onListItemAppOnBackground),
                modifier = Modifier.updateHelpDialogPosition(helpInfo, R.string.help_view_score__hsg_title)
        )
    }
}

@Composable
private fun HandicapColumn(
        entry: ViewScoresEntry,
        helpInfo: HelpShowcase,
) {
    Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = columnVerticalArrangement,
    ) {
        Text(
                text = stringResource(R.string.view_score__handicap),
                style = CodexTypography.SMALL.copy(
                        color = CodexTheme.colors.onListItemAppOnBackground.copy(alpha = 0.55f)
                ),
        )
        Box(
                contentAlignment = Alignment.Center
        ) {
            Text(
                    text = entry.handicap?.toString()
                            ?: stringResource(R.string.view_score__handicap_placeholder),
                    style = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onListItemAppOnBackground),
                    modifier = Modifier.updateHelpDialogPosition(helpInfo, R.string.help_view_score__handicap_title)
            )
            // Force width to always accommodate "00" - this will forces columns into alignment
            Text(
                    text = "00",
                    style = CodexTypography.NORMAL.copy(color = Color.Transparent),
                    modifier = Modifier.clearAndSetSemantics { }
            )
        }
    }
}

fun viewScoresEntryRowAccessibilityString(context: Context, entry: ViewScoresEntry): String {
    fun accessibilityString(@StringRes title: Int, value: Int?) =
            value?.let { context.resources.getString(title) + " $it" }

    val dateFormat = Calendar.getInstance().apply {
        set(
                // y/m/d
                get(Calendar.YEAR), 0, 1,
                // hr/min/s
                1, 1, 1
        )
    }.time.before(entry.info.archerRound.dateShot).let { wasThisYear ->
        if (wasThisYear) DateTimeFormat.LONG_DAY_MONTH else DateTimeFormat.LONG_DATE_FULL_YEAR
    }

    return listOfNotNull(
            dateFormat.format(entry.info.archerRound.dateShot),
            entry.info.displayName,
            accessibilityString(title = R.string.view_score__score, value = entry.info.score),
            accessibilityString(title = R.string.view_score__handicap_full, value = entry.handicap),
            accessibilityString(title = R.string.view_score__golds, value = entry.golds),
            accessibilityString(title = R.string.view_score__hits, value = entry.info.hits),
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
                helpInfo = HelpShowcase(),
                showPbs = true,
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_LIGHT_ACCENT
)
@Composable
fun Incomplete_ViewScoresEntryRow_Preview() {
    CodexTheme {
        ViewScoresEntryRow(
                entry = ViewScoresEntryPreviewProvider.generateIncompleteRound(),
                helpInfo = HelpShowcase(),
                showPbs = true,
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_LIGHT_ACCENT
)
@Composable
fun PersonalBest_ViewScoresEntryRow_Preview() {
    CodexTheme {
        ViewScoresEntryRow(
                entry = ViewScoresEntryPreviewProvider.generateEntries(1).setPersonalBests(listOf(0)).first(),
                helpInfo = HelpShowcase(),
                showPbs = true,
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_LIGHT_ACCENT
)
@Composable
fun TiedPersonalBest_ViewScoresEntryRow_Preview() {
    CodexTheme {
        ViewScoresEntryRow(
                entry = ViewScoresEntryPreviewProvider.generateEntries(1)
                        .setPersonalBests(listOf(0))
                        .setTiedPersonalBests(listOf(0))
                        .first(),
                helpInfo = HelpShowcase(),
                showPbs = true,
        )
    }
}
