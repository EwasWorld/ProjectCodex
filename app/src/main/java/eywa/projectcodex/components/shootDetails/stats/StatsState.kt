package eywa.projectcodex.components.shootDetails.stats

import eywa.projectcodex.common.utils.classificationTables.ClassificationTablesUseCase
import eywa.projectcodex.common.utils.classificationTables.model.Classification
import eywa.projectcodex.components.shootDetails.ShootDetailsState
import eywa.projectcodex.database.arrows.DatabaseArrowScore
import eywa.projectcodex.database.rounds.FullRoundInfo
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import eywa.projectcodex.model.Handicap

class StatsState(
        main: ShootDetailsState,
        extras: StatsExtras,
        classificationTablesUseCase: ClassificationTablesUseCase,
) {
    val fullShootInfo = main.fullShootInfo!!
    val endSize = main.scorePadEndSize
    val useBetaFeatures = main.useBetaFeatures ?: false
    val openEditShootScreen = extras.openEditShootScreen
    val openEditHandicapInfoScreen = extras.openEditHandicapInfoScreen
    val openEditArcherInfoScreen = extras.openEditArcherInfoScreen
    val archerInfo = main.archerInfo
    val bow = main.bow
    val archerHandicaps = main.archerHandicaps?.sortedByDescending { it.dateSet }

    val archerHandicap
        get() = when {
            archerHandicaps.isNullOrEmpty() -> null
            fullShootInfo.round == null -> null
            else -> archerHandicaps.firstOrNull()
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

    val classification = getClassification(
            classificationTables = classificationTablesUseCase,
            use2023System = main.use2023System ?: true,
            wa1440FullRoundInfo = main.wa1440FullRoundInfo,
    )

    /**
     * @return classification of the current score TO isOfficialClassification
     */
    private fun getClassification(
            classificationTables: ClassificationTablesUseCase,
            use2023System: Boolean,
            wa1440FullRoundInfo: FullRoundInfo?,
    ): Pair<Classification, Boolean>? {
        if (
            archerInfo == null
            || bow == null
            || fullShootInfo.fullRoundInfo == null
            || fullShootInfo.arrowCounter != null
            || fullShootInfo.arrowsShot == 0
        ) return null

        val currentScore = (if (fullShootInfo.isRoundComplete) fullShootInfo.score else fullShootInfo.predictedScore)
                ?: return null

        val trueEntries = classificationTables.get(
                archerInfo.isGent,
                archerInfo.age,
                bow.type,
                fullShootInfo.fullRoundInfo!!,
                fullShootInfo.roundSubType?.subTypeId,
                use2023System,
        )
        val roughEntries = wa1440FullRoundInfo?.let {
            classificationTables.getRoughHandicaps(
                    archerInfo.isGent,
                    archerInfo.age,
                    bow.type,
                    wa1440FullRoundInfo,
                    use2023System,
            )
        }

        return (trueEntries ?: roughEntries)
                ?.filter { it.score!! <= currentScore }
                ?.maxByOrNull { it.score!! }?.classification
                ?.to(trueEntries != null)
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

    /*
     * Past scores
     */
    val pastRoundScoresTab = extras.pastRoundScoresTab
    private val recentPastRoundScores = main.pastRoundRecords?.sortedByDescending { it.dateShot }
    private val bestPastRoundScores = main.roundPbs?.sortedByDescending { it.score }
    val pastRoundScores =
            if (recentPastRoundScores == null || recentPastRoundScores.size < 2) null
            else if (pastRoundScoresTab == StatsScreenPastRecordsTabs.RECENT) recentPastRoundScores
            else bestPastRoundScores
    val pastRoundScoresPb
        get() = bestPastRoundScores?.firstOrNull()?.score
    val pastRoundScoresPbIsTied
        get() = bestPastRoundScores?.getOrNull(1)?.score?.let { it == pastRoundScoresPb } ?: false
    val isPastRoundRecordsDialogOpen = extras.isPastRoundRecordsDialogOpen

}

data class StatsExtras(
        val openEditShootScreen: Boolean = false,
        val openEditHandicapInfoScreen: Boolean = false,
        val openEditArcherInfoScreen: Boolean = false,
        val isPastRoundRecordsDialogOpen: Boolean = false,
        val pastRoundScoresTab: StatsScreenPastRecordsTabs = StatsScreenPastRecordsTabs.BEST,
)
