package eywa.projectcodex.components.shootDetails.headToHeadEnd.stats.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.grid.CodexGridColumnMetadata
import eywa.projectcodex.common.sharedUi.grid.CodexGridRowMetadata
import eywa.projectcodex.common.sharedUi.grid.CodexGridWithHeaders
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelperDsl
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.common.utils.asDecimalFormat
import eywa.projectcodex.common.utils.classificationTables.ClassificationTablesPreviewHelper
import eywa.projectcodex.components.referenceTables.headToHead.HeadToHeadUseCase
import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadArcherType
import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadResult
import eywa.projectcodex.components.shootDetails.headToHeadEnd.stats.HeadToHeadStatsIntent
import eywa.projectcodex.components.shootDetails.headToHeadEnd.stats.HeadToHeadStatsState
import eywa.projectcodex.components.shootDetails.headToHeadEnd.stats.ui.HeadToHeadStatsNumbersColumn.*
import eywa.projectcodex.components.shootDetails.stats.ui.StatsDivider
import eywa.projectcodex.model.Handicap
import eywa.projectcodex.model.headToHead.FullHeadToHeadHeat
import eywa.projectcodex.model.headToHead.RowArrows

@Composable
internal fun ColumnScope.NumbersBreakdown(
        state: HeadToHeadStatsState,
        modifier: Modifier = Modifier,
        listener: (HeadToHeadStatsIntent) -> Unit,
) {
    val heats = state.fullShootInfo.h2h?.heats ?: return
    val helpListener = { it: HelpShowcaseIntent -> listener(HeadToHeadStatsIntent.HelpShowcaseAction(it)) }

    if (heats.isEmpty()) {
        // Heats info section already displays a no data message for this
        return
    }
    else {
        val calculateHandicap = { totalScore: Int, arrowCount: Int ->
            val fullRoundInfo = state.fullShootInfo.fullRoundInfo?.maxDistanceOnlyWithArrowCount(arrowCount)
            if (fullRoundInfo != null) {
                Handicap.getHandicapForRound(
                        round = fullRoundInfo,
                        subType = null,
                        score = totalScore,
                        innerTenArcher = state.fullShootInfo.isInnerTenArcher,
                        use2023Handicaps = state.fullShootInfo.use2023HandicapSystem,
                        faces = state.fullShootInfo.faces,
                )
            }
            else null
        }

        var data: List<HeadToHeadStatsNumbersBreakdownDataRow> = heats.sortedByDescending { it.heat.heat }
                .mapNotNull {
                    if (it.heat.isBye) null
                    else HeadToHeadStatsNumbersBreakdownDataRow.Heat(it, calculateHandicap)
                }
                .takeIf { it.isNotEmpty() }
                ?: return

        val columns = calculateColumns(data, state.fullShootInfo.h2h.headToHead.teamSize)

        if (columns.isNullOrEmpty()) {
            Text(
                    text = stringResource(R.string.head_to_head_stats__heats_grid_no_data),
                    modifier = modifier
            )
        }
        else {
            if (data.size > 1) {
                data = data.plus(
                        HeadToHeadStatsNumbersBreakdownDataRow.Total(
                                teamSize = state.fullShootInfo.h2h.headToHead.teamSize,
                                arrowData = data.map { it.arrowData }.reduce { acc, map ->
                                    val keys = acc.keys + map.keys
                                    keys.associateWith { key ->
                                        val accArrow = acc[key]
                                        val mapArrow = map[key]
                                        if (accArrow != null && mapArrow != null) accArrow + mapArrow
                                        else accArrow ?: mapArrow!!
                                    }
                                },
                                calculateHandicap = calculateHandicap,
                        ),
                )
            }

            StatsDivider(modifier = Modifier.padding(vertical = 10.dp))
            CodexGridWithHeaders(
                    data = data,
                    columnMetadata = columns,
                    extraData = Unit,
                    helpListener = helpListener,
                    modifier = modifier
            )
        }
    }
}

