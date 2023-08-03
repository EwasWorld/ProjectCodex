package eywa.projectcodex.common.sharedUi.previewHelpers

import eywa.projectcodex.database.arrows.DatabaseArrowScore
import eywa.projectcodex.database.rounds.FullRoundInfo
import eywa.projectcodex.database.shootData.DatabaseFullShootInfo
import eywa.projectcodex.database.shootData.DatabaseShoot
import eywa.projectcodex.database.shootData.DatabaseShootRound
import eywa.projectcodex.model.Arrow
import eywa.projectcodex.model.FullShootInfo
import java.util.*

object ShootPreviewHelper {
    fun newShoot(
            id: Int = 1,
            date: Calendar = Calendar.getInstance(),
    ) = DatabaseShoot(
            shootId = id,
            dateShot = date,
            archerId = 1,
    )

    fun newFullShootInfo(id: Int) =
            FullShootInfo(shoot = newShoot(id = id), arrows = null, use2023HandicapSystem = true)

    fun newFullShootInfo(shoot: DatabaseShoot = newShoot()) =
            FullShootInfo(shoot = shoot, arrows = null, use2023HandicapSystem = true)

    fun FullShootInfo.addIdenticalArrows(size: Int, score: Int, isX: Boolean = false) =
            copy(arrows = ArrowScoresPreviewHelper.getArrows(shoot.shootId, size, 1, score, isX))

    fun FullShootInfo.addFullSetOfArrows() =
            copy(arrows = ArrowScoresPreviewHelper.getArrowsInOrderFullSet(shoot.shootId))

    fun FullShootInfo.addArrows(arrows: List<Arrow>) =
            copy(arrows = arrows.mapIndexed { i, arrow -> arrow.toArrowScore(shoot.shootId, i + 1) })

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
