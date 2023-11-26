package eywa.projectcodex.database.archer

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationAge
import eywa.projectcodex.database.archer.DatabaseArcher.Companion.TABLE_NAME

@Entity(tableName = TABLE_NAME)
data class DatabaseArcher(
        @PrimaryKey(autoGenerate = true) val archerId: Int,
        var name: String,
        @ColumnInfo(defaultValue = "1") val isGent: Boolean = true,
        @ColumnInfo(defaultValue = "1") val age: ClassificationAge = ClassificationAge.SENIOR,
) {
    companion object {
        const val TABLE_NAME = "archers"
    }
}
