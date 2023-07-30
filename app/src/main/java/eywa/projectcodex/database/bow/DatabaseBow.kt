package eywa.projectcodex.database.bow

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import eywa.projectcodex.database.bow.DatabaseBow.Companion.TABLE_NAME

@Entity(tableName = TABLE_NAME)
data class DatabaseBow(
        @PrimaryKey(autoGenerate = true) val id: Int,
        @ColumnInfo(defaultValue = "Default") val name: String,
        @ColumnInfo(defaultValue = "NULL") val description: String? = null,
        val isSightMarkDiagramHighestAtTop: Boolean = false,
) {
    companion object {
        const val TABLE_NAME = "bows"
    }
}
