package eywa.projectcodex.components.infoTable

import android.content.res.Resources
import eywa.projectcodex.R
import eywa.projectcodex.components.archeryObjects.End
import eywa.projectcodex.components.archeryObjects.GoldsType
import eywa.projectcodex.components.archeryObjects.getGoldsType
import eywa.projectcodex.components.commonUtils.DateTimeFormat
import eywa.projectcodex.components.commonUtils.resourceStringReplace
import eywa.projectcodex.database.archerRound.ArcherRoundWithRoundInfoAndName
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import kotlin.math.min

private const val GOLDS_HEADER_PLACE_HOLDER = -1

const val TOTAL_CELL_ID = "Total"
private const val DISTANCE_TOTAL_CELL_ID_PREFIX = "distance$TOTAL_CELL_ID"
private const val GRAND_TOTAL_CELL_ID_PREFIX = "grand$TOTAL_CELL_ID"

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
        goldsType: GoldsType? = null
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
    return toCellsHeader(stringsList, true)
}

/**
 * Displays the arrow data along with total rows and a grand total row
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
    val tableData = mutableListOf<MutableList<InfoTableCell>>()
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
            val endRowData = mutableListOf<Any>()
            val end = End(
                    endArrows,
                    resources.getString(R.string.end_to_string_arrow_placeholder),
                    resources.getString(R.string.end_to_string_arrow_deliminator)
            )
            end.reorderScores()
            val endScore = end.getScore()
            runningTotal += endScore

            endRowData.add(end.toString())
            endRowData.add(end.getHits())
            endRowData.add(endScore)
            endRowData.add(end.getGolds(goldsType))
            endRowData.add(runningTotal)

            // Add row
            val rowCells = toCells(endRowData, tableData.size)
            check(rowCells.size == scorePadColumnHeaderIds.size) { "Row length doesn't match headers length" }
            tableData.add(rowCells)
        }

        /*
         * Distance total
         */
        if (distancesInfo.size > 1) {
            val distanceRowData = mutableListOf<Any>()
            distanceRowData.add(
                    if (distance != null) {
                        resourceStringReplace(
                                resources.getString(R.string.score_pad__distance_total),
                                mapOf("distance" to distance.toString(), "unit" to distanceUnit.toString())
                        )
                    }
                    else {
                        resources.getString(R.string.score_pad__surplus_total)
                    }
            )
            distanceRowData.add(distanceArrows.count { it.score != 0 })
            distanceRowData.add(distanceArrows.sumOf { it.score })
            distanceRowData.add(distanceArrows.count { goldsType.isGold(it) })
            distanceRowData.add(resources.getString(R.string.score_pad__running_total_placeholder))
            check(distanceRowData.size == scorePadColumnHeaderIds.size) { "Row length doesn't match headers length" }
            // Having 'total' in the id makes it bold - see InfoTableViewAdapter.setBoldIfTotal
            tableData.add(toCells(distanceRowData, distance, DISTANCE_TOTAL_CELL_ID_PREFIX))
        }
    }

    /*
     * Grand total
     */
    val grandTotalRowData = mutableListOf<Any>()
    grandTotalRowData.add(resources.getString(R.string.score_pad__grand_total))
    grandTotalRowData.add(arrows.count { it.score != 0 })
    grandTotalRowData.add(arrows.sumOf { it.score })
    grandTotalRowData.add(arrows.count { goldsType.isGold(it) })
    grandTotalRowData.add(resources.getString(R.string.score_pad__running_total_placeholder))
    check(grandTotalRowData.size == scorePadColumnHeaderIds.size) { "Row length doesn't match headers length" }
    // Having 'total' in the id makes it bold - see InfoTableViewAdapter.setBoldIfTotal
    tableData.add(toCells(grandTotalRowData, null, GRAND_TOTAL_CELL_ID_PREFIX))

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
        rowData.add(DateTimeFormat.SHORT_DATE_TIME_FORMAT.format(archerRound.dateShot))
        rowData.add(archerRoundInfo.roundSubTypeName ?: archerRoundInfo.round?.displayName ?: "")

        // H/S/G
        val relevantArrows = arrows.filter { arrow -> arrow.archerRoundId == archerRound.archerRoundId }
        rowData.add(relevantArrows.count { it.score != 0 })
        rowData.add(relevantArrows.sumOf { it.score })
        val goldsType = archerRoundInfo.round?.let {
            getGoldsType(
                    it.isOutdoor, it.isMetric
            )
        } ?: defaultGoldsType
        rowData.add(relevantArrows.count { goldsType.isGold(it) })

        val countsToHandicap =
                if (archerRound.countsTowardsHandicap) R.string.short_boolean_true else R.string.short_boolean_false
        rowData.add(resources.getString(countsToHandicap))

        tableData.add(toCells(rowData, tableData.size))
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
 * @param rowsPerDistance the number of rows after which a total-for-distance row will appear (no total-for-distance for
 * a singleton). Will be cut down until [rowsPerDistance].sum() <= [rowsCompleted]. e.g. (5, 5) means 5 rows then a
 * total, then 5 more then a total. If [rowsCompleted] is 8 there will be 5 rows then a total, then 3 rows then a total
 * @param rowsCompleted the number of rows actually shot (null if this was all of them)
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
    require(!rowsPerDistance.contains(0)) { "Cannot have zero rows at a distance" }
    require(rowsCompleted == null || rowsCompleted >= 0) { "rowsCompleted must be >= 0" }
    require(resources != null || (!grandTotal && rowsPerDistance.size == 1))
    { "Totals rows require resources to be not null" }
    for (count in rowsPerDistance) {
        require(count > 0) { "Row counts cannot be <= 0" }
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
    val headers = toCellsHeader(
            IntRange(1, rowsCompleted ?: rowsPerDistance.sum()).map { it.toString() }, false
    ).toMutableList()

    // Add in distance total headers
    if (!rowsPerDistanceCompleted.isNullOrEmpty()) {
        var nextLocation = 0
        for ((i, distanceRowCount) in rowsPerDistanceCompleted.withIndex()) {
            nextLocation += distanceRowCount
            headers.add(
                    nextLocation,
                    InfoTableCell(
                            resources!!.getString(R.string.score_pad__distance_total_row_header),
                            "$DISTANCE_TOTAL_CELL_ID_PREFIX${i}Header"
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
                            "${DISTANCE_TOTAL_CELL_ID_PREFIX}SurplusHeader"
                    )
            )
        }
    }

    // Grand total header
    if (grandTotal) {
        headers.add(
                InfoTableCell(
                        resources!!.getString(R.string.score_pad__grand_total_row_header),
                        "${GRAND_TOTAL_CELL_ID_PREFIX}Header"
                )
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
