package eywa.projectcodex.common.sharedUi.previewHelpers

import eywa.projectcodex.database.arrows.DatabaseArrowScore
import eywa.projectcodex.database.rounds.FullRoundInfo
import eywa.projectcodex.database.shootData.DatabaseFullArcherRoundInfo
import eywa.projectcodex.database.shootData.DatabaseShoot
import eywa.projectcodex.database.shootData.DatabaseShootRound
import eywa.projectcodex.model.Arrow
import eywa.projectcodex.model.FullArcherRoundInfo
import java.util.*

object ArcherRoundPreviewHelper {
    fun newArcherRound(
            id: Int = 1,
            date: Calendar = Calendar.getInstance(),
    ) = DatabaseShoot(
            archerRoundId = id,
            dateShot = date,
            archerId = 1,
    )

    fun newFullArcherRoundInfo(id: Int) =
            FullArcherRoundInfo(shoot = newArcherRound(id = id), arrows = null, use2023HandicapSystem = true)

    fun newFullArcherRoundInfo(shoot: DatabaseShoot = newArcherRound()) =
            FullArcherRoundInfo(shoot = shoot, arrows = null, use2023HandicapSystem = true)

    fun FullArcherRoundInfo.addIdenticalArrows(size: Int, score: Int, isX: Boolean = false) =
            copy(arrows = ArrowScoresPreviewHelper.getArrows(shoot.archerRoundId, size, 1, score, isX))

    fun FullArcherRoundInfo.addFullSetOfArrows() =
            copy(arrows = ArrowScoresPreviewHelper.getArrowsInOrderFullSet(shoot.archerRoundId))

    fun FullArcherRoundInfo.addArrows(arrows: List<Arrow>) =
            copy(arrows = arrows.mapIndexed { i, arrow -> arrow.toArrowScore(shoot.archerRoundId, i + 1) })

    fun FullArcherRoundInfo.addRound(fullRoundInfo: FullRoundInfo) =
            copy(
                    round = fullRoundInfo.round,
                    roundArrowCounts = fullRoundInfo.roundArrowCounts,
                    roundSubType = fullRoundInfo.roundSubTypes?.find { it.subTypeId == 1 },
                    roundDistances = fullRoundInfo.getDistances(subTypeId = 1),
                    shootRound = DatabaseShootRound(
                            archerRoundId = shoot.archerRoundId,
                            roundId = fullRoundInfo.round.roundId,
                            roundSubTypeId = 1,
                    ),
            )

    fun FullArcherRoundInfo.completeRound(arrowScore: Int, isX: Boolean = false) =
            copy(
                    arrows = List(roundArrowCounts!!.sumOf { it.arrowCount }) {
                        DatabaseArrowScore(shoot.archerRoundId, it, arrowScore, isX)
                    }
            )

    fun FullArcherRoundInfo.asDatabaseFullArcherRoundInfo() = DatabaseFullArcherRoundInfo(
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
