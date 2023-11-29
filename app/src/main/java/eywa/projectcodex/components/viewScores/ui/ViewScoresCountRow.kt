package eywa.projectcodex.components.viewScores.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.helpShowcase.HelpState
import eywa.projectcodex.common.helpShowcase.updateHelpDialogPosition
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.sharedUi.ComposeUtils.orderPreviews
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.testTag
import eywa.projectcodex.components.viewScores.data.ViewScoresEntryList
import eywa.projectcodex.components.viewScores.ui.ViewScoresEntryPreviewProvider.convertToArrowCounters
import eywa.projectcodex.components.viewScores.ui.ViewScoresEntryPreviewProvider.setPersonalBests
import eywa.projectcodex.components.viewScores.ui.ViewScoresEntryPreviewProvider.setTiedPersonalBests

@Composable
fun ViewScoresCountRow(
        entries: ViewScoresEntryList,
        helpInfo: HelpShowcaseUseCase,
        modifier: Modifier = Modifier,
) {
    val helpListener = { it: HelpShowcaseIntent -> helpInfo.handle(it, CodexNavRoute.VIEW_SCORES::class) }

    Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier.padding(start = 8.dp, end = 15.dp, top = 5.dp, bottom = 8.dp)
    ) {
        Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
        ) {
            DateAndFirstNameColumn(entries, helpListener, Modifier.weight(1f))
            ArrowsShotColumn(entries, helpListener)
        }
        OtherNamesColumn(entries)
    }
}

@Composable
private fun ArrowsShotColumn(
        entries: ViewScoresEntryList,
        helpListener: (HelpShowcaseIntent) -> Unit,
) {
    val helpState = HelpState(
            helpListener = helpListener,
            helpShowcaseItem = HelpShowcaseItem(
                    helpTitle = stringResource(R.string.help_view_score__arrow_count_title),
                    helpBody = stringResource(R.string.help_view_score__arrow_count_body),
                    priority = ViewScoreHelpPriority.SPECIFIC_ROW_ACTION.ordinal,
            )
    )

    val title = stringResource(R.string.view_score__arrow_count)
    val count = entries.arrowsShotWithoutSighters.toString()
    Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = columnVerticalArrangement,
            // Not using merge descendants as that is already used by ViewScoresListItem (which will contain this).
            // "descendants that themselves have set mergeDescendants are not included in the merge"
            // https://developer.android.com/jetpack/compose/semantics
            modifier = Modifier.clearAndSetSemantics {
                contentDescription = "$title $count"
            }
    ) {
        Text(
                text = title,
                style = CodexTypography.SMALL.copy(
                        color = CodexTheme.colors.onListItemAppOnBackground.copy(alpha = 0.55f)
                ),
        )
        Text(
                text = count,
                style = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onListItemAppOnBackground),
                modifier = Modifier
                        .updateHelpDialogPosition(helpState)
                        .testTag(ViewScoresRowTestTag.COUNT)
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_LIGHT_ACCENT,
        widthDp = 350,
)
@Composable
fun ViewScoresCountRow_Preview(
        @PreviewParameter(ViewScoresCountRowPreviewProvider::class) param: ViewScoresEntryList,
) {
    CodexTheme {
        ViewScoresCountRow(
                entries = param,
                helpInfo = HelpShowcaseUseCase(),
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
class ViewScoresCountRowPreviewProvider : CollectionPreviewParameterProvider<ViewScoresEntryList>(
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
        ).map { ViewScoresEntryList(it.convertToArrowCounters()) }.orderPreviews()
)
