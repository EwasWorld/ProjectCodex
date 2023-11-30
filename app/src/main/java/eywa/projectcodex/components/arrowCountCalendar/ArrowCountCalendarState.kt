package eywa.projectcodex.components.arrowCountCalendar

import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.database.shootData.DatabaseArrowCountCalendarData
import java.util.Calendar

data class ArrowCountCalendarState(
        val monthDisplayed: Calendar = Calendar.getInstance(),
        val firstDayOfWeek: Int = Calendar.MONDAY,
        private val arrowsShot: List<DatabaseArrowCountCalendarData> = emptyList(),
) {
    val calendarHeadings =
            DayOfWeek.values()
                    // firstDayOfWeek is 1-indexed, hence the `-1`s
                    .let { it.drop(firstDayOfWeek - 1) + it.take(firstDayOfWeek - 1) }
                    .map { it.shortString }
                    .plus(ResOrActual.Actual("Total"))

    val data: ArrowCountCalendarDisplayData

    init {
        val entries = mutableListOf<ArrowCountCalendarDisplayData.Entry>()

        // Note mutable datatype, is incremented in for loop
        val dateToCheck = getFirstDateToDisplay()
        val (lastDay, lastMonth) = getLastDateToDisplayExclusive()
                .let { it.get(Calendar.DATE) to it.get(Calendar.MONTH) }
        val currentMonth = monthDisplayed.get(Calendar.MONTH) + 1

        var monthlyTotal = 0
        var weeklyTotal = 0

        do {
            val day = dateToCheck.get(Calendar.DATE)
            val month = dateToCheck.get(Calendar.MONTH) + 1
            val count = arrowsShot
                    .filter { day == it.day && month == it.month }
                    .takeIf { it.isNotEmpty() }
                    ?.sumOf { it.count }
            if (count != null) {
                weeklyTotal += count
                if (month == currentMonth) {
                    monthlyTotal += count
                }
            }

            entries.add(ArrowCountCalendarDisplayData.Entry.Day(day, month == currentMonth, count))

            dateToCheck.add(Calendar.DATE, 1)
            if (dateToCheck.get(Calendar.DAY_OF_WEEK) == firstDayOfWeek) {
                entries.add(ArrowCountCalendarDisplayData.Entry.WeeklyTotal(weeklyTotal))
                weeklyTotal = 0
            }
        } while (dateToCheck.get(Calendar.DATE) != lastDay || dateToCheck.get(Calendar.MONTH) != lastMonth)

        data = ArrowCountCalendarDisplayData(monthlyTotal, entries)
    }

    /**
     * The first day of the week that includes the first day of the month
     */
    private fun getFirstDateToDisplay(): Calendar {
        val firstDate = monthDisplayed.clone() as Calendar
        firstDate.firstDayOfWeek = firstDayOfWeek
        firstDate.set(Calendar.DATE, 1)
        // Roll back to first day of week
        while (firstDate.get(Calendar.DAY_OF_WEEK) != firstDayOfWeek) {
            firstDate.add(Calendar.DATE, -1)
        }
        return firstDate
    }

    /**
     * The last day of the week that includes the last day of the month
     */
    private fun getLastDateToDisplayExclusive(): Calendar {
        val lastDate = monthDisplayed.clone() as Calendar
        lastDate.set(Calendar.DATE, 28)
        // Roll forward to first day of next month
        while (lastDate.get(Calendar.MONTH) == monthDisplayed.get(Calendar.MONTH)
            || lastDate.get(Calendar.DAY_OF_WEEK) != firstDayOfWeek
        ) {
            lastDate.add(Calendar.DATE, 1)
        }

        return lastDate
    }
}

data class ArrowCountCalendarDisplayData(
        val totalForMonth: Int,
        val entries: List<Entry>,
) {
    sealed class Entry {
        data class Day(
                val date: Int,
                val isCurrentMonth: Boolean,
                val count: Int?,
        ) : Entry()

        data class WeeklyTotal(val count: Int) : Entry()
    }
}


/**
 * Note DO NOT change the order of these, they match the order of [Calendar.SUNDAY] etc.
 */
enum class DayOfWeek {
    SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY
    ;

    val shortString: ResOrActual<String> = ResOrActual.Actual(name.take(1))
}
