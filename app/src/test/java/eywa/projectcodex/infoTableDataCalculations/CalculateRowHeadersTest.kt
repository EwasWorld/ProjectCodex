package eywa.projectcodex.infoTableDataCalculations

import android.content.res.Resources
import eywa.projectcodex.R
import eywa.projectcodex.common.TestData
import eywa.projectcodex.common.archeryObjects.GoldsType
import eywa.projectcodex.components.archerRoundScore.scorePad.infoTable.ScorePadData
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

class CalculateRowHeadersTest {
    companion object {
        private const val TOTAL_ROW_HEADER = "T"
        private const val GRAND_TOTAL_ROW_HEADER = "GT"
        private const val DISTANCE_UNIT = "unit"
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
                else -> ""
            }
        }
        rowId = 0
    }

    @Test
    fun testNoData() {
        testRowHeaders(ScorePadData(listOf(), 1, GoldsType.TENS, resources), listOf())
    }

    @Test
    fun testNormalHeaders() {
        for (testSize in listOf(1, 6, 20)) {
            val expectedOutput = List(testSize) { Outputs.NUMBER }.plus(Outputs.GRAND_TOTAL)
            val input = ScorePadData(TestData.generateArrowValues(testSize, 1), 1, GoldsType.TENS, resources)
            testRowHeaders(input, expectedOutput)
        }
    }

    @Test
    fun testHeadersWithDistanceTotal() {
        for (testDistanceSizes in listOf(listOf(3, 3), listOf(4, 2), listOf(2, 2, 2))) {
            val expectedOutput = testDistanceSizes.map { distanceSize ->
                List(distanceSize) { Outputs.NUMBER }.plus(Outputs.TOTAL)
            }.flatten().plus(Outputs.GRAND_TOTAL)
            val input = ScorePadData(
                    TestData.generateArrowValues(testDistanceSizes.sum(), 1),
                    1,
                    GoldsType.TENS,
                    resources,
                    testDistanceSizes.mapIndexed { index, distanceSize ->
                        RoundArrowCount(1, index, 1.0, distanceSize)
                    },
                    testDistanceSizes.mapIndexed { index, _ -> RoundDistance(1, index, 1, index * 10) },
                    DISTANCE_UNIT
            )
            testRowHeaders(input, expectedOutput)
        }
    }

    private enum class Outputs { NUMBER, TOTAL, GRAND_TOTAL }

    private fun testRowHeaders(inputData: ScorePadData, expected: List<Outputs>) {
        val actual = inputData.generateRowHeaders(TOTAL_ROW_HEADER, GRAND_TOTAL_ROW_HEADER)
        var maxNumberSeen = 0
        var grandTotalSeen = false
        for (i in expected.indices) {
            if (grandTotalSeen) Assert.fail("Should not be anything after grand total")

            val tableCell = actual[i]
            if (tableCell.content == null) {
                Assert.fail("No content")
            }
            when (expected[i]) {
                Outputs.NUMBER -> {
                    if (!tableCell.id.contains("row")) Assert.fail("Incorrect rowId: ${tableCell.id}")
                    val intContent = tableCell.content as String
                    if (intContent == (maxNumberSeen + 1).toString()) maxNumberSeen++
                    else Assert.fail("Non-ascending row-headers")
                }
                Outputs.TOTAL -> {
                    if (!tableCell.id.contains("distanceTotal")) Assert.fail(
                            "Incorrect rowId: ${tableCell.id}"
                    )
                    if (!(tableCell.content as String).contains(TOTAL_ROW_HEADER)) Assert.fail(
                            "Incorrect row header: ${tableCell.content}"
                    )
                }
                Outputs.GRAND_TOTAL -> {
                    if (!tableCell.id.contains("grandTotalHeader")) Assert.fail(
                            "Incorrect rowId: ${tableCell.id}"
                    )
                    if (!(tableCell.content as String).contains(GRAND_TOTAL_ROW_HEADER)) Assert.fail(
                            "Incorrect row header: ${tableCell.content}"
                    )
                    grandTotalSeen = true
                }
            }
        }
    }
}