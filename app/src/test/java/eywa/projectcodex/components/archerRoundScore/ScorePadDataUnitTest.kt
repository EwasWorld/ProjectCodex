package eywa.projectcodex.components.archerRoundScore

import android.content.res.Resources
import eywa.projectcodex.R
import eywa.projectcodex.common.archeryObjects.GoldsType
import eywa.projectcodex.common.sharedUi.previewHelpers.ArcherRoundPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ArcherRoundPreviewHelper.addIdenticalArrows
import eywa.projectcodex.common.sharedUi.previewHelpers.ArcherRoundPreviewHelper.addRound
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.components.archerRoundScore.scorePad.infoTable.ScorePadDataNew
import eywa.projectcodex.components.archerRoundScore.scorePad.infoTable.ScorePadDataNew.ColumnHeader
import eywa.projectcodex.components.archerRoundScore.scorePad.infoTable.ScorePadDataNew.ScorePadRow
import eywa.projectcodex.components.archerRoundScore.scorePad.infoTable.ScorePadDataNew.ScorePadRow.*
import eywa.projectcodex.database.arrowValue.ArrowValue
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.kotlin.anyVararg
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class ScorePadDataUnitTest {
    private lateinit var resources: Resources

    @Before
    fun setUp() {
        resources = mock {
            on { getString(anyInt()) } doAnswer {
                when (it.arguments[0]) {
                    R.string.end_to_string_arrow_deliminator -> ARROW_DELIMITER
                    R.string.end_to_string_arrow_placeholder -> ARROW_PLACEHOLDER
                    R.string.score_pad__running_total_placeholder -> RUNNING_TOTAL_PLACEHOLDER
                    R.string.arrow_value_m -> M_ARROW
                    R.string.arrow_value_x -> X_ARROW
                    R.string.units_yards_short -> YARDS_UNIT
                    R.string.score_pad__surplus_total -> SURPLUS_TOTAL
                    R.string.score_pad__grand_total -> GRAND_TOTAL
                    else -> throw IllegalStateException()
                }
            }
            on { getString(anyInt(), anyVararg()) } doAnswer {
                when (it.arguments[0]) {
                    R.string.score_pad__distance_total -> DISTANCE_TOTAL_STRING
                    else -> throw IllegalStateException()
                }
            }
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun testZeroEndSize() {
        ScorePadDataNew(ArcherRoundPreviewHelper.newFullArcherRoundInfo().addIdenticalArrows(5, 3), 0, GoldsType.NINES)
        Assert.fail("Created ScorePadData with 0 endSize")
    }

    @Test
    fun testGeneral_NoArrows() {
        assertEquals(
                listOf<ScorePadRow>(),
                ScorePadDataNew(ArcherRoundPreviewHelper.newFullArcherRoundInfo(), 6, GoldsType.NINES).data
        )
    }

    @Test
    fun testGeneral_NoRound() {
        val totalArrows = 50
        val endSize = 6
        val arrowScore = 7

        val expectedRows = getExpectedRows(totalArrows, endSize, arrowScore)
                .plus(
                        GrandTotal(
                                hits = totalArrows,
                                score = totalArrows * arrowScore,
                                golds = 0,
                        )
                )
        val actualRows = ScorePadDataNew(
                ArcherRoundPreviewHelper.newFullArcherRoundInfo().addIdenticalArrows(totalArrows, arrowScore),
                endSize,
                GoldsType.NINES,
        ).data

        expectedRows.forEachIndexed { index, expected ->
            assertEquals("Row $index different", expected, actualRows[index])
        }
        assertEquals(expectedRows.size, actualRows.size)
    }

    @Test
    fun testGeneral_WithRound() {
        val totalArrows = 50
        val endSize = 5
        val arrowScore = 7

        val firstDistanceSize = 36
        val lastDistanceSize = totalArrows - firstDistanceSize

        val expectedRows = getExpectedRows(firstDistanceSize, endSize, arrowScore)
                .plus(
                        DistanceTotal(
                                distance = 100,
                                distanceUnit = R.string.units_yards_short,
                                hits = firstDistanceSize,
                                score = firstDistanceSize * arrowScore,
                                golds = 0,
                        )
                )
                .plus(
                        getExpectedRows(
                                totalArrows = lastDistanceSize,
                                endSize = endSize,
                                arrowScore = arrowScore,
                                firstEndNumber = 9,
                                currentRunningTotal = firstDistanceSize * arrowScore,
                        )
                )
                .plus(
                        DistanceTotal(
                                distance = 80,
                                distanceUnit = R.string.units_yards_short,
                                hits = lastDistanceSize,
                                score = lastDistanceSize * arrowScore,
                                golds = 0,
                        )
                )
                .plus(
                        GrandTotal(
                                hits = totalArrows,
                                score = totalArrows * arrowScore,
                                golds = 0,
                        )
                )
        val actualRows = ScorePadDataNew(
                ArcherRoundPreviewHelper
                        .newFullArcherRoundInfo()
                        .addRound(RoundPreviewHelper.outdoorImperialRoundData)
                        .addIdenticalArrows(totalArrows, arrowScore),
                endSize,
                GoldsType.NINES,
        ).data

        expectedRows.forEachIndexed { index, expected ->
            assertEquals("Row $index different", expected, actualRows[index])
        }
        assertEquals(expectedRows.size, actualRows.size)
    }

    @Test
    fun testGeneral_WithRoundAndSurplus() {
        val totalArrows = 70
        val endSize = 5
        val arrowScore = 7

        val distanceSize = 60
        val surplusSize = totalArrows - distanceSize

        val expectedRows = getExpectedRows(distanceSize, endSize, arrowScore)
                .plus(
                        DistanceTotal(
                                distance = 18,
                                distanceUnit = R.string.units_meters_short,
                                hits = distanceSize,
                                score = distanceSize * arrowScore,
                                golds = 0,
                        )
                )
                .plus(
                        getExpectedRows(
                                totalArrows = surplusSize,
                                endSize = endSize,
                                arrowScore = arrowScore,
                                firstEndNumber = 13,
                                currentRunningTotal = distanceSize * arrowScore,
                        )
                )
                .plus(
                        SurplusTotal(
                                hits = surplusSize,
                                score = surplusSize * arrowScore,
                                golds = 0,
                        )
                )
                .plus(
                        GrandTotal(
                                hits = totalArrows,
                                score = totalArrows * arrowScore,
                                golds = 0,
                        )
                )
        val actualRows = ScorePadDataNew(
                ArcherRoundPreviewHelper
                        .newFullArcherRoundInfo()
                        .addRound(RoundPreviewHelper.indoorMetricRoundData)
                        .addIdenticalArrows(totalArrows, arrowScore),
                endSize,
                GoldsType.NINES,
        ).data

        expectedRows.forEachIndexed { index, expected ->
            assertEquals("Row $index different", expected, actualRows[index])
        }
        assertEquals(expectedRows.size, actualRows.size)
    }

    @Test
    fun testAllTargetArrowScores() {
        val info = ArcherRoundPreviewHelper
                .newFullArcherRoundInfo()
                .let { info ->
                    info.copy(
                            arrows = List(11) { ArrowValue(info.id, it, it, false) }
                                    .plus(ArrowValue(info.id, 11, 10, true))
                    )
                }

        val expectedRows = listOf(
                End(
                        endNumber = 1,
                        arrowValues = ALL_ARROW_VALUE_RES_OR_ACTUAL.take(6),
                        hits = 5,
                        score = 15,
                        golds = 0,
                        runningTotal = 15,
                ),
                End(
                        endNumber = 2,
                        arrowValues = ALL_ARROW_VALUE_RES_OR_ACTUAL.drop(6),
                        hits = 6,
                        score = 50,
                        golds = 3,
                        runningTotal = 65,
                ),
                GrandTotal(
                        hits = 11,
                        score = 65,
                        golds = 3,
                ),
        )
        val actualRows = ScorePadDataNew(
                info,
                6,
                GoldsType.NINES,
        ).data

        expectedRows.forEachIndexed { index, expected ->
            assertEquals("Row $index different", expected, actualRows[index])
        }
        assertEquals(expectedRows.size, actualRows.size)
    }

    @Test
    fun testGolds() {
        fun getExpectedRows(expectedGolds: Int) = listOf(
                End(
                        endNumber = 1,
                        arrowValues = ALL_ARROW_VALUE_RES_OR_ACTUAL,
                        hits = 11,
                        score = 65,
                        golds = expectedGolds,
                        runningTotal = 65,
                ),
                GrandTotal(
                        hits = 11,
                        score = 65,
                        golds = expectedGolds,
                ),
        )

        val info = ArcherRoundPreviewHelper
                .newFullArcherRoundInfo()
                .let { info ->
                    info.copy(
                            arrows = List(11) { ArrowValue(info.id, it, it, false) }
                                    .plus(ArrowValue(info.id, 11, 10, true))
                    )
                }

        assertEquals(
                getExpectedRows(3),
                ScorePadDataNew(info, 100, GoldsType.NINES).data,
        )
        assertEquals(
                getExpectedRows(2),
                ScorePadDataNew(info, 100, GoldsType.TENS).data,
        )
        assertEquals(
                getExpectedRows(1),
                ScorePadDataNew(info, 100, GoldsType.XS).data,
        )
    }

    @Test
    fun testRowHeaders() {
        val end = End(
                endNumber = 1,
                arrowValues = ALL_ARROW_VALUE_RES_OR_ACTUAL,
                hits = 11,
                score = 65,
                golds = 3,
                runningTotal = 65,
        )

        assertEquals(ResOrActual.fromActual("1"), end.getRowHeader())
        assertEquals(ResOrActual.fromActual("2"), end.copy(endNumber = 2).getRowHeader())

        assertEquals(
                ResOrActual.fromRes<String>(R.string.score_pad__distance_total_row_header),
                DistanceTotal(1, 1, 1, 1, 1).getRowHeader()
        )
        assertEquals(
                ResOrActual.fromRes<String>(R.string.score_pad__distance_total_row_header),
                SurplusTotal(1, 1, 1).getRowHeader()
        )
        assertEquals(
                ResOrActual.fromRes<String>(R.string.score_pad__grand_total_row_header),
                GrandTotal(1, 1, 1).getRowHeader()
        )
    }

    @Test
    fun testGetContent_End() {
        val row = End(
                endNumber = 1,
                arrowValues = ALL_ARROW_VALUE_RES_OR_ACTUAL,
                hits = 11,
                score = 65,
                golds = 3,
                runningTotal = 65,
        )

        ColumnHeader.values().forEach { column ->
            val expected = when (column) {
                ColumnHeader.ARROWS -> listOf(M_ARROW)
                        .plus((1..10).map { it.toString() })
                        .plus(X_ARROW)
                        .joinToString(ARROW_DELIMITER)
                ColumnHeader.HITS -> "11"
                ColumnHeader.SCORE -> "65"
                ColumnHeader.GOLDS -> "3"
                ColumnHeader.RUNNING_TOTAL -> "65"
            }
            assertEquals(column.name, expected, row.getContent(column, resources))
        }
    }

    @Test
    fun testGetContent_DistanceTotal() {
        val row = DistanceTotal(
                distance = 100,
                distanceUnit = R.string.units_yards_short,
                hits = 11,
                score = 65,
                golds = 3,
        )

        ColumnHeader.values().forEach { column ->
            val expected = when (column) {
                ColumnHeader.ARROWS -> DISTANCE_TOTAL_STRING
                ColumnHeader.HITS -> "11"
                ColumnHeader.SCORE -> "65"
                ColumnHeader.GOLDS -> "3"
                ColumnHeader.RUNNING_TOTAL -> RUNNING_TOTAL_PLACEHOLDER
            }
            assertEquals(column.name, expected, row.getContent(column, resources))
        }

        verify(resources).getString(R.string.score_pad__distance_total, 100, YARDS_UNIT)
    }

    @Test
    fun testGetContent_SurplusTotal() {
        val row = SurplusTotal(
                hits = 11,
                score = 65,
                golds = 3,
        )

        ColumnHeader.values().forEach { column ->
            val expected = when (column) {
                ColumnHeader.ARROWS -> SURPLUS_TOTAL
                ColumnHeader.HITS -> "11"
                ColumnHeader.SCORE -> "65"
                ColumnHeader.GOLDS -> "3"
                ColumnHeader.RUNNING_TOTAL -> RUNNING_TOTAL_PLACEHOLDER
            }
            assertEquals(column.name, expected, row.getContent(column, resources))
        }
    }

    @Test
    fun testGetContent_GrandTotal() {
        val row = GrandTotal(
                hits = 11,
                score = 65,
                golds = 3,
        )

        ColumnHeader.values().forEach { column ->
            val expected = when (column) {
                ColumnHeader.ARROWS -> GRAND_TOTAL
                ColumnHeader.HITS -> "11"
                ColumnHeader.SCORE -> "65"
                ColumnHeader.GOLDS -> "3"
                ColumnHeader.RUNNING_TOTAL -> RUNNING_TOTAL_PLACEHOLDER
            }
            assertEquals(column.name, expected, row.getContent(column, resources))
        }
    }

    private fun getExpectedRows(
            totalArrows: Int,
            endSize: Int,
            arrowScore: Int,
            firstEndNumber: Int = 1,
            currentRunningTotal: Int = 0,
    ): List<End> {
        val fullEnds = totalArrows / endSize
        val lastEndSize = totalArrows % endSize

        var expected = List(fullEnds) { endIndex ->
            End(
                    endNumber = firstEndNumber + endIndex,
                    arrowValues = List(endSize) { ResOrActual.fromActual(arrowScore.toString()) },
                    hits = endSize,
                    score = endSize * arrowScore,
                    golds = 0,
                    runningTotal = currentRunningTotal + endSize * arrowScore * (endIndex + 1),
            )
        }
        if (lastEndSize > 0) {
            expected = expected.plus(
                    End(
                            endNumber = firstEndNumber + fullEnds,
                            arrowValues = List(lastEndSize) { ResOrActual.fromActual(arrowScore.toString()) },
                            hits = lastEndSize,
                            score = lastEndSize * arrowScore,
                            golds = 0,
                            runningTotal = currentRunningTotal + totalArrows * arrowScore,
                    )
            )
        }
        return expected
    }

    companion object {
        private const val ARROW_DELIMITER = "ARROW_DELIMITER"
        private const val ARROW_PLACEHOLDER = "ARROW_PLACEHOLDER"
        private const val RUNNING_TOTAL_PLACEHOLDER = "RUNNING_TOTAL_PLACEHOLDER"
        private const val M_ARROW = "M_ARROW"
        private const val X_ARROW = "X_ARROW"
        private const val YARDS_UNIT = "YARDS_UNIT"
        private const val DISTANCE_TOTAL_STRING = "DISTANCE_TOTAL_STRING"
        private const val SURPLUS_TOTAL = "SURPLUS_TOTAL"
        private const val GRAND_TOTAL = "GRAND_TOTAL"

        private val ALL_ARROW_VALUE_RES_OR_ACTUAL = listOf(
                ResOrActual.fromRes(R.string.arrow_value_m),
                ResOrActual.fromActual("1"),
                ResOrActual.fromActual("2"),
                ResOrActual.fromActual("3"),
                ResOrActual.fromActual("4"),
                ResOrActual.fromActual("5"),
                ResOrActual.fromActual("6"),
                ResOrActual.fromActual("7"),
                ResOrActual.fromActual("8"),
                ResOrActual.fromActual("9"),
                ResOrActual.fromActual("10"),
                ResOrActual.fromRes(R.string.arrow_value_x),
        )
    }
}
