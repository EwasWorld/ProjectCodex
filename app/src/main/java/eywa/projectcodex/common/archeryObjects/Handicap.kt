package eywa.projectcodex.common.archeryObjects

import eywa.projectcodex.common.utils.getDistances
import eywa.projectcodex.database.rounds.FullRoundInfo
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import kotlin.math.*

fun Float.roundHandicap() = ceil(this).roundToInt()

object Handicap {
    fun getHandicapForRound(
            round: FullRoundInfo,
            subType: Int?,
            score: Int,
            innerTenArcher: Boolean = false,
            arrows: Int? = null,
            use2023Handicaps: Boolean = false,
    ): Float? {
        val distances = round.getDistances(subType)
        if (round.roundArrowCounts == null || distances == null) return null
        return getHandicapForRound(
                round.round,
                round.roundArrowCounts,
                distances,
                score,
                innerTenArcher,
                arrows,
                use2023Handicaps
        )
    }

    /**
     * Binary search for the handicap of the given [score]. Will find the worst (highest) possible (e.g. 599 portsmouth
     * can be 3, 4, or 5 so return 5)
     *
     * @param innerTenArcher whether or not the shooter uses inner ten scoring (unused if the round doesn't use inner
     * ten scoring)
     * @param arrows get the handicap only for the first this many arrows (if null then for a full round)
     * @return handicap 0-150
     */
    fun getHandicapForRound(
            round: Round,
            roundArrowCounts: List<RoundArrowCount>,
            roundDistances: List<RoundDistance>,
            score: Int,
            innerTenArcher: Boolean = false,
            arrows: Int? = null,
            use2023Handicaps: Boolean = false,
    ): Float {
        require(arrows == null || arrows > 0) { "Arrows must be greater than 0" }
        require(arrows == null || arrows <= roundArrowCounts.sumOf { it.arrowCount }) { "Arrows must be at most arrowCounts total" }
        require(roundArrowCounts.size == roundDistances.size) { "Arrow counts and distances size not equal" }
        require(roundArrowCounts.all { it.roundId == round.roundId }) { "Arrow count round ID incorrect" }
        require(roundDistances.all { it.roundId == round.roundId }) { "Distance round ID incorrect" }
        require(roundDistances.distinctBy { it.subTypeId }.size == 1) { "Multiple subtypes given" }

        fun calculate(hc: Float) =
                getScoreForRound(round, roundArrowCounts, roundDistances, hc, innerTenArcher, arrows, use2023Handicaps)

        val accuracy = -2

        var low = 0f // best possible handicap
        // TODO Error if score is worse than max handicap
        var high = if (use2023Handicaps) 150f else 100f // worst possible handicap
        while (true) {
            var testHC = (high + low) / 2f
            val testHCScore = calculate(testHC)
            when {
                testHCScore == score -> {
                    // Some scores can have multiple possible handicaps - find the worst (highest) integer
                    val cl = calculate(ceil(testHC))
                    val fl = calculate(floor(testHC))

                    if (fl - cl > 1) return testHC
                    testHC = if (cl == score) ceil(testHC) else floor(testHC)
                    while (calculate(testHC + 1) == score) {
                        testHC++
                    }
                    return testHC
                }
                high - low <= 10f.pow(accuracy) -> return if (score < testHCScore) low else high
                score < testHCScore -> low = testHC
                else -> high = testHC
            }
        }
    }

    /**
     * Calculates the expected score for [round] for an archer with [handicap]
     *
     * @param roundArrowCounts all arrow counts pertaining to [round], roundId field must be identical for all items
     * @param roundDistances all distances pertaining to [round], roundId and sub type fields must be identical for all items
     * @param innerTenArcher whether or not the shooter uses inner ten scoring (unused if the round doesn't use inner ten scoring)
     * @param arrows null to calculate expected score for whole round, else calculate for X arrows shot
     * @return expected score for the handicap
     */
    fun getScoreForRound(
            round: Round,
            roundArrowCounts: List<RoundArrowCount>,
            roundDistances: List<RoundDistance>,
            handicap: Float,
            innerTenArcher: Boolean,
            arrows: Int?,
            use2023Handicaps: Boolean = false,
    ): Int {
        require(arrows == null || arrows > 0) { "Arrows must be greater than 0" }
        require(arrows == null || arrows <= roundArrowCounts.sumOf { it.arrowCount }) { "Arrows must be at most arrowCounts total" }
        require(roundArrowCounts.size == roundDistances.size) { "Arrow counts and distances size not equal" }
        require(roundArrowCounts.all { it.roundId == round.roundId }) { "Arrow count round ID incorrect" }
        require(roundDistances.all { it.roundId == round.roundId }) { "Distance round ID incorrect" }
        require(roundDistances.distinctBy { it.subTypeId }.size == 1) { "Multiple subtypes given" }

        // Get score
        val scoringType = ScoringType.getScoringType(round)
        var currentArrowCount = 0
        var score = 0.0f
        for (countsDistances in roundArrowCounts.sortedBy { it.distanceNumber }
                .zip(roundDistances.sortedBy { it.distanceNumber })) {
            var arrowCount = countsDistances.first.arrowCount
            val faceSizeInCm = countsDistances.first.faceSizeInCm
            val distance = countsDistances.second.distance
            if (arrows == null || currentArrowCount < arrows) {
                if (arrows != null && currentArrowCount + arrowCount > arrows) {
                    arrowCount = arrows - currentArrowCount
                }
                score += arrowCount * scoringType.averageScorePerArrow(
                        if (round.isMetric) distance.toFloat() else distance * 0.9144f,
                        faceSizeInCm,
                        handicap,
                        innerTenArcher && !round.isOutdoor,
                        use2023Handicaps,
                )
                currentArrowCount += arrowCount
            }
        }
        return score.roundToInt()
    }

