package eywa.projectcodex.components.shootDetails.headToHead.stats.ui

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.sharedUi.grid.CodexGridColumnMetadata
import eywa.projectcodex.common.sharedUi.grid.CodexGridRowMetadata
import eywa.projectcodex.common.sharedUi.grid.CodexGridWithHeaders
import eywa.projectcodex.common.sharedUi.testTag
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.components.referenceTables.headToHead.HeadToHeadUseCase
import eywa.projectcodex.components.shootDetails.headToHead.stats.HeadToHeadStatsIntent
import eywa.projectcodex.components.shootDetails.headToHead.stats.HeadToHeadStatsState
import eywa.projectcodex.model.headToHead.FullHeadToHeadMatch

@Composable
internal fun MatchInfoTable(
        state: HeadToHeadStatsState,
        modifier: Modifier = Modifier,
        listener: (HeadToHeadStatsIntent) -> Unit,
) {
    val matches = state.fullShootInfo.h2h?.matches ?: return
    val helpListener = { it: HelpShowcaseIntent -> listener(HeadToHeadStatsIntent.HelpShowcaseAction(it)) }

    if (matches.isEmpty()) {
        Text(
                text = stringResource(R.string.head_to_head_stats__heats_grid_no_data),
                modifier = modifier.testTag(HeadToHeadStatsTestTag.NO_MATCHES_TEXT)
        )
    }
    else {
        val columns = listOf(
                HeadToHeadStatsMatchesColumn.MATCH,
                HeadToHeadStatsMatchesColumn.OPPONENT,
                HeadToHeadStatsMatchesColumn.RANK,
                HeadToHeadStatsMatchesColumn.RESULT,
        )

        CodexGridWithHeaders(
                data = matches
                        .sortedByDescending { it.match.heat }
                        .map { HeadToHeadStatsMatchesInfoDataRow(it) },
                columnMetadata = columns,
                extraData = Unit,
                helpListener = helpListener,
                modifier = modifier.testTag(HeadToHeadStatsTestTag.MATCHES_TABLE)
        )
    }
}

data class HeadToHeadStatsMatchesInfoDataRow(val match: FullHeadToHeadMatch) : CodexGridRowMetadata

enum class HeadToHeadStatsMatchesColumn : CodexGridColumnMetadata<HeadToHeadStatsMatchesInfoDataRow, Unit> {
    MATCH {
        override val primaryTitle: ResOrActual<String>
            get() = ResOrActual.StringResource(R.string.head_to_head_stats__heats_grid_match_title)

        override val mapping: (HeadToHeadStatsMatchesInfoDataRow) -> ResOrActual<String>
            get() = { row ->
                row.match.match.heat?.let { it -> HeadToHeadUseCase.shortRoundName(it) }
                        ?: ResOrActual.Actual(row.match.match.matchNumber.toString())
            }

        override val testTag: CodexTestTag
            get() = HeadToHeadStatsTestTag.MATCHES_TABLE_MATCH_CELL
    },
    OPPONENT {
        override val primaryTitle: ResOrActual<String>
            get() = ResOrActual.StringResource(R.string.head_to_head_stats__heats_grid_opponent_title)

        override val mapping: (HeadToHeadStatsMatchesInfoDataRow) -> ResOrActual<String>
            get() = { data ->
                data.match.match.opponent?.let { ResOrActual.Actual(it) }
                        ?: ResOrActual.StringResource(R.string.head_to_head_stats__heats_grid_empty)
            }

        override val testTag: CodexTestTag
            get() = HeadToHeadStatsTestTag.MATCHES_TABLE_OPPONENT_CELL
    },
    RANK {
        override val primaryTitle: ResOrActual<String>
            get() = ResOrActual.StringResource(R.string.head_to_head_stats__heats_grid_rank_title)

        override val mapping: (HeadToHeadStatsMatchesInfoDataRow) -> ResOrActual<String>
            get() = { data ->
                data.match.match.opponentQualificationRank?.let { ResOrActual.Actual(it.toString()) }
                        ?: ResOrActual.StringResource(R.string.head_to_head_stats__heats_grid_empty)
            }

        override val testTag: CodexTestTag
            get() = HeadToHeadStatsTestTag.MATCHES_TABLE_RANK_CELL
    },
    RESULT {
        override val primaryTitle: ResOrActual<String>
            get() = ResOrActual.StringResource(R.string.head_to_head_stats__heats_grid_result_title)

        override val mapping: (HeadToHeadStatsMatchesInfoDataRow) -> ResOrActual<String>
            get() = { data ->
                if (data.match.match.isBye) {
                    ResOrActual.StringResource(R.string.head_to_head_score_pad__is_bye)
                }
                else {
                    val running = data.match.runningTotals.lastOrNull()?.left
                    val result = data.match.result.title

                    if (running != null) {
                        ResOrActual.StringResource(
                                R.string.head_to_head_stats__heats_grid_result,
                                listOf(running.first, running.second, result),
                        )
                    }
                    else {
                        result
                    }
                }
            }

        override val testTag: CodexTestTag
            get() = HeadToHeadStatsTestTag.MATCHES_TABLE_RESULT_CELL
    },
    ;

    override val primaryTitleHorizontalSpan: Int
        get() = 1
    override val primaryTitleVerticalSpan: Int
        get() = 1
    override val secondaryTitle: ResOrActual<String>?
        get() = null
    override val helpTitle: ResOrActual<String>?
        get() = null
    override val helpBody: ResOrActual<String>?
        get() = null
    override val cellContentDescription: (HeadToHeadStatsMatchesInfoDataRow, Unit) -> ResOrActual<String>?
        get() = { _, _ -> null }
}
