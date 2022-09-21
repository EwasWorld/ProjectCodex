package eywa.projectcodex.components.viewScores.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
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
import eywa.projectcodex.components.viewScores.utils.ViewScoresDropdownMenuItem
import java.util.*

internal val columnVerticalArrangement = Arrangement.spacedBy(2.dp)

interface ViewScoresEntryListener {
    fun entryClicked(entryId: Int)
    fun entryLongClicked(entryId: Int)
    fun dropdownMenuItemClicked(menuItem: ViewScoresDropdownMenuItem): Boolean
}

/**
 * Displays a [ViewScoresEntry]
 *
 * @param dropdownMenuItems required to add custom actions in the semantics
 *      (this function does not create a dropdown menu)
 */
@Composable
internal fun ViewScoresEntryRow(
        entry: ViewScoresEntry,
        dropdownMenuItems: List<ViewScoresDropdownMenuItem>?,
        listener: ViewScoresEntryListener,
        helpInfo: ComposeHelpShowcaseMap,
) {
    getHelpInfoEntries().forEach { helpInfo.add(it) }

    Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                    .padding(
                            start = 8.dp,
                            end = 15.dp,
                            top = 5.dp,
                            bottom = 5.dp,
                    )
                    .pointerInput(null) {
                        detectTapGestures(
                                onTap = { listener.entryClicked(entry.id) },
                                onLongPress = { listener.entryLongClicked(entry.id) },
                        )
                    }
                    .customSemantics(
                            entry = entry,
                            dropdownMenuItems = dropdownMenuItems,
                            onDropdownMenuItemClicked = { listener.dropdownMenuItemClicked(it) },
                            entryClickedListener = { listener.entryClicked(entry.id) }
                    )
    ) {
        DateAndRoundNameColumn(entry)
        HsgColumn(entry, helpInfo)
        HandicapColumn(entry, helpInfo)
    }
}

@Composable
private fun RowScope.DateAndRoundNameColumn(entry: ViewScoresEntry) {
    Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = columnVerticalArrangement,
            modifier = Modifier.weight(1f)
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

private fun getHelpInfoEntries() = listOf(
        ComposeHelpShowcaseItem(
                helpTitle = R.string.help_view_score__hsg_title,
                helpBody = R.string.help_view_score__hsg_body,
                priority = ViewScoreScreen.HelpItemPriority.SPECIFIC_ROW_ACTION.ordinal
        ),
        ComposeHelpShowcaseItem(
                helpTitle = R.string.help_view_score__handicap_title,
                helpBody = R.string.help_view_score__handicap_body,
                priority = ViewScoreScreen.HelpItemPriority.SPECIFIC_ROW_ACTION.ordinal
        )
)

private fun Modifier.customSemantics(
        entry: ViewScoresEntry,
        dropdownMenuItems: List<ViewScoresDropdownMenuItem>?,
        onDropdownMenuItemClicked: (ViewScoresDropdownMenuItem) -> Unit,
        entryClickedListener: () -> Unit,
) = composed {
    val semanticsString = viewScoresRowAccessibilityString(entry)
    val semanticsOnClickLabel = stringResource(id = R.string.view_scores_menu__score_pad)
    val itemCustomActions = dropdownMenuItems?.map {
        CustomAccessibilityAction(stringResource(id = it.title)) { onDropdownMenuItemClicked(it); true }
    } ?: listOf()

    clearAndSetSemantics {
        contentDescription = semanticsString
        customActions = itemCustomActions
        onClick(semanticsOnClickLabel) { entryClickedListener(); true }
    }
}

@Composable
private fun viewScoresRowAccessibilityString(entry: ViewScoresEntry): String {
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
                dropdownMenuItems = listOf(),
                listener = object : ViewScoresEntryListener {
                    override fun entryClicked(entryId: Int) {}
                    override fun entryLongClicked(entryId: Int) {}
                    override fun dropdownMenuItemClicked(menuItem: ViewScoresDropdownMenuItem): Boolean = true
                },
                helpInfo = ComposeHelpShowcaseMap(),
        )
    }
}
