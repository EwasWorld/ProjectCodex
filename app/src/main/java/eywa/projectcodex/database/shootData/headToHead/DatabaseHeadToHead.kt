package eywa.projectcodex.database.shootData.headToHead

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import eywa.projectcodex.database.shootData.DatabaseShoot

@Entity(
        tableName = DatabaseHeadToHead.TABLE_NAME,
        foreignKeys = [
            ForeignKey(
                    entity = DatabaseShoot::class,
                    parentColumns = ["shootId"],
                    childColumns = ["shootId"],
                    onDelete = ForeignKey.CASCADE,
            ),
        ]
)
data class DatabaseHeadToHead(
        @PrimaryKey val shootId: Int,
        val isRecurveStyle: Boolean,
        val teamSize: Int,
        val qualificationRank: Int?,
) {
    companion object {
        const val TABLE_NAME = "head_to_head"
    }
}
