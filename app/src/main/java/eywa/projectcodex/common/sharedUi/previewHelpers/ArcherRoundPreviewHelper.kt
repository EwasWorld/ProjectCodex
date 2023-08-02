package eywa.projectcodex.common.sharedUi.previewHelpers

import eywa.projectcodex.database.archerRound.ArcherRound
import eywa.projectcodex.database.archerRound.DatabaseFullArcherRoundInfo
import eywa.projectcodex.database.archerRound.DatabaseShootRound
import eywa.projectcodex.database.arrows.DatabaseArrowScore
import eywa.projectcodex.database.rounds.FullRoundInfo
import eywa.projectcodex.model.Arrow
import eywa.projectcodex.model.FullArcherRoundInfo
import java.util.*

object ArcherRoundPreviewHelper {
    fun newArcherRound(
            id: Int = 1,
            date: Calendar = Calendar.getInstance(),
    ) = ArcherRound(
            archerRoundId = id,
            dateShot = date,
            archerId = 1,
    )

    fun newFullArcherRoundInfo(id: Int) =
            FullArcherRoundInfo(archerRound = newArcherRound(id = id), arrows = null, use2023HandicapSystem = true)

    fun newFullArcherRoundInfo(archerRound: ArcherRound = newArcherRound()) =
            FullArcherRoundInfo(archerRound = archerRound, arrows = null, use2023HandicapSystem = true)

    fun FullArcherRoundInfo.addIdenticalArrows(size: Int, score: Int, isX: Boolean = false) =
            copy(arrows = ArrowScoresPreviewHelper.getArrows(archerRound.archerRoundId, size, 1, score, isX))

    fun FullArcherRoundInfo.addFullSetOfArrows() =
            copy(arrows = ArrowScoresPreviewHelper.getArrowsInOrderFullSet(archerRound.archerRoundId))

    fun FullArcherRoundInfo.addArrows(arrows: List<Arrow>) =
            copy(arrows = arrows.mapIndexed { i, arrow -> arrow.toArrowScore(archerRound.archerRoundId, i + 1) })

    fun FullArcherRoundInfo.addRound(fullRoundInfo: FullRoundInfo) =
            copy(
                    round = fullRoundInfo.round,
                    roundArrowCounts = fullRoundInfo.roundArrowCounts,
                    roundSubType = fullRoundInfo.roundSubTypes?.find { it.subTypeId == 1 },
                    roundDistances = fullRoundInfo.getDistances(subTypeId = 1),
                    shootRound = DatabaseShootRound(
                            archerRoundId = archerRound.archerRoundId,
                            roundId = fullRoundInfo.round.roundId,
                            roundSubTypeId = 1,
                    ),
            )

    fun FullArcherRoundInfo.completeRound(arrowScore: Int, isX: Boolean = false) =
            copy(
                    arrows = List(roundArrowCounts!!.sumOf { it.arrowCount }) {
                        DatabaseArrowScore(archerRound.archerRoundId, it, arrowScore, isX)
                    }
            )

    fun FullArcherRoundInfo.asDatabaseFullArcherRoundInfo() = DatabaseFullArcherRoundInfo(
            archerRound = archerRound,
            arrows = arrows,
            round = round,
            roundArrowCounts = roundArrowCounts,
            allRoundSubTypes = roundSubType?.let { listOf(it) },
            allRoundDistances = roundDistances,
    )
}
