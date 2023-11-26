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
    private var sightersCount: Int = 0

    fun addRound(round: FullRoundInfo, sightersCount: Int = 0) {
        this.round = round
        this.sightersCount = sightersCount
    }

    fun addIdenticalArrows(size: Int, score: Int, isX: Boolean = false) {
        arrows = ArrowScoresPreviewHelper.getArrows(shoot.shootId, size, 1, score, isX)
    }

    fun addFullSetOfArrows() {
        arrows = ArrowScoresPreviewHelper.getArrowsInOrderFullSet(shoot.shootId)
    }

    fun addArrows(a: List<Arrow>) {
        arrows = a.mapIndexed { i, arrow -> arrow.asArrowScore(shoot.shootId, i + 1) }
    }

    fun addArrowCounter(count: Int) {
        counter = DatabaseArrowCounter(shoot.shootId, count)
    }

    fun completeRound(arrowScore: Int, isX: Boolean = false) {
        arrows = List(round!!.roundArrowCounts!!.sumOf { it.arrowCount }) {
            DatabaseArrowScore(shoot.shootId, it, arrowScore, isX)
        }
    }

    fun completeRoundWithCounter() {
        addArrowCounter(round!!.roundArrowCounts!!.sumOf { it.arrowCount })
    }

    fun completeRound(finalScore: Int) {
        val arrowCount = round!!.roundArrowCounts!!.sumOf { it.arrowCount }
        val tens = finalScore.floorDiv(10)

        val newArrows = List(tens) { Arrow(10) } +
                Arrow(finalScore % 10) +
                List(arrowCount - tens - 1) { Arrow(0) }

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
            arrows = arrows,
            isPersonalBest = isPersonalBest,
            isTiedPersonalBest = isTiedPersonalBest,
            shootRound = asDatabaseShootRound(),
            shootDetail = asDatabaseShootDetail(),
            arrowCounter = counter,
    )

    fun asFullShootInfo() = FullShootInfo(
            shoot = shoot,
            round = round?.round,
            roundArrowCounts = round?.roundArrowCounts,
            roundSubType = round?.roundSubTypes?.find { it.subTypeId == (roundSubTypeId ?: 1) },
            roundDistances = round?.getDistances(roundSubTypeId),
            arrows = arrows,
            use2023HandicapSystem = use2023HandicapSystem,
            isPersonalBest = isPersonalBest,
            isTiedPersonalBest = isTiedPersonalBest,
            shootRound = asDatabaseShootRound(),
            shootDetail = asDatabaseShootDetail(),
            arrowCounter = counter,
    )

    companion object {
        fun create(config: ShootPreviewHelperDsl.() -> Unit) =
                ShootPreviewHelperDsl().apply { config() }.asFullShootInfo()
    }
}

@Deprecated("Use Dsl")
object ShootPreviewHelper {
    fun newShoot(id: Int = 1, date: Calendar = Calendar.getInstance()) =
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

    fun FullShootInfo.joinToPrevious() = copy(shoot = shoot.copy(joinWithPrevious = true))

    fun FullShootInfo.setDate(date: Calendar) = copy(shoot = shoot.copy(dateShot = date))

    fun FullShootInfo.completeRound(finalScore: Int): FullShootInfo {
        val arrowCount = roundArrowCounts!!.sumOf { it.arrowCount }
        val tens = finalScore.floorDiv(10)

        val newArrows = List(tens) { Arrow(10) } +
                Arrow(finalScore % 10) +
                List(arrowCount - tens - 1) { Arrow(0) }

        return copy(arrows = newArrows.mapIndexed { index, arrow -> arrow.asArrowScore(shoot.shootId, index) })
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
}