fun calculateColumns(
        data: List<HeadToHeadStatsNumbersBreakdownDataRow>,
        teamSize: Int,
): List<CodexGridColumnMetadata<HeadToHeadStatsNumbersBreakdownDataRow, Unit>>? {
    fun List<HeadToHeadStatsNumbersBreakdownDataRow>.contains(type: HeadToHeadArcherType): Boolean =
            any { it.arrowData.containsKey(type) }

    val hasTeamData = data.contains(HeadToHeadArcherType.TEAM)

    val showSelfColumn = data.contains(HeadToHeadArcherType.SELF)
    val showTeamColumn = teamSize > 1 && hasTeamData
    val showOppColumn = data.contains(HeadToHeadArcherType.OPPONENT)
    val showDiffColumn =
            hasTeamData && showOppColumn &&
                    data.any {
                        it.arrowData.containsKey(HeadToHeadArcherType.OPPONENT)
                                && it.arrowData.containsKey(HeadToHeadArcherType.TEAM)
                    }

    val handicapColumn =
            when {
                showSelfColumn -> HeadToHeadStatsFixedNumbersColumn.SELF_HANDICAP
                hasTeamData -> HeadToHeadStatsFixedNumbersColumn.TEAM_HANDICAP
                else -> null
            }

    fun List<HeadToHeadStatsNumbersColumn>.setColumnCountAndTitle(title: ResOrActual<String>) =
            mapIndexed { index, column ->
                column.update(
                        type = if (index != 0) null else title,
                        columnCount = size,
                )
            }

    val averages = listOfNotNull(
            SelfAverageArrow(null, 1).takeIf { showSelfColumn },
            TeamAverageArrow(null, 1).takeIf { showTeamColumn },
            OpponentAverageArrow(null, 1).takeIf { showOppColumn },
            DiffAverageArrow(null, 1).takeIf { showDiffColumn },
    ).setColumnCountAndTitle(ResOrActual.Actual("Arrow average"))
            .takeIf { it.isNotEmpty() } ?: return null

    val ends = listOfNotNull(
            SelfAverageEnd(null, 1).takeIf { showSelfColumn },
            TeamAverageEnd(null, 1).takeIf { showTeamColumn },
            OpponentAverageEnd(null, 1).takeIf { showOppColumn },
            DiffAverageEnd(null, 1).takeIf { showDiffColumn },
    ).setColumnCountAndTitle(ResOrActual.Actual("End average"))

    return listOfNotNull(
            HeadToHeadStatsFixedNumbersColumn.MATCH,
            handicapColumn,
    ).plus(averages).plus(ends)
}

sealed class HeadToHeadStatsNumbersBreakdownDataRow : CodexGridRowMetadata {
    abstract val calculateHandicap: (totalScore: Int, arrowCount: Int) -> Double?
    abstract val endSize: Int
    abstract val arrowData: Map<HeadToHeadArcherType, RowArrows?>

    val selfHandicap: Double?
        get() {
            val data = arrowData[HeadToHeadArcherType.SELF] ?: return null
            return calculateHandicap(data.total, data.arrowCount)
        }
    val teamHandicap: Double?
        get() {
            val data = arrowData[HeadToHeadArcherType.TEAM] ?: return null
            return calculateHandicap(data.total, data.arrowCount)
        }

    data class Heat(
            val heat: FullHeadToHeadHeat,
            override val calculateHandicap: (totalScore: Int, arrowCount: Int) -> Double?,
    ) : HeadToHeadStatsNumbersBreakdownDataRow() {
        override val endSize = HeadToHeadUseCase.endSize(teamSize = heat.teamSize, isShootOff = false)

        override val arrowData =
                listOf(
                        HeadToHeadArcherType.SELF,
                        HeadToHeadArcherType.TEAM,
                        HeadToHeadArcherType.OPPONENT,
                ).mapNotNull {
                    val arrows = heat.getArrows(it) ?: return@mapNotNull null
                    it to arrows
                }.toMap()

    }

    data class Total(
            val teamSize: Int,
            override val arrowData: Map<HeadToHeadArcherType, RowArrows?>,
            override val calculateHandicap: (totalScore: Int, arrowCount: Int) -> Double?,
    ) : HeadToHeadStatsNumbersBreakdownDataRow() {
        override val endSize = HeadToHeadUseCase.endSize(teamSize = teamSize, isShootOff = false)

        override fun isTotal(): Boolean = true
    }
}

