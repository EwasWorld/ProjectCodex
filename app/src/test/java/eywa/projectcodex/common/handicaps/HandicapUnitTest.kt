package eywa.projectcodex.common.handicaps

import eywa.projectcodex.common.archeryObjects.Handicap
import eywa.projectcodex.common.archeryObjects.roundHandicap
import eywa.projectcodex.database.RoundFace
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * http://www.crystalpalacebowmen.org.uk/handicap.shtml appears to also implement the same formulas as me
 * Codex's calculated numbers for the following rounds match this website but not printed handicap tables (circa 1998):
 * - Long Metric V
 */
class HandicapUnitTest {
    data class ScoreHandicapMapping(val score: Int, val handicap: Int)

    class HandicapTableEntry(
            roundInfo: List<Any>,
            val mappings: List<ScoreHandicapMapping>,
            val useInnerTen: Boolean = false,
            val face: RoundFace? = null,
    ) {
        val round: Round = roundInfo.filterIsInstance<Round>().first()
        val arrowCounts: List<RoundArrowCount> = roundInfo.filterIsInstance<RoundArrowCount>()
        val distances: List<RoundDistance> = roundInfo.filterIsInstance<RoundDistance>()
    }

    class Outcome {
        private var count: Int = 0
        private val fails: MutableList<String> = mutableListOf()

        fun <T : Comparable<T>> add(name: String, expected: T, actual: T) {
            count++
            if (actual != expected) {
                fails.add("$name - expected: $expected - actual: $actual")
            }
        }

        fun getFails() =
                if (fails.isEmpty()) null
                else "${fails.size} of $count\n" + fails.joinToString("\n")
    }

    @Suppress("KotlinConstantConditions") // It is mistaken
    private fun testScoreToHandicap(entries: Iterable<HandicapTableEntry>, use2023Handicaps: Boolean) {
        val outcome = Outcome()
        for (hcEntry in entries) {
            var previous: ScoreHandicapMapping? = null
            for (mapping in hcEntry.mappings.sortedByDescending { it.score }) {
                // If the handicaps differ by one but the scores differ by more than one, test all values in the score
                //      range to ensure the worst handicap is always given
                val range = when (mapping.handicap) {
                    previous?.handicap?.plus(1) -> mapping.score until previous.score
                    else -> mapping.score.rangeTo(mapping.score)
                }
                if (range.isEmpty()) {
                    throw IllegalStateException()
                }
                for (score in range) {
                    outcome.add(
                            "${hcEntry.round.displayName}: $score",
                            mapping.handicap,
                            Handicap.getHandicapForRound(
                                    hcEntry.round,
                                    hcEntry.arrowCounts,
                                    hcEntry.distances,
                                    score,
                                    hcEntry.useInnerTen,
                                    null,
                                    faces = hcEntry.face?.let { listOf(it) },
                                    use2023Handicaps = use2023Handicaps,
                            ).roundHandicap()
                    )
                }
                previous = mapping
            }
        }

        assertEquals(null, outcome.getFails())
    }

    @Suppress("KotlinConstantConditions") // It is mistaken
    private fun testHandicapToScore(entries: Iterable<HandicapTableEntry>, use2023Handicaps: Boolean) {
        val outcome = Outcome()
        for (hcEntry in entries) {
            var previous: ScoreHandicapMapping? = null
            for (mapping in hcEntry.mappings.sortedByDescending { it.score }) {
                // If the scores differ by one but the handicaps differ by more than one, test all values in the
                //      handicap to ensure the correct score is always given
                val range = when (mapping.score) {
                    previous?.score?.plus(1) -> mapping.handicap until previous.handicap
                    else -> mapping.handicap.rangeTo(mapping.handicap)
                }
                if (range.isEmpty()) {
                    throw IllegalStateException()
                }

                for (handicap in range) {
                    outcome.add(
                            "${hcEntry.round.displayName}: $handicap",
                            mapping.score,
                            Handicap.getScoreForRound(
                                    hcEntry.round,
                                    hcEntry.arrowCounts,
                                    hcEntry.distances,
                                    handicap.toFloat(),
                                    hcEntry.useInnerTen,
                                    null,
                                    faces = hcEntry.face?.let { listOf(it) },
                                    use2023Handicaps = use2023Handicaps,
                            ),
                    )
                }
                previous = mapping
            }
        }

        assertEquals(null, outcome.getFails())
    }

    @Test
    fun testDavidLaneHandicaps_ScoreToHandicap() = testScoreToHandicap(HandicapData.davidLaneHandicapEntries, false)

    @Test
    fun testDavidLaneHandicaps_HandicapToScore() = testHandicapToScore(HandicapData.davidLaneHandicapEntries, false)

    @Test
    fun testAgb2023Handicaps_ScoreToHandicap() =
            testScoreToHandicap(HandicapData.archerGb2023HandicapTableEntries, true)

    @Test
    fun testAgb2023Handicaps_HandicapToScore() =
            testHandicapToScore(HandicapData.archerGb2023HandicapTableEntries, true)

    @Test
    fun testPartialHandicap() {
        // TODO_CURRENT testPartialHandicap
    }
}
