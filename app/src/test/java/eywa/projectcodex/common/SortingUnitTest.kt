package eywa.projectcodex.common

import eywa.projectcodex.common.utils.Sorting
import org.junit.Assert.assertEquals
import org.junit.Test

class SortingUnitTest {
    @Test
    fun testNumericalStringSort() {
        val expectedOrder = listOf(
                null,
                "",
                "test",
                "test 1",
                "test 1 and more",
                "test 2",
                "test 10",
        )

        assertEquals(
                expectedOrder,
                expectedOrder.sortedWith(Sorting.NUMERIC_STRING_SORT),
        )
        assertEquals(
                expectedOrder,
                expectedOrder.reversed().sortedWith(Sorting.NUMERIC_STRING_SORT),
        )
        expectedOrder.shuffled().let { shuffledList ->
            assertEquals(
                    "Shuffled list: " + shuffledList.joinToString(),
                    expectedOrder,
                    shuffledList.sortedWith(Sorting.NUMERIC_STRING_SORT),
            )
        }
    }
}