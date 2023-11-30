package eywa.projectcodex.components

import eywa.projectcodex.common.utils.DateTimeFormat
import eywa.projectcodex.components.arrowCountCalendar.ArrowCountCalendarDisplayData
import eywa.projectcodex.components.arrowCountCalendar.ArrowCountCalendarState
import eywa.projectcodex.database.shootData.DatabaseArrowCountCalendarData
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class ArrowCountCalendarUnitTest {
    @Test
    fun testState() {
        val date = DateTimeFormat.SHORT_DATE.parse("15/4/23")
        val firstDayOfWeek = Calendar.MONDAY
        date.firstDayOfWeek = firstDayOfWeek

        val state = ArrowCountCalendarState(
                monthDisplayed = date,
                firstDayOfWeek = firstDayOfWeek,
                arrowsShot = listOf(
                        DatabaseArrowCountCalendarData("02-04", 24),
                        DatabaseArrowCountCalendarData("31-03", 6),
                )
        )

        assertEquals(
                ArrowCountCalendarDisplayData.Entry.Day(31, false, 6),
                state.data.entries.find { it is ArrowCountCalendarDisplayData.Entry.Day && it.date == 31 && !it.isCurrentMonth },
        )
        assertEquals(
                ArrowCountCalendarDisplayData.Entry.Day(2, true, 24),
                state.data.entries.find { it is ArrowCountCalendarDisplayData.Entry.Day && it.date == 2 && it.isCurrentMonth },
        )
        assertEquals(
                24,
                state.data.totalForMonth,
        )
    }
}
