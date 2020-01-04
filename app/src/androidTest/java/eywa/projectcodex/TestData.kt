package eywa.projectcodex

import eywa.projectcodex.database.entities.Archer
import eywa.projectcodex.database.entities.ArcherRound
import eywa.projectcodex.database.entities.ArrowValue
import java.sql.Date


class TestData {
    companion object {
        val ARCHERS = arrayOf(
                Archer(1, "Tony"),
                Archer(2, "Jeff")
        )
        val ARCHER_ROUNDS = arrayOf(
                ArcherRound(1, Date.valueOf("2019-12-22"), 1, false),
                ArcherRound(2, Date.valueOf("2019-12-23"), 1, false),
                ArcherRound(3, Date.valueOf("2019-12-24"), 1, true),
                ArcherRound(4, Date.valueOf("2019-12-25"), 1, false),
                ArcherRound(5, Date.valueOf("2019-12-26"), 2, true),
                ArcherRound(6, Date.valueOf("2019-12-27"), 2, false)
        )
        val ARROWS = arrayOf(
                Arrow(0, false),
                Arrow(1, false),
                Arrow(2, false),
                Arrow(3, false),
                Arrow(4, false),
                Arrow(5, false),
                Arrow(6, false),
                Arrow(7, false),
                Arrow(8, false),
                Arrow(9, false),
                Arrow(10, false),
                Arrow(10, true)
        )

        private val CORE_ARROWS = arrayOf(ARROWS[5], ARROWS[11], ARROWS[0], ARROWS[10])

        fun generateArrowValues(size: Int, archerRoundId: Int): List<ArrowValue> {
            check(size > 0)

            // Ensure specific arrow numbers get in
            var arrows = CORE_ARROWS.toMutableList()

            // Fill out or reduce list
            if (arrows.size >= size) {
                arrows = arrows.subList(0, size)
            }
            else {
                while (arrows.size < size) {
                    arrows.add(ARROWS.random())
                }
            }

            var arrowNumber = 0
            return arrows.map { it.toArrowValue(archerRoundId, arrowNumber++) }.shuffled()
        }
    }
}