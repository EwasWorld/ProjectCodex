package eywa.projectcodex.components.shootDetails.stats

import eywa.projectcodex.components.shootDetails.ShootDetailsState
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
    val openEditScoreScreen = extras.openEditScoreScreen

    val extras: List<ExtraStats>?
        get() {
            val info = fullShootInfo
            val calculateHandicap =
                    { arrows: List<DatabaseArrowScore>, arrowCount: RoundArrowCount, distance: RoundDistance ->
                        if (info.handicap == null) null
                        else Handicap.getHandicapForRound(
                                round = info.round!!,
                                roundArrowCounts = listOf(arrowCount.copy(arrowCount = arrows.count())),
                                roundDistances = listOf(distance),
                                score = arrows.sumOf { it.score },
                                innerTenArcher = info.isInnerTenArcher,
                                arrows = null,
                                use2023Handicaps = info.use2023HandicapSystem,
                                faces = info.getFaceForDistance(distance)?.let { listOf(it) },
                        )
                    }

            if (info.roundDistances == null || info.roundArrowCounts == null || info.arrows == null) return null
            // Suppressed because the compiler is wrong and requires !! for unknown reasons
            @Suppress("UNNECESSARY_NOT_NULL_ASSERTION") var tempArrows = info.arrows!!
            return info.roundDistances.sortedBy { it.distanceNumber }
                    .zip(info.roundArrowCounts.sortedBy { it.distanceNumber })
                    .mapNotNull { (distance, arrowCount) ->
                        val distArrows = tempArrows.take(arrowCount.arrowCount)
                        tempArrows = tempArrows.drop(arrowCount.arrowCount)
                        if (distArrows.isEmpty()) return@mapNotNull null
                        DistanceExtra(
                                distance,
                                arrowCount,
                                distArrows,
                                endSize,
                                calculateHandicap,
                        )
                    }
                    .plus(GrandTotalExtra(info.arrows, endSize, info.handicapFloat))
        }
}

data class StatsExtras(
        val openEditScoreScreen: Boolean = false
)
