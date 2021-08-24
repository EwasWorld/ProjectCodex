package eywa.projectcodex.common.utils

import java.text.SimpleDateFormat
import java.util.*

// TODO Locale-based date/time format
enum class DateTimeFormat(val pattern: String) {
    LONG_DATE_FORMAT("dd MMM yy"),
    SHORT_DATE_FORMAT("dd/MM/yy"),
    TIME_FORMAT("HH:mm"),
    LONG_DATE_TIME_FORMAT("${LONG_DATE_FORMAT.pattern} ${TIME_FORMAT.pattern}"),
    SHORT_DATE_TIME_FORMAT("${SHORT_DATE_FORMAT.pattern} ${TIME_FORMAT.pattern}");

    fun format(date: Date): String {
        return SimpleDateFormat(this.pattern).format(date)
    }
}