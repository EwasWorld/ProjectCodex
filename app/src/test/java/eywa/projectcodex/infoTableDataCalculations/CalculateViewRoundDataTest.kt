package eywa.projectcodex.infoTableDataCalculations

import android.content.res.Resources
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import eywa.projectcodex.GoldsType
import eywa.projectcodex.R
import eywa.projectcodex.TestData
import eywa.projectcodex.database.entities.ArrowValue
import eywa.projectcodex.infoTable.calculateViewRoundsTableData
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
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
        resources = mock()
        Mockito.`when`(resources.getString(any())).thenAnswer { invocation ->
            when (invocation.getArgument<Int>(0)) {
                R.string.short_boolean_true -> yes
                R.string.short_boolean_false -> no
                R.string.table_delete -> delete
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

        val generatedArcherRounds = TestData.generateArcherRounds(arrowsSizes.size, 1)
        val sortedGenArcherRounds = generatedArcherRounds.sortedByDescending { it.dateShot }
        val generatedArrows = mutableListOf<List<ArrowValue>>()
        for (round in sortedGenArcherRounds) {
            val originalIndex = generatedArcherRounds.indexOf(round)
            generatedArrows.add(TestData.generateArrowValues(arrowsSizes[originalIndex], round.archerRoundId))
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
            val archerRound = sortedGenArcherRounds[i]
            val arrows = generatedArrows[i]
            val data = viewRoundsData[i]

            if (data.size != 7) {
                Assert.assertEquals(6, data.size)
            }
            else {
                Assert.assertEquals("delete$i", data[6].id)
                Assert.assertEquals(delete, data[6].content)
            }

            val expected = mutableListOf<Any>()
            expected.add(archerRound.archerRoundId)
            expected.add(dateFormat.format(archerRound.dateShot))
            expected.add(arrows.count { it.score != 0 })
            expected.add(arrows.sumBy { it.score })
            expected.add(arrows.count { goldsType.isGold(it) })
            expected.add(if (archerRound.countsTowardsHandicap) yes else no)

            for (j in expected.indices.filterIndexed { k, _ -> !removedColumnIndexes.contains(k) }) {
                Assert.assertEquals("cell$i$j", data[j].id)
                Assert.assertEquals(expected[j], data[j].content)
            }
        }
    }
}