sealed class HeadToHeadStatsNumbersColumn : CodexGridColumnMetadata<HeadToHeadStatsNumbersBreakdownDataRow, Unit> {
    abstract val type: ResOrActual<String>?
    abstract val columnCount: Int

    data class SelfAverageArrow(
            override val type: ResOrActual<String>?,
            override val columnCount: Int,
    ) : HeadToHeadStatsNumbersColumn() {
        override val secondaryTitle: ResOrActual<String>
            get() = ResOrActual.Actual("Self")

        override val mapping: (HeadToHeadStatsNumbersBreakdownDataRow) -> ResOrActual<String>
            get() = { it.arrowData[HeadToHeadArcherType.SELF]?.averageArrowScore.asDecimalFormat() }

        override fun update(type: ResOrActual<String>?, columnCount: Int): HeadToHeadStatsNumbersColumn =
                SelfAverageArrow(type, columnCount)
    }

    data class TeamAverageArrow(
            override val type: ResOrActual<String>?,
            override val columnCount: Int,
    ) : HeadToHeadStatsNumbersColumn() {
        override val secondaryTitle: ResOrActual<String>
            get() = ResOrActual.Actual("Team")

        override val mapping: (HeadToHeadStatsNumbersBreakdownDataRow) -> ResOrActual<String>
            get() = { it.arrowData[HeadToHeadArcherType.TEAM]?.averageArrowScore.asDecimalFormat() }

        override fun update(type: ResOrActual<String>?, columnCount: Int): HeadToHeadStatsNumbersColumn =
                TeamAverageArrow(type, columnCount)
    }

    data class OpponentAverageArrow(
            override val type: ResOrActual<String>?,
            override val columnCount: Int,
    ) : HeadToHeadStatsNumbersColumn() {
        override val secondaryTitle: ResOrActual<String>
            get() = ResOrActual.Actual("Opp")

        override val mapping: (HeadToHeadStatsNumbersBreakdownDataRow) -> ResOrActual<String>
            get() = { it.arrowData[HeadToHeadArcherType.OPPONENT]?.averageArrowScore.asDecimalFormat() }

        override fun update(type: ResOrActual<String>?, columnCount: Int): HeadToHeadStatsNumbersColumn =
                OpponentAverageArrow(type, columnCount)
    }

    data class DiffAverageArrow(
            override val type: ResOrActual<String>?,
            override val columnCount: Int,
    ) : HeadToHeadStatsNumbersColumn() {
        override val secondaryTitle: ResOrActual<String>
            get() = ResOrActual.Actual("Diff")

        override val mapping: (HeadToHeadStatsNumbersBreakdownDataRow) -> ResOrActual<String>
            get() = {
                val team = it.arrowData[HeadToHeadArcherType.TEAM]?.averageArrowScore
                val opponent = it.arrowData[HeadToHeadArcherType.OPPONENT]?.averageArrowScore
                val diff = if (team != null && opponent != null) team - opponent else null
                diff.asDecimalFormat()
            }

        @Composable
        override fun textColour(rowData: HeadToHeadStatsNumbersBreakdownDataRow): Color? {
            val text = (mapping(rowData) as? ResOrActual.Actual)?.actual
            return when {
                text == null || text == "0.0" || text == "-" -> null
                text.startsWith("-") -> CodexTheme.colors.errorOnAppBackground
                else -> CodexTheme.colors.successOnAppBackground
            }
        }

        override fun update(type: ResOrActual<String>?, columnCount: Int): HeadToHeadStatsNumbersColumn =
                DiffAverageArrow(type, columnCount)
    }

    data class SelfAverageEnd(
            override val type: ResOrActual<String>?,
            override val columnCount: Int,
    ) : HeadToHeadStatsNumbersColumn() {
        override val secondaryTitle: ResOrActual<String>
            get() = ResOrActual.Actual("Self")

        override val mapping: (HeadToHeadStatsNumbersBreakdownDataRow) -> ResOrActual<String>
            get() = {
                it.arrowData[HeadToHeadArcherType.SELF]?.averageArrowScore?.times(it.endSize).asDecimalFormat()
            }

        override fun update(type: ResOrActual<String>?, columnCount: Int): HeadToHeadStatsNumbersColumn =
                SelfAverageEnd(type, columnCount)
    }

