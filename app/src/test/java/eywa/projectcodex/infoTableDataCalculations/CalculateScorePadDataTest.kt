package eywa.projectcodex.infoTableDataCalculations

import android.content.res.Resources
import eywa.projectcodex.R
import eywa.projectcodex.TestData
import eywa.projectcodex.components.archeryObjects.End
import eywa.projectcodex.components.archeryObjects.GoldsType
import eywa.projectcodex.components.infoTable.InfoTableCell
import eywa.projectcodex.components.infoTable.calculateScorePadTableData
import eywa.projectcodex.components.infoTable.scorePadColumnHeaderIds
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import kotlin.math.ceil

class CalculateScorePadDataTest {
    private val arrowPlaceHolder = "."
    private val arrowDeliminator = "-"
    private val grandTotal = "Grand total:"
    private val runningTotalPlaceholder = "-"

    private val size = 36
    private val endSize = 6
    private val goldsType = GoldsType.TENS

    private lateinit var resources: Resources

    @Before
    fun setUp() {
        resources = mock(Resources::class.java)
        `when`(resources.getString(anyInt())).thenAnswer { invocation ->
            when (invocation.getArgument<Int>(0)) {
                R.string.end_to_string_arrow_placeholder -> arrowPlaceHolder
                R.string.end_to_string_arrow_deliminator -> arrowDeliminator
                R.string.score_pad__grand_total -> grandTotal
                R.string.score_pad__running_total_placeholder -> runningTotalPlaceholder
                R.string.score_pad__distance_total -> "Total at {distance}{unit}"
                R.string.score_pad__surplus_total -> "Surplus Total"
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

    @Test
    fun testDistanceTotals() {
        val arrows = listOf(
                4, 0, 3, 6, 4, 7,
                11, 10, 9, 8, 7, 6,
                7, 8, 11, 6, 10, 9,
                11, 10, 9, 8, 7, 6,
                11, 10, 9, 8, 7, 6,
                11, 10, 9, 8, 7, 6
        ).mapIndexed { arrowNumber, arrowValue -> TestData.ARROWS[arrowValue].toArrowValue(1, arrowNumber) }
        val arrowCounts = listOf(
                RoundArrowCount(1, 1, 10.0, 12),
                RoundArrowCount(1, 2, 10.0, 12),
                RoundArrowCount(1, 3, 10.0, 12)
        )
        val distances = listOf(
                RoundDistance(1, 1, 1, 60),
                RoundDistance(1, 2, 1, 50),
                RoundDistance(1, 3, 1, 40)
        )

        val otherRows = listOf(
                InfoTableCell("X-10-9-8-7-6", "cell<row>0"),
                InfoTableCell(6, "cell<row>1"),
                InfoTableCell(50, "cell<row>2"),
                InfoTableCell(2, "cell<row>3"),
                InfoTableCell(-1, "cell<row>4")
        )
        val expectedRows = listOf(
                listOf(
                        InfoTableCell("7-6-4-4-3-m", "cell00"),
                        InfoTableCell(5, "cell01"),
                        InfoTableCell(24, "cell02"),
                        InfoTableCell(0, "cell03"),
                        InfoTableCell(24, "cell04")
                ),
                otherRows.map {
                    InfoTableCell(if (it.content == -1) 74 else it.content, it.id.replace("<row>", "1"))
                },
                listOf(
                        InfoTableCell("Total at 60unit", "distanceTotal600"),
                        InfoTableCell(11, "distanceTotal601"),
                        InfoTableCell(74, "distanceTotal602"),
                        InfoTableCell(2, "distanceTotal603"),
                        InfoTableCell("-", "distanceTotal604")
                ),
                otherRows.map {
                    InfoTableCell(if (it.content == -1) 124 else it.content, it.id.replace("<row>", "3"))
                },
                otherRows.map {
                    InfoTableCell(if (it.content == -1) 174 else it.content, it.id.replace("<row>", "4"))
                },
                listOf(
                        InfoTableCell("Total at 50unit", "distanceTotal500"),
                        InfoTableCell(12, "distanceTotal501"),
                        InfoTableCell(100, "distanceTotal502"),
                        InfoTableCell(4, "distanceTotal503"),
                        InfoTableCell("-", "distanceTotal504")
                ),
                otherRows.map {
                    InfoTableCell(if (it.content == -1) 224 else it.content, it.id.replace("<row>", "6"))
                },
                otherRows.map {
                    InfoTableCell(if (it.content == -1) 274 else it.content, it.id.replace("<row>", "7"))
                },
                listOf(
                        InfoTableCell("Total at 40unit", "distanceTotal400"),
                        InfoTableCell(12, "distanceTotal401"),
                        InfoTableCell(100, "distanceTotal402"),
                        InfoTableCell(4, "distanceTotal403"),
                        InfoTableCell("-", "distanceTotal404")
                ),
                listOf(
                        InfoTableCell("Grand total:", "grandTotal0"),
                        InfoTableCell(35, "grandTotal1"),
                        InfoTableCell(274, "grandTotal2"),
                        InfoTableCell(10, "grandTotal3"),
                        InfoTableCell("-", "grandTotal4")
                )
        )

        val scorePadData =
                calculateScorePadTableData(arrows, endSize, GoldsType.TENS, resources, arrowCounts, distances, "unit")

        Assert.assertEquals(expectedRows.size, scorePadData.size)
        for (i in scorePadData.indices) {
            Assert.assertEquals(expectedRows[i], scorePadData[i])
        }
    }

    @Test
    fun testArrowsInputBeyondRoundEnd() {
        val arrows = listOf(
                11, 10, 9, 8, 7, 6,
                11, 10, 9, 8, 7, 6,
                11, 10, 9, 8, 7, 6
        ).mapIndexed { arrowNumber, arrowValue -> TestData.ARROWS[arrowValue].toArrowValue(1, arrowNumber) }
        val arrowCounts = listOf(
                RoundArrowCount(1, 1, 10.0, 12)
        )
        val distances = listOf(
                RoundDistance(1, 1, 1, 60)
        )

        val scorePadData =
                calculateScorePadTableData(arrows, endSize, GoldsType.TENS, resources, arrowCounts, distances, "unit")

        val otherRows = listOf(
                InfoTableCell("X-10-9-8-7-6", "cell<row>0"),
                InfoTableCell(6, "cell<row>1"),
                InfoTableCell(50, "cell<row>2"),
                InfoTableCell(2, "cell<row>3"),
                InfoTableCell(-1, "cell<row>4")
        )
        val expectedRows = listOf(
                otherRows.map {
                    InfoTableCell(if (it.content == -1) 50 else it.content, it.id.replace("<row>", "0"))
                },
                otherRows.map {
                    InfoTableCell(if (it.content == -1) 100 else it.content, it.id.replace("<row>", "1"))
                },
                listOf(
                        InfoTableCell("Total at 60unit", "distanceTotal600"),
                        InfoTableCell(12, "distanceTotal601"),
                        InfoTableCell(100, "distanceTotal602"),
                        InfoTableCell(4, "distanceTotal603"),
                        InfoTableCell("-", "distanceTotal604")
                ),
                otherRows.map {
                    InfoTableCell(if (it.content == -1) 150 else it.content, it.id.replace("<row>", "3"))
                },
                listOf(
                        InfoTableCell("Surplus Total", "distanceTotal0"),
                        InfoTableCell(6, "distanceTotal1"),
                        InfoTableCell(50, "distanceTotal2"),
                        InfoTableCell(2, "distanceTotal3"),
                        InfoTableCell("-", "distanceTotal4")
                ),
                listOf(
                        InfoTableCell("Grand total:", "grandTotal0"),
                        InfoTableCell(18, "grandTotal1"),
                        InfoTableCell(150, "grandTotal2"),
                        InfoTableCell(6, "grandTotal3"),
                        InfoTableCell("-", "grandTotal4")
                )
        )

        Assert.assertEquals(expectedRows.size, scorePadData.size)
        for (i in scorePadData.indices) {
            Assert.assertEquals(expectedRows[i], scorePadData[i])
        }
    }

    @Test
    fun testNoData() {
        Assert.assertTrue(calculateScorePadTableData(listOf(), endSize, goldsType, resources).isNullOrEmpty())
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
            Assert.assertEquals(scorePadColumnHeaderIds.size, data.size)

            val end = End(
                    chunkedArrows[i], arrowPlaceHolder, arrowDeliminator
            )
            end.reorderScores()
            runningTotal += end.getScore()
            val deleteVal = "Delete"
            val expected = listOf(
                    end.toString(), end.getHits(), end.getScore(), end.getGolds(goldsType), runningTotal, deleteVal
            )
            for (j in data.indices) {
                if (data[j].content?.equals(deleteVal) == true) {
                    Assert.assertEquals("delete$i", data[j].id)
                    continue
                }
                Assert.assertEquals("cell$i$j", data[j].id)
                Assert.assertEquals(expected[j], data[j].content)
            }
        }

        // Grand total row
        val totalRow = scorePadData[scorePadData.size - 1]
        val expected = listOf(
                grandTotal,
                generatedArrows.count { it.score != 0 },
                generatedArrows.sumOf { it.score },
                generatedArrows.count { goldsType.isGold(it) },
                "-",
                ""
        )
        for (i in totalRow.indices) {
            Assert.assertEquals("grandTotal$i", totalRow[i].id)
            Assert.assertEquals(expected[i], totalRow[i].content)
        }
    }
}