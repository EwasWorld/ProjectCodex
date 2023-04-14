package eywa.projectcodex.components.viewScores.ui

import android.os.Build
import androidx.annotation.RequiresApi
import eywa.projectcodex.common.archeryObjects.Arrow
import eywa.projectcodex.common.archeryObjects.FullArcherRoundInfo
import eywa.projectcodex.common.logging.CustomLogger
import eywa.projectcodex.components.viewScores.data.ViewScoresEntry
import eywa.projectcodex.database.archerRound.ArcherRound
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import java.util.*

/**
 * Used to generate data for [ViewScoresScreen]
 */
@RequiresApi(Build.VERSION_CODES.O)
object ViewScoresEntryPreviewProvider {
    private val dates = listOf(
            Calendar.Builder().setDate(2021, 6, 17).setTimeOfDay(18, 5, 1).build().time,
            Calendar.Builder().setDate(2023, 5, 12).setTimeOfDay(9, 47, 1).build().time,
    )

    private val roundNames = listOf(
            "Metric II / Cadet Ladies 1440",
            null,
            "Hereford",
            "Albion",
            "Gents 1440",
            "Short Junior National",
            "National",
    )

    private val desiredHsg = listOf(
            listOf(36, 217, 12),
            listOf(0, 0, 0),
            listOf(36, 285, 5),
            listOf(12, 70, 2),
            null,
            listOf(144, 1280, 65),
    )

    fun generateEntries(size: Int) = List(size) { index ->
        val displayName = roundNames[index % roundNames.size]
        val hsg = desiredHsg[index % desiredHsg.size]
        val hasRoundInfo = index % 5 != 1 && displayName != null
        ViewScoresEntry(
                info = FullArcherRoundInfo(
                        archerRound = ArcherRound(
                                archerRoundId = index + 1,
                                dateShot = dates[index % dates.size],
                                archerId = 1,
                                roundId = displayName?.let { 1 }
                        ),
                        round = displayName?.let {
                            Round(1, "", displayName, true, true, listOf())
                        },
                        roundSubType = null,
                        arrows = hsg?.let { generateArrows(index + 1, hsg) },
                        roundArrowCounts = (
                                if (!hasRoundInfo) null
                                else hsg?.let { listOf(RoundArrowCount(1, 1, 1.0, hsg[0])) }
                                ),
                        roundDistances = if (!hasRoundInfo) null else listOf(RoundDistance(1, 1, 1, 20)),
                ),
                isSelected = index % 3 != 1,
                customLogger = CustomLogger(),
        )
    }

    fun generateIncompleteRound() = listOf(36, 217, 12).let { hsg ->
        ViewScoresEntry(
                info = FullArcherRoundInfo(
                        archerRound = ArcherRound(
                                archerRoundId = 1,
                                dateShot = dates[0],
                                archerId = 1,
                                roundId = 1
                        ),
                        round = Round(1, "", roundNames[0]!!, true, true, listOf()),
                        roundSubType = null,
                        arrows = generateArrows(1, hsg),
                        roundArrowCounts = listOf(RoundArrowCount(1, 1, 1.0, hsg[0] + 1)),
                        roundDistances = listOf(RoundDistance(1, 1, 1, 20)),
                ),
                isSelected = false,
                customLogger = CustomLogger(),
        )
    }

    private fun generateArrows(archerRoundId: Int, hsg: List<Int>) =
            generateArrows(archerRoundId, hsg[0], hsg[1], hsg[2])

    private fun generateArrows(archerRoundId: Int, hits: Int, score: Int, golds: Int): List<ArrowValue> {
        if (hits == 0 && score == 0 && golds == 0) {
            return listOf(ArrowValue(archerRoundId, 1, 0, false))
        }

        val nonGoldHits = hits - golds

        // Make all golds 10s, assume all hits need to be at least 1
        var remainingTotal = score - (golds * 10 + nonGoldHits)
        check(remainingTotal >= 0)

        val goldArrows = List(golds) { Arrow(10, true) }
        val hitArrows = List(nonGoldHits) {
            val arrowScore = minOf(remainingTotal + 1, 8)
            remainingTotal -= arrowScore - 1 // -1 because we already assumed all hits must be at least 1
            Arrow(arrowScore, false)
        }

        return goldArrows.plus(hitArrows)
                .mapIndexed { index, arrow ->
                    arrow.toArrowValue(archerRoundId, index)
                }
    }

    fun List<ViewScoresEntry>.setPersonalBests(indexes: List<Int>) = mapIndexed { index, entry ->
        entry.copy(info = entry.info.copy(isPersonalBest = indexes.contains(index)))
    }

    fun List<ViewScoresEntry>.setTiedPersonalBests(indexes: List<Int>) = mapIndexed { index, entry ->
        entry.copy(info = entry.info.copy(isTiedPersonalBest = indexes.contains(index)))
    }
}
