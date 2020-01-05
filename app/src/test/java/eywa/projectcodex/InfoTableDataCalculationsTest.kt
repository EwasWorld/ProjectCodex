package eywa.projectcodex

import eywa.projectcodex.database.entities.ArrowValue
import eywa.projectcodex.infoTable.calculateScorePadTableData
import eywa.projectcodex.infoTable.calculateViewRoundsTableData
import eywa.projectcodex.infoTable.generateNumberedRowHeaders
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil

class InfoTableDataCalculationsTest {
    private val dateFormat = SimpleDateFormat("dd/MM/yy HH:mm", Locale.UK)

    @Test
    fun testScorePadColumnHeaders() {
        // TODO find a way to get or mock Resources
    }

    @Test
    fun testViewRoundsColumnHeaders() {
        // TODO find a way to get or mock Resources
    }

    @Test
    fun testCalculateScorePadTableData() {
        val size = 36
        val endSize = 6
        val goldsType = GoldsType.TENS
        for (testSize in listOf(3, 36, 60, 144, 5, 7)) {
            checkScorePadData(testSize, endSize, goldsType)
        }
        for (testEndSize in listOf(3, 6, 12, 5)) {
            checkScorePadData(size, testEndSize, goldsType)
        }
        for (testGoldsType in GoldsType.values()) {
            checkScorePadData(size, endSize, testGoldsType)
        }

        try {
            checkScorePadData(0, endSize, goldsType)
            fail("Create table data with no data")
        }
        catch (e: IllegalArgumentException) {
        }

        try {
            checkScorePadData(size, 0, goldsType)
            fail("Create table data with 0 endSize")
        }
        catch (e: IllegalArgumentException) {
        }
    }

    private fun checkScorePadData(size: Int, endSize: Int, goldsType: GoldsType) {
        val arrowPHold = "."
        val arrowDelim = "-"

        val generatedArrows = TestData.generateArrowValues(size, 1)
        val scorePadData = calculateScorePadTableData(
                generatedArrows,
                endSize,
                goldsType,
                arrowPHold,
                arrowDelim
        )

        val chunkedArrows = generatedArrows.chunked(endSize)
        assertEquals(ceil(generatedArrows.size / endSize.toDouble()).toInt(), scorePadData.size)
        assertEquals(chunkedArrows.size, scorePadData.size)

        var runningTotal = 0
        for (i in chunkedArrows.indices) {
            val data = scorePadData[i]
            assertEquals(5, data.size)

            val end = End(chunkedArrows[i], endSize, arrowPHold, arrowDelim)
            end.reorderScores()
            runningTotal += end.getScore()
            val expected = listOf(end.toString(), end.getHits(), end.getScore(), end.getGolds(goldsType), runningTotal)
            for (j in data.indices) {
                assertEquals("cell$i$j", data[j].id)
                assertEquals(expected[j], data[j].content)
            }
        }
    }

    @Test
    fun testCalculateViewRoundsTableData() {
        for (testGoldsType in GoldsType.values()) {
            checkViewRoundsData(listOf(36, 144), testGoldsType)
        }
        for (testArrowsSizes in listOf(listOf(36), listOf(36, 144), listOf(36, 0), listOf(6, 36, 45, 144, 60, 60))) {
            checkViewRoundsData(testArrowsSizes, GoldsType.TENS)
        }

        try {
            calculateViewRoundsTableData(
                    listOf(),
                    TestData.generateArrowValues(3, 1),
                    GoldsType.TENS,
                    "Y",
                    "N"
            )
            fail("Create table data with no data")
        }
        catch (e: IllegalArgumentException) {
        }
    }

    /**
     * @param arrowsSizes size of list is how many rounds to test, value is number of arrows for that round
     */
    private fun checkViewRoundsData(arrowsSizes: List<Int>, goldsType: GoldsType) {
        val yes = "Y"
        val no = "N"

        val generatedArcherRounds = TestData.generateArcherRounds(arrowsSizes.size, 1)
        val generatedArrows = mutableListOf<List<ArrowValue>>()
        for ((i, arrowCount) in arrowsSizes.withIndex()) {
            generatedArrows.add(TestData.generateArrowValues(arrowCount, i + 1))
        }
        val viewRoundsData =
            calculateViewRoundsTableData(
                    generatedArcherRounds,
                    generatedArrows.flatten(),
                    goldsType,
                    yes,
                    no
            )
        assertEquals(generatedArcherRounds.size, viewRoundsData.size)

        for (i in generatedArcherRounds.indices) {
            val archerRound = generatedArcherRounds[i]
            val arrows = generatedArrows[i]
            val data = viewRoundsData[i]
            assertEquals(5, data.size)

            val expected = mutableListOf<Any>()
            expected.add(dateFormat.format(archerRound.dateShot))
            expected.add(arrows.count { it.score != 0 })
            expected.add(arrows.sumBy { it.score })
            expected.add(arrows.count { goldsType.isGold(it) })
            expected.add(if (archerRound.countsTowardsHandicap) yes else no)

            for (j in data.indices) {
                assertEquals("cell$i$j", data[j].id)
                assertEquals(expected[j], data[j].content)
            }
        }
    }

    @Test
    fun testGenerateNumberedRowHeaders() {
        for (size in listOf(1, 6, 20)) {
            var rowId = 1
            for (cell in generateNumberedRowHeaders(6)) {
                cell.content = rowId
                cell.id = "row" + rowId++
            }
        }

        try {
            generateNumberedRowHeaders(0)
            fail("Generate no header rows")
        }
        catch (e: IllegalArgumentException) {
        }
    }
}