package eywa.projectcodex.infoTableDataCalculations

import android.content.res.Resources
import eywa.projectcodex.R
import eywa.projectcodex.components.archerRoundScore.scorePad.infoTable.InfoTableCell
import eywa.projectcodex.components.archerRoundScore.scorePad.infoTable.ScorePadData
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

class CalculateRowHeadersTest {
    companion object {
        private const val TOTAL_ROW_HEADER = "T"
        private const val GRAND_TOTAL_ROW_HEADER = "GT"
    }

    private lateinit var resources: Resources
    private var rowId = 0

    @Before
    fun setUp() {
        resources = mock(Resources::class.java)
        `when`(resources.getString(anyInt())).thenAnswer { invocation ->
            when (invocation.getArgument<Int>(0)) {
                R.string.score_pad__grand_total_row_header -> GRAND_TOTAL_ROW_HEADER
                R.string.score_pad__distance_total_row_header -> TOTAL_ROW_HEADER
                else -> Assert.fail("Bad string passed to resources")
            }
        }
        rowId = 0
    }

    @Test
    fun testNormalHeaders() {
        for (testSize in listOf(1, 6, 20)) {
            testRowHeaders(
                    ScorePadData.generateRowHeaders(listOf(testSize)),
                    List(testSize) { Outputs.NUMBER }
            )
        }
    }

    @Test
    fun testHeadersWithGrandTotal() {
        testRowHeaders(
                ScorePadData.generateRowHeaders(
                        listOf(5),
                        null,
                        resources,
                        true
                ),
                listOf(
                        Outputs.NUMBER, Outputs.NUMBER, Outputs.NUMBER, Outputs.NUMBER, Outputs.NUMBER,
                        Outputs.GRAND_TOTAL
                )
        )
    }

    @Test
    fun testHeadersWithDistanceTotal() {
        testRowHeaders(
                ScorePadData.generateRowHeaders(
                        listOf(3, 3),
                        null,
                        resources,
                        false
                ),
                listOf(
                        Outputs.NUMBER, Outputs.NUMBER, Outputs.NUMBER, Outputs.TOTAL, Outputs.NUMBER, Outputs.NUMBER,
                        Outputs.NUMBER, Outputs.TOTAL
                )
        )
        testRowHeaders(
                ScorePadData.generateRowHeaders(
                        listOf(4, 2),
                        null,
                        resources,
                        false
                ),
                listOf(
                        Outputs.NUMBER, Outputs.NUMBER, Outputs.NUMBER, Outputs.NUMBER, Outputs.TOTAL, Outputs.NUMBER,
                        Outputs.NUMBER, Outputs.TOTAL
                )
        )
        testRowHeaders(
                ScorePadData.generateRowHeaders(
                        listOf(2, 2, 2),
                        null,
                        resources,
                        false
                ),
                listOf(
                        Outputs.NUMBER, Outputs.NUMBER, Outputs.TOTAL, Outputs.NUMBER, Outputs.NUMBER, Outputs.TOTAL,
                        Outputs.NUMBER, Outputs.NUMBER, Outputs.TOTAL
                )
        )
    }

    @Test
    fun testHeadersWithDistanceTotalAndArrowsComplete() {
        testRowHeaders(
                ScorePadData.generateRowHeaders(
                        listOf(5),
                        3,
                        resources,
                        false
                ),
                listOf(Outputs.NUMBER, Outputs.NUMBER, Outputs.NUMBER)
        )
        testRowHeaders(
                ScorePadData.generateRowHeaders(
                        listOf(2, 4, 6),
                        5,
                        resources,
                        false
                ),
                listOf(
                        Outputs.NUMBER, Outputs.NUMBER, Outputs.TOTAL, Outputs.NUMBER, Outputs.NUMBER, Outputs.NUMBER,
                        Outputs.TOTAL
                )
        )
    }

    @Test
    fun testArrowsInputBeyondEndOfRound() {
        testRowHeaders(
                ScorePadData.generateRowHeaders(
                        listOf(3),
                        5,
                        resources,
                        false
                ),
                listOf(
                        Outputs.NUMBER, Outputs.NUMBER, Outputs.NUMBER, Outputs.TOTAL, Outputs.NUMBER, Outputs.NUMBER,
                        Outputs.TOTAL
                )
        )
    }

    private enum class Outputs { NUMBER, TOTAL, GRAND_TOTAL }

    private fun testRowHeaders(actual: List<InfoTableCell>, expected: List<Outputs>) {
        var maxNumberSeen = 0
        var grandTotalSeen = false
        for (i in actual.indices) {
            if (grandTotalSeen) Assert.fail("Should not be anything after grand total")

            val tableCell = actual[i]
            tableCell.content?.let { content ->
                when (expected[i]) {
                    Outputs.NUMBER -> {
                        if (!tableCell.id.contains("row")) Assert.fail("Incorrect rowId: ${tableCell.id}")
                        val intContent = content as Int
                        if (intContent == maxNumberSeen + 1) maxNumberSeen = intContent
                        else Assert.fail("Non-ascending row-headers")
                    }
                    Outputs.TOTAL -> {
                        if (!tableCell.id.contains("distanceTotal")) Assert.fail(
                                "Incorrect rowId: ${tableCell.id}"
                        )
                        if (!(content as String).contains(TOTAL_ROW_HEADER)) Assert.fail(
                                "Incorrect row header: ${tableCell.content}"
                        )
                    }
                    Outputs.GRAND_TOTAL -> {
                        if (!tableCell.id.contains("grandTotalHeader")) Assert.fail(
                                "Incorrect rowId: ${tableCell.id}"
                        )
                        if (!(content as String).contains(GRAND_TOTAL_ROW_HEADER)) Assert.fail(
                                "Incorrect row header: ${tableCell.content}"
                        )
                        grandTotalSeen = true
                    }
                }
            } ?: Assert.fail("No content")
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun testNoData() {
        ScorePadData.generateRowHeaders(listOf(0))
        Assert.fail("Generate no header rows")
    }

    @Test(expected = IllegalArgumentException::class)
    fun testNegativeRowCount() {
        ScorePadData.generateRowHeaders(listOf(-1))
        Assert.fail("Negative row count for distance")
    }

    @Test(expected = IllegalArgumentException::class)
    fun testGrandTotalNoResource() {
        ScorePadData.generateRowHeaders(listOf(24), grandTotal = true)
        Assert.fail("Resources required, grand total")
    }

    @Test(expected = IllegalArgumentException::class)
    fun testDistanceTotalNoResource() {
        ScorePadData.generateRowHeaders(listOf(1, 4))
        Assert.fail("Resources required, multiple distances")
    }

    @Test(expected = IllegalArgumentException::class)
    fun testBothTotalNoResource() {
        ScorePadData.generateRowHeaders(listOf(1, 4), grandTotal = true)
        Assert.fail("Resources required, grand total and multiple distances")
    }
}