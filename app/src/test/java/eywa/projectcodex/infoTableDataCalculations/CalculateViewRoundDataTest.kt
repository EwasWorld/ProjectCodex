package eywa.projectcodex.infoTableDataCalculations

import android.content.res.Resources
import eywa.projectcodex.R
import eywa.projectcodex.TestData
import eywa.projectcodex.components.archeryObjects.GoldsType
import eywa.projectcodex.components.archeryObjects.getGoldsType
import eywa.projectcodex.components.infoTable.calculateViewRoundsTableData
import eywa.projectcodex.database.archerRound.ArcherRoundWithRoundInfoAndName
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.rounds.Round
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.text.SimpleDateFormat
import java.util.*

class CalculateViewRoundDataTest {
    private val yes = "Y"
    private val no = "N"
    private val delete = "Delete"
    private val dateFormat = SimpleDateFormat("dd/MM/yy HH:mm", Locale.UK)
    private lateinit var resources: Resources

    @Before
    fun setUp() {
        resources = mock(Resources::class.java)
        `when`(resources.getString(anyInt())).thenAnswer { invocation ->
            when (invocation.getArgument<Int>(0)) {
                R.string.short_boolean_true -> yes
                R.string.short_boolean_false -> no
                else -> Assert.fail("Bad string passed to resources")
            }
        }
    }

    @Test
    fun testGoldsTypes() {
        for (testGoldsType in GoldsType.values()) {
            checkViewRoundsData(listOf(36, 144), testGoldsType)
        }
    }

    @Test
    fun testDifferentRoundSizes() {
        for (testArrowsSizes in listOf(listOf(36), listOf(36, 144), listOf(36, 0), listOf(6, 36, 45, 144, 60, 60))) {
            checkViewRoundsData(testArrowsSizes, GoldsType.TENS)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun testNoData() {
        calculateViewRoundsTableData(listOf(), TestData.generateArrowValues(3, 1), GoldsType.TENS, resources)
        Assert.fail("Create table data with no data")
    }


    /**
     * @param arrowsSizes size of list is how many rounds to test, value is number of arrows for that round
     */
    private fun checkViewRoundsData(arrowsSizes: List<Int>, goldsType: GoldsType) {
        val removedColumnIndexes = listOf(0, 5)

        var currentName = 1
        val rounds = listOf(
                Round(1, "round1", "Round1", true, true, listOf()),
                Round(2, "round2", "Round2", true, false, listOf()),
                Round(3, "round3", "Round3", false, true, listOf()),
                null
        )
        val generatedArcherRounds = TestData.generateArcherRounds(arrowsSizes.size, 1).mapIndexed { i, round ->
            val roundSubTypeName = if (i % 2 == 0) currentName++.toString() else null
            ArcherRoundWithRoundInfoAndName(round, rounds[i % rounds.size], roundSubTypeName)
        }
        val sortedGenArcherRounds = generatedArcherRounds.sortedByDescending { it.archerRound.dateShot }
        val generatedArrows = mutableListOf<List<ArrowValue>>()
        for (round in sortedGenArcherRounds) {
            val originalIndex = generatedArcherRounds.indexOf(round)
            generatedArrows.add(
                    TestData.generateArrowValues(arrowsSizes[originalIndex], round.archerRound.archerRoundId)
            )
        }
        val viewRoundsData =
            calculateViewRoundsTableData(
                    generatedArcherRounds,
                    generatedArrows.flatten(),
                    goldsType,
                    resources
            )
        Assert.assertEquals(generatedArcherRounds.size, viewRoundsData.size)

        for (i in sortedGenArcherRounds.indices) {
            val archerRound = sortedGenArcherRounds[i].archerRound
            val arrows = generatedArrows[i]
            val data = viewRoundsData[i]
            Assert.assertEquals(7, data.size)

            val expected = mutableListOf<Any>()
            expected.add(archerRound.archerRoundId)
            expected.add(dateFormat.format(archerRound.dateShot))
            expected.add(sortedGenArcherRounds[i].roundSubTypeName ?: sortedGenArcherRounds[i].round?.displayName ?: "")
            expected.add(arrows.count { it.score != 0 })
            expected.add(arrows.sumBy { it.score })
            val expectedGoldsType =
                    rounds[i % rounds.size]?.let {
                        getGoldsType(
                                it.isOutdoor, it.isMetric
                        )
                    } ?: goldsType
            expected.add(arrows.count { expectedGoldsType.isGold(it) })
            expected.add(if (archerRound.countsTowardsHandicap) yes else no)

            for (j in expected.indices.filterIndexed { k, _ -> !removedColumnIndexes.contains(k) }) {
                Assert.assertEquals("cell$i$j", data[j].id)
                Assert.assertEquals(expected[j], data[j].content)
            }
        }
    }
}