    data class TeamAverageEnd(
            override val type: ResOrActual<String>?,
            override val columnCount: Int,
    ) : HeadToHeadStatsNumbersColumn() {
        override val secondaryTitle: ResOrActual<String>
            get() = ResOrActual.Actual("Team")

        override val mapping: (HeadToHeadStatsNumbersBreakdownDataRow) -> ResOrActual<String>
            get() = {
                it.arrowData[HeadToHeadArcherType.TEAM]?.averageArrowScore?.times(it.endSize).asDecimalFormat()
            }

        override fun update(type: ResOrActual<String>?, columnCount: Int): HeadToHeadStatsNumbersColumn =
                TeamAverageEnd(type, columnCount)
    }

    data class OpponentAverageEnd(
            override val type: ResOrActual<String>?,
            override val columnCount: Int,
    ) : HeadToHeadStatsNumbersColumn() {
        override val secondaryTitle: ResOrActual<String>
            get() = ResOrActual.Actual("Opp")

        override val mapping: (HeadToHeadStatsNumbersBreakdownDataRow) -> ResOrActual<String>
            get() = {
                it.arrowData[HeadToHeadArcherType.OPPONENT]?.averageArrowScore?.times(it.endSize).asDecimalFormat()
            }

        override fun update(type: ResOrActual<String>?, columnCount: Int): HeadToHeadStatsNumbersColumn =
                OpponentAverageEnd(type, columnCount)
    }

    data class DiffAverageEnd(
            override val type: ResOrActual<String>?,
            override val columnCount: Int,
    ) : HeadToHeadStatsNumbersColumn() {
        override val secondaryTitle: ResOrActual<String>
            get() = ResOrActual.Actual("Diff")

        override val mapping: (HeadToHeadStatsNumbersBreakdownDataRow) -> ResOrActual<String>
            get() = {
                val team = it.arrowData[HeadToHeadArcherType.TEAM]?.averageArrowScore
                val opponent = it.arrowData[HeadToHeadArcherType.OPPONENT]?.averageArrowScore
                val diff = if (team != null && opponent != null) team - opponent else null
                diff?.times(it.endSize).asDecimalFormat()
            }

        @Composable
        override fun textColour(rowData: HeadToHeadStatsNumbersBreakdownDataRow): Color? {
            val text = (mapping(rowData) as? ResOrActual.Actual)?.actual
            return when {
                text == null || text == "0.0" || text == "-" -> null
                text.startsWith("-") -> CodexTheme.colors.errorOnAppBackground
                else -> CodexTheme.colors.successOnAppBackground
            }
        }

        override fun update(type: ResOrActual<String>?, columnCount: Int): HeadToHeadStatsNumbersColumn =
                DiffAverageEnd(type, columnCount)
    }

    override val primaryTitle: ResOrActual<String>?
        get() = type
    override val primaryTitleHorizontalSpan: Int
        get() = columnCount
    override val primaryTitleVerticalSpan: Int
        get() = 1
    override val helpTitle: ResOrActual<String>?
        get() = null
    override val helpBody: ResOrActual<String>?
        get() = null
    override val testTag: CodexTestTag?
        get() = null
    override val cellContentDescription: (HeadToHeadStatsNumbersBreakdownDataRow, Unit) -> ResOrActual<String>?
        get() = { _, _ -> null }

    override fun padding(): PaddingValues =
            if (type != null) PaddingValues(start = 5.dp)
            else PaddingValues()

    abstract fun update(type: ResOrActual<String>?, columnCount: Int): HeadToHeadStatsNumbersColumn
}

