package eywa.projectcodex.model

import android.content.res.Resources
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import eywa.projectcodex.R
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.common.utils.get
import eywa.projectcodex.database.arrowValue.*
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance

class ScorePadData(
        info: FullArcherRoundInfo,
        endSize: Int,
        val goldsType: GoldsType,
) {
    val data = generateData(info, endSize, goldsType)

    /**
     * Calculates totals for each end, distance, and the round
     *
     * @param endSize how many arrows each row represents
     *
     * @return an list of rows each containing their end string, hits, score, and golds etc.
     * Has rows for distance totals and a grand total in appropriate places
     */
    private fun generateData(
            info: FullArcherRoundInfo,
            endSize: Int,
            goldsType: GoldsType,
    ): List<ScorePadRow> {
        require(endSize > 0) { "endSize must be >0" }
        if (info.arrows.isNullOrEmpty()) return mutableListOf()

        val distanceUnit = when {
            info.round == null -> null
            info.round.isMetric -> R.string.units_meters_short
            else -> R.string.units_yards_short
        }

        val remainingArrows = info.arrows.toMutableList()
        val tableData = mutableListOf<ScorePadRow>()

        // No round info: add all arrows
        if (info.round == null) {
            tableData.addAll(
                    generateRowsForDistance(
                            arrows = remainingArrows,
                            endSize = endSize,
                            goldsType = goldsType,
                    )
            )
        }
        else {
            // Has round info: add distance totals
            for (distance in info.roundDistances!!) {
                tableData.addAll(
                        generateRowsForDistance(
                                arrows = remainingArrows,
                                distance = distance,
                                arrowCount = info.roundArrowCounts!!
                                        .find { it.distanceNumber == distance.distanceNumber },
                                distanceUnit = distanceUnit,
                                addDistanceTotal = info.roundDistances.size > 1 || info.hasSurplusArrows!!,
                                runningTotal = tableData.mapNotNull { it.runningTotal }.maxOrNull(),
                                endSize = endSize,
                                goldsType = goldsType,
                                endNumber = tableData
                                        .filterIsInstance<ScorePadRow.End>()
                                        .maxOfOrNull { it.endNumber },
                        )
                )
                if (remainingArrows.isEmpty()) break
            }
            // If too many arrows
            if (remainingArrows.isNotEmpty()) {
                tableData.addAll(
                        generateRowsForDistance(
                                arrows = remainingArrows,
                                isSurplus = true,
                                addDistanceTotal = true,
                                runningTotal = tableData.mapNotNull { it.runningTotal }.maxOrNull(),
                                endSize = endSize,
                                goldsType = goldsType,
                                endNumber = tableData
                                        .filterIsInstance<ScorePadRow.End>()
                                        .maxOfOrNull { it.endNumber }
                        )
                )
            }
        }

        // Grand total
        tableData.add(ScorePadRow.GrandTotal(info.arrows, goldsType))

        return tableData
    }

    /**
     * @param endNumber 1-indexed
     */
    private fun generateRowsForDistance(
            arrows: MutableList<ArrowValue>,
            distance: RoundDistance? = null,
            arrowCount: RoundArrowCount? = null,
            @StringRes distanceUnit: Int? = null,
            isSurplus: Boolean = false,
            addDistanceTotal: Boolean = false,
            endNumber: Int? = null,
            runningTotal: Int? = null,
            endSize: Int,
            goldsType: GoldsType,
    ): List<ScorePadRow> {
        require(arrows.isNotEmpty()) { "Arrows cannot be empty" }
        require(distance?.roundId == arrowCount?.roundId) { "Round ids differ" }
        require(distance?.distanceNumber == arrowCount?.distanceNumber) { "Distance numbers differ" }
        if (!isSurplus && addDistanceTotal) {
            require(distance != null && distanceUnit != null) { "Distance and unit are required to record distance total" }
        }

        val distanceArrows = arrowCount?.arrowCount?.let { arrows.take(it) } ?: arrows.toList()
        arrows.removeAll(distanceArrows)

        val tableData = mutableListOf<ScorePadRow>()
        var currentRunningTotal = runningTotal ?: 0
        var currentEndNumber = endNumber ?: 0
        for (endArrows in distanceArrows.chunked(endSize)) {
            currentRunningTotal += endArrows.getScore()
            tableData.add(
                    ScorePadRow.End(++currentEndNumber, endArrows, goldsType, currentRunningTotal)
            )
        }

        if (addDistanceTotal) {
            tableData.add(
                    if (distance != null) {
                        ScorePadRow.DistanceTotal(distanceArrows, goldsType, distance.distance, distanceUnit!!)
                    }
                    else {
                        ScorePadRow.SurplusTotal(distanceArrows, goldsType)
                    }
            )
        }

        return tableData
    }

    fun isNullOrEmpty(): Boolean {
        return data.isEmpty()
    }

    /**
     * Converts the score pad data to a string for display.
     * One line for each end which shows data in the order given by [columnOrder], separated by at least one space.
     * Includes a grand total at the end.
     * The beginning of every item is padded so that columns are a fixed width.
     * Headers are also padded to the same fixed with.
     */
    fun getDetailsAsString(
            columnOrder: List<ColumnHeader>,
            resources: Resources,
            includeDistanceRows: Boolean
    ): ScorePadDetailsString {
        val headers = columnOrder.associateWith { resources.getString(it.getShortResourceId(goldsType)) }

        var outputData: List<ScorePadRow> = data
        if (!includeDistanceRows) {
            outputData = outputData.filterNot { it is ScorePadRow.DistanceTotal || it is ScorePadRow.SurplusTotal }
        }
        // Will later pad the start of each item to the max column width
        val maxWidths = columnOrder.associateWith { colHeader ->
            outputData.maxOf { it.getContent(colHeader, resources).length }
                    .coerceAtLeast(headers[colHeader]!!.length)
        }

        val header = columnOrder.joinToString(" ") { column ->
            "%${maxWidths[column]}s".format(headers[column]!!)
        }
        val details = outputData.joinToString("\n") { row ->
            columnOrder.joinToString(" ") { column ->
                "%${maxWidths[column]}s".format(row.getContent(column, resources))
            }
        }
        return ScorePadDetailsString(header, details)
    }

    /**
     * Converts the score pad data to a CSV.
     * One line for each end which shows data in the order given by [columnOrder], separated by a comma.
     * Includes a grand total at the end.
     */
    fun getDetailsAsCsv(
            columnOrder: List<ColumnHeader>,
            resources: Resources,
            includeDistanceTotals: Boolean
    ): ScorePadDetailsString {
        val header = columnOrder.joinToString(",") { resources.getString(it.getShortResourceId(goldsType)) }
        var outputData: List<ScorePadRow> = data
        if (!includeDistanceTotals) {
            outputData = outputData.filterNot { it is ScorePadRow.DistanceTotal || it is ScorePadRow.SurplusTotal }
        }
        val details = outputData.joinToString("\n") { row ->
            columnOrder.joinToString(",") { column -> row.getContent(column, resources) }
        }
        return ScorePadDetailsString(header, details)
    }

    sealed class ScorePadRow {
        protected abstract val hits: Int
        protected abstract val score: Int
        protected abstract val golds: Int
        internal open val runningTotal: Int? = null

        abstract fun getRowHeader(): ResOrActual<String>
        protected abstract fun getArrowsString(resources: Resources): String

        fun getContent(columnHeader: ColumnHeader, resources: Resources) = when (columnHeader) {
            ColumnHeader.ARROWS -> getArrowsString(resources)
            ColumnHeader.HITS -> hits.toString()
            ColumnHeader.SCORE -> score.toString()
            ColumnHeader.GOLDS -> golds.toString()
            ColumnHeader.RUNNING_TOTAL -> runningTotal?.toString()
                    ?: resources.getString(R.string.score_pad__running_total_placeholder)
        }

        /**
         * @param endNumber 1-indexed
         */
        data class End(
                internal val endNumber: Int,
                val arrowValues: List<ResOrActual<String>>,
                override val hits: Int,
                override val score: Int,
                override val golds: Int,
                override val runningTotal: Int,
        ) : ScorePadRow() {
            internal constructor(
                    endNumber: Int,
                    arrows: List<ArrowValue>,
                    goldsType: GoldsType,
                    runningTotal: Int,
            ) : this(
                    endNumber,
                    arrows.map { it.asString() },
                    arrows.getHits(),
                    arrows.getScore(),
                    arrows.getGolds(goldsType),
                    runningTotal
            )

            override fun getArrowsString(resources: Resources): String = arrowValues
                    .joinToString(resources.getString(R.string.end_to_string_arrow_deliminator)) { it.get(resources) }

            override fun getRowHeader() = ResOrActual.fromActual(endNumber.toString())
        }

        data class DistanceTotal(
                private val distance: Int,
                @StringRes private val distanceUnit: Int,
                override val hits: Int,
                override val score: Int,
                override val golds: Int,
        ) : ScorePadRow() {
            internal constructor(
                    arrows: List<ArrowValue>,
                    goldsType: GoldsType,
                    distance: Int,
                    @StringRes distanceUnit: Int
            ) : this(distance, distanceUnit, arrows.getHits(), arrows.getScore(), arrows.getGolds(goldsType))

            override fun getArrowsString(resources: Resources): String = resources.getString(
                    R.string.score_pad__distance_total,
                    distance,
                    resources.getString(distanceUnit),
            )

            override fun getRowHeader() = ResOrActual.fromRes<String>(R.string.score_pad__distance_total_row_header)
        }

        data class SurplusTotal(
                override val hits: Int,
                override val score: Int,
                override val golds: Int,
        ) : ScorePadRow() {
            constructor(arrows: List<ArrowValue>, goldsType: GoldsType)
                    : this(arrows.getHits(), arrows.getScore(), arrows.getGolds(goldsType))

            override fun getArrowsString(resources: Resources): String =
                    resources.getString(R.string.score_pad__surplus_total)

            override fun getRowHeader() = ResOrActual.fromRes<String>(R.string.score_pad__distance_total_row_header)
        }

        data class GrandTotal(
                override val hits: Int,
                override val score: Int,
                override val golds: Int,
        ) : ScorePadRow() {
            constructor(arrows: List<ArrowValue>, goldsType: GoldsType)
                    : this(arrows.getHits(), arrows.getScore(), arrows.getGolds(goldsType))

            override fun getArrowsString(resources: Resources): String =
                    resources.getString(R.string.score_pad__grand_total)

            override fun getRowHeader() = ResOrActual.fromRes<String>(R.string.score_pad__grand_total_row_header)
        }
    }

    enum class ColumnHeader(
            private val headingId: Int?,
            private val helpTitleId: Int,
            private val helpBodyId: Int,
    ) {
        ARROWS(
                headingId = R.string.score_pad__end_string_header,
                helpTitleId = R.string.help_score_pad__arrow_column_title,
                helpBodyId = R.string.help_score_pad__arrow_column_body,
        ),
        HITS(
                headingId = R.string.table_hits_header,
                helpTitleId = R.string.help_score_pad__hits_column_title,
                helpBodyId = R.string.help_score_pad__hits_column_body,
        ),
        SCORE(
                headingId = R.string.table_score_header,
                helpTitleId = R.string.help_score_pad__score_column_title,
                helpBodyId = R.string.help_score_pad__score_column_body,
        ),
        GOLDS(
                headingId = null,
                helpTitleId = R.string.help_score_pad__golds_column_title,
                helpBodyId = R.string.help_score_pad__golds_column_body,

                ),
        RUNNING_TOTAL(
                headingId = R.string.score_pad__running_total_header,
                helpTitleId = R.string.help_score_pad__running_column_title,
                helpBodyId = R.string.help_score_pad__running_column_body,
        ),
        ;

        fun getShortResourceId(goldsType: GoldsType) = if (this == GOLDS) goldsType.shortStringId else headingId!!

        @Composable
        fun getHelpTitle() = stringResource(helpTitleId)

        @Composable
        fun getHelpBody(goldsType: GoldsType) =
                if (this == GOLDS) stringResource(helpBodyId, stringResource(goldsType.helpString))
                else stringResource(helpBodyId)
    }

    data class ScorePadDetailsString(val headerRow: String?, val details: String)
}
