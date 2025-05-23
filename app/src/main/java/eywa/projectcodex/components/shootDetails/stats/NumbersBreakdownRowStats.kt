package eywa.projectcodex.components.shootDetails.stats

import eywa.projectcodex.common.sharedUi.grid.CodexGridRowMetadata
import eywa.projectcodex.common.utils.standardDeviation
import eywa.projectcodex.database.arrows.DatabaseArrowScore
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import eywa.projectcodex.model.roundHandicap

open class NumbersBreakdownRowStats(
        arrows: List<DatabaseArrowScore>,
        endSize: Int,
        val handicap: Double?,
        private val isTotal: Boolean = false,
) : CodexGridRowMetadata {
    val averageEnd: Float
    val endStDev: Float
    val averageArrow: Float
    val arrowStdDev: Float

    override fun isTotal(): Boolean = isTotal

    init {
        val scores = arrows.map { it.score }
        val completedEnds = scores.windowed(endSize, endSize)

        if (completedEnds.isEmpty()) {
            averageEnd = scores.sum().toFloat()
            endStDev = 0f
        }
        else {
            averageEnd = completedEnds.flatten().sum().toFloat() / completedEnds.size.toFloat()
            endStDev = completedEnds
                    .map { end -> end.sumOf { it }.toFloat() }
                    .standardDeviation()
        }
        averageArrow = scores.sum().toFloat() / arrows.count().toFloat()
        arrowStdDev = scores.standardDeviation()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NumbersBreakdownRowStats) return false

        if (handicap?.roundHandicap() != other.handicap?.roundHandicap()) return false
        if (averageEnd != other.averageEnd) return false
        if (endStDev != other.endStDev) return false
        if (averageArrow != other.averageArrow) return false
        if (arrowStdDev != other.arrowStdDev) return false

        return true
    }

    override fun hashCode(): Int {
        var result = handicap?.hashCode() ?: 0
        result = 31 * result + averageEnd.hashCode()
        result = 31 * result + endStDev.hashCode()
        result = 31 * result + averageArrow.hashCode()
        result = 31 * result + arrowStdDev.hashCode()
        return result
    }
}

class DistanceBreakdownRow(
        val distance: RoundDistance,
        roundArrowCount: RoundArrowCount,
        arrows: List<DatabaseArrowScore>,
        endSize: Int,
        calculateHandicap: (arrows: List<DatabaseArrowScore>, arrowCount: RoundArrowCount, distance: RoundDistance) -> Double?,
) : NumbersBreakdownRowStats(arrows, endSize, calculateHandicap(arrows, roundArrowCount, distance), false)

class GrandTotalBreakdownRow(
        arrows: List<DatabaseArrowScore>,
        endSize: Int,
        handicap: Double?,
) : NumbersBreakdownRowStats(arrows, endSize, handicap, true)
