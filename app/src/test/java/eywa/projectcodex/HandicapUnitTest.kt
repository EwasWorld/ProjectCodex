package eywa.projectcodex

import eywa.projectcodex.common.archeryObjects.Handicap
import eywa.projectcodex.common.archeryObjects.roundHandicap
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import org.junit.Assert
import org.junit.Test

/**
 * http://www.crystalpalacebowmen.org.uk/handicap.shtml appears to also implement the same formulas as me
 * Codex's calculated numbers for the following rounds match this website but not printed handicap tables (circa 1998):
 * - Long Metric V
 */
class HandicapUnitTest {
    data class ScoreHandicapMapping(val score: Int, val handicap: Int)

    class HandicapTableEntry(roundInfo: List<Any>, val mappings: List<ScoreHandicapMapping>, val useInnerTen: Boolean) {
        val round: Round = roundInfo.filterIsInstance<Round>().first()
        val arrowCounts: List<RoundArrowCount> = roundInfo.filterIsInstance<RoundArrowCount>()
        val distances: List<RoundDistance> = roundInfo.filterIsInstance<RoundDistance>()
    }

    private val handicapTableEntries = listOf(
            /*
             * Imperial
             */
            HandicapTableEntry(
                    listOf(
                            Round(1, "western", "western", true, false, listOf()),
                            RoundArrowCount(1, 1, 122f, 48),
                            RoundArrowCount(1, 2, 122f, 48),
                            RoundDistance(1, 1, 1, 60),
                            RoundDistance(1, 2, 1, 50)
                    ),
                    listOf(
                            // Sparse high score end
                            ScoreHandicapMapping(864, 6),
                            ScoreHandicapMapping(863, 9),
                            ScoreHandicapMapping(862, 10),
                            ScoreHandicapMapping(861, 12),
                            ScoreHandicapMapping(859, 13),
                            ScoreHandicapMapping(858, 14),
                            // Mid range
                            ScoreHandicapMapping(764, 35),
                            ScoreHandicapMapping(756, 36),
                            ScoreHandicapMapping(748, 37),
                            ScoreHandicapMapping(739, 38),
                            ScoreHandicapMapping(730, 39)
                    ),
                    true
            ),
            /*
             * Metric
             */
            HandicapTableEntry(
                    listOf(
                            Round(2, "longmetricvi", "long metric vi", true, true, listOf()),
                            RoundArrowCount(2, 1, 122f, 36),
                            RoundArrowCount(2, 2, 122f, 36),
                            RoundDistance(2, 1, 1, 40),
                            RoundDistance(2, 2, 1, 30)
                    ),
                    listOf(
                            // Sparse high score end
                            ScoreHandicapMapping(719, 3),
                            ScoreHandicapMapping(718, 5),
                            ScoreHandicapMapping(717, 7),
                            ScoreHandicapMapping(716, 8),
                            ScoreHandicapMapping(715, 9),
                            // Mid range
                            ScoreHandicapMapping(340, 70),
                            ScoreHandicapMapping(323, 71),
                            ScoreHandicapMapping(306, 72),
                            ScoreHandicapMapping(289, 73),
                            ScoreHandicapMapping(272, 74)
                    ),
                    true
            ),
            HandicapTableEntry(
                    listOf(
                            Round(3, "longmetricgents", "long metric gents", true, true, listOf()),
                            RoundArrowCount(3, 1, 122f, 36),
                            RoundArrowCount(3, 2, 122f, 36),
                            RoundDistance(3, 1, 1, 90),
                            RoundDistance(3, 2, 1, 70)
                    ),
                    listOf(
                            // Sparse low score end
                            ScoreHandicapMapping(5, 83),
                            ScoreHandicapMapping(4, 84),
                            ScoreHandicapMapping(3, 86),
                            ScoreHandicapMapping(2, 89),
                            ScoreHandicapMapping(1, 95)
                    ),
                    true
            ),
            /*
             * Vegas
             */
            // Recurve
            HandicapTableEntry(
                    listOf(
                            Round(4, "vegas", "vegas", false, true, listOf("vegas")),
                            RoundArrowCount(4, 1, 40f, 60),
                            RoundDistance(4, 1, 1, 18)
                    ),
                    listOf(
                            ScoreHandicapMapping(294, 55),
                            ScoreHandicapMapping(280, 56),
                            ScoreHandicapMapping(267, 57),
                            ScoreHandicapMapping(254, 58),
                            ScoreHandicapMapping(241, 59)
                    ),
                    false
            ),
            // Compound
            HandicapTableEntry(
                    listOf(
                            Round(4, "vegas", "vegas", false, true, listOf("vegas")),
                            RoundArrowCount(4, 1, 40f, 60),
                            RoundDistance(4, 1, 1, 18)
                    ),
                    listOf(
                            ScoreHandicapMapping(292, 55),
                            ScoreHandicapMapping(278, 56),
                            ScoreHandicapMapping(265, 57),
                            ScoreHandicapMapping(252, 58),
                            ScoreHandicapMapping(240, 59)
                    ),
                    true
            ),
            /*
             * Worcester
             */
            HandicapTableEntry(
                    listOf(
                            Round(5, "worcester", "worcester", false, false, listOf(), fiveArrowEnd = true),
                            RoundArrowCount(5, 1, 16 * 2.54f, 60),
                            RoundDistance(5, 1, 1, 20)
                    ),
                    listOf(
                            ScoreHandicapMapping(241, 45),
                            ScoreHandicapMapping(237, 46),
                            ScoreHandicapMapping(234, 47),
                            ScoreHandicapMapping(230, 48),
                            ScoreHandicapMapping(226, 49)
                    ),
                    false
            )
    )

