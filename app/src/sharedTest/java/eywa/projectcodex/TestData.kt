package eywa.projectcodex

import eywa.projectcodex.database.entities.Archer
import eywa.projectcodex.database.entities.ArcherRound
import eywa.projectcodex.database.entities.ArrowValue
import java.sql.Date
import kotlin.math.min
import kotlin.random.Random

class TestData {
    companion object {
        val ARCHERS = arrayOf(
                Archer(1, "Tony"),
                Archer(2, "Jeff")
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

        /**
         * @param size the number of arrows to generate
         * @return a list of randomly generated arrows containing at least a 5, an X, a miss, and a 10 (size permitting)
         */
        fun generateArrows(size: Int): List<Arrow> {
            require(size >= 0)
            if (size == 0) {
                return listOf()
            }

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

            arrows.shuffle()
            return arrows
        }

        /**
         * @see generateArrows
         */
        fun generateArrowValues(size: Int, archerRoundId: Int): List<ArrowValue> {
            var arrowNumber = 0
            return generateArrows(size).map { it.toArrowValue(archerRoundId, arrowNumber++) }
        }

        /**
         * @param size the number of ArcherRounds to generate
         * @param numberOfArchers the number of Archers to spread the rounds across
         * @return a list of randomly generated ArcherRounds containing at least one round for each archer
         * (size permitting). List is sorted by date (roundIds and archerIds start at 1)
         * @see generateDate
         */
        fun generateArcherRounds(size: Int, numberOfArchers: Int): List<ArcherRound> {
            val dates = List(size) { generateDate() }.sorted()

            val archerRounds = mutableListOf<ArcherRound>()
            var roundId = 1
            for (i in 0 until min(numberOfArchers, size)) {
                archerRounds.add(ArcherRound(roundId, dates[roundId - 1], i + 1, Random.nextBoolean()))
                roundId++
            }

            while (archerRounds.size < size) {
                archerRounds.add(
                        ArcherRound(
                                roundId,
                                dates[roundId - 1],
                                Random.nextInt(numberOfArchers) + 1,
                                Random.nextBoolean()
                        )
                )
                roundId++
            }
            return archerRounds
        }

        /**
         * @return a valid date in the given year (will never return 31st of a month or 29th Feb), time 00:00
         */
        private fun generateDate(year: Int = 2019): Date {
            val month = Random.nextInt(12) + 1
            var day = Random.nextInt(30) + 1
            if (month == 2 && day > 28) {
                day = Random.nextInt(28) + 1
            }
            return Date.valueOf("$year-$month-$day")
        }
    }
}