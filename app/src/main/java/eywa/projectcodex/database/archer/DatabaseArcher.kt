package eywa.projectcodex.database.archer

import androidx.room.Entity
import androidx.room.PrimaryKey
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationAge
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationBow
import eywa.projectcodex.database.archer.DatabaseArcher.Companion.TABLE_NAME

@Entity(tableName = TABLE_NAME)
data class DatabaseArcher(
        @PrimaryKey(autoGenerate = true) val archerId: Int,
        var name: String,
        val isGent: Boolean = true,
        val age: ClassificationAge = ClassificationAge.SENIOR,
        val bow: ClassificationBow = ClassificationBow.RECURVE,
) {
    companion object {
        const val TABLE_NAME = "archers"
    }
}
