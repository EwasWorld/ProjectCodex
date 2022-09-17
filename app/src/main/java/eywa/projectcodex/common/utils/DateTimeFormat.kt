package eywa.projectcodex.common.utils

import java.text.SimpleDateFormat
import java.util.*

// TODO Locale-based date/time format
enum class DateTimeFormat(val pattern: String) {
    // 1 Jan 21
    LONG_DATE("d MMM yy"),
    LONG_DAY_MONTH("d MMM"),

    // 01/01/21
    SHORT_DATE("dd/MM/yy"),

    TIME_24_HOUR("HH:mm"),
    TIME_12_HOUR("hh:mm a"),

    LONG_DATE_TIME("${LONG_DATE.pattern} ${TIME_24_HOUR.pattern}"),
    SHORT_DATE_TIME("${SHORT_DATE.pattern} ${TIME_24_HOUR.pattern}"),
    ;

    fun format(date: Date): String {
        return SimpleDateFormat(pattern).format(date)
    }
}