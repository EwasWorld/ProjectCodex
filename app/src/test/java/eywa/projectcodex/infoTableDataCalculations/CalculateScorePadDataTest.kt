package eywa.projectcodex.infoTableDataCalculations

import android.content.res.Resources
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import eywa.projectcodex.End
import eywa.projectcodex.GoldsType
import eywa.projectcodex.R
import eywa.projectcodex.TestData
import eywa.projectcodex.infoTable.calculateScorePadTableData
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import kotlin.math.ceil

class CalculateScorePadDataTest {
    private val arrowPlaceHolder = "."
    private val arrowDeliminator = "-"
    private val grandTotal = "Grand total:"

    private val size = 36
    private val endSize = 6
    private val goldsType = GoldsType.TENS

    private lateinit var resources: Resources

    @Before
    fun setUp() {
        resources = mock()
        Mockito.`when`(resources.getString(any())).thenAnswer { invocation ->
            when (invocation.getArgument<Int>(0)) {
                R.string.end_to_string_arrow_placeholder -> arrowPlaceHolder
                R.string.end_to_string_arrow_deliminator -> arrowDeliminator
                R.string.score_pad__grand_total -> grandTotal
                else -> Assert.fail("Bad string passed to resources")
            }
        }
    }

    @Test
    fun testDifferentArrowCounts() {
        for (testSize in listOf(3, 36, 60, 144, 5, 7)) {
            checkScorePadData(testSize, endSize, goldsType)
        }
    }

    @Test
    fun testDifferentEndSizes() {
        for (testEndSize in listOf(3, 6, 12, 5)) {
            checkScorePadData(size, testEndSize, goldsType)
        }
    }

    @Test
    fun testDifferentGoldsTypes() {
        for (testGoldsType in GoldsType.values()) {
            checkScorePadData(size, endSize, testGoldsType)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun testNoData() {
        checkScorePadData(0, endSize, goldsType)
        Assert.fail("Create table data with no data")
    }

    @Test(expected = IllegalArgumentException::class)
    fun testZeroEndSize() {
        checkScorePadData(size, 0, goldsType)
        Assert.fail("Create table data with 0 endSize")
    }

    private fun checkScorePadData(size: Int, endSize: Int, goldsType: GoldsType) {
        val generatedArrows = TestData.generateArrowValues(size, 1)
        val scorePadData = calculateScorePadTableData(generatedArrows, endSize, goldsType, resources)

        val chunkedArrows = generatedArrows.chunked(endSize)
        // -1 for grand total
        Assert.assertEquals(ceil(generatedArrows.size / endSize.toDouble()).toInt(), scorePadData.size - 1)
        Assert.assertEquals(chunkedArrows.size, scorePadData.size - 1)

        // Main score pad
        var runningTotal = 0
        for (i in chunkedArrows.indices) {
            val data = scorePadData[i]
            Assert.assertEquals(5, data.size)

            val end = End(chunkedArrows[i], endSize, arrowPlaceHolder, arrowDeliminator)
            end.reorderScores()
            runningTotal += end.getScore()
            val expected = listOf(end.toString(), end.getHits(), end.getScore(), end.getGolds(goldsType), runningTotal)
            for (j in data.indices) {
                Assert.assertEquals("cell$i$j", data[j].id)
                Assert.assertEquals(expected[j], data[j].content)
            }
        }

        // Grand total row
        val totalRow = scorePadData[scorePadData.size - 1]
        val expected = listOf(
                grandTotal,
                generatedArrows.count { it.score != 0 },
                generatedArrows.sumBy { it.score },
                generatedArrows.count { goldsType.isGold(it) },
                "-"
        )
        for (i in totalRow.indices) {
            Assert.assertEquals("grandTotal$i", totalRow[i].id)
            Assert.assertEquals(expected[i], totalRow[i].content)
        }
    }
}