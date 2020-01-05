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

/**
 * Columns: End string, H, S, G, running total
 */
fun getScorePadColumnHeaders(resources: Resources, goldsType: GoldsType): List<InfoTableCell> {
    return toCellsHeader(
            listOf(
                    resources.getString(R.string.scorepad_end_string_header),
                    resources.getString(R.string.table_hits_header),
                    resources.getString(R.string.table_score_header),
                    resources.getString(goldsType.colHeaderStringId),
                    resources.getString(R.string.scorepad_running_total_header)
            ), true
    )
}

/**
 * @see getScorePadColumnHeaders
 */
fun calculateScorePadTableData(
        allArrows: List<ArrowValue>,
        endSize: Int,
        goldsType: GoldsType,
        arrowPlaceholder: String,
        arrowDeliminator: String
): List<List<InfoTableCell>> {
    require(allArrows.isNotEmpty()) { "allArrows cannot be empty" }
    require(endSize > 0) { "endSize must be >0" }

    val tableData = mutableListOf<List<InfoTableCell>>()
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
 * Columns: Date, H, S, G, Counts to HC
 */
fun getViewRoundsColumnHeaders(resources: Resources, goldsType: GoldsType): List<InfoTableCell> {
    return toCellsHeader(
            listOf(
                    resources.getString(R.string.view_round_date_header),
                    resources.getString(R.string.table_hits_header),
                    resources.getString(R.string.table_score_header),
                    resources.getString(goldsType.colHeaderStringId),
                    resources.getString(R.string.view_round_counts_to_hc_header)
            ), true
    )
}

/**
 * @see getViewRoundsColumnHeaders
 */
fun calculateViewRoundsTableData(
        archerRounds: List<ArcherRound>,
        arrows: List<ArrowValue>,
        goldsType: GoldsType,
        yes: String,
        no: String
): List<List<InfoTableCell>> {
    require(archerRounds.isNotEmpty()) { "archerRounds cannot be empty" }

    val tableData = mutableListOf<List<InfoTableCell>>()
    for (archerRound in archerRounds.sortedBy { archerRound -> archerRound.dateShot }) {
        val rowData = mutableListOf<Any>()
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

/**
 * Convert from List<String> to List<InfoTableCell> for cells
 */
private fun toCells(rowData: List<Any>, rowId: Int): List<InfoTableCell> {
    require(rowData.isNotEmpty()) { "Data cannot be empty" }
    var col = 0
    return rowData.map { InfoTableCell(it, "cell" + (rowId).toString() + col++.toString()) }
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