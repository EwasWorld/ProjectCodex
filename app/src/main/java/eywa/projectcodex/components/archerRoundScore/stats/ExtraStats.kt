package eywa.projectcodex.components.archerRoundScore.stats

import eywa.projectcodex.common.utils.standardDeviation
import eywa.projectcodex.common.utils.standardDeviationInt
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance

open class ExtraStats(
        arrows: List<ArrowValue>,
        endSize: Int,
        val handicap: Double?
) {
    val averageEnd: Float
    val endStDev: Float
    val averageArrow: Float
    val arrowStdDev: Float

    init {
        val arrowCount = arrows.count()
        val scores = arrows.map { it.score }
        val sum = scores.sum()
        val endCount = arrowCount.toFloat() / endSize.toFloat()

        averageEnd = sum.toFloat() / endCount
        endStDev = scores
                .windowed(endSize, endSize)
                .map { end -> end.sumOf { it }.toFloat() }
                .standardDeviation()
        averageArrow = sum.toFloat() / arrowCount.toFloat()
        arrowStdDev = scores.standardDeviationInt()
    }
}

class DistanceExtra(
        val distance: RoundDistance,
        roundArrowCount: RoundArrowCount,
        arrows: List<ArrowValue>,
        endSize: Int,
        calculateHandicap: (arrows: List<ArrowValue>, arrowCount: RoundArrowCount, distance: RoundDistance) -> Double?,
) : ExtraStats(arrows, endSize, calculateHandicap(arrows, roundArrowCount, distance))

class GrandTotalExtra(
        arrows: List<ArrowValue>,
        endSize: Int,
        handicap: Double?,
) : ExtraStats(arrows, endSize, handicap)
