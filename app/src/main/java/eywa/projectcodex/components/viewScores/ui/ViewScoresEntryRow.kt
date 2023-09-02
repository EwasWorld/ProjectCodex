package eywa.projectcodex.components.viewScores.ui

import android.content.Context
import android.content.res.Resources
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.*
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.sharedUi.ComposeUtils.orderPreviews
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.utils.DateTimeFormat
import eywa.projectcodex.components.viewScores.data.ViewScoresEntry
import eywa.projectcodex.components.viewScores.data.ViewScoresEntryList
import eywa.projectcodex.components.viewScores.data.ViewScoresRoundNameInfo
import eywa.projectcodex.components.viewScores.ui.ViewScoresEntryPreviewProvider.setPersonalBests
import eywa.projectcodex.components.viewScores.ui.ViewScoresEntryPreviewProvider.setTiedPersonalBests
import eywa.projectcodex.model.getOverallPbString
import java.util.*

internal val columnVerticalArrangement = Arrangement.spacedBy(2.dp)

// TODO PB labels help
/**
 * Displays a [ViewScoresEntry]
 */
@Composable
internal fun ViewScoresEntryRow(
        entry: ViewScoresEntry,
        helpInfo: HelpShowcaseUseCase,
        modifier: Modifier = Modifier,
        showPbs: Boolean,
) = ViewScoresEntryRow(
        entries = ViewScoresEntryList(entry),
        helpInfo = helpInfo,
        showPbs = showPbs,
        modifier = modifier,
)

/**
 * Displays a [ViewScoresEntry]
 *
 * @param showPbs true if pb labels should be shown if applicable
 */
