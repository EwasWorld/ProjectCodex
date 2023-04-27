package eywa.projectcodex.components.sightMarks

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*

class SightMarksStateUnitTest {
    @Test
    fun test() {
        val sights = listOf(
                SightMark(30, true, Calendar.getInstance(), 5f),
                SightMark(30, true, Calendar.getInstance(), 4.5f),
                SightMark(30, true, Calendar.getInstance(), 4f),
                SightMark(30, true, Calendar.getInstance(), 3.5f),
                SightMark(50, false, Calendar.getInstance(), 3f),
        )
        val state = SightMarksState(sightMarks = sights, isHighestNumberAtTheTop = false)

        assertEquals(
                sights.map { 0.25f + (it.sightMark - 3f) / 2f },
                sights.map { state.getSightMarkAsPercentage(it) },
        )
    }
}
