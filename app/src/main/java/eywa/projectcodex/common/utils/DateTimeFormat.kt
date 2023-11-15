package eywa.projectcodex.common.utils

import android.os.Build
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


fun Long.asCalendar(): Calendar = Date(this).asCalendar()

fun Date.asCalendar(): Calendar =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Calendar.Builder().setInstant(this).build()
        }
        else {
            Calendar.getInstance(Locale.getDefault()).apply {
                set(year + 1900, month, date, hours, minutes, seconds)
            }
        }

// TODO Locale-based date/time format
enum class DateTimeFormat(val pattern: String) {
    // 1 Jan 2021
    LONG_DATE_FULL_YEAR("d MMM yyyy"),

    // 1 Jan 21
    LONG_DATE("d MMM yy"),
    LONG_DAY_MONTH("d MMM"),

    // 01/01/21
    SHORT_DATE("dd/MM/yy"),
    SEMANTICS_DATE("yy/MM/dd"),

    // November 2023
    LONG_MONTH_YEAR("MMMMM yyyy"),

    TIME_24_HOUR("HH:mm"),
    TIME_12_HOUR("hh:mm a"),

    LONG_DATE_TIME("${LONG_DATE.pattern} ${TIME_24_HOUR.pattern}"),
    SHORT_DATE_TIME("${SHORT_DATE.pattern} ${TIME_24_HOUR.pattern}"),
    ;

    fun format(date: Calendar): String = SimpleDateFormat(pattern, Locale.UK).format(date.time)
    fun parse(date: String): Calendar = SimpleDateFormat(pattern, Locale.UK).parse(date)!!.asCalendar()
}
