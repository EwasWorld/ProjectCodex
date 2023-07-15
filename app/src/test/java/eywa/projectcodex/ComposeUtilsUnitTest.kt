package eywa.projectcodex

import eywa.projectcodex.common.sharedUi.ComposeUtils.orderPreviews
import org.junit.Assert.assertEquals
import org.junit.Test

class ComposeUtilsUnitTest {
    @Test
    fun testOrderPreviews() {
        val items = (0..11).toList()

        assertEquals(
                items,
                items.toList().orderPreviews().withIndex().sortedBy { it.index.toString() }.map { it.value },
        )
    }
}
