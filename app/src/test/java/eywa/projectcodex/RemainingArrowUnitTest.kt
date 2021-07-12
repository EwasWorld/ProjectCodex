package eywa.projectcodex

import eywa.projectcodex.TestData.Companion.MAX_ARROW_COUNT_ARROWS
import eywa.projectcodex.components.archerRoundScore.inputEnd.RemainingArrows
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import org.junit.Assert.assertEquals
import org.junit.Test

class RemainingArrowUnitTest {
    private val arrowsPerArrowCount = 10
    private val arrowCounts = listOf(
            RoundArrowCount(1, 1, 1.0, arrowsPerArrowCount),
            RoundArrowCount(1, 2, 1.0, arrowsPerArrowCount),
            RoundArrowCount(1, 3, 1.0, arrowsPerArrowCount),
            RoundArrowCount(1, 4, 1.0, arrowsPerArrowCount),
            RoundArrowCount(1, 5, 1.0, arrowsPerArrowCount)
    )
    private val distances =
            TestData.generateDistances(1, arrowCounts.size, arrowCounts.size).filter { it.subTypeId == 1 }

    @Test
    fun getRemainingArrowsPerDistance_General1() {
        checkOutputOfGetRemainingArrowsPerDistance(5, arrowCounts, distances)
    }

    @Test
    fun getRemainingArrowsPerDistance_General2() {
        checkOutputOfGetRemainingArrowsPerDistance(34, arrowCounts, distances)
    }

    @Test
    fun getRemainingArrowsPerDistance_General3() {
        checkOutputOfGetRemainingArrowsPerDistance(10, arrowCounts, distances)
    }

    @Test
    fun getRemainingArrowsPerDistance_General4() {
        checkOutputOfGetRemainingArrowsPerDistance(0, arrowCounts, distances)
    }

    @Test
    fun getRemainingArrowsPerDistance_NoArrows() {
        val returned = RemainingArrows(0, listOf(), listOf(), "foo").toString("bar")
        assertEquals(Pair("", ""), returned)
    }

    @Test
    fun getRemainingArrowsPerDistance_HigherCurrentCount() {
        val size = 1
        val arrowCounts = TestData.generateArrowCounts(1, size, size)
        val distances = TestData.generateDistances(1, size, size)

        val returned = RemainingArrows(MAX_ARROW_COUNT_ARROWS + 10, arrowCounts, distances, "foo").toString("bar")
        assertEquals(Pair("", ""), returned)
    }

    private fun checkOutputOfGetRemainingArrowsPerDistance(
            currentArrowCount: Int, arrowCounts: List<RoundArrowCount>, distances: List<RoundDistance>
    ) {
        val at = "foo"
        val unit = "bar"

        val returned = RemainingArrows(currentArrowCount, arrowCounts, distances, unit).toString(at)

        var total = 0
        val strings = mutableListOf<String>()
        for (arrowCount in arrowCounts) {
            val distance = distances.find { it.distanceNumber == arrowCount.distanceNumber }!!.distance
            if (total + arrowCount.arrowCount > currentArrowCount) {
                if (total < currentArrowCount) {
                    strings.add(
                            String.format("%s $at %d$unit", total + arrowCount.arrowCount - currentArrowCount, distance)
                    )
                }
                else {
                    strings.add(String.format("%s $at %d$unit", arrowCount.arrowCount, distance))
                }
            }
            total += arrowCount.arrowCount
        }

        assertEquals(strings[0], returned.first)
        strings.removeAt(0)
        assertEquals(strings.joinToString(", "), returned.second)
    }
}
