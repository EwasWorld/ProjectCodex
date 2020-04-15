package eywa.projectcodex

import eywa.projectcodex.database.entities.*
import org.junit.Assert
import java.sql.Date
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class TestData {
    companion object {
        val ARCHERS = arrayOf(
                Archer(1, "Tony"),
                Archer(2, "Jeff")
        )
        val ARROWS = arrayOf(
                Arrow(0, false), Arrow(1, false), Arrow(2, false), Arrow(3, false), Arrow(4, false), Arrow(5, false),
                Arrow(6, false), Arrow(7, false), Arrow(8, false), Arrow(9, false), Arrow(10, false), Arrow(10, true)
        )
        val ROUND_SUB_TYPES = listOf(
                RoundInfo.RoundInfoSubType(1, "1", null, null),
                RoundInfo.RoundInfoSubType(2, "2", 0, 0),
                RoundInfo.RoundInfoSubType(3, "3", 12, 14)
        )
        val ROUND_ARROW_COUNTS = listOf(
                RoundInfo.RoundInfoArrowCount(1, 122.0, 48),
                RoundInfo.RoundInfoArrowCount(2, 122.0, 36),
                RoundInfo.RoundInfoArrowCount(3, 60.0, 24)
        )

        private val CORE_ARROWS = arrayOf(ARROWS[5], ARROWS[11], ARROWS[0], ARROWS[10])
        private val CORE_ROUND_NAMES = arrayOf("WA 1440", "St. George", "National", "York/Hereford", "WA 70m")
        private val CORE_ROUND_FACES = arrayOf("NO_TRIPLE", "FIVE_ZONE", "FIVE_CENTRE", "VEGAS")

        /**
         * @param size the number of arrows to generate
         * @return a list of randomly generated arrows containing at least a 5, an X, a miss, and a 10 (size permitting)
         */
        fun generateArrows(size: Int = 4): List<Arrow> {
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
            require(size >= 0)
            if (size == 0) {
                return listOf()
            }
            require(numberOfArchers >= 0)

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

        fun generateRounds(size: Int = 6): List<Round> {
            require(size >= 0)
            if (size == 0) {
                return listOf()
            }

            val rounds = mutableListOf<Round>()

            var roundId = 1
            rounds.add(
                    Round(
                            roundId, formatNameString(CORE_ROUND_NAMES[roundId - 1]), CORE_ROUND_NAMES[roundId - 1],
                            true, false, listOf(), false, false
                    )
            )
            roundId++
            rounds.add(
                    Round(
                            roundId, formatNameString(CORE_ROUND_NAMES[roundId - 1]), CORE_ROUND_NAMES[roundId - 1],
                            true, true, listOf(), false, false
                    )
            )
            roundId++
            rounds.add(
                    Round(
                            roundId, formatNameString(CORE_ROUND_NAMES[roundId - 1]), CORE_ROUND_NAMES[roundId - 1],
                            false, false, listOf(), false, false
                    )
            )
            roundId++
            rounds.add(
                    Round(
                            roundId, formatNameString(CORE_ROUND_NAMES[roundId - 1]), CORE_ROUND_NAMES[roundId - 1],
                            false, true, listOf(), true, false
                    )
            )
            roundId++
            rounds.add(
                    Round(
                            roundId, formatNameString(CORE_ROUND_NAMES[roundId - 1]), CORE_ROUND_NAMES[roundId - 1],
                            false, true, listOf(), true, true
                    )
            )
            roundId++
            rounds.add(
                    Round(roundId, formatNameString("00"), "00", false, true, CORE_ROUND_FACES.asList(), true, false)
            )

            while (rounds.size < size) {
                var name: String
                do {
                    name = Random.nextInt(1000).toString()
                } while (rounds.find { formatNameString(it.displayName) == name } != null)

                roundId++
                rounds.add(
                        Round(
                                roundId, name, name, Random.nextBoolean(), Random.nextBoolean(), listOf(),
                                Random.nextBoolean(), Random.nextBoolean()
                        )
                )
            }

            return rounds.subList(0, size)
        }

        /**
         * @return a list of RoundInfoDistances with 1-[arrowCountsCount] distance numbers and 1-[subTypeCountIn] sub
         * types ensuring that as distancesNumber increases, distance decreases
         */
        private fun generateDistanceSet(arrowCountsCount: Int, subTypeCountIn: Int): List<RoundInfo.RoundInfoDistance> {
            require(arrowCountsCount > 0) { "Must have at least one arrow count" }
            require(subTypeCountIn >= 0) { "Sub type count cannot be negative" }
            val subTypeCount = max(1, subTypeCountIn)

            val distancesLengths = mutableListOf<Int>()
            while (distancesLengths.size < arrowCountsCount * subTypeCount) {
                distancesLengths.add(Random.nextInt(150))
            }
            distancesLengths.sortDescending()

            val distanceLengthsIt = distancesLengths.iterator()
            val distances = mutableListOf<RoundInfo.RoundInfoDistance>()
            for (distanceNumber in 1..arrowCountsCount) {
                for (subTypeId in 1..subTypeCount) {
                    check(distanceLengthsIt.hasNext()) { "Insufficient distance values generated" }
                    distances.add(RoundInfo.RoundInfoDistance(distanceNumber, subTypeId, distanceLengthsIt.next()))
                }
            }

            return distances
        }

        /**
         * @param sets the number of arrow count sets to create (each set contains 1-[maxSetSize] arrow counts and has a
         * unique roundId)
         */
        fun generateArrowCounts(sets: Int = 1, maxSetSize: Int = 3): List<RoundArrowCount> {
            require(sets >= 0)
            if (sets == 0) {
                return listOf()
            }

            require(maxSetSize > 0)
            val arrowCounts =
                ROUND_ARROW_COUNTS.map { it.toRoundArrowCount(1) }.subList(0, min(ROUND_ARROW_COUNTS.size, maxSetSize))
                        .toMutableList()
            if (sets == 1) {
                return arrowCounts
            }

            for (set in 2..sets) {
                for (distanceNumber in 0..Random.nextInt(maxSetSize)) {
                    arrowCounts.add(
                            RoundArrowCount(set, distanceNumber + 1, Random.nextDouble(122.0), Random.nextInt(60))
                    )
                }
            }

            check(arrowCounts.map { it.roundId }.distinct().size == sets) { "Arrow counts size incorrect" }
            return arrowCounts
        }

        /**
         * @param sets the number of sub type sets to create (each set contains 1-[maxSetSize] sub types and has a
         * unique roundId)
         */
        fun generateSubTypes(sets: Int = 1, maxSetSize: Int = 3): List<RoundSubType> {
            require(sets >= 0)
            if (sets == 0) {
                return listOf()
            }

            require(maxSetSize > 0)
            val subTypes =
                ROUND_SUB_TYPES.map { it.toRoundSubType(1) }.subList(0, min(ROUND_SUB_TYPES.size, maxSetSize))
                        .toMutableList()
            if (sets == 1) {
                return subTypes
            }

            for (set in 2..sets) {
                for (subtypeId in 0..Random.nextInt(maxSetSize)) {
                    var name: String
                    do {
                        name = Random.nextInt(1000).toString()
                    } while (subTypes.find { it.name == name } != null)

                    var gents: Int? = Random.nextInt(20)
                    if (gents == 19) {
                        gents = null
                    }
                    var ladies: Int? = Random.nextInt(20)
                    if (ladies == 19) {
                        ladies = null
                    }

                    subTypes.add(RoundSubType(set, subtypeId + 1, name, gents, ladies))
                }
            }

            check(subTypes.map { it.roundId }.distinct().size == sets) { "Sub types size incorrect" }
            return subTypes
        }

        /**
         * @param sets the number of distance sets to create (each set contains 1-[maxSetSize] arrow counts and
         * 1-[maxSetSize] sub types and has a unique roundId)
         * @see generateDistanceSet
         */
        fun generateDistances(sets: Int = 1, maxSetSize: Int = 3): List<RoundDistance> {
            require(sets >= 0)
            if (sets == 0) {
                return listOf()
            }
            require(maxSetSize > 0)

            val distances = mutableListOf<RoundDistance>()
            for (set in 1..sets) {
                distances.addAll(generateDistanceSet(
                        Random.nextInt(maxSetSize - 1) + 1, Random.nextInt(maxSetSize)
                ).map { it.toRoundDistance(set) })
            }

            check(distances.map { it.roundId }.distinct().size == sets) { "Distances size incorrect" }
            return distances
        }

        fun assertEquals(one: List<Any>, two: List<Any>) {
            assert(one.toSet().containsAll(two.toSet()))
            Assert.assertEquals(one.size, two.size)
        }
    }
}