package eywa.projectcodex.components.viewScores.screenUi

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
import androidx.compose.ui.res.pluralStringResource
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
import eywa.projectcodex.common.helpShowcase.HelpState
import eywa.projectcodex.common.helpShowcase.updateHelpDialogPosition
import eywa.projectcodex.common.sharedUi.ComposeUtils.orderPreviews
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelperDsl
import eywa.projectcodex.common.sharedUi.testTag
import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadResult
import eywa.projectcodex.components.viewScores.data.ViewScoresEntryList
import eywa.projectcodex.components.viewScores.data.ViewScoresEntryPreviewProvider.toEntry

@Composable
fun ViewScoresHeadToHeadRow(
        entries: ViewScoresEntryList,
        entryIndex: Int,
        modifier: Modifier = Modifier,
        helpListener: (HelpShowcaseIntent) -> Unit,
) {
    Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier.padding(start = 8.dp, end = 15.dp, top = 5.dp, bottom = 8.dp)
    ) {
        Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
        ) {
            DateAndFirstNameColumn(entries, entryIndex, helpListener, Modifier.weight(1f))
            MatchesColumn(entries, entryIndex, helpListener)
        }
        OtherNamesColumn(entries)
    }
}

@Composable
private fun MatchesColumn(
        entries: ViewScoresEntryList,
        entryIndex: Int,
        helpListener: (HelpShowcaseIntent) -> Unit,
) {
    val helpState = HelpState(
            helpListener = helpListener,
            helpShowcaseItem = HelpShowcaseItem(
                    helpTitle = stringResource(R.string.help_view_score__head_to_head_title),
                    helpBody = stringResource(R.string.help_view_score__head_to_head_body),
                    priority = ViewScoreHelpPriority.SPECIFIC_ROW_ACTION.ordinal,
                    boundsId = ViewScoresHelpBoundaries.LIST.ordinal,
            )
    )

    val (wins, losses, other) = entries.h2hWinsLossesOther

    val description =
            if (wins > 0 || losses > 0) {
                stringResource(
                        R.string.view_score__h2h_semantics,
                        pluralStringResource(R.plurals.view_score__h2h_wins, wins, wins),
                        pluralStringResource(R.plurals.view_score__h2h_losses, losses, losses),
                        pluralStringResource(R.plurals.view_score__h2h_other_matches, other, other),
                )
            }
            else {
                pluralStringResource(R.plurals.view_score__h2h_matches, other, other)
            }

    Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = columnVerticalArrangement,
            // Not using merge descendants as that is already used by ViewScoresListItem (which will contain this).
            // "descendants that themselves have set mergeDescendants are not included in the merge"
            // https://developer.android.com/jetpack/compose/semantics
            modifier = Modifier
                    .clearAndSetSemantics {
                        contentDescription = description
                    }
                    .updateHelpDialogPosition(helpState, entryIndex)
    ) {
        if (wins > 0 || losses > 0) {
            Text(
                    text = stringResource(R.string.view_score__h2h_wins_losses, wins, losses),
                    style = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onListItemAppOnBackground),
                    modifier = Modifier.testTag(ViewScoresRowTestTag.H2H_WINS_LOSSES)
            )
            if (other > 0) {
                Text(
                        text = pluralStringResource(R.plurals.view_score__h2h_other_matches, other, other),
                        style = CodexTypography.SMALL.copy(color = CodexTheme.colors.onListItemAppOnBackground),
                        color = CodexTheme.colors.onListItemAppOnBackground.copy(alpha = 0.55f),
                        modifier = Modifier.testTag(ViewScoresRowTestTag.H2H_OTHER_MATCHES)
                )
            }
        }
        else {
            Text(
                    text = pluralStringResource(R.plurals.view_score__h2h_matches, other, other),
                    style = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onListItemAppOnBackground),
                    modifier = Modifier.testTag(ViewScoresRowTestTag.H2H_MATCHES)
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_LIGHT_ACCENT,
        widthDp = 350,
)
@Composable
fun ViewScoresHeadToHeadRow_Preview(
        @PreviewParameter(ViewScoresHeadToHeadRowPreviewProvider::class) param: ViewScoresEntryList,
) {
    CodexTheme {
        ViewScoresHeadToHeadRow(entries = param, entryIndex = 1) {}
    }
}

@RequiresApi(Build.VERSION_CODES.O)
class ViewScoresHeadToHeadRowPreviewProvider : CollectionPreviewParameterProvider<ViewScoresEntryList>(
        listOf(
                listOf(
                        ShootPreviewHelperDsl.create {
                            round = RoundPreviewHelper.wa70RoundData
                            addH2h {
                                addHeat {
                                    addSet { addRows() }
                                    addSet { addRows() }
                                    addSet { addRows() }
                                }
                                addHeat {
                                    addSet { addRows(result = HeadToHeadResult.LOSS) }
                                    addSet { addRows(result = HeadToHeadResult.LOSS) }
                                    addSet { addRows(result = HeadToHeadResult.LOSS) }
                                }
                            }
                        }.toEntry()
                ),
                listOf(
                        ShootPreviewHelperDsl.create {
                            addH2h {
                                addHeat {
                                    addSet { addRows() }
                                    addSet { addRows() }
                                    addSet { addRows() }
                                }
                                addHeat {
                                    addSet { addRows(result = HeadToHeadResult.LOSS) }
                                    addSet { addRows(result = HeadToHeadResult.LOSS) }
                                    addSet { addRows(result = HeadToHeadResult.LOSS) }
                                }
                                addHeat {
                                    addSet { addRows(result = HeadToHeadResult.UNKNOWN) }
                                    addSet { addRows(result = HeadToHeadResult.UNKNOWN) }
                                    addSet { addRows(result = HeadToHeadResult.UNKNOWN) }
                                }
                            }
                        }.toEntry()
                ),
                listOf(
                        ShootPreviewHelperDsl.create {
                            addH2h {
                                addHeat {
                                    addSet { addRows(result = HeadToHeadResult.UNKNOWN) }
                                    addSet { addRows(result = HeadToHeadResult.UNKNOWN) }
                                    addSet { addRows(result = HeadToHeadResult.UNKNOWN) }
                                }
                            }
                        }.toEntry()
                ),
        ).map { ViewScoresEntryList(it) }.orderPreviews()
)
