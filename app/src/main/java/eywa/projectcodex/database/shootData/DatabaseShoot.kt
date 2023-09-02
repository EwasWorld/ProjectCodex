package eywa.projectcodex.database.shootData

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import eywa.projectcodex.database.archer.DatabaseArcher
import eywa.projectcodex.database.bow.DatabaseBow
import eywa.projectcodex.database.shootData.DatabaseShoot.Companion.TABLE_NAME
import java.util.*


/**
 * Main information about a round/session an archer has shot
 */
@Entity(
        tableName = TABLE_NAME,
        foreignKeys = [
            ForeignKey(
                    entity = DatabaseBow::class,
                    parentColumns = ["id"],
                    childColumns = ["bowId"],
                    onDelete = ForeignKey.SET_NULL,
            ),
            ForeignKey(
                    entity = DatabaseArcher::class,
                    parentColumns = ["archerId"],
                    childColumns = ["archerId"],
                    onDelete = ForeignKey.SET_NULL,
            ),
        ],
)
data class DatabaseShoot(
        @PrimaryKey(autoGenerate = true) val shootId: Int,
        val dateShot: Calendar,
        val archerId: Int,
        val countsTowardsHandicap: Boolean = true,
        @ColumnInfo(index = true) val bowId: Int? = null,
        val goalScore: Int? = null,
        val shootStatus: String? = null,
        @ColumnInfo(defaultValue = "0") val joinWithPrevious: Boolean = false,
) {
    companion object {
        const val TABLE_NAME = "archer_rounds"
    }
}
