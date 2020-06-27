package eywa.projectcodex.infoTable

import android.content.res.Resources
import eywa.projectcodex.End
import eywa.projectcodex.GoldsType
import eywa.projectcodex.R
import eywa.projectcodex.database.entities.ArcherRoundWithName
import eywa.projectcodex.database.entities.ArrowValue
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
        GOLDS_HEADER_PLACE_HOLDER,
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
        allArrows: List<ArrowValue>,
        endSize: Int,
        goldsType: GoldsType,
        resources: Resources
): MutableList<MutableList<InfoTableCell>> {
    require(allArrows.isNotEmpty()) { "allArrows cannot be empty" }
    require(endSize > 0) { "endSize must be >0" }

    /*
     * Main score pad
     */
    val tableData = mutableListOf<MutableList<InfoTableCell>>()
    var runningCount = 0
    for (sublist in allArrows.chunked(endSize)) {
        val rowData = mutableListOf<Any>()
        val end = End(
                sublist,
                endSize,
                resources.getString(R.string.end_to_string_arrow_placeholder),
                resources.getString(R.string.end_to_string_arrow_deliminator)
        )
        end.reorderScores()
        rowData.add(end.toString())

        // H/S/G
        rowData.add(end.getHits())
        rowData.add(end.getScore())
        rowData.add(end.getGolds(goldsType))

        runningCount += sublist.size
        rowData.add(allArrows.subList(0, runningCount).sumBy { arrow -> arrow.score })
        tableData.add(toCells(rowData, tableData.size))
    }

    /*
     * Totals
     */
    val rowData = mutableListOf<Any>()
    rowData.add(resources.getString(R.string.score_pad__grand_total))
    rowData.add(allArrows.count { it.score != 0 })
    rowData.add(allArrows.sumBy { it.score })
    rowData.add(allArrows.count { goldsType.isGold(it) })
    rowData.add("-")
    tableData.add(toCells(rowData, null, "grandTotal"))

    return tableData
}

/**
 * Adds a delete column on the end
 * @see viewRoundsColumnHeaderIds
 */
fun calculateViewRoundsTableData(
        archerRounds: List<ArcherRoundWithName>,
        arrows: List<ArrowValue>,
        goldsType: GoldsType,
        resources: Resources
): MutableList<MutableList<InfoTableCell>> {
    require(archerRounds.isNotEmpty()) { "archerRounds cannot be empty" }

    val tableData = mutableListOf<MutableList<InfoTableCell>>()
    for (archerRoundInfo in archerRounds.sortedByDescending { archerRound -> archerRound.archerRound.dateShot }) {
        val archerRound = archerRoundInfo.archerRound

        val rowData = mutableListOf<Any>()
        rowData.add(archerRound.archerRoundId)
        rowData.add(dateFormat.format(archerRound.dateShot))
        rowData.add(archerRoundInfo.roundSubTypeName ?: archerRoundInfo.roundName ?: "")

        // H/S/G
        val relevantArrows = arrows.filter { arrow -> arrow.archerRoundId == archerRound.archerRoundId }
        rowData.add(relevantArrows.count { it.score != 0 })
        rowData.add(relevantArrows.sumBy { it.score })
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
 * @param grandTotal whether there is a row for the grand total
 * @param rowsPerDistance the number of rows after which a total-for-distance row will appear (singleton if no total-for-distance rows desired)
 * @see toCellsHeader
 */
fun generateNumberedRowHeaders(
        rowsPerDistance: List<Int>,
        resources: Resources? = null,
        grandTotal: Boolean = false
): List<InfoTableCell> {
    require(rowsPerDistance.sum() > 0) { "Must have at least one distance (array item) with at least one row in it" }
    require(resources != null || (!grandTotal && rowsPerDistance.size == 1)) { "Totals rows require resources to be not null" }
    for (count in rowsPerDistance) {
        require(count > 0) { "Row counts cannot be <= 0" }
    }

    val headers = toCellsHeader(IntRange(1, rowsPerDistance.sum()).map { it.toString() }, false).toMutableList()

    // Distance total headers
    if (rowsPerDistance.size > 1) {
        var nextLocation = 0
        for ((i, distanceRowCount) in rowsPerDistance.withIndex()) {
            nextLocation += distanceRowCount
            headers.add(
                    nextLocation,
                    InfoTableCell(resources!!.getString(R.string.score_pad__total_row_header), "totalRow$i")
            )
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
        resources: Resources? = null,
        grandTotal: Boolean = false
): List<InfoTableCell> {
    return generateNumberedRowHeaders(listOf(rowsPerDistance), resources, grandTotal)
}

private fun createDeleteCell(resources: Resources, rowId: Int): InfoTableCell {
    return InfoTableCell(resources.getString(R.string.table_delete), "delete$rowId")
}