package eywa.projectcodex.common.sharedUi.previewHelpers

import eywa.projectcodex.common.archeryObjects.FullArcherRoundInfo
import eywa.projectcodex.database.archerRound.ArcherRound
import eywa.projectcodex.database.archerRound.DatabaseFullArcherRoundInfo
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.rounds.FullRoundInfo
import java.util.*

object ArcherRoundPreviewHelper {
    fun newArcherRound(
            id: Int = 1,
            date: Date = Calendar.getInstance().time,
    ) = ArcherRound(
            archerRoundId = id,
            dateShot = date,
            archerId = 1,
    )

    fun newFullArcherRoundInfo(id: Int) =
            FullArcherRoundInfo(archerRound = newArcherRound(id = id), arrows = null)

    fun newFullArcherRoundInfo(archerRound: ArcherRound = newArcherRound()) =
            FullArcherRoundInfo(archerRound = archerRound, arrows = null)

    fun FullArcherRoundInfo.addArrows(size: Int, arrowScore: Int, isX: Boolean = false) =
            copy(
                    arrows = (arrows ?: emptyList())
                            .plus(List(size) { ArrowValue(archerRound.archerRoundId, it, arrowScore, isX) })
            )

    fun FullArcherRoundInfo.addRound(fullRoundInfo: FullRoundInfo) =
            copy(
                    archerRound = archerRound.copy(roundId = fullRoundInfo.round.roundId, roundSubTypeId = 1),
                    arrows = null,
                    round = fullRoundInfo.round,
                    roundArrowCounts = fullRoundInfo.roundArrowCounts,
                    roundSubType = fullRoundInfo.roundSubTypes?.find { it.subTypeId == 1 },
                    roundDistances = fullRoundInfo.roundDistances!!.filter { it.subTypeId == 1 },
            )

    fun FullArcherRoundInfo.completeRound(arrowScore: Int, isX: Boolean = false) =
            copy(
                    arrows = List(roundArrowCounts!!.sumOf { it.arrowCount }) {
                        ArrowValue(archerRound.archerRoundId, it, arrowScore, isX)
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
