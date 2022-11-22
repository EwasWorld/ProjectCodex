package eywa.projectcodex.common.utils

import java.util.*

data class UpdateCalendarInfo(
        val day: Int? = null,
        val month: Int? = null,
        val year: Int? = null,
        val hours: Int? = null,
        val minutes: Int? = null,
) {
    init {
        require(
                day != null || month != null || year != null
                        || hours != null || minutes != null
        ) { "No new values set" }
    }

    /**
     * @return a copy of [initial] with the non-null values in this object replacing those in [initial].
     * For example if only [day] is not null, [initial]'s day will be replaced with [day] and all other fields will be
     * retained
     */
    fun updateCalendar(initial: Calendar): Calendar {
        val newValue = initial.clone() as Calendar
        day?.let { newValue.set(Calendar.DATE, day) }
        month?.let { newValue.set(Calendar.MONTH, month) }
        year?.let { newValue.set(Calendar.YEAR, year) }
        hours?.let { newValue.set(Calendar.HOUR_OF_DAY, hours) }
        minutes?.let { newValue.set(Calendar.MINUTE, minutes) }
        return newValue
    }
}