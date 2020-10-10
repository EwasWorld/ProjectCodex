package eywa.projectcodex.infoTable

import android.content.res.Resources
import eywa.projectcodex.logic.End
import eywa.projectcodex.logic.GoldsType
import eywa.projectcodex.R
import eywa.projectcodex.database.entities.ArcherRoundWithRoundInfoAndName
import eywa.projectcodex.database.entities.ArrowValue
import eywa.projectcodex.database.entities.RoundArrowCount
import eywa.projectcodex.database.entities.RoundDistance
import eywa.projectcodex.logic.getGoldsType
import java.text.SimpleDateFormat
import java.util.*

private val dateFormat = SimpleDateFormat("dd/MM/yy HH:mm", Locale.UK)
private const val GOLDS_HEADER_PLACE_HOLDER = -1

val viewRoundsColumnHeaderIds = listOf(
        R.string.view_round__id_header,
        R.string.view_round__date_header,
        R.string.view_round__round_name_header,
        R.string.table_hits_header,
        R.string.table_score_header,
        R.string.table_golds_header,
        R.string.view_round__counts_to_hc_header
)
val scorePadColumnHeaderIds = listOf(
        R.string.score_pad__end_string_header,
        R.string.table_hits_header,
        R.string.table_score_header,
        GOLDS_HEADER_PLACE_HOLDER,
        R.string.score_pad__running_total_header
)

/**
 * @param headerStringIds the resource IDs of each of the column headers in order (golds column should be -1)
 * @param goldsType the goldsType to get the golds column header from (required if a headerStringId contains a -1)
 */
fun getColumnHeadersForTable(
        headerStringIds: List<Int>,
        resources: Resources,
        goldsType: GoldsType? = null,
        deleteColumn: Boolean = false
): List<InfoTableCell> {
    require(headerStringIds.isNotEmpty()) { "No headers provided" }
    require(!headerStringIds.contains(GOLDS_HEADER_PLACE_HOLDER) || goldsType != null) {
        "Must provide a goldsType if stringIds contains the golds placeholder, $GOLDS_HEADER_PLACE_HOLDER"
    }
    val stringsList = headerStringIds.map {
        if (it == GOLDS_HEADER_PLACE_HOLDER) {
            resources.getString(goldsType!!.colHeaderStringId)
        }
        else {
            resources.getString(it)
        }
    }.toMutableList()
    if (deleteColumn) {
        stringsList.add(resources.getString(R.string.table_delete))
    }
    return toCellsHeader(stringsList, true)
}

/**
 * Displays the arrow data along with a grand total row
 * @see scorePadColumnHeaderIds
 */
