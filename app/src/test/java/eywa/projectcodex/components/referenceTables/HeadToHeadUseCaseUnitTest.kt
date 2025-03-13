package eywa.projectcodex.components.referenceTables

import eywa.projectcodex.R
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.components.referenceTables.headToHead.HeadToHeadUseCase
import org.junit.Assert.assertEquals
import org.junit.Test

class HeadToHeadUseCaseUnitTest {
    @Test
    fun testGetOpponent() {
        assertEquals(
                listOf(2, 4, 8, 16, 32, 64),
                HeadToHeadUseCase.getOpponents(1, 64),
        )
        assertEquals(
                listOf(1, 3, 7, 15, 31, 63),
                HeadToHeadUseCase.getOpponents(2, 64),
        )
        assertEquals(
                listOf(2, 4, 8, 16, 32, null),
                HeadToHeadUseCase.getOpponents(1, 50),
        )
        assertEquals(
                listOf(2, 4, 8, 16, 32, 1),
                HeadToHeadUseCase.getOpponents(64, 64),
        )
        assertEquals(
                listOf(2, 4, 8, null),
                HeadToHeadUseCase.getOpponents(1, 9),
        )
    }

    @Test
    fun testMeetInHeat() {
        assertEquals(
                5,
                HeadToHeadUseCase.meetInHeat(1, 64),
        )
        assertEquals(
                0,
                HeadToHeadUseCase.meetInHeat(1, 2),
        )
        assertEquals(
                1,
                HeadToHeadUseCase.meetInHeat(4, 1),
        )
    }

    @Test
    fun testHeatNames() {
        assertEquals(
                ResOrActual.StringResource(R.string.head_to_head_ref__round_name, listOf(8)),
                HeadToHeadUseCase.heatName(3),
        )
        assertEquals(
                ResOrActual.StringResource(R.string.head_to_head_ref__round_name, listOf(16)),
                HeadToHeadUseCase.heatName(4),
        )
    }
}
