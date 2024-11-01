package eywa.projectcodex.common.sharedUi.previewHelpers

import eywa.projectcodex.common.utils.CodexPreviewHelperDsl
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

    fun asDatabaseFullShootInfo() = DatabaseFullShootInfo(
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
    )

    fun asFullShootInfo() = FullShootInfo(asDatabaseFullShootInfo(), use2023HandicapSystem)

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
)

@Deprecated("Use Dsl")
object ShootPreviewHelper {
    private fun newShoot(id: Int = 1, date: Calendar = Calendar.getInstance()) =
            DatabaseShoot(shootId = id, dateShot = date)

    fun newFullShootInfo(id: Int) =
            FullShootInfo(shoot = newShoot(id = id), arrows = null, use2023HandicapSystem = true)

    fun newFullShootInfo(shoot: DatabaseShoot = newShoot()) =
            FullShootInfo(shoot = shoot, arrows = null, use2023HandicapSystem = true)

    fun FullShootInfo.addIdenticalArrows(size: Int, score: Int, isX: Boolean = false) =
            copy(arrows = ArrowScoresPreviewHelper.getArrows(shoot.shootId, size, 1, score, isX))

    fun FullShootInfo.addFullSetOfArrows() =
            copy(arrows = ArrowScoresPreviewHelper.getArrowsInOrderFullSet(shoot.shootId))

    fun FullShootInfo.addArrows(arrows: List<Arrow>) =
            copy(arrows = arrows.mapIndexed { i, arrow -> arrow.asArrowScore(shoot.shootId, i + 1) })

    fun FullShootInfo.addRound(fullRoundInfo: FullRoundInfo) =
            copy(
                    round = fullRoundInfo.round,
                    roundArrowCounts = fullRoundInfo.roundArrowCounts,
                    roundSubType = fullRoundInfo.roundSubTypes?.find { it.subTypeId == 1 },
                    roundDistances = fullRoundInfo.getDistances(subTypeId = 1),
                    shootRound = DatabaseShootRound(
                            shootId = shoot.shootId,
                            roundId = fullRoundInfo.round.roundId,
                            roundSubTypeId = 1,
                    ),
            )

    fun FullShootInfo.completeRound(arrowScore: Int, isX: Boolean = false) =
            copy(
                    arrows = List(roundArrowCounts!!.sumOf { it.arrowCount }) {
                        DatabaseArrowScore(shoot.shootId, it, arrowScore, isX)
                    }
            )

    fun FullShootInfo.completeRound(finalScore: Int): FullShootInfo {
        val arrowCount = roundArrowCounts!!.sumOf { it.arrowCount }
        val tens = finalScore.floorDiv(10)

        val newArrows = List(tens) { Arrow(10) } +
                Arrow(finalScore % 10) +
                List(arrowCount - tens - 1) { Arrow(0) }

        return copy(arrows = newArrows.mapIndexed { index, arrow -> arrow.asArrowScore(shoot.shootId, index) })
    }
}
