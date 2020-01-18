package eywa.projectcodex.infoTable

import android.content.res.Resources
import eywa.projectcodex.End
import eywa.projectcodex.GoldsType
import eywa.projectcodex.R
import eywa.projectcodex.database.entities.ArcherRound
import eywa.projectcodex.database.entities.ArrowValue
import java.text.SimpleDateFormat
import java.util.*

val dateFormat = SimpleDateFormat("dd/MM/yy HH:mm", Locale.UK)

val viewRoundsColumnHeaderIds = listOf(
        R.string.view_round__id_header,
        R.string.view_round__date_header,
        R.string.table_hits_header,
        R.string.table_score_header,
        -1,
        R.string.view_round__counts_to_hc_header
)
val scorePadColumnHeaderIds = listOf(
        R.string.score_pad__end_string_header,
        R.string.table_hits_header,
        R.string.table_score_header,
        -1,
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
    require(!headerStringIds.contains(-1) || goldsType != null) {
        "Must provide a goldsType if stringIds contains the golds placeholder, -1"
    }
    return toCellsHeader(
            headerStringIds.map {
                if (it == -1) {
                    resources.getString(goldsType!!.colHeaderStringId)
                }
                else {
                    resources.getString(it)
                }
            }, true
    )
}

/**
 * @see scorePadColumnHeaderIds
 */
fun calculateScorePadTableData(
        allArrows: List<ArrowValue>,
        endSize: Int,
        goldsType: GoldsType,
        arrowPlaceholder: String,
        arrowDeliminator: String
): MutableList<MutableList<InfoTableCell>> {
    require(allArrows.isNotEmpty()) { "allArrows cannot be empty" }
    require(endSize > 0) { "endSize must be >0" }

    val tableData = mutableListOf<MutableList<InfoTableCell>>()
    var runningCount = 0
    for (sublist in allArrows.chunked(endSize)) {
        val rowData = mutableListOf<Any>()
        val end = End(sublist, endSize, arrowPlaceholder, arrowDeliminator)
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
    return tableData
}

/**
 * @see viewRoundsColumnHeaderIds
 */
fun calculateViewRoundsTableData(
        archerRounds: List<ArcherRound>,
        arrows: List<ArrowValue>,
        goldsType: GoldsType,
        yes: String,
        no: String
): MutableList<MutableList<InfoTableCell>> {
    require(archerRounds.isNotEmpty()) { "archerRounds cannot be empty" }

    val tableData = mutableListOf<MutableList<InfoTableCell>>()
    for (archerRound in archerRounds.sortedByDescending { archerRound -> archerRound.dateShot }) {
        val rowData = mutableListOf<Any>()
        rowData.add(archerRound.archerRoundId)
        rowData.add(dateFormat.format(archerRound.dateShot))

        // H/S/G
        val relevantArrows = arrows.filter { arrow -> arrow.archerRoundId == archerRound.archerRoundId }
        rowData.add(relevantArrows.count { it.score != 0 })
        rowData.add(relevantArrows.sumBy { it.score })
        rowData.add(relevantArrows.count { goldsType.isGold(it) })

        rowData.add(if (archerRound.countsTowardsHandicap) yes else no)
        tableData.add(toCells(rowData, tableData.size))
    }
    return tableData
}

fun blankViewRoundsTableData(): List<List<InfoTableCell>> {
    var cellId = 0
    return listOf(List(viewRoundsColumnHeaderIds.size) { InfoTableCell("-", "cell0" + cellId++) })
}

/**
 * Convert from List<String> to List<InfoTableCell> for cells
 */
private fun toCells(rowData: List<Any>, rowId: Int): MutableList<InfoTableCell> {
    require(rowData.isNotEmpty()) { "Data cannot be empty" }
    var col = 0
    return rowData.map { InfoTableCell(it, "cell" + (rowId).toString() + col++.toString()) }.toMutableList()
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
 * Generate row headers that increment from **1** to size
 */
fun generateNumberedRowHeaders(size: Int): List<InfoTableCell> {
    require(size > 0) { "Size cannot be <0" }
    return toCellsHeader(IntRange(1, size).map { it.toString() }, false)
}