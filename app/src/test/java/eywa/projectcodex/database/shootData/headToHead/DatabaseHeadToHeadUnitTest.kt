package eywa.projectcodex.database.shootData.headToHead

import eywa.projectcodex.R
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.common.utils.ResOrActual.*
import org.junit.Assert.assertEquals
import org.junit.Test

class DatabaseHeadToHeadUnitTest {
    @Test
    fun testExpectedOpponentRank() {
        listOf(
                OpponentRankParameters(
                        matchNumber = 1,
                        qualiRank = 1,
                        totalArchers = null,
                        expectedResult = null,
                ),
                OpponentRankParameters(
                        matchNumber = 1,
                        qualiRank = null,
                        totalArchers = 60,
                        expectedResult = null,
                ),
                OpponentRankParameters(
                        matchNumber = 1,
                        qualiRank = 1,
                        totalArchers = 60,
                        expectedResult = Opponent.Bye,
                ),
                OpponentRankParameters(
                        matchNumber = 2,
                        qualiRank = 1,
                        totalArchers = 60,
                        expectedResult = Opponent.Rank(32),
                ),
                OpponentRankParameters(
                        matchNumber = 3,
                        qualiRank = 1,
                        totalArchers = 60,
                        expectedResult = Opponent.Rank(16),
                ),
                OpponentRankParameters(
                        matchNumber = 6,
                        qualiRank = 1,
                        totalArchers = 60,
                        expectedResult = Opponent.Rank(2),
                ),
                // Too many matches
                OpponentRankParameters(
                        matchNumber = 7,
                        qualiRank = 1,
                        totalArchers = 60,
                        expectedResult = null,
                ),
        ).forEach {
            assertEquals(
                    it.toString(),
                    it.expectedResult,
                    it.calculate,
            )
        }
    }

    @Test
    fun testDescription() {
        listOf(
                DatabaseHeadToHead(
                        shootId = 1,
                        isSetPoints = true,
                        teamSize = 1,
                        qualificationRank = null,
                        endSize = null,
                        totalArchers = null,
                ) to DescriptionParameters(
                        team = StringResource(R.string.head_to_head__info_individual),
                        style = StringResource(R.string.create_round__h2h_style_recurve),
                        rank = Blank,
                        totalArchers = Blank,
                        format = Blank,
                ),
                DatabaseHeadToHead(
                        shootId = 2,
                        isSetPoints = false,
                        teamSize = 3,
                        qualificationRank = 5,
                        endSize = 3,
                        totalArchers = 60,
                ) to DescriptionParameters(
                        team = StringResource(R.string.head_to_head__info_teams, listOf(3)),
                        style = StringResource(R.string.create_round__h2h_style_compound),
                        rank = StringResource(R.string.head_to_head__info_quali_rank_total_archers, listOf(5, 60)),
                        totalArchers = Blank,
                        format = StringResource(R.string.head_to_head__info_non_standard),
                ),
                DatabaseHeadToHead(
                        shootId = 3,
                        isSetPoints = false,
                        teamSize = 1,
                        qualificationRank = 5,
                        endSize = null,
                        totalArchers = null,
                ) to DescriptionParameters(
                        team = StringResource(R.string.head_to_head__info_individual),
                        style = StringResource(R.string.create_round__h2h_style_compound),
                        rank = StringResource(R.string.head_to_head__info_quali_rank, listOf(5)),
                        totalArchers = Blank,
                        format = Blank,
                ),
                DatabaseHeadToHead(
                        shootId = 4,
                        isSetPoints = true,
                        teamSize = 3,
                        qualificationRank = null,
                        endSize = null,
                        totalArchers = 60,
                ) to DescriptionParameters(
                        team = StringResource(R.string.head_to_head__info_teams, listOf(3)),
                        style = StringResource(R.string.create_round__h2h_style_recurve),
                        rank = Blank,
                        totalArchers = StringResource(R.string.head_to_head__info_total_archers, listOf(60)),
                        format = Blank,
                ),
        ).forEach { (h2h, expected) ->
            assertEquals(
                    h2h.toString(),
                    expected.description,
                    h2h.description,
            )
        }
    }

    private data class OpponentRankParameters(
            val matchNumber: Int,
            val qualiRank: Int?,
            val totalArchers: Int?,
            val expectedResult: Opponent?,
    ) {
        val calculate
            get() = DatabaseHeadToHead(
                    shootId = 1,
                    isSetPoints = true,
                    teamSize = 1,
                    qualificationRank = qualiRank,
                    endSize = null,
                    totalArchers = totalArchers,
            ).getExpectedOpponentRank(matchNumber)
    }

    private data class DescriptionParameters(
            val team: ResOrActual<String>,
            val style: ResOrActual<String>,
            val rank: ResOrActual<String>,
            val totalArchers: ResOrActual<String>,
            val format: ResOrActual<String>,
    ) {
        val description
            get() = JoinToStringResource(
                    listOf(
                            team,
                            StringResource(R.string.head_to_head__info_separator),
                            style,
                            rank,
                            totalArchers,
                            format,
                    ),
                    Blank,
            )
    }
}
