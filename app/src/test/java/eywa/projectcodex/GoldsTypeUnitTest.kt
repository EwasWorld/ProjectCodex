package eywa.projectcodex

import org.junit.Test
import org.junit.Assert.assertFalse

class GoldsTypeUnitTest {
    private var arrows = arrayOf(
            Arrow(0, false), Arrow(1, false), Arrow(2, false),
            Arrow(3, false), Arrow(4, false), Arrow(5, false),
            Arrow(6, false), Arrow(7, false), Arrow(8, false),
            Arrow(9, false), Arrow(10, false), Arrow(10, true)
    )

    @Test
    fun testIsGold() {
        for (arrow in arrows.copyOfRange(0, 9)) {
            assertFalse(GoldsType.NINES.isGold(arrow))
            assertFalse(GoldsType.TENS.isGold(arrow))
            assertFalse(GoldsType.XS.isGold(arrow))
        }

        // 9 counts only for NINES
        assert(GoldsType.NINES.isGold(arrows[9]))
        assertFalse(GoldsType.TENS.isGold(arrows[9]))
        assertFalse(GoldsType.XS.isGold(arrows[9]))

        // 10 counts for NINES and TENS
        assert(GoldsType.NINES.isGold(arrows[10]))
        assert(GoldsType.TENS.isGold(arrows[10]))
        assertFalse(GoldsType.XS.isGold(arrows[10]))

        // X counts for all
        assert(GoldsType.NINES.isGold(arrows[11]))
        assert(GoldsType.TENS.isGold(arrows[11]))
        assert(GoldsType.XS.isGold(arrows[11]))
    }
}