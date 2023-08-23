package eywa.projectcodex.database.archer

import androidx.room.Entity
import androidx.room.PrimaryKey
import eywa.projectcodex.database.archer.DatabaseArcher.Companion.TABLE_NAME

@Entity(tableName = TABLE_NAME)
data class DatabaseArcher(
        @PrimaryKey(autoGenerate = true) val archerId: Int,
        var name: String
) {
    companion object {
        const val TABLE_NAME = "archers"
    }
}
