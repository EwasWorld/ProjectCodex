package eywa.projectcodex.database

import androidx.room.TypeConverter
import eywa.projectcodex.common.utils.asCalendar
import java.util.*

class DatabaseConverters {
    @TypeConverter
    fun timestampToDate(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun dateToTimestamp(value: Date?): Long? = value?.time

    @TypeConverter
    fun timestampToCalendar(value: Long?): Calendar? = timestampToDate(value)?.asCalendar()

    @TypeConverter
    fun calendarToTimestamp(value: Calendar?): Long? = dateToTimestamp(value?.time)

    @TypeConverter
    fun toStringList(value: String): List<String> = if (value.isEmpty()) listOf() else value.split(':')

    @TypeConverter
    fun toFlatString(value: List<String>): String = value.joinToString(":")
}