fun calculateScorePadTableData(
        arrows: List<ArrowValue>,
        endSize: Int,
        goldsType: GoldsType,
        resources: Resources,
        arrowCounts: List<RoundArrowCount> = listOf(),
        distances: List<RoundDistance> = listOf(),
        distanceUnit: String? = null
): MutableList<MutableList<InfoTableCell>> {
    require(arrows.isNotEmpty()) { "allArrows cannot be empty" }
    require(endSize > 0) { "endSize must be >0" }
    require(arrowCounts.size == distances.size) { "Must have the same number of arrow counts as distances" }
    require(arrowCounts.isEmpty() || !distanceUnit.isNullOrEmpty()) { "Must provide a unit for distance totals" }

    val arrowCountsSorted = arrowCounts.sortedBy { it.distanceNumber }
    val cumulativeArrowsAtDistance = arrowCountsSorted.mapIndexed { i, _ ->
        arrowCountsSorted.filterIndexed { j, _ -> j <= i }.sumBy { it.arrowCount }
    }.toMutableList()
    val distancesSorted = distances.sortedBy { it.distanceNumber }.map { it.distance }.toMutableList()

    /*
     * Main score pad
     */
    val tableData = mutableListOf<MutableList<InfoTableCell>>()
    var runningArrowCount = 0
    var distanceStartArrowIndex = 0
    for (endArrows in arrows.chunked(endSize)) {
        val endRowData = mutableListOf<Any>()
        val end = End(
                endArrows,
                endSize,
                resources.getString(R.string.end_to_string_arrow_placeholder),
                resources.getString(R.string.end_to_string_arrow_deliminator)
        )
        end.reorderScores()
        endRowData.add(end.toString())

        // H/S/G
        endRowData.add(end.getHits())
        endRowData.add(end.getScore())
        endRowData.add(end.getGolds(goldsType))

        // Running total
        runningArrowCount += endArrows.size
        endRowData.add(arrows.subList(0, runningArrowCount).sumBy { arrow -> arrow.score })

        // Add row
        tableData.add(toCells(endRowData, tableData.size))

        /*
         * Distance total
         */
        if (arrowCounts.isNotEmpty() && runningArrowCount >= cumulativeArrowsAtDistance[0]) {
            val distanceRowData = mutableListOf<Any>()
            distanceRowData.add(
                    String.format(
                            resources.getString(R.string.score_pad__distance_total), distancesSorted[0], distanceUnit
                    )
            )
            val distanceSubset = arrows.subList(distanceStartArrowIndex, runningArrowCount)
            distanceRowData.add(distanceSubset.count { it.score != 0 })
            distanceRowData.add(distanceSubset.sumBy { it.score })
            distanceRowData.add(distanceSubset.count { goldsType.isGold(it) })
            distanceRowData.add(resources.getString(R.string.score_pad__running_total_placeholder))
            // Having 'total' in the id makes it bold - see InfoTableViewAdapter.setBoldIfTotal
            tableData.add(toCells(distanceRowData, distancesSorted[0], "distanceTotal"))

            cumulativeArrowsAtDistance.removeAt(0)
            distancesSorted.removeAt(0)
            distanceStartArrowIndex = runningArrowCount
        }
    }

    /*
     * Grand total
     */
    val grandTotalRowData = mutableListOf<Any>()
    grandTotalRowData.add(resources.getString(R.string.score_pad__grand_total))
    grandTotalRowData.add(arrows.count { it.score != 0 })
    grandTotalRowData.add(arrows.sumBy { it.score })
    grandTotalRowData.add(arrows.count { goldsType.isGold(it) })
    grandTotalRowData.add(resources.getString(R.string.score_pad__running_total_placeholder))
    // Having 'total' in the id makes it bold - see InfoTableViewAdapter.setBoldIfTotal
    tableData.add(toCells(grandTotalRowData, null, "grandTotal"))

    return tableData
}

/**
 * Adds a delete column on the end
 * @param defaultGoldsType The default GoldsType to use if an archer round doesn't have a round
 * @see viewRoundsColumnHeaderIds
 */
fun calculateViewRoundsTableData(
        archerRounds: List<ArcherRoundWithRoundInfoAndName>,
        arrows: List<ArrowValue>,
        defaultGoldsType: GoldsType,
        resources: Resources
): MutableList<MutableList<InfoTableCell>> {
    require(archerRounds.isNotEmpty()) { "archerRounds cannot be empty" }

    val tableData = mutableListOf<MutableList<InfoTableCell>>()
    for (archerRoundInfo in archerRounds.sortedByDescending { archerRound -> archerRound.archerRound.dateShot }) {
        val archerRound = archerRoundInfo.archerRound

        val rowData = mutableListOf<Any>()
        rowData.add(archerRound.archerRoundId)
        rowData.add(dateFormat.format(archerRound.dateShot))
        rowData.add(archerRoundInfo.roundSubTypeName ?: archerRoundInfo.round?.displayName ?: "")

        // H/S/G
        val relevantArrows = arrows.filter { arrow -> arrow.archerRoundId == archerRound.archerRoundId }
        rowData.add(relevantArrows.count { it.score != 0 })
        rowData.add(relevantArrows.sumBy { it.score })
        val goldsType = archerRoundInfo.round?.let {
            getGoldsType(
                    it.isOutdoor, it.isMetric
            )
        } ?: defaultGoldsType
        rowData.add(relevantArrows.count { goldsType.isGold(it) })

        val countsToHandicap =
                if (archerRound.countsTowardsHandicap) R.string.short_boolean_true else R.string.short_boolean_false
        rowData.add(resources.getString(countsToHandicap))

        val rowCells = toCells(rowData, tableData.size)
        rowCells.add(createDeleteCell(resources, tableData.size))

        tableData.add(rowCells)
    }
    return tableData
}

