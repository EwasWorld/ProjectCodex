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
    fun stringToList(value: String?) = if (value.isNullOrBlank()) listOf() else value.split(':')

    @TypeConverter
    fun listToString(value: List<String>?) = value?.joinToString(":")

    @TypeConverter
    fun listRoundFaceToString(value: List<RoundFace>?) = value?.map { roundFaceToInt(it).toString() }

    @TypeConverter
    fun stringToListRoundFace(value: String?) =
            value?.let { stringToList(value).map { intToRoundFace(it.toInt()) } }

    @TypeConverter
    fun roundFaceToInt(value: RoundFace) = value.toDbData()

    @TypeConverter
    fun intToRoundFace(value: Int) = RoundFace.fromDbData(value)
}
