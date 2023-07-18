package eywa.projectcodex.common.archeryObjects

import android.content.res.Resources
import eywa.projectcodex.R
import eywa.projectcodex.common.utils.DateTimeFormat
import eywa.projectcodex.database.archerRound.ArcherRound
import eywa.projectcodex.database.archerRound.DatabaseFullArcherRoundInfo
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.arrowValue.getGolds
import eywa.projectcodex.database.arrowValue.getHits
import eywa.projectcodex.database.arrowValue.getScore
import eywa.projectcodex.database.rounds.*

data class FullArcherRoundInfo(
        val archerRound: ArcherRound,
        val arrows: List<ArrowValue>?,
        val round: Round? = null,
        val roundArrowCounts: List<RoundArrowCount>? = null,
        val roundSubType: RoundSubType? = null,
        val roundDistances: List<RoundDistance>? = null,
        val isPersonalBest: Boolean = false,
        val isTiedPersonalBest: Boolean = false,
        val use2023HandicapSystem: Boolean = false,
) {
    constructor(full: DatabaseFullArcherRoundInfo, use2023HandicapSystem: Boolean) : this(
            archerRound = full.archerRound,
            arrows = full.arrows,
            round = full.round,
            roundArrowCounts = full.roundArrowCounts,
            roundSubType = full.roundSubType,
            roundDistances = full.roundDistances,
            isPersonalBest = full.isPersonalBest ?: false,
            isTiedPersonalBest = (full.isPersonalBest ?: false) && (full.isTiedPersonalBest ?: false),
            use2023HandicapSystem = use2023HandicapSystem,
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

    val goldsType = if (round == null) GoldsType.defaultGoldsType else GoldsType.getGoldsType(round)
    fun golds(type: GoldsType? = null) = arrows?.getGolds(type ?: goldsType) ?: 0

    val pbType
        get() = when {
            isTiedPersonalBest -> PbType.SINGLE_TIED
            isPersonalBest -> PbType.SINGLE
            else -> null
        }

    val arrowsShot by lazy { arrows?.size ?: 0 }

    val remainingArrows by lazy {
        roundArrowCounts
                ?.takeIf { it.isNotEmpty() }
                ?.sumOf { it.arrowCount }
                ?.minus(arrowsShot)
    }

    val isRoundComplete
        get() = remainingArrows?.let { it <= 0 } ?: false

    /**
     * Pairs of arrow counts to distances in order (earlier distances first)
     */
    val remainingArrowsAtDistances: List<Pair<Int, Int>>? by lazy {
        if (remainingArrows == null || remainingArrows!! <= 0) return@lazy null

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

    val hasSurplusArrows by lazy { remainingArrows.let { it != null && it < 0 } }

    val isInnerTenArcher = false

    val handicapFloat by lazy {
        if (
            round == null
            || roundArrowCounts.isNullOrEmpty()
            || roundDistances.isNullOrEmpty()
            || arrows.isNullOrEmpty()
            || hasSurplusArrows
        ) return@lazy null

        Handicap.getHandicapForRound(
                round = round,
                roundArrowCounts = roundArrowCounts,
                roundDistances = roundDistances,
                score = score,
                innerTenArcher = isInnerTenArcher,
                arrows = arrowsShot,
                use2023Handicaps = use2023HandicapSystem,
                faces = archerRound.faces,
        )
    }

    val handicap by lazy { handicapFloat?.roundHandicap() }

    val predictedScore by lazy {
        if (handicapFloat == null) return@lazy null
        // No need to predict a score if round is already completed
        if (remainingArrows!! == 0) return@lazy null

        Handicap.getScoreForRound(
                round = round!!,
                roundArrowCounts = roundArrowCounts!!,
                roundDistances = roundDistances!!,
                handicap = handicapFloat!!,
                innerTenArcher = isInnerTenArcher,
                arrows = null,
                use2023Handicaps = use2023HandicapSystem,
        )
    }

    fun getScorePadData(endSize: Int): ScorePadData? {
        if (arrows.isNullOrEmpty()) {
            return null
        }
        return ScorePadData(this, endSize, goldsType)
    }

    fun getScoreSummary(resources: Resources): String =
            if (arrowsShot > 0) {
                val res = resources.getString(R.string.create_round__no_round)
                resources.getString(
                        R.string.email_round_summary,
                        displayName ?: res,
                        DateTimeFormat.SHORT_DATE.format(archerRound.dateShot),
                        hits,
                        score,
                        resources.getString(goldsType.longStringId),
                        golds(),
                )
            }
            else {
                resources.getString(
                        R.string.email_round_summary_no_arrows,
                        displayName ?: resources.getString(R.string.create_round__no_round),
                        DateTimeFormat.SHORT_DATE.format(archerRound.dateShot),
                )
            }
}
