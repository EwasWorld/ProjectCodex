package eywa.projectcodex.common.sharedUi.previewHelpers

import eywa.projectcodex.common.utils.CodexPreviewHelperDsl
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationBow
import eywa.projectcodex.database.RoundFace
import eywa.projectcodex.database.arrows.DatabaseArrowCounter
import eywa.projectcodex.database.arrows.DatabaseArrowScore
import eywa.projectcodex.database.rounds.FullRoundInfo
import eywa.projectcodex.database.shootData.DatabaseFullShootInfo
import eywa.projectcodex.database.shootData.DatabaseShoot
import eywa.projectcodex.database.shootData.DatabaseShootDetail
import eywa.projectcodex.database.shootData.DatabaseShootRound
import eywa.projectcodex.model.Arrow
import eywa.projectcodex.model.FullShootInfo
import eywa.projectcodex.model.headToHead.FullHeadToHead
import java.util.Calendar


@CodexPreviewHelperDsl
class ShootPreviewHelperDsl {
    var shoot = DatabaseShoot(shootId = 1, dateShot = Calendar.getInstance())
    var round: FullRoundInfo? = null
    var roundSubTypeId: Int? = null
    var arrows: List<DatabaseArrowScore>? = null
    var isPersonalBest = false
    var isTiedPersonalBest = false
    var use2023HandicapSystem = true
    var faces: List<RoundFace>? = null
    var counter: DatabaseArrowCounter? = null
    var sightersCount: Int = 0
    var h2h: FullHeadToHead? = null

    fun addH2h(config: HeadToHeadPreviewHelperDsl.() -> Unit) {
        h2h = HeadToHeadPreviewHelperDsl(shoot.shootId).apply(config).asFull()
    }

    fun addRound(round: FullRoundInfo, sightersCount: Int = 0) {
        this.round = round
        this.sightersCount = sightersCount
    }

    fun addIdenticalArrows(size: Int, score: Int, isX: Boolean = false) {
        arrows = arrows.orEmpty().plus(
                ArrowScoresPreviewHelper.getArrows(shoot.shootId, size, 1, score, isX),
        )
    }

    fun addFullSetOfArrows() {
        arrows = arrows.orEmpty().plus(
                ArrowScoresPreviewHelper.getArrowsInOrderFullSet(shoot.shootId),
        )
    }

    fun addArrows(a: List<Arrow>) {
        arrows = arrows.orEmpty().plus(
                a.mapIndexed { i, arrow -> arrow.asArrowScore(shoot.shootId, i + 1) },
        )
    }

    fun addDbArrows(a: List<DatabaseArrowScore>) {
        val first = (arrows?.maxOf { it.arrowNumber } ?: 0) + 1
        arrows = arrows.orEmpty().plus(
                a.mapIndexed { index, arrow -> arrow.copy(arrowNumber = first + index) },
        )
    }

    fun appendArrows(a: List<Arrow>) {
        val first = arrows?.maxByOrNull { it.arrowNumber }
        val firstArrowNumber = first?.arrowNumber ?: 1
        val newArrows = a.mapIndexed { index, arrow ->
            arrow.asArrowScore(shootId = first?.shootId ?: shoot.shootId, arrowNumber = firstArrowNumber + index)
        }
        arrows = arrows.orEmpty().plus(newArrows)
    }

    fun deleteLastArrow() {
        arrows = arrows?.dropLast(1)
    }

    fun addArrowCounter(count: Int) {
        counter = DatabaseArrowCounter(shoot.shootId, count)
    }

    fun completeRound(arrowScore: Int, isX: Boolean = false) {
        arrows = List(round!!.roundArrowCounts!!.sumOf { it.arrowCount }) {
            DatabaseArrowScore(shoot.shootId, it + 1, arrowScore, isX)
        }
    }

    fun completeRoundWithFullSet() {
        arrows = ArrowScoresPreviewHelper.getArrowsInOrder(
                shootId = shoot.shootId,
                size = round!!.roundArrowCounts!!.sumOf { it.arrowCount },
                firstArrowNumber = 1,
                ascending = true,
        )
    }

    fun completeRoundWithCounter() {
        addArrowCounter(round!!.roundArrowCounts!!.sumOf { it.arrowCount })
    }

    fun completeRoundWithFinalScore(finalScore: Int) {
        val arrowCount = round!!.roundArrowCounts!!.sumOf { it.arrowCount }
        setArrowsWithFinalScore(finalScore, arrowCount)
    }

    fun setArrowsWithFinalScore(finalScore: Int, arrowsShot: Int? = null) {
        val tens = finalScore.floorDiv(10)

        val missesCount = arrowsShot?.let { it - tens - 1 } ?: 0
        require(missesCount >= 0) { "Cannot achieve a score of $finalScore with $arrowsShot arrows" }

        val newArrows = List(tens) { Arrow(10) } +
                Arrow(finalScore % 10) +
                List(missesCount) { Arrow(0) }

        arrows = newArrows.mapIndexed { index, arrow -> arrow.asArrowScore(shoot.shootId, index) }
    }

    private fun asDatabaseShootRound() = round?.let {
        DatabaseShootRound(
                shootId = shoot.shootId,
                roundId = it.round.roundId,
                roundSubTypeId = roundSubTypeId,
                faces = faces,
                sightersCount = sightersCount,
        )
    }

    private fun asDatabaseShootDetail() =
            when {
                round != null -> null
                faces.isNullOrEmpty() -> null
                else -> DatabaseShootDetail(
                        shootId = shoot.shootId,
                        face = faces!!.first(),
                )
            }

    private fun asDatabaseFullShootInfo() = DatabaseFullShootInfo(
            shoot = shoot,
            round = round?.round,
            roundArrowCounts = round?.roundArrowCounts,
            allRoundSubTypes = round?.roundSubTypes,
            allRoundDistances = round?.roundDistances,
            arrows = arrows
                    ?.mapIndexed { index, arrow -> arrow.copy(shootId = shoot.shootId, arrowNumber = index + 1) },
            isPersonalBest = isPersonalBest,
            isTiedPersonalBest = isTiedPersonalBest,
            shootRound = asDatabaseShootRound(),
            shootDetail = asDatabaseShootDetail(),
            arrowCounter = counter,
            bow = ClassificationBow.RECURVE,
    )

    fun asFullShootInfo() = FullShootInfo(asDatabaseFullShootInfo(), use2023HandicapSystem).copy(h2h = h2h)

    companion object {
        fun create(config: ShootPreviewHelperDsl.() -> Unit) =
                ShootPreviewHelperDsl().apply { config() }.asFullShootInfo()
    }
}

fun FullShootInfo.asDatabaseFullShootInfo() = DatabaseFullShootInfo(
        shoot = shoot,
        arrows = arrows,
        round = round,
        roundArrowCounts = roundArrowCounts,
        allRoundSubTypes = roundSubType?.let { listOf(it) },
        allRoundDistances = roundDistances,
        shootRound = shootRound,
        shootDetail = shootDetail,
        bow = bow,
)
