package eywa.projectcodex.model.headToHead

import eywa.projectcodex.common.sharedUi.previewHelpers.HeadToHeadMatchPreviewHelperDsl
import eywa.projectcodex.components.shootDetails.headToHead.HeadToHeadResult
import eywa.projectcodex.model.Either
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class FullHeadToHeadMatchUnitTest {
    @Test
    fun testRunningTotals_Recurve() {
        fun dsl(teamSize: Int) =
                HeadToHeadMatchPreviewHelperDsl(
                        shootId = 1,
                        teamSize = teamSize,
                        isRecurveStyle = true,
                        matchNumber = 1,
                        endSize = null,
                )

        fun getData(teamSize: Int): List<Pair<HeadToHeadMatchPreviewHelperDsl, List<HeadToHeadRunningTotal>>> =
                listOf(
                        // Empty
                        dsl(teamSize) to listOf(),
                        // Incomplete and unknown sets
                        dsl(teamSize).apply {
                            addSet { addRows(result = HeadToHeadResult.INCOMPLETE) }
                            addSet { addRows(result = HeadToHeadResult.WIN) }
                            addSet { addRows(result = HeadToHeadResult.UNKNOWN) }
                            addSet { addRows(result = HeadToHeadResult.WIN) }
                        } to listOf(
                                Either.Right(HeadToHeadNoResult.Incomplete),
                                Either.Right(HeadToHeadNoResult.Incomplete),
                                Either.Right(HeadToHeadNoResult.Unknown),
                                Either.Right(HeadToHeadNoResult.Unknown),
                        ),
                        // Shoot off
                        dsl(teamSize).apply {
                            addSet { addRows(result = HeadToHeadResult.WIN) }
                            addSet { addRows(result = HeadToHeadResult.LOSS) }
                            addSet { addRows(result = HeadToHeadResult.LOSS) }
                            addSet { addRows(result = HeadToHeadResult.WIN) }
                            if (teamSize == 1) {
                                addSet { addRows(result = HeadToHeadResult.TIE) }
                            }
                            addSet { addRows(result = HeadToHeadResult.LOSS, winnerScore = 10, loserScore = 1) }
                        } to listOfNotNull(
                                Either.Left(2 to 0),
                                Either.Left(2 to 2),
                                Either.Left(2 to 4),
                                Either.Left(4 to 4),
                                Either.Left<Pair<Int, Int>, HeadToHeadNoResult>(5 to 5).takeIf { teamSize == 1 },
                                Either.Left<Pair<Int, Int>, HeadToHeadNoResult>(5 to 6).takeIf { teamSize == 1 },
                                Either.Left<Pair<Int, Int>, HeadToHeadNoResult>(4 to 5).takeIf { teamSize != 1 },
                        ),
                        dsl(teamSize).apply {
                            addSet { addRows(result = HeadToHeadResult.WIN) }
                            addSet { addRows(result = HeadToHeadResult.LOSS) }
                            addSet { addRows(result = HeadToHeadResult.LOSS) }
                            addSet { addRows(result = HeadToHeadResult.WIN) }
                            if (teamSize == 1) {
                                addSet { addRows(result = HeadToHeadResult.TIE) }
                            }
                            addSet(isShootOff = true) {
                                addRows(result = HeadToHeadResult.WIN, winnerScore = 10, loserScore = 1)
                            }
                        } to listOfNotNull(
                                Either.Left(2 to 0),
                                Either.Left(2 to 2),
                                Either.Left(2 to 4),
                                Either.Left(4 to 4),
                                Either.Left<Pair<Int, Int>, HeadToHeadNoResult>(5 to 5).takeIf { teamSize == 1 },
                                Either.Left<Pair<Int, Int>, HeadToHeadNoResult>(6 to 5).takeIf { teamSize == 1 },
                                Either.Left<Pair<Int, Int>, HeadToHeadNoResult>(5 to 4).takeIf { teamSize != 1 },
                        ),
                )

        getData(teamSize = 1).forEach { (heat, expected) ->
            assertEquals(expected, heat.asFull().runningTotals)
        }
        getData(teamSize = 2).forEach { (heat, expected) ->
            assertEquals(expected, heat.asFull().runningTotals)
        }
    }

    @Test
    fun testRunningTotals_Compound() {
        fun dsl(teamSize: Int) =
                HeadToHeadMatchPreviewHelperDsl(
                        shootId = 1,
                        teamSize = teamSize,
                        isRecurveStyle = false,
                        matchNumber = 1,
                        endSize = null,
                )

        fun getData(teamSize: Int): List<Pair<HeadToHeadMatchPreviewHelperDsl, List<HeadToHeadRunningTotal>>> =
                listOf(
                        // Empty
                        dsl(teamSize) to listOf(),
                        // Incomplete and unknown sets
                        dsl(teamSize).apply {
                            addSet { addRows(result = HeadToHeadResult.INCOMPLETE) }
                            addSet { addRows(result = HeadToHeadResult.WIN) }
                            addSet { addRows(result = HeadToHeadResult.UNKNOWN) }
                            addSet { addRows(result = HeadToHeadResult.WIN) }
                        } to listOf(
                                Either.Right(HeadToHeadNoResult.Incomplete),
                                Either.Right(HeadToHeadNoResult.Incomplete),
                                Either.Right(HeadToHeadNoResult.Unknown),
                                Either.Right(HeadToHeadNoResult.Unknown),
                        ),
                        // Shoot off
                        dsl(teamSize).apply {
                            addSet { addRows(result = HeadToHeadResult.WIN) }
                            addSet { addRows(result = HeadToHeadResult.LOSS) }
                            addSet { addRows(result = HeadToHeadResult.LOSS) }
                            addSet { addRows(result = HeadToHeadResult.WIN) }
                            if (teamSize == 1) {
                                addSet { addRows(result = HeadToHeadResult.TIE) }
                            }
                            addSet { addRows(result = HeadToHeadResult.LOSS, winnerScore = 10, loserScore = 1) }
                        } to listOfNotNull(
                                Either.Left(30 to 20),
                                Either.Left(50 to 50),
                                Either.Left(70 to 80),
                                Either.Left(100 to 100),
                                Either.Left<Pair<Int, Int>, HeadToHeadNoResult>(130 to 130).takeIf { teamSize == 1 },
                                Either.Left<Pair<Int, Int>, HeadToHeadNoResult>(131 to 140).takeIf { teamSize == 1 },
                                Either.Left<Pair<Int, Int>, HeadToHeadNoResult>(101 to 110).takeIf { teamSize != 1 },
                        ),
                        dsl(teamSize).apply {
                            addSet { addRows(result = HeadToHeadResult.WIN) }
                            addSet { addRows(result = HeadToHeadResult.LOSS) }
                            addSet { addRows(result = HeadToHeadResult.LOSS) }
                            addSet { addRows(result = HeadToHeadResult.WIN) }
                            if (teamSize == 1) {
                                addSet { addRows(result = HeadToHeadResult.TIE) }
                            }
                            addSet(isShootOff = true) {
                                addRows(result = HeadToHeadResult.WIN, winnerScore = 10, loserScore = 1)
                            }
                        } to listOfNotNull(
                                Either.Left(30 to 20),
                                Either.Left(50 to 50),
                                Either.Left(70 to 80),
                                Either.Left(100 to 100),
                                Either.Left<Pair<Int, Int>, HeadToHeadNoResult>(130 to 130).takeIf { teamSize == 1 },
                                Either.Left<Pair<Int, Int>, HeadToHeadNoResult>(140 to 131).takeIf { teamSize == 1 },
                                Either.Left<Pair<Int, Int>, HeadToHeadNoResult>(110 to 101).takeIf { teamSize != 1 },
                        ),
                )

        getData(teamSize = 1).forEach { (heat, expected) ->
            assertEquals(expected, heat.asFull().runningTotals)
        }
        getData(teamSize = 2).forEach { (heat, expected) ->
            assertEquals(expected, heat.asFull().runningTotals)
        }
    }

    @Test
    fun testResult() {
        fun dsl(teamSize: Int, isRecurve: Boolean) =
                HeadToHeadMatchPreviewHelperDsl(
                        shootId = 1,
                        teamSize = teamSize,
                        isRecurveStyle = isRecurve,
                        matchNumber = 1,
                        endSize = null,
                )

        fun getData(isRecurve: Boolean, dsl: () -> HeadToHeadMatchPreviewHelperDsl) =
                listOf(
                        /*
                         * Edge cases
                         */
                        dsl() to HeadToHeadResult.INCOMPLETE,
                        dsl().apply { match = match.copy(isBye = true) } to HeadToHeadResult.WIN,
                        dsl().apply {
                            if (teamSize == 1) {
                                addSet { addRows(result = HeadToHeadResult.TIE) }
                            }
                            addSet { addRows(result = HeadToHeadResult.INCOMPLETE) }
                            addSet { addRows(result = HeadToHeadResult.WIN) }
                            addSet { addRows(result = HeadToHeadResult.WIN) }
                            addSet { addRows(result = HeadToHeadResult.WIN) }
                        } to HeadToHeadResult.INCOMPLETE,
                        dsl().apply {
                            addSet { addRows(result = HeadToHeadResult.INCOMPLETE) }
                            addSet { addRows(result = HeadToHeadResult.WIN) }
                            addSet { addRows(result = HeadToHeadResult.UNKNOWN) }
                            addSet { addRows(result = HeadToHeadResult.WIN) }
                        } to HeadToHeadResult.UNKNOWN,

                        /*
                         * No shoot off
                         */
                        dsl().apply {
                            addSet { addRows(result = HeadToHeadResult.WIN) }
                            addSet { addRows(result = HeadToHeadResult.LOSS) }
                            addSet { addRows(result = HeadToHeadResult.WIN) }
                            addSet { addRows(result = HeadToHeadResult.WIN) }
                            if (!isRecurveStyle && teamSize == 1) {
                                addSet { addRows(result = HeadToHeadResult.TIE) }
                            }
                        } to HeadToHeadResult.WIN,
                        dsl().apply {
                            addSet { addRows(result = HeadToHeadResult.WIN) }
                            addSet { addRows(result = HeadToHeadResult.WIN) }
                            addSet { addRows(result = HeadToHeadResult.WIN) }
                        } to if (isRecurve) HeadToHeadResult.WIN else HeadToHeadResult.INCOMPLETE,
                        dsl().apply {
                            addSet { addRows(result = HeadToHeadResult.LOSS) }
                            addSet { addRows(result = HeadToHeadResult.LOSS) }
                            addSet { addRows(result = HeadToHeadResult.LOSS) }
                            if (!isRecurveStyle) {
                                addSet { addRows(result = HeadToHeadResult.TIE) }
                                if (teamSize == 1) {
                                    addSet { addRows(result = HeadToHeadResult.TIE) }
                                }
                            }
                        } to HeadToHeadResult.LOSS,

                        /*
                         * Shoot off
                         */
                        dsl().apply {
                            addSet { addRows(result = HeadToHeadResult.WIN) }
                            addSet { addRows(result = HeadToHeadResult.LOSS) }
                            addSet { addRows(result = HeadToHeadResult.LOSS) }
                            addSet { addRows(result = HeadToHeadResult.WIN) }
                            if (teamSize == 1) {
                                addSet { addRows(result = HeadToHeadResult.TIE) }
                            }
                        } to HeadToHeadResult.INCOMPLETE,
                        dsl().apply {
                            addSet { addRows(result = HeadToHeadResult.WIN) }
                            addSet { addRows(result = HeadToHeadResult.LOSS) }
                            addSet { addRows(result = HeadToHeadResult.LOSS) }
                            addSet { addRows(result = HeadToHeadResult.WIN) }
                            if (teamSize == 1) {
                                addSet { addRows(result = HeadToHeadResult.TIE) }
                            }
                            addSet { addRows(result = HeadToHeadResult.LOSS, winnerScore = 10, loserScore = 1) }
                        } to HeadToHeadResult.LOSS,
                )

        listOf(1 to true, 2 to true, 1 to false, 2 to false).forEach { (teamSize, isRecurve) ->
            getData(isRecurve) { dsl(teamSize, isRecurve) }.forEachIndexed { i, (dsl, expected) ->
                assertEquals("$teamSize $isRecurve $i", expected, dsl.asFull().result)
            }
            assertThrows(IllegalStateException::class.java) {
                dsl(teamSize, isRecurve).apply {
                    match = match.copy(isBye = true)
                    addSet { addRows(result = HeadToHeadResult.UNKNOWN) }
                }.asFull().result
            }
        }
    }
}
