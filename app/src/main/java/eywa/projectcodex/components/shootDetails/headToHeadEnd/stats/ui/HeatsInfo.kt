package eywa.projectcodex.components.shootDetails.headToHeadEnd.stats.ui

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
import eywa.projectcodex.components.shootDetails.headToHeadEnd.stats.HeadToHeadStatsIntent
import eywa.projectcodex.components.shootDetails.headToHeadEnd.stats.HeadToHeadStatsState
import eywa.projectcodex.model.headToHead.FullHeadToHeadHeat

@Composable
internal fun HeatsInfo(
        state: HeadToHeadStatsState,
        modifier: Modifier = Modifier,
        listener: (HeadToHeadStatsIntent) -> Unit,
) {
    val heats = state.fullShootInfo.h2h?.heats ?: return
    val helpListener = { it: HelpShowcaseIntent -> listener(HeadToHeadStatsIntent.HelpShowcaseAction(it)) }

    if (heats.isEmpty()) {
        Text(
                text = stringResource(R.string.head_to_head_stats__heats_grid_no_data),
                modifier = modifier.testTag(HeadToHeadStatsTestTag.NO_HEATS_TEXT)
        )
    }
    else {
        val columns = listOf(
                HeadToHeadStatsHeatsColumn.MATCH,
                HeadToHeadStatsHeatsColumn.OPPONENT,
                HeadToHeadStatsHeatsColumn.RANK,
                HeadToHeadStatsHeatsColumn.RESULT,
        )

        CodexGridWithHeaders(
                data = heats
                        .sortedByDescending { it.heat.heat }
                        .map { HeadToHeadStatsHeatInfoDataRow(it) },
                columnMetadata = columns,
                extraData = Unit,
                helpListener = helpListener,
                modifier = modifier.testTag(HeadToHeadStatsTestTag.HEATS_TABLE)
        )
    }
}

data class HeadToHeadStatsHeatInfoDataRow(val heat: FullHeadToHeadHeat) : CodexGridRowMetadata

enum class HeadToHeadStatsHeatsColumn : CodexGridColumnMetadata<HeadToHeadStatsHeatInfoDataRow, Unit> {
    MATCH {
        override val primaryTitle: ResOrActual<String>
            get() = ResOrActual.StringResource(R.string.head_to_head_stats__heats_grid_match_title)

        override val mapping: (HeadToHeadStatsHeatInfoDataRow) -> ResOrActual<String>
            get() = {
                it.heat.heat.heat?.let { heat -> HeadToHeadUseCase.shortRoundName(heat) }
                        ?: ResOrActual.Actual(it.heat.heat.matchNumber.toString())
            }

        override val testTag: CodexTestTag
            get() = HeadToHeadStatsTestTag.MATCHES_TABLE_MATCH_CELL
    },
    OPPONENT {
        override val primaryTitle: ResOrActual<String>
            get() = ResOrActual.StringResource(R.string.head_to_head_stats__heats_grid_opponent_title)

        override val mapping: (HeadToHeadStatsHeatInfoDataRow) -> ResOrActual<String>
            get() = { data ->
                data.heat.heat.opponent?.let { ResOrActual.Actual(it) }
                        ?: ResOrActual.StringResource(R.string.head_to_head_stats__heats_grid_empty)
            }

        override val testTag: CodexTestTag
            get() = HeadToHeadStatsTestTag.MATCHES_TABLE_OPPONENT_CELL
    },
    RANK {
        override val primaryTitle: ResOrActual<String>
            get() = ResOrActual.StringResource(R.string.head_to_head_stats__heats_grid_rank_title)

        override val mapping: (HeadToHeadStatsHeatInfoDataRow) -> ResOrActual<String>
            get() = { data ->
                data.heat.heat.opponentQualificationRank?.let { ResOrActual.Actual(it.toString()) }
                        ?: ResOrActual.StringResource(R.string.head_to_head_stats__heats_grid_empty)
            }

        override val testTag: CodexTestTag
            get() = HeadToHeadStatsTestTag.MATCHES_TABLE_RANK_CELL
    },
    RESULT {
        override val primaryTitle: ResOrActual<String>
            get() = ResOrActual.StringResource(R.string.head_to_head_stats__heats_grid_result_title)

        override val mapping: (HeadToHeadStatsHeatInfoDataRow) -> ResOrActual<String>
            get() = { data ->
                if (data.heat.heat.isBye) {
                    ResOrActual.StringResource(R.string.head_to_head_score_pad__is_bye)
                }
                else {
                    val running = data.heat.runningTotals.lastOrNull()?.left
                    val result = data.heat.result.title

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
    override val cellContentDescription: (HeadToHeadStatsHeatInfoDataRow, Unit) -> ResOrActual<String>?
        get() = { _, _ -> null }
}
