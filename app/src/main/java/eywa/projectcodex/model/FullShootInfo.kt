package eywa.projectcodex.model

import android.content.res.Resources
import eywa.projectcodex.R
import eywa.projectcodex.common.utils.DateTimeFormat
import eywa.projectcodex.database.RoundFace
import eywa.projectcodex.database.arrows.DatabaseArrowScore
import eywa.projectcodex.database.arrows.getGolds
import eywa.projectcodex.database.arrows.getHits
import eywa.projectcodex.database.arrows.getScore
import eywa.projectcodex.database.rounds.*
import eywa.projectcodex.database.shootData.DatabaseFullShootInfo
import eywa.projectcodex.database.shootData.DatabaseShoot
import eywa.projectcodex.database.shootData.DatabaseShootDetail
import eywa.projectcodex.database.shootData.DatabaseShootRound

data class FullShootInfo(
        val shoot: DatabaseShoot,
        val arrows: List<DatabaseArrowScore>?,
        val round: Round? = null,
        val roundArrowCounts: List<RoundArrowCount>? = null,
        val roundSubType: RoundSubType? = null,
        val roundDistances: List<RoundDistance>? = null,
        val isPersonalBest: Boolean = false,
        val isTiedPersonalBest: Boolean = false,
        val use2023HandicapSystem: Boolean = false,
        val shootRound: DatabaseShootRound? = null,
        val shootDetail: DatabaseShootDetail? = null,
) {
    constructor(full: DatabaseFullShootInfo, use2023HandicapSystem: Boolean) : this(
            shoot = full.shoot,
            arrows = full.arrows,
            round = full.round,
            roundArrowCounts = full.roundArrowCounts,
            roundSubType = full.roundSubType,
            roundDistances = full.roundDistances,
            isPersonalBest = full.isPersonalBest ?: false,
            isTiedPersonalBest = (full.isPersonalBest ?: false) && (full.isTiedPersonalBest ?: false),
            use2023HandicapSystem = use2023HandicapSystem,
            shootRound = full.shootRound,
            shootDetail = full.shootDetail,
    )

    init {
        require(arrows?.all { it.shootId == shoot.shootId } != false) { "Arrows mismatched id" }
        require(roundArrowCounts?.all { it.roundId == round?.roundId } != false) { "Arrow counts mismatched id" }
        require(
                roundDistances?.all {
                    it.roundId == round?.roundId && it.subTypeId == (shootRound?.roundSubTypeId ?: 1)
                } != false
        ) { "Distances mismatched id" }
        require(shootRound == null || shootDetail == null) { "Cannot have a round and detail" }
    }

    val displayName by lazy { roundSubType?.name ?: round?.displayName }

    val distanceUnit by lazy { round?.getDistanceUnitRes() }

    val id: Int by lazy { shoot.shootId }

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

    val faces = shootRound?.faces ?: shootDetail?.face?.let { listOf(it) }

    val currentFace
        get() = when {
            faces.isNullOrEmpty() -> null
            faces.size == 1 -> faces.first()
            round == null -> throw IllegalStateException("Cannot have more than one face with no round")
            remainingArrows == null || remainingArrows!! <= 0 -> null
            else -> {
                val distancesRemaining = remainingArrowsAtDistances!!.size
                faces[faces.size - distancesRemaining]
            }
        }

    fun getFaceForDistance(distance: RoundDistance): RoundFace? {
        if (faces.isNullOrEmpty()) return null
        if (roundDistances.isNullOrEmpty()) throw IllegalStateException("No distances found")
        val distanceIndex = roundDistances
                .sortedBy { it.distanceNumber }
                .indexOfFirst { it.distanceNumber == distance.distanceNumber }
                .takeIf { it != -1 }
                ?: throw IllegalStateException("Distance ${distance.distanceNumber} not found")
        return faces.getOrNull(distanceIndex)
                ?: faces.getOrNull(0)
    }

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
                faces = faces,
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
                faces = faces,
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
                        DateTimeFormat.SHORT_DATE.format(shoot.dateShot),
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
                        DateTimeFormat.SHORT_DATE.format(shoot.dateShot),
                )
            }
}