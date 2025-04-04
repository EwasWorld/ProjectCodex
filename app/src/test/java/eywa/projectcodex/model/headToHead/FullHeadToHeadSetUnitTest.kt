package eywa.projectcodex.model.headToHead

import eywa.projectcodex.components.referenceTables.headToHead.HeadToHeadUseCase
import eywa.projectcodex.components.shootDetails.headToHead.HeadToHeadArcherType
import eywa.projectcodex.components.shootDetails.headToHead.HeadToHeadArcherType.*
import eywa.projectcodex.components.shootDetails.headToHead.HeadToHeadResult
import eywa.projectcodex.components.shootDetails.headToHead.grid.HeadToHeadGridRowData
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHeadDetail
import eywa.projectcodex.model.Arrow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class FullHeadToHeadSetUnitTest {
    private fun List<Pair<HeadToHeadArcherType, Int?>>.asData(
            isTeam: Boolean = false,
            isShootOff: Boolean = false,
            isShootOffWin: Boolean = false,
            isSetPoints: Boolean = true,
    ): FullHeadToHeadSet {
        val teamSize = if (isTeam) 2 else 1
        var data: List<HeadToHeadGridRowData> = map { (type, total) ->
            when (type) {
                RESULT -> total!!.let { HeadToHeadGridRowData.Result.fromDbValue(it) }
                SHOOT_OFF -> total!!.let { HeadToHeadGridRowData.ShootOff.fromDbValue(it) }
                else -> HeadToHeadGridRowData.Total(type, 1, total)
            }
        }
        if (isShootOff && data.none { it is HeadToHeadGridRowData.ShootOff }) {
            data = data.plus(
                    HeadToHeadGridRowData.ShootOff(if (isShootOffWin) HeadToHeadResult.WIN else HeadToHeadResult.LOSS),
            )
        }
        return FullHeadToHeadSet(
                setNumber = if (!isShootOff) 1 else HeadToHeadUseCase.shootOffSet(teamSize),
                data = data,
                teamSize = teamSize,
                isSetPoints = isSetPoints,
                endSize = if (isShootOff) 1 else if (isTeam) 2 else 3,
        )
    }

    @Test
    fun testTeamTotal() {
        listOf(
                // Single item - ignores irrelevant fields
                listOf(SELF to 10, RESULT to 1, OPPONENT to 1) to 10,
                listOf(TEAM to 10, RESULT to 1, OPPONENT to 1) to 10,

                // Self only
                listOf(SELF to 3, TEAM_MATE to 7) to 3,
                listOf(SELF to null, TEAM_MATE to 7) to null,

                // Missing field
                listOf(TEAM_MATE to 7) to null,

                // Team takes precedence
                listOf(SELF to 3, TEAM_MATE to 7, TEAM to 15) to 15,
                listOf(SELF to 3, TEAM_MATE to 7, TEAM to null) to null,
        ).forEachIndexed { index, (data, expected) ->
            assertEquals(index.toString(), expected, data.asData(isTeam = false).teamEndScore)
        }

        listOf(
                // Single item - ignores irrelevant fields
                listOf(TEAM to 10, RESULT to 1, OPPONENT to 1) to 10,

                // Sum
                listOf(SELF to 3, TEAM_MATE to 7) to 10,
                listOf(SELF to 3, TEAM_MATE to null) to 3,
                listOf(SELF to null, TEAM_MATE to 7) to 7,

                // Missing field
                listOf(SELF to 10) to null,
                listOf(TEAM_MATE to 10) to null,

                // Team takes precedence
                listOf(SELF to 3, TEAM_MATE to 7, TEAM to 15) to 15,
                listOf(SELF to 3, TEAM_MATE to 7, TEAM to null) to null,
        ).forEachIndexed { index, (data, expected) ->
            assertEquals(index.toString(), expected, data.asData(isTeam = true).teamEndScore)
        }
    }

    @Test
    fun testResult() {
        listOf(
                // Empty
                listOf<Pair<HeadToHeadArcherType, Int?>>() to HeadToHeadResult.UNKNOWN,

                // Result takes precedence
                listOf(RESULT to 2, TEAM to 10, OPPONENT to 1) to HeadToHeadResult.WIN,
                listOf(RESULT to 1, TEAM to 10, OPPONENT to 1) to HeadToHeadResult.TIE,
                listOf(RESULT to 0, TEAM to 10, OPPONENT to 1) to HeadToHeadResult.LOSS,

                // Unknown
                listOf(TEAM to 1) to HeadToHeadResult.UNKNOWN,
                listOf(OPPONENT to 1) to HeadToHeadResult.UNKNOWN,

                // Incomplete
                listOf(TEAM to 1, OPPONENT to null) to HeadToHeadResult.INCOMPLETE,
                listOf(TEAM to null, OPPONENT to 1) to HeadToHeadResult.INCOMPLETE,
                listOf(TEAM to null, OPPONENT to null) to HeadToHeadResult.INCOMPLETE,

                // Simple
                listOf(TEAM to 10, OPPONENT to 1) to HeadToHeadResult.WIN,
                listOf(TEAM to 1, OPPONENT to 1) to HeadToHeadResult.TIE,
                listOf(TEAM to 1, OPPONENT to 10) to HeadToHeadResult.LOSS,
        ).forEach { (data, expected) ->
            assertEquals(expected, data.asData(isTeam = false).result)
        }

        listOf(
                // Result takes precedence over score
                (listOf(RESULT to 2, TEAM to 1, OPPONENT to 10) to true) to HeadToHeadResult.WIN,
                (listOf(RESULT to 0, TEAM to 10, OPPONENT to 1) to false) to HeadToHeadResult.LOSS,

                // Score takes precedence over isShootOffWin
                (listOf(TEAM to 1, OPPONENT to 10) to true) to HeadToHeadResult.LOSS,
                (listOf(TEAM to 1, OPPONENT to 10) to false) to HeadToHeadResult.LOSS,
                (listOf(TEAM to 10, OPPONENT to 1) to false) to HeadToHeadResult.WIN,
                (listOf(TEAM to 10, OPPONENT to 1) to true) to HeadToHeadResult.WIN,

                // isWin respected if score is tied
                (listOf(TEAM to 1, OPPONENT to 1) to true) to HeadToHeadResult.WIN,
                (listOf(TEAM to 1, OPPONENT to 1) to false) to HeadToHeadResult.LOSS,
        ).forEach { (input, expected) ->
            val (data, isWin) = input
            assertEquals(expected, data.asData(isTeam = false, isShootOff = true, isShootOffWin = isWin).result)
        }

        // ResultsShouldAgree
        assertThrows(IllegalStateException::class.java) {
            listOf(RESULT to 2, TEAM to 1, OPPONENT to 10)
                    .asData(isSetPoints = true, isShootOff = true, isShootOffWin = false).result
        }
        assertThrows(IllegalStateException::class.java) {
            listOf(RESULT to 0, TEAM to 10, OPPONENT to 1)
                    .asData(isSetPoints = true, isShootOff = true, isShootOffWin = true).result
        }
        // Result tie on shoot off
        assertThrows(IllegalStateException::class.java) {
            listOf(RESULT to 1, TEAM to 10, OPPONENT to 1).asData(isShootOff = true).result
        }
        // Result in compound match
        assertThrows(IllegalStateException::class.java) {
            listOf(RESULT to 2, TEAM to 10, OPPONENT to 1).asData(isSetPoints = false).result
        }
    }

    @Test
    fun testAsDatabaseDetails() {
        listOf(
                HeadToHeadGridRowData.Arrows(
                        type = SELF,
                        expectedArrowCount = 3,
                        arrows = List(3) { Arrow(10, it == 0) },
                        dbIds = listOf(),
                ) to List(3) {
                    DatabaseHeadToHeadDetail(
                            headToHeadArrowScoreId = 0,
                            shootId = 1,
                            matchNumber = 0,
                            type = SELF,
                            isTotal = false,
                            setNumber = 1,
                            arrowNumber = it + 1,
                            score = 10,
                            isX = it == 0,
                    )
                },
                HeadToHeadGridRowData.Arrows(
                        type = TEAM,
                        expectedArrowCount = 3,
                        arrows = listOf(8, 7, 6).map { Arrow(it, false) },
                        dbIds = listOf(1, 2, 3),
                ) to List(3) {
                    DatabaseHeadToHeadDetail(
                            headToHeadArrowScoreId = it + 1,
                            shootId = 1,
                            matchNumber = 0,
                            type = TEAM,
                            isTotal = false,
                            setNumber = 1,
                            arrowNumber = it + 1,
                            score = 8 - it,
                            isX = false,
                    )
                },
                HeadToHeadGridRowData.Total(
                        type = TEAM_MATE,
                        expectedArrowCount = 3,
                        total = 25,
                        dbId = null,
                ) to listOf(
                        DatabaseHeadToHeadDetail(
                                headToHeadArrowScoreId = 0,
                                shootId = 1,
                                matchNumber = 0,
                                type = TEAM_MATE,
                                isTotal = true,
                                setNumber = 1,
                                arrowNumber = 1,
                                score = 25,
                                isX = false,
                        )
                ),
                HeadToHeadGridRowData.Total(
                        type = TEAM,
                        expectedArrowCount = 3,
                        total = 25,
                        dbId = 1,
                ) to listOf(
                        DatabaseHeadToHeadDetail(
                                headToHeadArrowScoreId = 1,
                                shootId = 1,
                                matchNumber = 0,
                                type = TEAM,
                                isTotal = true,
                                setNumber = 1,
                                arrowNumber = 1,
                                score = 25,
                                isX = false,
                        )
                ),
                HeadToHeadGridRowData.EditableTotal(
                        type = TEAM_MATE,
                        expectedArrowCount = 3,
                        dbId = null,
                ).let { it.copy(text = it.text.onTextChanged("25")) } to listOf(
                        DatabaseHeadToHeadDetail(
                                headToHeadArrowScoreId = 0,
                                shootId = 1,
                                matchNumber = 0,
                                type = TEAM_MATE,
                                isTotal = true,
                                setNumber = 1,
                                arrowNumber = 1,
                                score = 25,
                                isX = false,
                        )
                ),
                HeadToHeadGridRowData.EditableTotal(
                        type = TEAM,
                        expectedArrowCount = 3,
                        dbId = 1,
                ).let { it.copy(text = it.text.onTextChanged("25")) } to listOf(
                        DatabaseHeadToHeadDetail(
                                headToHeadArrowScoreId = 1,
                                shootId = 1,
                                matchNumber = 0,
                                type = TEAM,
                                isTotal = true,
                                setNumber = 1,
                                arrowNumber = 1,
                                score = 25,
                                isX = false,
                        )
                ),
        ).forEach { (input, expected) ->
            assertEquals(
                    expected,
                    FullHeadToHeadSet(
                            setNumber = 1,
                            data = listOf(input),
                            teamSize = 1,
                            isSetPoints = true,
                            endSize = 3,
                    ).asDatabaseDetails(1, 0),
            )
        }

        assertEquals(
                listOf(
                        DatabaseHeadToHeadDetail(
                                headToHeadArrowScoreId = 0,
                                shootId = 3,
                                matchNumber = 4,
                                type = TEAM,
                                isTotal = true,
                                setNumber = 2,
                                arrowNumber = 1,
                                score = 25,
                                isX = false,
                        ),
                ),
                FullHeadToHeadSet(
                        setNumber = 2,
                        data = listOf(
                                HeadToHeadGridRowData.Total(
                                        type = TEAM,
                                        expectedArrowCount = 3,
                                        total = 25,
                                ),
                        ),
                        teamSize = 1,
                        isSetPoints = true,
                        endSize = 3,
                ).asDatabaseDetails(3, 4),
        )
    }

    @Test
    fun testGetArrows() {
        fun check(
                typeToTotal: List<Pair<HeadToHeadArcherType, Int>>,
                type: HeadToHeadArcherType,
                expected: RowArrows?,
                teamSize: Int = 1,
        ) {
            assertEquals(
                    expected,
                    typeToTotal.asData(isTeam = teamSize > 1).getArrows(type),
            )
        }

        // Simple exists or not
        check(
                typeToTotal = listOf(SELF to 10, OPPONENT to 1),
                type = SELF,
                expected = RowArrows.Total(10, 3),
        )
        check(
                typeToTotal = listOf(SELF to 10),
                type = OPPONENT,
                expected = null,
        )

        // Self/team interchangeable for team size 1
        check(
                typeToTotal = listOf(SELF to 10, OPPONENT to 1),
                type = TEAM,
                expected = RowArrows.Total(10, 3),
        )
        check(
                typeToTotal = listOf(TEAM to 10, OPPONENT to 1),
                type = SELF,
                expected = RowArrows.Total(10, 3),
        )

        // Team size 2 cannot get self alone
        check(
                typeToTotal = listOf(TEAM to 10, OPPONENT to 1),
                teamSize = 2,
                type = SELF,
                expected = null,
        )

        // Team size 2 can combine self and team mate for team
        check(
                typeToTotal = listOf(SELF to 5, TEAM_MATE to 10, OPPONENT to 1),
                teamSize = 2,
                type = TEAM,
                expected = RowArrows.Total(15, 4),
        )
    }
}
