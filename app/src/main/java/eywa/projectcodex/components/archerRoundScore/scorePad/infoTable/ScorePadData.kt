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

        /**
         * Generate row headers that increment from **1** to size, adding in totals rows where appropriate.
         *
         * Cell IDs: Normal row headers, distance totals have their own numbering 'totalRow$i', grand total row: grandTotalHeader
         *
         * @param rowsPerDistance the number of rows after which a total-for-distance row will appear (no total-for-distance for
         * a singleton). Will be cut down until [rowsPerDistance].sum() <= [rowsCompleted]. e.g. (5, 5) means 5 rows then a
         * total, then 5 more then a total. If [rowsCompleted] is 8 there will be 5 rows then a total, then 3 rows then a total
         * @param rowsCompleted the number of rows actually shot (null if this was all of them)
         * @param grandTotal whether there is a row for the grand total
         */
        fun generateRowHeaders(
                rowsPerDistance: List<Int>,
                rowsCompleted: Int? = null,
                resources: Resources? = null,
                grandTotal: Boolean = false
        ): List<InfoTableCell> {
            require(rowsPerDistance.sum() > 0) {
                "Must have at least one distance in rowsPerDistance with at least one row in it"
            }
            require(rowsPerDistance.all { it > 0 }) { "Row counts must be greater than zero" }
            require(rowsCompleted == null || rowsCompleted >= 0) { "rowsCompleted must be at least zero or null" }
            require(resources != null || (!grandTotal && rowsPerDistance.size == 1)) {
                "Totals rows require resources to be not null"
            }

            /*
             * Cut down rowsPerDistance if necessary
             */
            var rowsPerDistanceCompleted: MutableList<Int>? = null
            if (rowsPerDistance.size > 1 || (rowsCompleted != null && rowsCompleted > rowsPerDistance.sum())) {
                if (rowsCompleted == null) {
                    rowsPerDistanceCompleted = rowsPerDistance.toMutableList()
                }
                else {
                    rowsPerDistanceCompleted = mutableListOf()
                    var totalRowCount = 0
                    for (rows in rowsPerDistance) {
                        if (rowsCompleted >= totalRowCount + rows) {
                            rowsPerDistanceCompleted.add(rows)
                        }
                        else if (rowsCompleted == totalRowCount) {
                            break
                        }
                        else {
                            rowsPerDistanceCompleted.add(rowsCompleted - totalRowCount)
                            break
                        }
                        totalRowCount += rows
                    }
                }
            }

            /*
             * Generate rows
             */
            // Generate integer row headers for ends
            val headers = IntRange(1, rowsCompleted ?: rowsPerDistance.sum()).map { rowIndex ->
                InfoTableCell(rowIndex, "row$rowIndex")
            }.toMutableList()

            // Add in distance total headers
            if (!rowsPerDistanceCompleted.isNullOrEmpty()) {
                var nextLocation = 0
                for ((i, distanceRowCount) in rowsPerDistanceCompleted.withIndex()) {
                    nextLocation += distanceRowCount
                    headers.add(
                            nextLocation,
                            InfoTableCell(
                                    resources!!.getString(R.string.score_pad__distance_total_row_header),
                                    "$DISTANCE_TOTAL_CELL_ID_PREFIX${i}Header",
                                    InfoTableCell.CellStyle.BOLD
                            )
                    )
                    // Account for the row that's just been added
                    nextLocation++
                }
                // Surplus total
                if (!headers.last().id.contains(DISTANCE_TOTAL_CELL_ID_PREFIX)) {
                    headers.add(
                            InfoTableCell(
                                    resources!!.getString(R.string.score_pad__distance_total_row_header),
                                    "${DISTANCE_TOTAL_CELL_ID_PREFIX}SurplusHeader",
                                    InfoTableCell.CellStyle.BOLD
                            )
                    )
                }
            }

            // Grand total header
            if (grandTotal) {
                headers.add(
                        InfoTableCell(
                                resources!!.getString(R.string.score_pad__grand_total_row_header),
                                "${GRAND_TOTAL_CELL_ID_PREFIX}Header",
                                InfoTableCell.CellStyle.BOLD
                        )
                )
            }
            return headers
        }
    }

    private val data: MutableList<MutableMap<ColumnHeader, Any>> =
            calculateScorePadData(arrows, endSize, goldsType, resources, arrowCounts, distances, distanceUnit)

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