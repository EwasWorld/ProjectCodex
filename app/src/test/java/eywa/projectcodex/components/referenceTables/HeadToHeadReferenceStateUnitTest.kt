package eywa.projectcodex.components.referenceTables

import eywa.projectcodex.R
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.components.referenceTables.headToHead.HeadToHeadReferenceRow
import eywa.projectcodex.components.referenceTables.headToHead.HeadToHeadReferenceState
import eywa.projectcodex.components.referenceTables.headToHead.HeadToHeadUseCase
import org.junit.Assert.assertEquals
import org.junit.Test

class HeadToHeadReferenceStateUnitTest {
    @Test
    fun testTotalArchersFull_MinValues() {
        val state = HeadToHeadReferenceState()

        assertEquals(
                20,
                state.copy(
                        archerRank = state.archerRank.onTextChanged("5"),
                        opponentRank = state.opponentRank.onTextChanged("17"),
                        totalArchers = state.totalArchers.onTextChanged("20"),
                ).totalArchersFull.parsed,
        )

        assertEquals(
                ResOrActual.StringResource(R.string.err__invalid_must_be_at_least, listOf(2)),
                state.copy(
                        totalArchers = state.totalArchers.onTextChanged("1"),
                ).totalArchersFull.error,
        )

        assertEquals(
                ResOrActual.StringResource(R.string.err__invalid_must_be_at_least, listOf(17)),
                state.copy(
                        archerRank = state.archerRank.onTextChanged("3"),
                        opponentRank = state.opponentRank.onTextChanged("17"),
                        totalArchers = state.totalArchers.onTextChanged("5"),
                ).totalArchersFull.error,
        )

        assertEquals(
                ResOrActual.StringResource(R.string.err__invalid_must_be_at_least, listOf(17)),
                state.copy(
                        archerRank = state.archerRank.onTextChanged("17"),
                        opponentRank = state.opponentRank.onTextChanged("3"),
                        totalArchers = state.totalArchers.onTextChanged("5"),
                ).totalArchersFull.error,
        )

        assertEquals(
                ResOrActual.StringResource(R.string.err__invalid_must_be_at_least, listOf(2)),
                state.copy(
                        archerRank = state.archerRank.onTextChanged("-17"),
                        opponentRank = state.opponentRank.onTextChanged("-3"),
                        totalArchers = state.totalArchers.onTextChanged("1"),
                ).totalArchersFull.error,
        )
    }

    @Test
    fun testTableData() {
        val state = HeadToHeadReferenceState()

        assertEquals(
                null,
                state.copy(
                        totalArchers = state.totalArchers.onTextChanged("20"),
                ).tableData,
        )

        assertEquals(
                listOf(
                        HeadToHeadReferenceRow(6, HeadToHeadUseCase.getOpponents(6, 6)),
                ),
                state.copy(
                        archerRank = state.archerRank.onTextChanged("6"),
                ).tableData,
        )

        assertEquals(
                listOf(
                        HeadToHeadReferenceRow(6, HeadToHeadUseCase.getOpponents(6, 20)),
                ),
                state.copy(
                        archerRank = state.archerRank.onTextChanged("6"),
                        totalArchers = state.totalArchers.onTextChanged("20"),
                ).tableData,
        )

        assertEquals(
                listOf(
                        HeadToHeadReferenceRow(6, HeadToHeadUseCase.getOpponents(6, 20)),
                        HeadToHeadReferenceRow(17, HeadToHeadUseCase.getOpponents(17, 20)),
                ),
                state.copy(
                        archerRank = state.archerRank.onTextChanged("6"),
                        opponentRank = state.opponentRank.onTextChanged("17"),
                        totalArchers = state.totalArchers.onTextChanged("20"),
                ).tableData,
        )
    }
}
