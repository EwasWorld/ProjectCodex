package eywa.projectcodex

import eywa.projectcodex.components.archeryObjects.GoldsType
import org.junit.Assert.assertFalse
import org.junit.Test

class GoldsTypeUnitTest {
    @Test
    fun testIsGold() {
        for (arrow in TestData.ARROWS.copyOfRange(0, 9)) {
            assertFalse(GoldsType.NINES.isGold(arrow))
            assertFalse(GoldsType.TENS.isGold(arrow))
            assertFalse(GoldsType.XS.isGold(arrow))
        }

        // 9 counts only for NINES
        assert(GoldsType.NINES.isGold(TestData.ARROWS[9]))
        assertFalse(GoldsType.TENS.isGold(TestData.ARROWS[9]))
        assertFalse(GoldsType.XS.isGold(TestData.ARROWS[9]))

        // 10 counts for NINES and TENS
        assert(GoldsType.NINES.isGold(TestData.ARROWS[10]))
        assert(GoldsType.TENS.isGold(TestData.ARROWS[10]))
        assertFalse(GoldsType.XS.isGold(TestData.ARROWS[10]))

        // X counts for all
        assert(GoldsType.NINES.isGold(TestData.ARROWS[11]))
        assert(GoldsType.TENS.isGold(TestData.ARROWS[11]))
        assert(GoldsType.XS.isGold(TestData.ARROWS[11]))
    }
}