package eywa.projectcodex.database.sightMarks

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import eywa.projectcodex.database.bow.DatabaseBow
import eywa.projectcodex.database.sightMarks.DatabaseSightMark.Companion.TABLE_NAME
import java.util.Calendar

@Entity(
        tableName = TABLE_NAME,
        foreignKeys = [
            ForeignKey(
                    entity = DatabaseBow::class,
                    parentColumns = ["bowId"],
                    childColumns = ["bowId"],
                    onDelete = ForeignKey.CASCADE,
            ),
        ],
)
data class DatabaseSightMark(
        @PrimaryKey(autoGenerate = true) val sightMarkId: Int,
        @ColumnInfo(index = true) val bowId: Int?,
        val distance: Int,
        val isMetric: Boolean,
        val dateSet: Calendar,
        val sightMark: Float,
        val note: String? = null,
        val isMarked: Boolean = false,
        val isArchived: Boolean = false,
        val useInPredictions: Boolean = true,
) {
    companion object {
        const val TABLE_NAME = "sight_marks"
    }
}
