package eywa.projectcodex.components.shootDetails.stats

import eywa.projectcodex.components.shootDetails.ShootDetailsState
import eywa.projectcodex.database.archer.HandicapType
import eywa.projectcodex.database.arrows.DatabaseArrowScore
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import eywa.projectcodex.model.Handicap

class StatsState(
        main: ShootDetailsState,
        extras: StatsExtras,
) {
    val fullShootInfo = main.fullShootInfo!!
    val endSize = main.scorePadEndSize
    val useBetaFeatures = main.useBetaFeatures ?: false
    val openEditShootScreen = extras.openEditShootScreen
    val openEditArcherInfoScreen = extras.openEditArcherInfoScreen
    val archerHandicaps = main.archerHandicaps
    val recentPastRoundScores = main.pastRoundRecords?.sortedByDescending { it.dateShot }
    val bestPastRoundScores = main.roundPbs?.sortedByDescending { it.score }
    val pastRoundScoresTab = extras.pastRoundScoresTab
    val isPastRoundRecordsDialogOpen = extras.isPastRoundRecordsDialogOpen

    val archerHandicap
        get() = when {
            archerHandicaps.isNullOrEmpty() -> null
            fullShootInfo.round == null -> null
            fullShootInfo.round.isOutdoor -> archerHandicaps.find { it.handicapType == HandicapType.OUTDOOR }
            else -> archerHandicaps.find { it.handicapType == HandicapType.INDOOR }
        }?.handicap

    val allowance: Int?
        get() {
            val handicap = archerHandicap ?: return null
            val roundInfo = fullShootInfo.fullRoundInfo ?: return null
            return Handicap.getAllowanceForRound(
                    round = roundInfo,
                    subType = null,
                    handicap = handicap.toDouble(),
                    innerTenArcher = false,
                    use2023Handicaps = fullShootInfo.use2023HandicapSystem,
                    faces = fullShootInfo.faces,
            )
        }

    val predictedAdjustedScore
        get() = allowance?.let { fullShootInfo.predictedScore?.plus(it) }

    val adjustedFinalScore
        get() = if (fullShootInfo.isRoundComplete) allowance?.let { it + fullShootInfo.score } else null

    private val calculateHandicapFn =
            { arrows: List<DatabaseArrowScore>, arrowCount: RoundArrowCount, distance: RoundDistance ->
                if (fullShootInfo.handicap == null) null
                else Handicap.getHandicapForRound(
                        round = fullShootInfo.round!!,
                        roundArrowCounts = listOf(arrowCount.copy(arrowCount = arrows.count())),
                        roundDistances = listOf(distance),
                        score = arrows.sumOf { it.score },
                        innerTenArcher = fullShootInfo.isInnerTenArcher,
                        arrows = null,
                        use2023Handicaps = fullShootInfo.use2023HandicapSystem,
                        faces = fullShootInfo.getFaceForDistance(distance)?.let { listOf(it) },
                )
            }

    val extras: List<ExtraStats>?
        get() {
            val distances = fullShootInfo.roundDistances ?: return null
            val arrowCounts = fullShootInfo.roundArrowCounts ?: return null
            var arrows = fullShootInfo.arrows ?: return null
            check(distances.size == arrowCounts.size)

            val extrasList = mutableListOf<ExtraStats>()
            for (index in distances.indices) {
                val arrowCount = arrowCounts[index]
                val distArrows = arrows.take(arrowCount.arrowCount)
                        .takeIf { it.isNotEmpty() }
                        ?: break
                arrows = arrows.drop(arrowCount.arrowCount)
                extrasList.add(DistanceExtra(distances[index], arrowCount, distArrows, endSize, calculateHandicapFn))
            }
            extrasList.add(GrandTotalExtra(fullShootInfo.arrows, endSize, fullShootInfo.handicapFloat))

            return extrasList
        }
}

data class StatsExtras(
        val openEditShootScreen: Boolean = false,
        val openEditArcherInfoScreen: Boolean = false,
        val isPastRoundRecordsDialogOpen: Boolean = false,
        val pastRoundScoresTab: StatsScreenPastRecordsTabs = StatsScreenPastRecordsTabs.BEST,
)