enum class HeadToHeadStatsFixedNumbersColumn : CodexGridColumnMetadata<HeadToHeadStatsNumbersBreakdownDataRow, Unit> {
    MATCH {
        override val primaryTitle: ResOrActual<String>
            get() = ResOrActual.StringResource(R.string.head_to_head_stats__heats_grid_match_title)

        override val primaryTitleVerticalSpan: Int
            get() = 2

        override val mapping: (HeadToHeadStatsNumbersBreakdownDataRow) -> ResOrActual<String>
            get() = {
                if (it is HeadToHeadStatsNumbersBreakdownDataRow.Heat) {
                    HeadToHeadUseCase.shortRoundName(it.heat.heat.heat)
                }
                else {
                    ResOrActual.Actual("Total")
                }
            }
    },
    SELF_HANDICAP {
        override val primaryTitle: ResOrActual<String>
            get() = ResOrActual.Actual("Self\nHC")

        override val primaryTitleVerticalSpan: Int
            get() = 2

        override val mapping: (HeadToHeadStatsNumbersBreakdownDataRow) -> ResOrActual<String>
            get() = { it.selfHandicap.asDecimalFormat() }
    },
    TEAM_HANDICAP {
        override val primaryTitle: ResOrActual<String>
            get() = ResOrActual.Actual("Team\nHC")

        override val primaryTitleVerticalSpan: Int
            get() = 2

        override val mapping: (HeadToHeadStatsNumbersBreakdownDataRow) -> ResOrActual<String>
            get() = { it.teamHandicap.asDecimalFormat() }
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
    override val testTag: CodexTestTag?
        get() = null
    override val cellContentDescription: (HeadToHeadStatsNumbersBreakdownDataRow, Unit) -> ResOrActual<String>?
        get() = { _, _ -> null }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
        widthDp = 520,
)
@Composable
fun NumbersBreakdown_Preview() {
    val shoot = ShootPreviewHelperDsl.create {
        round = RoundPreviewHelper.wa70RoundData
        addH2h {
            addHeat { addSet { addRows(winnerScore = 29, loserScore = 27) } }
            addHeat { addSet { addRows(winnerScore = 28, loserScore = 24, result = HeadToHeadResult.LOSS) } }
            addHeat { addSet { addRows(winnerScore = 29, loserScore = 27) } }
            addHeat { addSet { addRows(result = HeadToHeadResult.TIE) } }
        }
    }

    CodexTheme {
        Column {
            NumbersBreakdown(
                    state = HeadToHeadStatsState(
                            fullShootInfo = shoot,
                            classificationTablesUseCase = ClassificationTablesPreviewHelper.get(),
                    )
            ) {}
        }
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
        widthDp = 650,
)
@Composable
fun Team_NumbersBreakdown_Preview() {
    val shoot = ShootPreviewHelperDsl.create {
        round = RoundPreviewHelper.wa70RoundData
        addH2h {
            headToHead = headToHead.copy(teamSize = 2)

            addHeat {
                addSet {
                    addRows(
                            typesToIsTotal = mapOf(
                                    HeadToHeadArcherType.SELF to false,
                                    HeadToHeadArcherType.TEAM to false,
                                    HeadToHeadArcherType.OPPONENT to true,
                            ),
                            winnerScore = 29,
                            loserScore = 27,
                            selfScore = 10,
                    )
                }
            }
            addHeat { addSet { addRows(winnerScore = 28, loserScore = 24, result = HeadToHeadResult.LOSS) } }
            addHeat {
                addSet {
                    addRows(winnerScore = 29, loserScore = 27)
                    removeRow(HeadToHeadArcherType.TEAM)
                }
            }
            addHeat { addSet { addRows(result = HeadToHeadResult.TIE) } }
        }
    }

    CodexTheme {
        Column {
            NumbersBreakdown(
                    state = HeadToHeadStatsState(
                            fullShootInfo = shoot,
                            classificationTablesUseCase = ClassificationTablesPreviewHelper.get(),
                    )
            ) {}
        }
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
        widthDp = 550,
)
@Composable
fun TeamHc_NumbersBreakdown_Preview() {
    val shoot = ShootPreviewHelperDsl.create {
        round = RoundPreviewHelper.wa70RoundData
        addH2h {
            headToHead = headToHead.copy(teamSize = 2)

            addHeat {
                addSet {
                    addRows(winnerScore = 29, loserScore = 27)
                }
            }
            addHeat { addSet { addRows(winnerScore = 28, loserScore = 24, result = HeadToHeadResult.LOSS) } }
            addHeat { addSet { addRows(winnerScore = 29, loserScore = 27) } }
            addHeat { addSet { addRows(result = HeadToHeadResult.TIE) } }
        }
    }

    CodexTheme {
        Column {
            NumbersBreakdown(
                    state = HeadToHeadStatsState(
                            fullShootInfo = shoot,
                            classificationTablesUseCase = ClassificationTablesPreviewHelper.get(),
                    )
            ) {}
        }
    }
}