@Composable
internal fun ViewScoresEntryRow(
        entries: ViewScoresEntryList,
        helpInfo: HelpShowcaseUseCase,
        modifier: Modifier = Modifier,
        showPbs: Boolean,
) {
    val helpListener = { it: HelpShowcaseIntent -> helpInfo.handle(it, CodexNavRoute.VIEW_SCORES::class) }

    Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier.padding(start = 8.dp, end = 15.dp, top = 5.dp, bottom = 8.dp)
    ) {
        if (!entries.allPbTypes.isNullOrEmpty() && showPbs) {
            Surface(
                    color = CodexTheme.colors.personalBestTag,
                    shape = RoundedCornerShape(100),
            ) {
                Text(
                        text = stringResource(entries.allPbTypes.getOverallPbString(entries.isMulti)),
                        style = CodexTypography.SMALL.copy(color = CodexTheme.colors.onListItemAppOnBackground),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
        Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
        ) {
            DateAndFirstNameColumn(entries, helpListener, Modifier.weight(1f))
            HsgColumn(entries, helpListener)
            HandicapColumn(entries, helpListener)
        }
        if (entries.secondDisplayName != null || entries.totalUndisplayedNamesCount != null) {
            Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.align(Alignment.Start)
            ) {
                entries.secondDisplayName?.let {
                    DisplayName(
                            nameInfo = it,
                            modifier = Modifier.weight(1f, false)
                    )
                }
                // Will display up to 2 round names. Indicate how many more there are to the user
                entries.totalUndisplayedNamesCount?.let { remaining ->
                    Text(
                            text = stringResource(R.string.view_score__multiple_ellipses, remaining),
                            style = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onListItemAppOnBackground),
                            fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun DateAndFirstNameColumn(
        entries: ViewScoresEntryList,
        helpListener: (HelpShowcaseIntent) -> Unit,
        modifier: Modifier = Modifier,
) {
    val helpState = HelpState(
            helpListener = helpListener,
            helpShowcaseItem = HelpShowcaseItem(
                    helpTitle = stringResource(R.string.help_view_score__round_title),
                    helpBody = stringResource(R.string.help_view_score__round_body),
                    priority = ViewScoreHelpPriority.SPECIFIC_ROW_ACTION.ordinal,
            )
    )

    Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = columnVerticalArrangement,
            modifier = modifier
    ) {
        Text(
                text = DateTimeFormat.SHORT_DATE_TIME.format(entries.dateShot),
                style = CodexTypography.SMALL.copy(color = CodexTheme.colors.onListItemLight),
        )
        DisplayName(
                nameInfo = entries.firstDisplayName,
                modifier = Modifier.updateHelpDialogPosition(helpState)
        )
    }
}

@Composable
private fun DisplayName(
        nameInfo: ViewScoresRoundNameInfo,
        modifier: Modifier = Modifier,
) =
        Text(
                text = getDisplayName(nameInfo, LocalContext.current.resources),
                style = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onListItemAppOnBackground),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textDecoration = if (nameInfo.strikethrough) TextDecoration.LineThrough else TextDecoration.None,
                modifier = modifier
        )

private fun getDisplayName(
        nameInfo: ViewScoresRoundNameInfo,
        resources: Resources,
): String {
    var text = nameInfo.displayName ?: resources.getString(R.string.create_round__no_round)

    if (nameInfo.identicalCount == 2) {
        text = resources.getString(R.string.view_score__double, text)
    }
    else if (nameInfo.identicalCount > 2) {
        text = resources.getString(R.string.view_score__multiple, nameInfo.identicalCount, text)
    }

    if (nameInfo.prefixWithAmpersand) {
        text = resources.getString(R.string.view_score__joiner, text)
    }

    return text
}

@Composable
private fun HsgColumn(
        entries: ViewScoresEntryList,
        helpListener: (HelpShowcaseIntent) -> Unit,
) {
    val helpState = HelpState(
            helpListener = helpListener,
            helpShowcaseItem = HelpShowcaseItem(
                    helpTitle = stringResource(R.string.help_view_score__hsg_title),
                    helpBody = stringResource(R.string.help_view_score__hsg_body),
                    priority = ViewScoreHelpPriority.SPECIFIC_ROW_ACTION.ordinal,
            )
    )

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
        Text(
                text = entries.hitsScoreGolds ?: stringResource(R.string.view_score__hsg_placeholder),
                style = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onListItemAppOnBackground),
                modifier = Modifier.updateHelpDialogPosition(helpState)
        )
    }
}

@Composable
private fun HandicapColumn(
        entries: ViewScoresEntryList,
        helpListener: (HelpShowcaseIntent) -> Unit,
) {
    val helpState = HelpState(
            helpListener = helpListener,
            helpShowcaseItem = HelpShowcaseItem(
                    helpTitle = stringResource(R.string.help_view_score__handicap_title),
                    helpBody = stringResource(
                            if (entries.use2023System) R.string.help_view_score__handicap_2023_body
                            else R.string.help_view_score__handicap_old_body
                    ),
                    priority = ViewScoreHelpPriority.SPECIFIC_ROW_ACTION.ordinal,
            )
    )

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
                    text = entries.handicap?.toString()
                            ?: stringResource(R.string.view_score__handicap_placeholder),
                    style = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onListItemAppOnBackground),
                    modifier = Modifier.updateHelpDialogPosition(helpState)
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

fun viewScoresEntryRowAccessibilityString(context: Context, entry: ViewScoresEntry) =
        viewScoresEntryRowAccessibilityString(context.resources, ViewScoresEntryList(entry))

fun viewScoresEntryRowAccessibilityString(resources: Resources, entryList: ViewScoresEntryList): String {
    fun accessibilityString(@StringRes title: Int, value: Int?) =
            value?.let { resources.getString(title) + " $it" }

    val dateFormat = Calendar.getInstance().apply {
        set(
                // y/m/d
                get(Calendar.YEAR), 0, 1,
                // hr/min/s
                1, 1, 1
        )
    }.before(entryList.dateShot).let { wasThisYear ->
        if (wasThisYear) DateTimeFormat.LONG_DAY_MONTH else DateTimeFormat.LONG_DATE_FULL_YEAR
    }

    return listOfNotNull(
            dateFormat.format(entryList.dateShot),
            entryList.firstDisplayName.takeIf { it.displayName != null }?.let { getDisplayName(it, resources) },
            entryList.secondDisplayName?.let { getDisplayName(it, resources) },
            entryList.totalUndisplayedNamesCount
                    ?.let { resources.getString(R.string.view_score__multiple_ellipses, it) },
            accessibilityString(title = R.string.view_score__score, value = entryList.score),
            accessibilityString(title = R.string.view_score__handicap_full, value = entryList.handicap),
            accessibilityString(title = R.string.view_score__golds, value = entryList.golds),
            accessibilityString(title = R.string.view_score__hits, value = entryList.hits),
    ).joinToString()
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_LIGHT_ACCENT,
        widthDp = 350,
)
@Composable
fun ViewScoresEntryRow_Preview(
        @PreviewParameter(ViewScoresEntryRowPreviewProvider::class) param: ViewScoresEntryList,
) {
    CodexTheme {
        ViewScoresEntryRow(
                entries = param,
                helpInfo = HelpShowcaseUseCase(),
                showPbs = true,
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
class ViewScoresEntryRowPreviewProvider : CollectionPreviewParameterProvider<ViewScoresEntryList>(
        listOf(
                ViewScoresEntryPreviewProvider.generateEntries(1),
                listOf(ViewScoresEntryPreviewProvider.generateIncompleteRound()),
                ViewScoresEntryPreviewProvider.generateEntries(1).setPersonalBests(listOf(0)),
                ViewScoresEntryPreviewProvider.generateEntries(1)
                        .setPersonalBests(listOf(0))
                        .setTiedPersonalBests(listOf(0)),
                ViewScoresEntryPreviewProvider.generateEntries(2),
                ViewScoresEntryPreviewProvider.generateEntries(2).reversed(),
                ViewScoresEntryPreviewProvider.generateEntries(4),
                ViewScoresEntryPreviewProvider.generateEntries(4).let { listOf(it[1], it[0]).plus(it.drop(2)) },
                List(2) { ViewScoresEntryPreviewProvider.generateEntries(1).first() },
                List(3) { ViewScoresEntryPreviewProvider.generateEntries(1).first() },
                listOf(
                        ViewScoresEntryPreviewProvider.generateEntries(1).first(),
                        ViewScoresEntryPreviewProvider.generateIncompleteRound(),
                ),
                listOf(
                        ViewScoresEntryPreviewProvider.generateEntries(2).last(),
                        ViewScoresEntryPreviewProvider.generateIncompleteRound(),
                ),
        ).map { ViewScoresEntryList(it) }.orderPreviews()
)
