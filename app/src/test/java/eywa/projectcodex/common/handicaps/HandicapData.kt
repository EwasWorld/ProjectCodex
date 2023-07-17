package eywa.projectcodex.common.handicaps

import eywa.projectcodex.common.handicaps.HandicapUnitTest.HandicapTableEntry
import eywa.projectcodex.common.handicaps.HandicapUnitTest.ScoreHandicapMapping
import eywa.projectcodex.database.RoundFace
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance

object HandicapData {
        val davidLaneHandicapEntries = listOf(
                /*
                 * Imperial
                 */
                HandicapTableEntry(
                        listOf(
                                Round(1, "western", "western", true, false),
                                RoundArrowCount(1, 1, 122.0, 48),
                                RoundArrowCount(1, 2, 122.0, 48),
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
                ),
                /*
                 * Metric
                 */
                HandicapTableEntry(
                        listOf(
                                Round(2, "longmetricvi", "long metric vi", true, true),
                                RoundArrowCount(2, 1, 122.0, 36),
                                RoundArrowCount(2, 2, 122.0, 36),
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
                ),
                HandicapTableEntry(
                        listOf(
                                Round(3, "longmetricgents", "long metric gents", true, true),
                                RoundArrowCount(3, 1, 122.0, 36),
                                RoundArrowCount(3, 2, 122.0, 36),
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
                ),
                /*
                 * Vegas Triple
                 */
                // Recurve
                HandicapTableEntry(
                        listOf(
                                Round(4, "vegas", "vegas", false, true),
                                RoundArrowCount(4, 1, 40.0, 60),
                                RoundDistance(4, 1, 1, 18)
                        ),
                        listOf(
                                ScoreHandicapMapping(596, 0),
                                ScoreHandicapMapping(592, 4),
                                ScoreHandicapMapping(294, 55),
                                ScoreHandicapMapping(280, 56),
                                ScoreHandicapMapping(267, 57),
                                ScoreHandicapMapping(254, 58),
                                ScoreHandicapMapping(241, 59),
                                ScoreHandicapMapping(10, 98),
                        ),
                        false,
                        RoundFace.TRIPLE,
                ),
                // Compound
                HandicapTableEntry(
                        listOf(
                                Round(4, "vegas comp", "vegas comp", false, true),
                                RoundArrowCount(4, 1, 40.0, 60),
                                RoundDistance(4, 1, 1, 18)
                        ),
                        listOf(
                                ScoreHandicapMapping(576, 0),
                                ScoreHandicapMapping(570, 4),
                                ScoreHandicapMapping(292, 55),
                                ScoreHandicapMapping(278, 56),
                                ScoreHandicapMapping(265, 57),
                                ScoreHandicapMapping(252, 58),
                                ScoreHandicapMapping(240, 59),
                                ScoreHandicapMapping(10, 98),
                        ),
                        true,
                        RoundFace.TRIPLE,
                ),
                /*
                 * Worcester
                 */
                HandicapTableEntry(
                        listOf(
                                Round(5, "worcester", "worcester", false, false, fiveArrowEnd = true),
                                RoundArrowCount(5, 1, 16.0 * 2.54, 60),
                                RoundDistance(5, 1, 1, 20)
                        ),
                        listOf(
                                ScoreHandicapMapping(241, 45),
                                ScoreHandicapMapping(237, 46),
                                ScoreHandicapMapping(234, 47),
                                ScoreHandicapMapping(230, 48),
                                ScoreHandicapMapping(226, 49)
                        ),
                ),
                /*
                 * Worcester
                 */
                HandicapTableEntry(
                        listOf(
                                Round(5, "worcester  five", "worcester five", false, false, fiveArrowEnd = true),
                                RoundArrowCount(5, 1, 16.0 * 2.54, 60),
                                RoundDistance(5, 1, 1, 20)
                        ),
                        listOf(
                                ScoreHandicapMapping(199, 45),
                                ScoreHandicapMapping(192, 46),
                                ScoreHandicapMapping(185, 47),
                                ScoreHandicapMapping(178, 48),
                                ScoreHandicapMapping(170, 49)
                        ),
                        face = RoundFace.WORCESTER_FIVE,
                ),
        )

        val archerGb2023HandicapTableEntries = listOf(
                /*
                 * Imperial
                 */
                HandicapTableEntry(
                        listOf(
                                Round(1, "western", "western", true, false),
                                RoundArrowCount(1, 1, 122.0, 48),
                                RoundArrowCount(1, 2, 122.0, 48),
                                RoundDistance(1, 1, 1, 60),
                                RoundDistance(1, 2, 1, 50)
                        ),
                        listOf(
                                // Sparse high score end
                                ScoreHandicapMapping(864, 9),
                                ScoreHandicapMapping(863, 12),
                                ScoreHandicapMapping(862, 13),
                                ScoreHandicapMapping(861, 14),
                                ScoreHandicapMapping(859, 16),
                                ScoreHandicapMapping(858, 17),
                                // Mid range
                                ScoreHandicapMapping(791, 35),
                                ScoreHandicapMapping(785, 36),
                                ScoreHandicapMapping(779, 37),
                                ScoreHandicapMapping(772, 38),
                                ScoreHandicapMapping(766, 39),
                        ),
                ),
                /*
                 * Metric
                 */
                HandicapTableEntry(
                        listOf(
                                Round(2, "longmetricvi", "long metric iv", true, true),
                                RoundArrowCount(2, 1, 122.0, 36),
                                RoundArrowCount(2, 2, 122.0, 36),
                                RoundDistance(2, 1, 1, 40),
                                RoundDistance(2, 2, 1, 30)
                        ),
                        listOf(
                                // Sparse high score end
                                ScoreHandicapMapping(720, 4),
                                ScoreHandicapMapping(719, 7),
                                ScoreHandicapMapping(718, 9),
                                ScoreHandicapMapping(717, 11),
                                ScoreHandicapMapping(716, 12),
                                ScoreHandicapMapping(715, 13),
                                // Mid range
                                ScoreHandicapMapping(475, 70),
                                ScoreHandicapMapping(466, 71),
                                ScoreHandicapMapping(456, 72),
                                ScoreHandicapMapping(446, 73),
                                ScoreHandicapMapping(435, 74),
                        ),
                ),
                HandicapTableEntry(
                        listOf(
                                Round(3, "longmetricgents", "long metric gents", true, true),
                                RoundArrowCount(3, 1, 122.0, 36),
                                RoundArrowCount(3, 2, 122.0, 36),
                                RoundDistance(3, 1, 1, 90),
                                RoundDistance(3, 2, 1, 70)
                        ),
                        listOf(
                                // Sparse low score end
                                ScoreHandicapMapping(5, 127),
                                ScoreHandicapMapping(4, 131),
                                ScoreHandicapMapping(3, 137),
                                ScoreHandicapMapping(2, 147),
                        ),
                ),
                /*
                 * Vegas Triple
                 */
                // Recurve
                HandicapTableEntry(
                        listOf(
                                Round(4, "vegas", "vegas", false, true),
                                RoundArrowCount(4, 1, 40.0, 60),
                                RoundDistance(4, 1, 1, 18)
                        ),
                        listOf(
                                ScoreHandicapMapping(393, 55),
                                ScoreHandicapMapping(381, 56),
                                ScoreHandicapMapping(369, 57),
                                ScoreHandicapMapping(357, 58),
                                ScoreHandicapMapping(344, 59),
                        ),
                        false,
                        RoundFace.TRIPLE,
                ),
                // Compound
                HandicapTableEntry(
                        listOf(
                                Round(4, "vegas comp", "vegas comp", false, true),
                                RoundArrowCount(4, 1, 40.0, 60),
                                RoundDistance(4, 1, 1, 18)
                        ),
                        listOf(
                                ScoreHandicapMapping(390, 55),
                                ScoreHandicapMapping(378, 56),
                                ScoreHandicapMapping(366, 57),
                                ScoreHandicapMapping(354, 58),
                                ScoreHandicapMapping(342, 59),
                        ),
                        true,
                        RoundFace.TRIPLE,
                ),
                /*
                 * Worcester
                 */
                HandicapTableEntry(
                        listOf(
                                Round(5, "worcester", "worcester", false, false, fiveArrowEnd = true),
                                RoundArrowCount(5, 1, 16 * 2.54, 60),
                                RoundDistance(5, 1, 1, 20)
                        ),
                        listOf(
                                ScoreHandicapMapping(263, 45),
                                ScoreHandicapMapping(260, 46),
                                ScoreHandicapMapping(257, 47),
                                ScoreHandicapMapping(255, 48),
                                ScoreHandicapMapping(252, 49),
                        ),
                )
        )
}
