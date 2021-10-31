package eywa.projectcodex.components.archerRoundScore.scorePad.infoTable

import android.content.res.Resources
import eywa.projectcodex.R
import eywa.projectcodex.common.archeryObjects.End
import eywa.projectcodex.common.archeryObjects.GoldsType
import eywa.projectcodex.common.utils.resourceStringReplace
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import kotlin.math.min

/**
 * @see calculateScorePadData
 */
class ScorePadData(
        arrows: List<ArrowValue>,
        endSize: Int,
        goldsType: GoldsType,
        resources: Resources,
        arrowCounts: List<RoundArrowCount> = listOf(),
        distances: List<RoundDistance> = listOf(),
        distanceUnit: String? = null
) {
    companion object {
        private const val TOTAL_CELL_ID = "Total"
        private const val DISTANCE_TOTAL_CELL_ID_PREFIX = "distance$TOTAL_CELL_ID"
        private const val GRAND_TOTAL_CELL_ID_PREFIX = "grand$TOTAL_CELL_ID"

        /**
         * @param columnOrder the resource IDs of each of the column headers in order (golds column should be -1)
         * @param goldsType the goldsType to get the golds column header from (required if a headerStringId contains a -1)
         */
        fun getColumnHeadersForTable(
                columnOrder: List<ColumnHeader>,
                resources: Resources,
                goldsType: GoldsType? = null
        ): List<InfoTableCell> {
            require(columnOrder.isNotEmpty()) { "No headers provided" }

            return columnOrder.mapIndexed { colIndex, header ->
                val headerString =
                        if (header == ColumnHeader.GOLDS) {
                            require(goldsType != null) {
                                "Must provide a goldsType if columnOrder contains the ${ColumnHeader.GOLDS}"
                            }
                            resources.getString(goldsType.shortStringId)
                        }
                        else {
                            require(header.resourceId != null) {
                                "Cannot use $header as it doesn't have a corresponding resource id"
                            }
                            resources.getString(header.resourceId)
                        }
                InfoTableCell(headerString, "col$colIndex")
            }
        }
    }

    private val data: MutableList<MutableMap<ColumnHeader, Any>> =
            calculateScorePadData(arrows, endSize, goldsType, resources, arrowCounts, distances, distanceUnit)

    fun isNullOrEmpty(): Boolean {
        return data.isNullOrEmpty()
    }

    fun getAsTableCells(columnOrder: List<ColumnHeader>): List<List<InfoTableCell>> {
        return data.mapIndexed { rowIndex, rowData ->
            val rowType = rowData[ColumnHeader.ROW_TYPE] as ScorePadRowType
            val cellIdPrefix = when (rowType) {
                ScorePadRowType.DISTANCE_TOTAL -> DISTANCE_TOTAL_CELL_ID_PREFIX
                ScorePadRowType.GRAND_TOTAL -> GRAND_TOTAL_CELL_ID_PREFIX
                else -> "cell"
            }
            val cellRowIndex = when (rowType) {
                ScorePadRowType.DISTANCE_TOTAL -> rowData[ColumnHeader.DISTANCE]
                ScorePadRowType.GRAND_TOTAL -> ""
                else -> rowIndex
            }
            columnOrder.mapIndexed { colIndex, colHeader ->
                InfoTableCell(
                        rowData[colHeader],
                        cellIdPrefix + cellRowIndex + colIndex,
                        if (rowType.isTotal) InfoTableCell.CellStyle.BOLD else null
                )
            }
        }
    }

    /**
     * Generate row headers corresponding to the stored score pad data.
     * Headers increment from **1** to data.size, adding in total rows where appropriate.
     *
     * Cell IDs: ends: 'row$endIndex', distance totals: '[DISTANCE_TOTAL_CELL_ID_PREFIX]${distance}Header',
     * grand total row: [GRAND_TOTAL_CELL_ID_PREFIX]Header
     *
     * @param distanceTotalRowHeader the [InfoTableCell.cellContent] when the row is a distance total
     * @param grandTotalRowHeader the [InfoTableCell.cellContent] when the row is the grand total
     */
    fun generateRowHeaders(distanceTotalRowHeader: String, grandTotalRowHeader: String): List<InfoTableCell> {
        if (!data.isNullOrEmpty() && !data[0].isNullOrEmpty()) return listOf()

        var endCount = 0
        return data.map { row ->
            when (row[ColumnHeader.ROW_TYPE]) {
                ScorePadRowType.GRAND_TOTAL -> InfoTableCell(
                        grandTotalRowHeader,
                        "${GRAND_TOTAL_CELL_ID_PREFIX}Header",
                        InfoTableCell.CellStyle.BOLD
                )
                ScorePadRowType.DISTANCE_TOTAL -> InfoTableCell(
                        distanceTotalRowHeader,
                        "$DISTANCE_TOTAL_CELL_ID_PREFIX${row[ColumnHeader.DISTANCE]}Header",
                        InfoTableCell.CellStyle.BOLD
                )
                else -> {
                    endCount++
                    InfoTableCell(endCount.toString(), "row$endCount")
                }
            }
        }
    }

    /**
     * Calculates totals for each end, distance, and the round
     *
     * @param endSize how many arrows each row represents
     *
     * @return an list of rows each containing their end string, hits, score, and golds etc.
     * Has rows for distance totals and a grand total in appropriate places
     */
    private fun calculateScorePadData(
            arrows: List<ArrowValue>,
            endSize: Int,
            goldsType: GoldsType,
            resources: Resources,
            arrowCounts: List<RoundArrowCount> = listOf(),
            distances: List<RoundDistance> = listOf(),
            distanceUnit: String? = null
    ): MutableList<MutableMap<ColumnHeader, Any>> {
        if (arrows.isNullOrEmpty()) {
            return mutableListOf()
        }

        require(endSize > 0) { "endSize must be >0" }
        require(arrows.distinctBy { it.archerRoundId }.size == 1) { "Must only contain arrows from a single score" }
        require(arrowCounts.size == distances.size) { "Must have the same number of arrow counts as distances" }
        require(arrowCounts.isEmpty() || !distanceUnit.isNullOrEmpty()) { "Must provide a unit for distance totals" }

        if (arrows.isEmpty()) {
            return mutableListOf()
        }

        // Maps arrow count for distance to distance (e.g. 36 arrows at 70yds)
        val distancesInfo: MutableList<Pair<Int, Int?>>
        if (arrowCounts.isNotEmpty()) {
            distancesInfo = arrowCounts.sortedBy { it.distanceNumber }.map { it.arrowCount }
                    .zip(distances.sortedBy { it.distanceNumber }.map { it.distance }).toMutableList()
            // If shot beyond the end of a round
            val arrowsLeft = arrows.size - arrowCounts.sumOf { it.arrowCount }
            if (arrowsLeft > 0) {
                distancesInfo.add(arrowsLeft to null)
            }
        }
        else {
            distancesInfo = mutableListOf(arrows.size to null)
        }

        /*
         * Main score pad
         * Splitting it into a distance loop ensures that if an odd end size is chosen, the distances will still be split
         *     out correctly with their corresponding totals
         */
        val tableData = mutableListOf<MutableMap<ColumnHeader, Any>>()
        var runningArrowCount = 0
        var runningTotal = 0
        for (distanceInfo in distancesInfo) {
            val distanceArrowCount = distanceInfo.first
            val lastArrowIndex = min(arrows.size, runningArrowCount + distanceArrowCount)
            if (lastArrowIndex < runningArrowCount) break
            val distanceArrows = arrows.subList(runningArrowCount, lastArrowIndex)
            if (distanceArrows.isEmpty()) break
            runningArrowCount += distanceArrowCount
            val distance = distanceInfo.second

            /*
             * Ends
             */
            for (endArrows in distanceArrows.chunked(endSize)) {
                val endData = mutableMapOf<ColumnHeader, Any>()
                val end = End(
                        endArrows,
                        resources.getString(R.string.end_to_string_arrow_placeholder),
                        resources.getString(R.string.end_to_string_arrow_deliminator)
                )
                end.reorderScores()
                val endScore = end.getScore()
                runningTotal += endScore

                endData[ColumnHeader.ROW_TYPE] = ScorePadRowType.END
                endData[ColumnHeader.END_STRING] = end.toString()
                endData[ColumnHeader.HITS] = end.getHits()
                endData[ColumnHeader.SCORE] = endScore
                endData[ColumnHeader.GOLDS] = end.getGolds(goldsType)
                endData[ColumnHeader.RUNNING_TOTAL] = runningTotal
                tableData.add(endData)
            }

            /*
             * Distance total
             */
            if (distancesInfo.size > 1) {
                val distanceRowData = mutableMapOf<ColumnHeader, Any>()
                distanceRowData[ColumnHeader.ROW_TYPE] = ScorePadRowType.DISTANCE_TOTAL
                distanceRowData[ColumnHeader.END_STRING] =
                        if (distance != null) {
                            resourceStringReplace(
                                    resources.getString(R.string.score_pad__distance_total),
                                    mapOf("distance" to distance.toString(), "unit" to distanceUnit.toString())
                            )
                        }
                        else {
                            resources.getString(R.string.score_pad__surplus_total)
                        }

                distanceRowData[ColumnHeader.HITS] = distanceArrows.count { it.score != 0 }
                distanceRowData[ColumnHeader.SCORE] = distanceArrows.sumOf { it.score }
                distanceRowData[ColumnHeader.GOLDS] = distanceArrows.count { goldsType.isGold(it) }
                distanceRowData[ColumnHeader.RUNNING_TOTAL] =
                        resources.getString(R.string.score_pad__running_total_placeholder)
                distanceRowData[ColumnHeader.DISTANCE] = (distance ?: "Surplus").toString()
                tableData.add(distanceRowData)
            }
        }

        /*
         * Grand total
         */
        val grandTotalRowData = mutableMapOf<ColumnHeader, Any>()
        grandTotalRowData[ColumnHeader.ROW_TYPE] = ScorePadRowType.GRAND_TOTAL
        grandTotalRowData[ColumnHeader.END_STRING] = resources.getString(R.string.score_pad__grand_total)
        grandTotalRowData[ColumnHeader.HITS] = arrows.count { it.score != 0 }
        grandTotalRowData[ColumnHeader.SCORE] = arrows.sumOf { it.score }
        grandTotalRowData[ColumnHeader.GOLDS] = arrows.count { goldsType.isGold(it) }
        grandTotalRowData[ColumnHeader.RUNNING_TOTAL] =
                resources.getString(R.string.score_pad__running_total_placeholder)
        tableData.add(grandTotalRowData)

        return tableData
    }

    enum class ColumnHeader(val resourceId: Int? = null) {
        END_STRING(R.string.score_pad__end_string_header), HITS(R.string.table_hits_header),
        SCORE(R.string.table_score_header), GOLDS, RUNNING_TOTAL(R.string.score_pad__running_total_header), ROW_TYPE,
        DISTANCE
    }

    enum class ScorePadRowType(val isTotal: Boolean) {
        END(false), DISTANCE_TOTAL(true), GRAND_TOTAL(true)
    }
}