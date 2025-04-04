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
                        isSetPoints = true,
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
                            addSet(isShootOff = true) {
                                addRows(result = HeadToHeadResult.LOSS, winnerScore = 10, loserScore = 1)
                            }
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

        getData(teamSize = 1).forEachIndexed { index, (heat, expected) ->
            assertEquals("$index", expected, heat.asFull().runningTotals)
        }
        getData(teamSize = 2).forEachIndexed { index, (heat, expected) ->
            assertEquals("$index", expected, heat.asFull().runningTotals)
        }
    }

    @Test
    fun testRunningTotals_Compound() {
        fun dsl(teamSize: Int) =
                HeadToHeadMatchPreviewHelperDsl(
                        shootId = 1,
                        teamSize = teamSize,
                        isSetPoints = false,
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
        fun dsl(teamSize: Int, isSetPoints: Boolean) =
                HeadToHeadMatchPreviewHelperDsl(
                        shootId = 1,
                        teamSize = teamSize,
                        isSetPoints = isSetPoints,
                        matchNumber = 1,
                        endSize = null,
                )

        fun getData(isSetPoints: Boolean, dsl: () -> HeadToHeadMatchPreviewHelperDsl) =
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
                            if (!this.isSetPoints && teamSize == 1) {
                                addSet { addRows(result = HeadToHeadResult.TIE) }
                            }
                        } to HeadToHeadResult.WIN,
                        dsl().apply {
                            addSet { addRows(result = HeadToHeadResult.WIN) }
                            addSet { addRows(result = HeadToHeadResult.WIN) }
                            addSet { addRows(result = HeadToHeadResult.WIN) }
                        } to if (isSetPoints) HeadToHeadResult.WIN else HeadToHeadResult.INCOMPLETE,
                        dsl().apply {
                            addSet { addRows(result = HeadToHeadResult.LOSS) }
                            addSet { addRows(result = HeadToHeadResult.LOSS) }
                            addSet { addRows(result = HeadToHeadResult.LOSS) }
                            if (!this.isSetPoints) {
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

        listOf(1 to true, 2 to true, 1 to false, 2 to false).forEach { (teamSize, isSetPoints) ->
            getData(isSetPoints) { dsl(teamSize, isSetPoints) }.forEachIndexed { i, (dsl, expected) ->
                assertEquals("$teamSize $isSetPoints $i", expected, dsl.asFull().result)
            }
            assertThrows(IllegalStateException::class.java) {
                dsl(teamSize, isSetPoints).apply {
                    match = match.copy(isBye = true)
                    addSet { addRows(result = HeadToHeadResult.UNKNOWN) }
                }.asFull().result
            }
        }
    }

    @Test
    fun testIsComplete() {
        fun dsl(
                teamSize: Int = 1,
                isSetPoints: Boolean = true,
                isStandardFormat: Boolean = true,
        ) = HeadToHeadMatchPreviewHelperDsl(
                shootId = 1,
                matchNumber = 1,
                teamSize = teamSize,
                isSetPoints = isSetPoints,
                endSize = if (isStandardFormat) null else 3,
        )

        listOf(
                // Non standard
                dsl(isStandardFormat = false).apply {
                    addSet { addRows(result = HeadToHeadResult.WIN) }
                    addSet { addRows(result = HeadToHeadResult.WIN) }
                    addSet { addRows(result = HeadToHeadResult.WIN) }
                } to false,
                // No sets
                dsl() to false,
                // Result win/loss
                dsl().apply {
                    addSet { addRows(result = HeadToHeadResult.WIN) }
                    addSet { addRows(result = HeadToHeadResult.WIN) }
                    addSet { addRows(result = HeadToHeadResult.WIN) }
                } to true,
                // Result tie
                dsl().apply {
                    addSet { addRows(result = HeadToHeadResult.WIN) }
                    addSet { addRows(result = HeadToHeadResult.LOSS) }
                    addSet { addRows(result = HeadToHeadResult.WIN) }
                    addSet { addRows(result = HeadToHeadResult.LOSS) }
                    addSet { addRows(result = HeadToHeadResult.TIE) }
                } to false,
                // Result unknown - with shoot off win/loss as last set
                dsl().apply {
                    addSet { addRows(result = HeadToHeadResult.UNKNOWN) }
                    addSet { addRows(result = HeadToHeadResult.LOSS) }
                    addSet { addRows(result = HeadToHeadResult.WIN) }
                    addSet { addRows(result = HeadToHeadResult.LOSS) }
                    addSet { addRows(result = HeadToHeadResult.TIE) }
                    addSet(isShootOff = true) {
                        addRows(result = HeadToHeadResult.WIN, winnerScore = 10, loserScore = 1)
                    }
                } to true,
                // Result unknown - with shoot off tie as last set
                dsl().apply {
                    addSet { addRows(result = HeadToHeadResult.UNKNOWN) }
                    addSet { addRows(result = HeadToHeadResult.LOSS) }
                    addSet { addRows(result = HeadToHeadResult.WIN) }
                    addSet { addRows(result = HeadToHeadResult.LOSS) }
                    addSet { addRows(result = HeadToHeadResult.TIE) }
                    addSet(isShootOff = true) {
                        addRows(result = HeadToHeadResult.TIE, winnerScore = 10, loserScore = 1)
                    }
                } to false,
                // Result unknown - non-shoot off last set
                dsl().apply {
                    addSet { addRows(result = HeadToHeadResult.UNKNOWN) }
                    addSet { addRows(result = HeadToHeadResult.LOSS) }
                    addSet { addRows(result = HeadToHeadResult.WIN) }
                    addSet { addRows(result = HeadToHeadResult.TIE) }
                    addSet { addRows(result = HeadToHeadResult.LOSS) }
                } to false,
                // Total score (must go to 5 sets)
                dsl(isSetPoints = false).apply {
                    addSet { addRows(result = HeadToHeadResult.LOSS) }
                    addSet { addRows(result = HeadToHeadResult.WIN) }
                    addSet { addRows(result = HeadToHeadResult.LOSS) }
                    addSet { addRows(result = HeadToHeadResult.LOSS) }
                } to false,
        ).forEachIndexed { i, (dsl, expected) ->
            assertEquals("$i", expected, dsl.asFull().isComplete)
        }
    }
}
