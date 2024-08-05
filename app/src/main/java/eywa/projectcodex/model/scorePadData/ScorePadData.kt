package eywa.projectcodex.model.scorePadData

import android.content.res.Resources
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.common.utils.ResOrActual.StringResource
import eywa.projectcodex.database.arrows.DatabaseArrowScore
import eywa.projectcodex.database.arrows.getScore
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import eywa.projectcodex.database.rounds.getDistanceUnitRes
import eywa.projectcodex.model.FullShootInfo
import eywa.projectcodex.model.GoldsType
import kotlin.math.ceil

class ScorePadData(
        info: FullShootInfo,
        endSize: Int,
        val goldsTypes: List<GoldsType>,
) {
    constructor(
            info: FullShootInfo,
            endSize: Int,
            goldsTypes: GoldsType,
    ) : this(info, endSize, listOf(goldsTypes))

    val data = generateData(info, endSize, goldsTypes)

    /**
     * Calculates totals for each end, distance, and the round
     *
     * @param endSize how many arrows each row represents
     *
     * @return an list of rows each containing their end string, hits, score, and golds etc.
     * Has rows for distance totals and a grand total in appropriate places
     */
    private fun generateData(
            info: FullShootInfo,
            endSize: Int,
            goldsType: List<GoldsType>,
    ): List<ScorePadRow> {
        require(endSize > 0) { "endSize must be >0" }
        if (info.arrows.isNullOrEmpty()) return mutableListOf()

        val distanceUnit = info.round.getDistanceUnitRes()?.let { StringResource(it) }

        val remainingArrows = info.arrows.toMutableList()
        val tableData = mutableListOf<ScorePadRow>()

        // No round info: add all arrows
        if (info.round == null) {
            tableData.addAll(
                    generateRowsForDistance(
                            arrows = remainingArrows,
                            endSize = endSize,
                            goldsType = goldsType,
                    ),
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
                                addDistanceTotal = info.roundDistances.size > 1 || info.hasSurplusArrows,
                                runningTotal = tableData.mapNotNull { it.runningTotal }.maxOrNull(),
                                endSize = endSize,
                                goldsType = goldsType,
                                endNumber = tableData
                                        .filterIsInstance<ScorePadRow.End>()
                                        .maxOfOrNull { it.endNumber },
                        ),
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
                                        .maxOfOrNull { it.endNumber },
                        ),
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
            arrows: MutableList<DatabaseArrowScore>,
            distance: RoundDistance? = null,
            arrowCount: RoundArrowCount? = null,
            distanceUnit: ResOrActual<String>? = null,
            isSurplus: Boolean = false,
            addDistanceTotal: Boolean = false,
            endNumber: Int? = null,
            runningTotal: Int? = null,
            endSize: Int,
            goldsType: List<GoldsType>,
    ): List<ScorePadRow> {
        require(arrows.isNotEmpty()) { "Arrows cannot be empty" }
        require(distance?.roundId == arrowCount?.roundId) { "Round ids differ" }
        require(distance?.distanceNumber == arrowCount?.distanceNumber) { "Distance numbers differ" }
        if (!isSurplus && addDistanceTotal) {
            require(distance != null && distanceUnit != null) { "Distance and unit are required to record distance total" }
        }

        val distanceArrows = arrowCount?.arrowCount?.let { arrows.take(it) } ?: arrows.toList()
        arrows.removeAll(distanceArrows)
        val allEndArrows = distanceArrows.chunked(endSize)

        val tableData = mutableListOf<ScorePadRow>()
        var currentRunningTotal = runningTotal ?: 0
        var currentEndNumber = endNumber ?: 0

        val hasHalfWayTotal = distance != null && distanceArrows.size >= HALF_DISTANCE_TOTAL_ARROW_THRESHOLD
        // How many ends does the half-way total come after
        val halfWayEndCount = ceil(allEndArrows.size.toDouble() / 2).toInt()
        // Which end number the half-way total should be shown after
        val halfTimeEndNumber = currentEndNumber + halfWayEndCount

        for (endArrows in allEndArrows) {
            currentRunningTotal += endArrows.getScore()
            tableData.add(
                    ScorePadRow.End(++currentEndNumber, endArrows, goldsType, currentRunningTotal),
            )

            if (hasHalfWayTotal && currentEndNumber == halfTimeEndNumber) {
                tableData.add(
                        ScorePadRow.HalfDistanceTotal(
                                arrows = allEndArrows.take(halfWayEndCount).flatten(),
                                goldsTypes = goldsType,
                                distance = distance!!.distance,
                                distanceUnit = distanceUnit!!,
                                isFirstHalf = true,
                        ),
                )
            }
        }

        if (hasHalfWayTotal) {
            tableData.add(
                    ScorePadRow.HalfDistanceTotal(
                            arrows = allEndArrows.drop(halfWayEndCount).flatten(),
                            goldsTypes = goldsType,
                            distance = distance!!.distance,
                            distanceUnit = distanceUnit!!,
                            isFirstHalf = false,
                    ),
            )
        }

        if (addDistanceTotal) {
            tableData.add(
                    if (distance != null) {
                        ScorePadRow.DistanceTotal(distanceArrows, goldsType, distance.distance, distanceUnit!!)
                    }
                    else {
                        ScorePadRow.SurplusTotal(distanceArrows, goldsType)
                    },
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
            columnOrder: List<ScorePadColumnType>,
            resources: Resources,
            includeDistanceRows: Boolean
    ): ScorePadDetailsString {
        val internalColumns = columnOrder.filter { it != ScorePadColumnType.HEADER }.flatMap { toColumnMetadata(it) }
        val headers = internalColumns.associateWith { it.primaryTitle!!.get(resources) }

        var outputData: List<ScorePadRow> = data
        if (!includeDistanceRows) {
            outputData = outputData.filterNot { it is ScorePadRow.DistanceTotal || it is ScorePadRow.SurplusTotal }
        }
        // Will later pad the start of each item to the max column width
        val maxWidths = internalColumns.associateWith { colHeader ->
            outputData.maxOf { colHeader.mapping(it).get(resources).length }
                    .coerceAtLeast(headers[colHeader]!!.length)
        }

        val header = internalColumns.joinToString(" ") { column ->
            "%${maxWidths[column]}s".format(headers[column]!!)
        }
        val details = outputData.joinToString("\n") { row ->
            internalColumns.joinToString(" ") { column ->
                "%${maxWidths[column]}s".format(column.mapping(row).get(resources))
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
            columnOrder: List<ScorePadColumnType>,
            resources: Resources,
            includeDistanceTotals: Boolean
    ): ScorePadDetailsString {
        val internalColumns = columnOrder.filter { it != ScorePadColumnType.HEADER }.flatMap { toColumnMetadata(it) }
        val header = internalColumns.joinToString(",") { it.primaryTitle!!.get(resources) }
        var outputData: List<ScorePadRow> = data
        if (!includeDistanceTotals) {
            outputData = outputData.filterNot { it is ScorePadRow.DistanceTotal || it is ScorePadRow.SurplusTotal }
        }
        val details = outputData.joinToString("\n") { row ->
            internalColumns.joinToString(",") { column -> column.mapping(row).get(resources) }
        }
        return ScorePadDetailsString(header, details)
    }

    fun toColumnMetadata(columnType: ScorePadColumnType) = toColumnMetadata(columnType, goldsTypes)

    data class ScorePadDetailsString(val headerRow: String?, val details: String)

    /**
     * Simple types mostly used to dictate column ordering
     */
    enum class ScorePadColumnType {
        HEADER,
        ARROWS,
        HITS,
        SCORE,
        GOLDS,
        RUNNING_TOTAL,
    }

    companion object {
        /**
         * How many arrows need to be shot at a distance before it qualifies to have a half-way total
         */
        private const val HALF_DISTANCE_TOTAL_ARROW_THRESHOLD = 6 * 12

        fun toColumnMetadata(columnType: ScorePadColumnType, goldsTypes: List<GoldsType>) = when (columnType) {
            ScorePadColumnType.HEADER -> listOf(ScorePadColumn.Header)
            ScorePadColumnType.ARROWS -> listOf(ScorePadColumn.FixedData.ARROWS)
            ScorePadColumnType.HITS -> listOf(ScorePadColumn.FixedData.HITS)
            ScorePadColumnType.SCORE -> listOf(ScorePadColumn.FixedData.SCORE)
            ScorePadColumnType.GOLDS -> goldsTypes.map { ScorePadColumn.Golds(it) }
            ScorePadColumnType.RUNNING_TOTAL -> listOf(ScorePadColumn.FixedData.RUNNING_TOTAL)
        }
    }
}
