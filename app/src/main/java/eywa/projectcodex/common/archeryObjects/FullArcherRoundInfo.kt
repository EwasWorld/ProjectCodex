package eywa.projectcodex.common.archeryObjects

import eywa.projectcodex.components.archerRoundScore.Handicap
import eywa.projectcodex.database.archerRound.ArcherRound
import eywa.projectcodex.database.archerRound.DatabaseFullArcherRoundInfo
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.arrowValue.getGolds
import eywa.projectcodex.database.arrowValue.getHits
import eywa.projectcodex.database.arrowValue.getScore
import eywa.projectcodex.database.rounds.*

// TODO_CURRENT Bug: init can throw a null pointer exception on deletion of an archer round. Investigate
data class FullArcherRoundInfo(
        val archerRound: ArcherRound,
        val arrows: List<ArrowValue>?,
        val round: Round? = null,
        val roundArrowCounts: List<RoundArrowCount>? = null,
        val roundSubType: RoundSubType? = null,
        val roundDistances: List<RoundDistance>? = null,
        val isPersonalBest: Boolean = false,
        val isTiedPersonalBest: Boolean = false,
) {
    constructor(full: DatabaseFullArcherRoundInfo) : this(
            archerRound = full.archerRound,
            arrows = full.arrows,
            round = full.round,
            roundArrowCounts = full.roundArrowCounts,
            roundSubType = full.roundSubType,
            roundDistances = full.roundDistances,
            isPersonalBest = full.isPersonalBest ?: false,
            isTiedPersonalBest = (full.isPersonalBest ?: false) && (full.isTiedPersonalBest ?: false),
    )

    init {
        require(arrows?.all { it.archerRoundId == archerRound.archerRoundId } != false) { "Arrows mismatched id" }
        require(roundArrowCounts?.all { it.roundId == round?.roundId } != false) { "Arrow counts mismatched id" }
        require(
                roundDistances?.all {
                    it.roundId == round?.roundId && it.subTypeId == (archerRound.roundSubTypeId ?: 1)
                } != false
        ) { "Distances mismatched id" }
    }

    val displayName by lazy { roundSubType?.name ?: round?.displayName }

    val distanceUnit by lazy { round?.distanceUnitStringRes() }

    val id: Int by lazy { archerRound.archerRoundId }

    val hits by lazy { arrows?.getHits() ?: 0 }

    val score by lazy { arrows?.getScore() ?: 0 }

    fun golds(type: GoldsType) = arrows?.getGolds(type) ?: 0

    val arrowsShot by lazy { arrows?.size ?: 0 }

    val remainingArrows by lazy {
        roundArrowCounts
                ?.takeIf { it.isNotEmpty() }
                ?.sumOf { it.arrowCount }
                ?.minus(arrowsShot)
    }

    /**
     * Pairs of arrow counts to distances in order (earlier distances first)
     */
    val remainingArrowsAtDistances: List<Pair<Int, Int>>? by lazy {
        if ((remainingArrows ?: 0) <= 0) return@lazy null

        var shotCount = arrowsShot
        val arrowCounts = roundArrowCounts!!.toMutableList()

        while (shotCount > 0) {
            val nextCount = arrowCounts.first()
            if (nextCount.arrowCount <= shotCount) {
                shotCount -= nextCount.arrowCount
                arrowCounts.removeAt(0)
            }
            else {
                arrowCounts[0] = nextCount.copy(arrowCount = nextCount.arrowCount - shotCount)
                shotCount = 0
            }
        }

        arrowCounts.map { count ->
            count.arrowCount to roundDistances!!.find { it.distanceNumber == count.distanceNumber }!!.distance
        }
    }

    val hasSurplusArrows by lazy { remainingArrows?.let { it < 0 } }

    private val isInnerTenArcher by lazy { false }

    val handicap by lazy {
        if (round == null) return@lazy null
        if (listOf(roundArrowCounts, roundDistances, arrows).any { it.isNullOrEmpty() }) return@lazy null
        if (hasSurplusArrows == true) return@lazy null

        Handicap.getHandicapForRound(
                round,
                roundArrowCounts!!,
                roundDistances!!,
                arrows!!.sumOf { it.score },
                isInnerTenArcher,
                arrows.count()
        )
    }

    val predictedScore by lazy {
        if (handicap == null) return@lazy null
        // No need to predict a score if round is already completed
        if (remainingArrows!! == 0) return@lazy null

        Handicap.getScoreForRound(
                round!!, roundArrowCounts!!, roundDistances!!, handicap!!, isInnerTenArcher, null
        )
    }
}