/**
 * Convert from List<String> to List<InfoTableCell> for cells
 */
private fun toCells(rowData: List<Any>, rowId: Int?, prefix: String = "cell"): MutableList<InfoTableCell> {
    require(rowData.isNotEmpty()) { "Data cannot be empty" }
    val row = rowId?.toString() ?: ""
    var col = 0
    return rowData.map { InfoTableCell(it, prefix + row + col++.toString()) }.toMutableList()
}

/**
 * Convert from List<String> to List<InfoTableCell> for row or column headers
 */
private fun toCellsHeader(data: List<String>, isColumn: Boolean): List<InfoTableCell> {
    require(data.isNotEmpty()) { "Data cannot be empty" }
    val prefix = if (isColumn) "col" else "row"
    var index = 0
    return data.map { InfoTableCell(it, prefix + index++) }
}

/**
 * Generate row headers that increment from **1** to size, adding in totals rows where appropriate.
 *
 * Cell IDs: Normal row headers, distance totals have their own numbering 'totalRow$i', grand total row: grandTotalHeader
 *
 * @param rowsCompleted the number of rows actually shot (null if this was all of them), [rowsPerDistance] will be cut
 * down to total this number at most
 * @param rowsPerDistance the number of rows after which a total-for-distance row will appear (no total-for-distance for
 * a singleton)
 * @param grandTotal whether there is a row for the grand total
 * @see toCellsHeader
 */
fun generateNumberedRowHeaders(
        rowsPerDistance: List<Int>,
        rowsCompleted: Int? = null,
        resources: Resources? = null,
        grandTotal: Boolean = false
): List<InfoTableCell> {
    require(rowsPerDistance.sum() > 0) { "Must have at least one distance (array item) with at least one row in it" }
    require(resources != null || (!grandTotal && rowsPerDistance.size == 1))
    { "Totals rows require resources to be not null" }
    for (count in rowsPerDistance) {
        require(count > 0) { "Row counts cannot be <= 0" }
    }

    /*
     * Cut down rowsPerDistance if necessary
     */
    var rowsPerDistanceShot: List<Int> = rowsPerDistance
    rowsCompleted?.let { rowsCompletedNotNull ->
        var total = 0
        for (i in rowsPerDistance.indices) {
            if (total + rowsPerDistance[i] > rowsCompletedNotNull) {
                rowsPerDistanceShot = rowsPerDistance.subList(0, i).plus(rowsCompletedNotNull - total)
                break
            }
            total += rowsPerDistance[i]
        }
    }

    /*
     * Generate rows
     */
    // Generate integer row headers for ends
    val headers = toCellsHeader(IntRange(1, rowsPerDistanceShot.sum()).map { it.toString() }, false).toMutableList()

    // Add in distance total headers
    if (rowsPerDistanceShot.size > 1) {
        var nextLocation = 0
        for ((i, distanceRowCount) in rowsPerDistanceShot.withIndex()) {
            nextLocation += distanceRowCount
            headers.add(
                    nextLocation,
                    InfoTableCell(resources!!.getString(R.string.score_pad__distance_total_row_header), "totalRow$i")
            )
            // Account for the row that's just been added
            nextLocation++
        }
    }

    // Grand total header
    if (grandTotal) {
        headers.add(
                InfoTableCell(resources!!.getString(R.string.score_pad__grand_total_row_header), "grandTotalHeader")
        )
    }
    return headers
}

/**
 * @see generateNumberedRowHeaders
 */
fun generateNumberedRowHeaders(
        rowsPerDistance: Int,
        rowsCompleted: Int? = null,
        resources: Resources? = null,
        grandTotal: Boolean = false
): List<InfoTableCell> {
    return generateNumberedRowHeaders(listOf(rowsPerDistance), rowsCompleted, resources, grandTotal)
}

private fun createDeleteCell(resources: Resources, rowId: Int): InfoTableCell {
    return InfoTableCell(resources.getString(R.string.table_delete), "delete$rowId")
}