    @Test
    fun testHandicapCalculations() {
        for (hcEntry in handicapTableEntries) {
            var previous: ScoreHandicapMapping? = null
            for (mapping in hcEntry.mappings) {
                // If the handicaps differ by one but the scores differ by more than one, test all values in the score
                //      range to ensure the worst handicap is always given
                val range = when (mapping.handicap) {
                    previous?.handicap?.plus(1) -> (previous.score + 1)..mapping.score
                    else -> mapping.score.rangeTo(mapping.score)
                }
                for (score in range) {
                    Assert.assertEquals(
                            "Incorrect handicap for '${hcEntry.round.displayName}' score $score",
                            mapping.handicap,
                            Handicap.getHandicapForRound(
                                    hcEntry.round,
                                    hcEntry.arrowCounts,
                                    hcEntry.distances,
                                    score,
                                    hcEntry.useInnerTen,
                                    null,
                            ).roundHandicap()
                    )
                }
                previous = mapping
            }
        }
    }

    @Test
    fun testScoreCalculations() {
        for (hcEntry in handicapTableEntries) {
            var previous: ScoreHandicapMapping? = null
            for (mapping in hcEntry.mappings) {
                val delta = if (hcEntry.round.displayName.contains("worcester", ignoreCase = true)) 1.0 else 0.0

                // If the scores differ by one but the handicaps differ by more than one, test all values in the
                //      handicap to ensure the correct score is always given
                val startRangeAt = when (mapping.score) {
                    previous?.score?.plus(1) -> (previous.handicap + 1)
                    else -> mapping.handicap
                }

                for (handicap in startRangeAt..mapping.handicap) {
                    Assert.assertEquals(
                            "Incorrect score for '${hcEntry.round.displayName}' handicap $handicap",
                            mapping.score.toDouble(),
                            Handicap.getScoreForRound(
                                    hcEntry.round,
                                    hcEntry.arrowCounts,
                                    hcEntry.distances,
                                    handicap.toFloat(),
                                    hcEntry.useInnerTen,
                                    null
                            ).toDouble(),
                            delta
                    )
                }
                previous = mapping
            }
        }
    }

    @Test
    fun testPartialHandicap() {
        // TODO_CURRENT testPartialHandicap
    }

    @Test
    fun test2023Handicaps() {
        // TODO_CURRENT test2023Handicaps
    }
}
