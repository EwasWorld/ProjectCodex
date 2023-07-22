package eywa.projectcodex.components.archerRoundScore.stats

import eywa.projectcodex.components.archerRoundScore.state.HasBetaFeaturesFlag
import eywa.projectcodex.components.archerRoundScore.state.HasFullArcherRoundInfo
import eywa.projectcodex.components.archerRoundScore.state.HasScorePadEndSize
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import eywa.projectcodex.model.GoldsType
import eywa.projectcodex.model.Handicap

interface ArcherRoundStatsState : HasFullArcherRoundInfo, HasScorePadEndSize, HasBetaFeaturesFlag {
    val goldsType: GoldsType
    val openEditScoreScreen: Boolean

    val extras: List<ExtraStats>?
        get() {
            val info = fullArcherRoundInfo
            val calculateHandicap = { arrows: List<ArrowValue>, arrowCount: RoundArrowCount, distance: RoundDistance ->
                if (info.handicap == null) null
                else Handicap.getHandicapForRound(
                        round = info.round!!,
                        roundArrowCounts = listOf(arrowCount.copy(arrowCount = arrows.count())),
                        roundDistances = listOf(distance),
                        score = arrows.sumOf { it.score },
                        innerTenArcher = info.isInnerTenArcher,
                        arrows = null,
                        use2023Handicaps = info.use2023HandicapSystem,
                        faces = info.archerRound.faces,
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
                                scorePadEndSize,
                                calculateHandicap,
                        )
                    }
                    .plus(GrandTotalExtra(info.arrows, scorePadEndSize, info.handicapFloat))
        }
}
