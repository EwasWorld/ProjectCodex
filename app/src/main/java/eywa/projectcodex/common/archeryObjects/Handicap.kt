package eywa.projectcodex.common.archeryObjects

import eywa.projectcodex.common.utils.getDistances
import eywa.projectcodex.database.RoundFace
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
            faces: List<RoundFace>? = null,
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
                use2023Handicaps,
                faces,
        )
    }

    /**
     * Binary search for the handicap of the given [score]. Will find the worst (highest) possible (e.g. 599 portsmouth
     * can be 3, 4, or 5 so return 5). If the score is worse than the score for the worst (highest) possible handicap
     * (e.g. > 150 for [use2023Handicaps]), then the max handicap will be returned (150 in this case)
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
            faces: List<RoundFace>? = null,
    ): Float {
        require(arrows == null || arrows > 0) { "Arrows must be greater than 0" }
        require(arrows == null || arrows <= roundArrowCounts.sumOf { it.arrowCount }) { "Arrows must be at most arrowCounts total" }
        require(roundArrowCounts.size == roundDistances.size) { "Arrow counts and distances size not equal" }
        require(roundArrowCounts.all { it.roundId == round.roundId }) { "Arrow count round ID incorrect" }
        require(roundDistances.all { it.roundId == round.roundId }) { "Distance round ID incorrect" }
        require(roundDistances.distinctBy { it.subTypeId }.size == 1) { "Multiple subtypes given" }

        fun calculate(hc: Float) =
                HandicapPair(
                        handicap = hc,
                        score = getScoreForRound(
                                round, roundArrowCounts, roundDistances,
                                hc, innerTenArcher,
                                arrows, use2023Handicaps, faces,
                        ),
                )

        val accuracy = -2

        var low = calculate(0f) // best possible handicap
        var high = calculate(if (use2023Handicaps) 150f else 100f) // worst possible handicap

        if (low.score < score) return low.handicap
        if (high.score >= score) return high.handicap

        /*
         * Find the handicap at which the [score] turns into [score - 1]
         */
        while (true) {
            check(high.handicap > low.handicap) { "Binary search bounds gone bad" }

            val testHc = when {
                high.score != score - 1 || low.score != score || high.handicap - low.handicap > 10f.pow(accuracy) ->
                    (high.handicap + low.handicap) / 2f
                floor(high.handicap) == floor(low.handicap) -> break
                (low.isIntegerHandicap() || high.isIntegerHandicap()) -> break
                else -> floor(high.handicap)
            }

            val testPair = calculate(testHc)

            when {
                testPair.score < score -> high = testPair
                else -> low = testPair
            }
        }

        val lowCheck = calculate(floor(low.handicap))
        val highCheck = calculate(ceil(high.handicap))
        check(highCheck.handicap > lowCheck.handicap) { "Binary search bounds gone bad" }

        if (highCheck.handicap - lowCheck.handicap == 1f && lowCheck.score == score && highCheck.score < score) {
            return lowCheck.handicap
        }

        return low.handicap
    }

    fun getScoreForRound(
            round: FullRoundInfo,
            subType: Int?,
            handicap: Float,
            innerTenArcher: Boolean,
            arrows: Int?,
            use2023Handicaps: Boolean = false,
            faces: List<RoundFace>? = null,
    ): Int? {
        val distances = round.getDistances(subType)
        if (round.roundArrowCounts == null || distances == null) return null

        return getScoreForRound(
                round.round,
                round.roundArrowCounts,
                distances,
                handicap,
                innerTenArcher,
                arrows,
                use2023Handicaps,
                faces,
        )
    }

    /**
     * Calculates the expected score for [round] for an archer with [handicap]
     *
     * @param roundArrowCounts all arrow counts pertaining to [round], roundId field must be identical for all items
     * @param roundDistances all distances pertaining to [round], roundId and sub type fields must be identical for all items
     * @param innerTenArcher whether or not the shooter uses inner ten scoring (unused if the round doesn't use inner ten scoring)
     * @param arrows null to calculate expected score for whole round, else calculate for X arrows shot
     * @param faces [RoundFace]s shot at. Must be of length 0 (all [RoundFace.FULL]), 1 (all at given face),
     *   or [roundDistances].size (first face is for lowest [RoundDistance.distanceNumber])
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
            faces: List<RoundFace>? = null,
    ): Int {
        require(arrows == null || arrows > 0) { "Arrows must be greater than 0" }
        require(
                arrows == null || arrows <= roundArrowCounts.sumOf { it.arrowCount }
        ) { "Arrows must be at most arrowCounts total" }
        require(roundArrowCounts.size == roundDistances.size) { "Arrow counts and distances size not equal" }
        require(roundArrowCounts.all { it.roundId == round.roundId }) { "Arrow count round ID incorrect" }
        require(roundDistances.all { it.roundId == round.roundId }) { "Distance round ID incorrect" }
        require(roundDistances.distinctBy { it.subTypeId }.size == 1) { "Multiple subtypes given" }
        require(
                faces.isNullOrEmpty() || faces.size == 1 || faces.size == roundDistances.size
        ) { "Must provide 0, 1, or distances.size faces" }

        // Get score
        var currentArrowCount = 0
        var score = 0.0f
        val sortedArrowCounts = roundArrowCounts.sortedBy { it.distanceNumber }
        val sortedDistances = roundDistances.sortedBy { it.distanceNumber }
        val allFaces = when {
            faces.isNullOrEmpty() -> null
            faces.size == 1 -> List(roundDistances.size) { faces.first() }
            else -> faces
        }
        for (i in roundArrowCounts.indices) {
            val scoringType = ScoringType.getScoringType(round, allFaces?.get(i))
            var arrowCount = sortedArrowCounts[i].arrowCount
            val faceSizeInCm = sortedArrowCounts[i].faceSizeInCm
            val distance = sortedDistances[i].distance
            if (arrows == null || currentArrowCount < arrows) {
                if (arrows != null && currentArrowCount + arrowCount > arrows) {
                    arrowCount = arrows - currentArrowCount
                }
                score += arrowCount * scoringType.averageScorePerArrow(
                        if (round.isMetric) distance.toFloat() else distance * 0.9144f,
                        faceSizeInCm,
                        handicap,
                        innerTenArcher && !round.isOutdoor,
                        round.isOutdoor,
                        use2023Handicaps,
                )
                currentArrowCount += arrowCount
            }
        }
        if (use2023Handicaps) {
            score = ceil(score)
        }
        return score.roundToInt()
    }

    data class HandicapPair(
            val handicap: Float,
            val score: Int,
    ) {
        override fun toString(): String {
            return "%.3f-$score".format(handicap)
        }

        fun isIntegerHandicap() = floor(handicap) == handicap
    }

    /**
     * How arrows on a face are scored e.g. 10-zone metric or 5-zone imperial
     * Used for calculating average arrow score for a given handicap
     */
    private enum class ScoringType(
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

        // 80cm face cut off after 6 ring
        FITA_FIVE_ZONE(TRIPLE),

        // 80cm face cut off after 5 ring
        FITA_SIX_ZONE(10, 1, 1, 5, 20, 20f / 6f, 5),

        WORCESTER(5, 1, 1, 5, 10, 0f, 0),
        WORCESTER_FIVE(5, 1, 1, 1, 10, 10f / 2f, 4),
        ;

        companion object {
            fun getDefaultArrowRadiusInCm(isOutdoor: Boolean, use2023Handicaps: Boolean) =
                    when {
                        // Radius of an 18/64" diameter arrow
                        !use2023Handicaps -> 0.357f
                        isOutdoor -> 0.55f / 2f
                        else -> 0.93f / 2f
                    }


            fun getScoringType(round: Round, face: RoundFace? = null) =
                    when {
                        round.displayName.contains(WORCESTER.toString(), ignoreCase = true) ->
                            if (face == RoundFace.WORCESTER_FIVE) WORCESTER_FIVE else WORCESTER
                        face != null && face == RoundFace.TRIPLE -> TRIPLE
                        face != null && face == RoundFace.FITA_FIVE -> FITA_FIVE_ZONE
                        face != null && face == RoundFace.FITA_SIX -> FITA_SIX_ZONE
                        round.isMetric || !round.isOutdoor -> METRIC
                        else -> IMPERIAL
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
                isOutdoor: Boolean,
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

            fun expCalc(denominator: Float): Float = exp(
                    (
                            faceSizeInCm / denominator
                                    + getDefaultArrowRadiusInCm(isOutdoor, use2023Handicaps)
                            ).pow(2f) / (-sigma)
            )

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
