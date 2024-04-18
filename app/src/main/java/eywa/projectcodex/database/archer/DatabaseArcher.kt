package eywa.projectcodex.database.archer

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import eywa.projectcodex.R
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationAge
import eywa.projectcodex.database.archer.DatabaseArcher.Companion.TABLE_NAME

fun getGenderString(isGent: Boolean) = ResOrActual.StringResource(
        if (isGent) R.string.archer_info__gender_male
        else R.string.archer_info__gender_female
)

@Entity(tableName = TABLE_NAME)
data class DatabaseArcher(
        @PrimaryKey(autoGenerate = true) val archerId: Int,
        var name: String,
        @ColumnInfo(defaultValue = "1") val isGent: Boolean = true,
        @ColumnInfo(defaultValue = "1") val age: ClassificationAge = ClassificationAge.SENIOR,
) {
    val genderString
        get() = getGenderString(isGent)

    companion object {
        const val TABLE_NAME = "archers"
    }
}