    /**
     * How arrows on a face are scored e.g. 10-zone metric or 5-zone imperial
     * Used for calculating average arrow score for a given handicap
     */
    enum class ScoringType(
            // Constants used in averageScorePerArrow
            private var initial: Int,
            private var sumMultiplier: Int,
            private var sumStart: Int,
            private var sumEnd: Int,
            private var sumInternalDenominator: Int,
            private var subtractInternalDenominator: Float,
            private var subtractMultiplier: Int
    ) {
        IMPERIAL(9, 2, 1, 4, 10, 2f, 1),
        METRIC(10, 1, 1, 10, 20, 0f, 0),

        // 40cm face cut off after the 6 ring (3 separate targets in a vertical line)
        TRIPLE(10, 1, 1, 4, 20, 20f / 5f, 6),
        VEGAS(TRIPLE),

        // 80cm face cut off after 6 ring
        FITA_FIVE_ZONE(TRIPLE),
        WORCESTER(5, 1, 1, 5, 10, 0f, 0),

        // 80cm face cut off after 5 ring
        FITA_SIX_ZONE(10, 1, 1, 5, 20, 20f / 6f, 5);

        companion object {
            // This is the diameter of an 1864
            private const val arrowDiameterInCm = 0.357f

            // TODO Make a faces enum?
            fun getScoringType(round: Round, face: String? = null): ScoringType {
                return when {
                    round.displayName.contains(WORCESTER.toString(), ignoreCase = true) -> WORCESTER
                    round.displayName.contains(VEGAS.toString(), ignoreCase = true) -> VEGAS
                    face != null && face.equals("triple", ignoreCase = true) -> TRIPLE
                    face != null && face.equals("fita five", ignoreCase = true) -> FITA_FIVE_ZONE
                    face != null && face.equals("fita six", ignoreCase = true) -> FITA_SIX_ZONE
                    round.isMetric || !round.isOutdoor -> METRIC
                    else -> IMPERIAL
                }
            }
        }

        constructor(scoringType: ScoringType) : this(
                scoringType.initial,
                scoringType.sumMultiplier,
                scoringType.sumStart,
                scoringType.sumEnd,
                scoringType.sumInternalDenominator,
                scoringType.subtractInternalDenominator,
                scoringType.subtractMultiplier
        )

        /**
         * Formulas found in The Construction of the Graduated Handicap Tables for Target Archery by David Lane
         *
         * @param innerTenScoring true if only the inner ten ring should be counted as 10
         */
        fun averageScorePerArrow(
                rangeInM: Float,
                faceSizeInCm: Float,
                handicap: Float,
                innerTenScoring: Boolean,
                use2023Handicaps: Boolean,
        ): Float {
            val sigma = if (use2023Handicaps) {
                (rangeInM * 1.035f.pow(handicap + 6f)
                        * 0.05f
                        * exp(0.00365f * rangeInM)
                        ).pow(2f)
            }
            else {
                (rangeInM * 1.036f.pow(handicap + 12.9f)
                        * 0.05f
                        * (1 + 0.000001429f * 1.07f.pow(handicap + 4.3f) * rangeInM.pow(2f))
                        ).pow(2f)
            }

            fun expCalc(denominator: Float): Float =
                    exp(-(faceSizeInCm / denominator + arrowDiameterInCm).pow(2f) / sigma)

            val sumStart = sumStart + if (innerTenScoring) 1 else 0
            var total = 0f
            for (i in sumStart..sumEnd) {
                total += expCalc(sumInternalDenominator.toFloat() / i)
            }
            total = initial - sumMultiplier * total - (subtractMultiplier
                    * expCalc(subtractInternalDenominator))
            if (innerTenScoring) {
                total -= expCalc(40f)
            }
            return total
        }
    }
}
