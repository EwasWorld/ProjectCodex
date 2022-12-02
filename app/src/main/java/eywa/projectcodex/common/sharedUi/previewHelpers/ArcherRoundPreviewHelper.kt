package eywa.projectcodex.common.sharedUi.previewHelpers

import eywa.projectcodex.common.archeryObjects.FullArcherRoundInfo
import eywa.projectcodex.database.archerRound.DatabaseFullArcherRoundInfo
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.rounds.FullRoundInfo

object ArcherRoundPreviewHelper {
    val round = RoundPreviewHelper.outdoorImperialRoundData

    val fullArcherRoundInfo = FullArcherRoundInfo(
            DatabaseFullArcherRoundInfo(
                    archerRound = RoundPreviewHelper.archerRoundNoRound,
                    arrows = null,
                    round = null,
                    roundArrowCounts = listOf(),
                    allRoundSubTypes = listOf(),
                    allRoundDistances = listOf(),
            )
    )

    fun FullArcherRoundInfo.addArrows(size: Int, score: Int) =
            copy(arrows = List(size) { ArrowValue(1, it, score, false) })

    fun FullArcherRoundInfo.addRound(fullRoundInfo: FullRoundInfo) =
            copy(
                    archerRound = archerRound.copy(roundId = fullRoundInfo.round.roundId),
                    arrows = null,
                    round = fullRoundInfo.round,
                    roundArrowCounts = fullRoundInfo.roundArrowCounts,
                    roundSubType = fullRoundInfo.roundSubTypes?.find { it.subTypeId == 1 },
                    roundDistances = fullRoundInfo.roundDistances!!.filter { it.subTypeId == 1 },
            )
}