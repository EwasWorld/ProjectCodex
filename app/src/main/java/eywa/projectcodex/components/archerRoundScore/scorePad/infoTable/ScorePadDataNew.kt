package eywa.projectcodex.components.archerRoundScore.scorePad.infoTable

import android.annotation.SuppressLint
import android.content.res.Resources
import androidx.annotation.StringRes
import eywa.projectcodex.R
import eywa.projectcodex.common.archeryObjects.FullArcherRoundInfo
import eywa.projectcodex.common.archeryObjects.GoldsType
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.common.utils.get
import eywa.projectcodex.database.arrowValue.*
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance

sealed class ScorePadRow {
    abstract val hits: Int
    abstract val score: Int
    abstract val golds: Int
    open val runningTotal: Int? = null

    abstract fun getRowHeader(): ResOrActual<String>
    abstract fun getContent(resources: Resources): String

    fun getContent(columnHeader: ScorePadDataNew.ColumnHeader, resources: Resources) = when (columnHeader) {
        ScorePadDataNew.ColumnHeader.CONTENT -> getContent(resources)
        ScorePadDataNew.ColumnHeader.HITS -> hits.toString()
        ScorePadDataNew.ColumnHeader.SCORE -> score.toString()
        ScorePadDataNew.ColumnHeader.GOLDS -> golds.toString()
        ScorePadDataNew.ColumnHeader.RUNNING_TOTAL -> runningTotal?.toString()
                ?: resources.getString(R.string.score_pad__running_total_placeholder)
    }

    /**
     * @param endNumber 1-indexed
     */
    data class End(
            val endNumber: Int,
            val arrowValues: List<ResOrActual<String>>,
            override val hits: Int,
            override val score: Int,
            override val golds: Int,
            override val runningTotal: Int,
    ) : ScorePadRow() {
        constructor(endNumber: Int, arrows: List<ArrowValue>, goldsType: GoldsType, runningTotal: Int) : this(
                endNumber,
                arrows.map { it.asString() },
                arrows.getHits(),
                arrows.getScore(),
                arrows.getGolds(goldsType),
                runningTotal
        )

        override fun getContent(resources: Resources): String = arrowValues
                .joinToString(resources.getString(R.string.end_to_string_arrow_deliminator)) { it.get(resources) }

        override fun getRowHeader() = ResOrActual.fromActual(endNumber.toString())
    }

    data class DistanceTotal(
            val distance: Int,
            @StringRes val distanceUnit: Int,
            override val hits: Int,
            override val score: Int,
            override val golds: Int,
    ) : ScorePadRow() {
        constructor(arrows: List<ArrowValue>, goldsType: GoldsType, distance: Int, @StringRes distanceUnit: Int)
                : this(distance, distanceUnit, arrows.getHits(), arrows.getScore(), arrows.getGolds(goldsType))

        @SuppressLint("StringFormatInvalid") // You are wrong, the fat controller laughed
        override fun getContent(resources: Resources): String = resources.getString(
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

        override fun getContent(resources: Resources): String = resources.getString(R.string.score_pad__surplus_total)

        override fun getRowHeader() = ResOrActual.fromRes<String>(R.string.score_pad__distance_total_row_header)
    }

    data class GrandTotal(
            override val hits: Int,
            override val score: Int,
            override val golds: Int,
    ) : ScorePadRow() {
        constructor(arrows: List<ArrowValue>, goldsType: GoldsType)
                : this(arrows.getHits(), arrows.getScore(), arrows.getGolds(goldsType))

        override fun getContent(resources: Resources): String = resources.getString(R.string.score_pad__grand_total)

        override fun getRowHeader() = ResOrActual.fromRes<String>(R.string.score_pad__grand_total_row_header)
    }
}


class ScorePadDataNew(
        info: FullArcherRoundInfo,
        endSize: Int,
        val goldsType: GoldsType,
) {
    val data: MutableList<ScorePadRow> = calculateScorePadData(info, endSize, goldsType)

    /**
     * Calculates totals for each end, distance, and the round
     *
     * @param endSize how many arrows each row represents
     *
     * @return an list of rows each containing their end string, hits, score, and golds etc.
     * Has rows for distance totals and a grand total in appropriate places
     */
    private fun calculateScorePadData(
            info: FullArcherRoundInfo,
            endSize: Int,
            goldsType: GoldsType,
    ): MutableList<ScorePadRow> {
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
                    calculateTotalsForDistance(
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
                        calculateTotalsForDistance(
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
                                        .maxOfOrNull { it.endNumber }
                        )
                )
                if (remainingArrows.isEmpty()) break
            }
            // If too many arrows
            if (remainingArrows.isNotEmpty()) {
                tableData.addAll(
                        calculateTotalsForDistance(
                                arrows = remainingArrows,
                                isSurplus = true,
                                addDistanceTotal = true,
                                runningTotal = tableData.mapNotNull { it.runningTotal }.maxOrNull(),
                                endSize = endSize,
                                goldsType = goldsType,
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
    private fun calculateTotalsForDistance(
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

    /**
     * Generate row headers corresponding to the stored score pad data.
     * Headers increment from **1** to [data].size, adding in total rows where appropriate.
     */
    fun generateRowHeaders(resources: Resources): List<String> {
        if (data.isEmpty()) return listOf()

        var endNumber = 1
        return data.map { row ->
            when (row) {
                is ScorePadRow.GrandTotal -> resources.getString(R.string.score_pad__grand_total_row_header)
                is ScorePadRow.DistanceTotal,
                is ScorePadRow.SurplusTotal -> resources.getString(R.string.score_pad__distance_total_row_header)
                else -> endNumber++.toString()
            }
        }
    }

    enum class ColumnHeader(private val resourceId: Int? = null) {
        CONTENT(R.string.score_pad__end_string_header),
        HITS(R.string.table_hits_header),
        SCORE(R.string.table_score_header),
        GOLDS,
        RUNNING_TOTAL(R.string.score_pad__running_total_header),
        ;

        fun getShortResourceId(goldsType: GoldsType) = if (this == GOLDS) goldsType.shortStringId else resourceId!!
        fun getLongResourceId(goldsType: GoldsType) = if (this == GOLDS) goldsType.longStringId else resourceId!!
    }

    data class ScorePadDetailsString(val headerRow: String?, val details: String)


    companion object {
        /**
         * @param columnOrder the resource IDs of each of the column headers in order
         * @param goldsType the goldsType to get the golds column header from
         */
        fun getColumnHeadersForTable(
                columnOrder: List<ColumnHeader>,
                resources: Resources,
                goldsType: GoldsType,
        ) = columnOrder.map { resources.getString(it.getShortResourceId(goldsType)) }
    }
}