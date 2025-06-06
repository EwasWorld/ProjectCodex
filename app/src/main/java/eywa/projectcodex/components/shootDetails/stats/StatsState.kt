package eywa.projectcodex.components.shootDetails.stats

import eywa.projectcodex.common.utils.classificationTables.ClassificationTablesUseCase
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationBow
import eywa.projectcodex.components.shootDetails.ShootDetailsState
import eywa.projectcodex.components.shootDetails.stats.ui.StatsScreenPastRecordsTabs
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
    val openHandicapTablesScreen = extras.openHandicapTablesScreen
    val openClassificationTablesScreen = extras.openClassificationTablesScreen
    val archerInfo = main.archerInfo
    val bow = main.bow
    val archerHandicaps = main.archerHandicaps?.sortedByDescending { it.dateSet }
    val useSimpleView = main.useSimpleView

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
                    innerTenArcher = bow != null && bow.type == ClassificationBow.COMPOUND,
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
                        innerTenArcher = bow != null && bow.type == ClassificationBow.COMPOUND,
                        arrows = null,
                        use2023Handicaps = fullShootInfo.use2023HandicapSystem,
                        faces = fullShootInfo.getFaceForDistance(distance)?.let { listOf(it) },
                )
            }

    val classification = getClassification(
            classificationTables = classificationTablesUseCase,
            use2023System = main.use2023System ?: true,
            wa1440FullRoundInfo = main.wa1440FullRoundInfo,
            wa18FullRoundInfo = main.wa18FullRoundInfo,
    )

    /**
     * @return classification of the current score to isOfficialClassification
     */
    private fun getClassification(
            classificationTables: ClassificationTablesUseCase,
            use2023System: Boolean,
            wa1440FullRoundInfo: FullRoundInfo?,
            wa18FullRoundInfo: FullRoundInfo?,
    ) = classificationTables.getClassification(
            archerInfo = archerInfo,
            bow = bow,
            fullShootInfo = fullShootInfo,
            use2023System = use2023System,
            wa1440FullRoundInfo = wa1440FullRoundInfo,
            wa18FullRoundInfo = wa18FullRoundInfo,
    )


    val numbersBreakdownRowStats: List<NumbersBreakdownRowStats>?
        get() {
            val distances = fullShootInfo.roundDistances ?: return null
            val arrowCounts = fullShootInfo.roundArrowCounts ?: return null
            var arrows = fullShootInfo.arrows ?: return null
            check(distances.size == arrowCounts.size)

            val statsList = mutableListOf<NumbersBreakdownRowStats>()
            for (index in distances.indices) {
                val arrowCount = arrowCounts[index]
                val distArrows = arrows.take(arrowCount.arrowCount)
                        .takeIf { it.isNotEmpty() }
                        ?: break
                arrows = arrows.drop(arrowCount.arrowCount)
                statsList.add(
                        DistanceBreakdownRow(distances[index], arrowCount, distArrows, endSize, calculateHandicapFn),
                )
            }
            if (statsList.size > 1) {
                statsList.add(GrandTotalBreakdownRow(fullShootInfo.arrows, endSize, fullShootInfo.handicapFloat))
            }

            return statsList
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
        val openHandicapTablesScreen: Boolean = false,
        val openClassificationTablesScreen: Boolean = false,
        val isPastRoundRecordsDialogOpen: Boolean = false,
        val pastRoundScoresTab: StatsScreenPastRecordsTabs = StatsScreenPastRecordsTabs.BEST,
)
