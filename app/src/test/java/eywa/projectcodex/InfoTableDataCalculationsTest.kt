package eywa.projectcodex

import android.content.res.Resources
import com.nhaarman.mockitokotlin2.*
import eywa.projectcodex.database.entities.ArrowValue
import eywa.projectcodex.infoTable.calculateScorePadTableData
import eywa.projectcodex.infoTable.calculateViewRoundsTableData
import eywa.projectcodex.infoTable.generateNumberedRowHeaders
import eywa.projectcodex.infoTable.getColumnHeadersForTable
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import org.mockito.Mockito.`when`
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil

class InfoTableDataCalculationsTest {
    private val dateFormat = SimpleDateFormat("dd/MM/yy HH:mm", Locale.UK)

    @Test
    fun testGetColumnHeaders() {
        var headerIds = listOf(1, 4, 7, -1, 12)
        val goldsType = GoldsType.TENS

        for (testGoldsType in GoldsType.values()) {
            val resources = mock<Resources>()
            getColumnHeadersForTable(headerIds, resources, testGoldsType)
            argumentCaptor<Int>().apply {
                verify(resources, times(5)).getString(capture())
                for (i in allValues.indices) {
                    if (headerIds[i] == -1) {
                        assertEquals(testGoldsType.colHeaderStringId, allValues[i])
                    }
                    else {
                        assertEquals(headerIds[i], allValues[i])
                    }
                }
            }
        }

        // Delete column added
        val resources = mock<Resources>()
        getColumnHeadersForTable(headerIds, resources, goldsType, true)
        headerIds = headerIds.plus(R.string.table_delete)
        argumentCaptor<Int>().apply {
            verify(resources, times(6)).getString(capture())
            for (i in allValues.indices) {
                if (headerIds[i] == -1) {
                    assertEquals(goldsType.colHeaderStringId, allValues[i])
                }
                else {
                    assertEquals(headerIds[i], allValues[i])
                }
            }
        }

        try {
            getColumnHeadersForTable(listOf(), resources, goldsType)
            fail("Create column headers with no data")
        }
        catch (e: IllegalArgumentException) {
        }

        try {
            getColumnHeadersForTable(listOf(-1), resources)
            fail("Golds placeholder and no goldsType given")
        }
        catch (e: IllegalArgumentException) {
        }

        // Should not throw exception
        getColumnHeadersForTable(listOf(1), resources)
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

        val resources = mock<Resources>()
        try {
            calculateViewRoundsTableData(
                    listOf(),
                    TestData.generateArrowValues(3, 1),
                    GoldsType.TENS,
                    resources
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
        val delete = "Delete"

        val resources = mock<Resources>()
        `when`(resources.getString(any())).thenAnswer { invocation ->
            when (invocation.getArgument<Int>(0)) {
                R.string.short_boolean_true -> yes
                R.string.short_boolean_false -> no
                R.string.table_delete -> delete
                else -> ""
            }
        }

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
        assertEquals(generatedArcherRounds.size, viewRoundsData.size)

        for (i in sortedGenArcherRounds.indices) {
            val archerRound = sortedGenArcherRounds[i]
            val arrows = generatedArrows[i]
            val data = viewRoundsData[i]

            if (data.size != 7) {
                assertEquals(6, data.size)
            }
            else {
                assertEquals("delete$i", data[6].id)
                assertEquals(delete, data[6].content)
            }

            val expected = mutableListOf<Any>()
            expected.add(archerRound.archerRoundId)
            expected.add(dateFormat.format(archerRound.dateShot))
            expected.add(arrows.count { it.score != 0 })
            expected.add(arrows.sumBy { it.score })
            expected.add(arrows.count { goldsType.isGold(it) })
            expected.add(if (archerRound.countsTowardsHandicap) yes else no)

            for (j in expected.indices.filterIndexed { k, _ -> !removedColumnIndexes.contains(k) }) {
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