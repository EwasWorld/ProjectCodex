package eywa.projectcodex.database.arrows

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import eywa.projectcodex.database.arrows.DatabaseArrowCounter.Companion.TABLE_NAME
import eywa.projectcodex.database.shootData.DatabaseShoot

@Entity(
        tableName = TABLE_NAME,
        foreignKeys = [
            ForeignKey(
                    entity = DatabaseShoot::class,
                    parentColumns = ["shootId"],
                    childColumns = ["shootId"],
                    onDelete = ForeignKey.CASCADE,
            ),
        ]
)
data class DatabaseArrowCounter(
        @PrimaryKey val shootId: Int,
        val shotCount: Int,
) {
    init {
        require(shotCount >= 0) { "Shot count cannot be negative" }
    }

    companion object {
        const val TABLE_NAME = "arrow_counters"
    }
}
