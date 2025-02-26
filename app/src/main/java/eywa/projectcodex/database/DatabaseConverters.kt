package eywa.projectcodex.database

import androidx.room.TypeConverter
import eywa.projectcodex.common.utils.asCalendar
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationAge
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationBow
import eywa.projectcodex.database.archer.HandicapType
import java.util.Calendar
import java.util.Date

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
    fun roundFaceToInt(value: RoundFace) = value.ordinal

    @TypeConverter
    fun intToRoundFace(value: Int) = RoundFace.entries[value]

    @TypeConverter
    fun handicapTypeToInt(value: HandicapType) = value.ordinal

    @TypeConverter
    fun intToHandicapType(value: Int) = HandicapType.entries[value]

    @TypeConverter
    fun classificationAgeToInt(value: ClassificationAge) = value.ordinal

    @TypeConverter
    fun intToClassificationAge(value: Int) = ClassificationAge.entries[value]

    @TypeConverter
    fun classificationBowToInt(value: ClassificationBow) = value.ordinal

    @TypeConverter
    fun intToClassificationBow(value: Int) = ClassificationBow.entries[value]

    @TypeConverter
    fun stringToMapIntBool(value: String) = stringToList(value).associate {
        val (k, v) = it.split("-")
        k.toInt() to v.toBoolean()
    }

    @TypeConverter
    fun mapIntBoolToString(value: Map<Int, Boolean>) = listToString(value.entries.map { (key, value) -> "$key-$value" })
}
