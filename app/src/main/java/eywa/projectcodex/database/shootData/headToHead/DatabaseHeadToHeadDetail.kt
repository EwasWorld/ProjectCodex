package eywa.projectcodex.database.shootData.headToHead

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadArcherType
import eywa.projectcodex.database.shootData.DatabaseShoot

@Entity(
        tableName = DatabaseHeadToHeadDetail.TABLE_NAME,
        foreignKeys = [
            ForeignKey(
                    entity = DatabaseShoot::class,
                    parentColumns = ["shootId"],
                    childColumns = ["shootId"],
                    onDelete = ForeignKey.CASCADE,
            ),
        ]
)
data class DatabaseHeadToHeadDetail(
        @PrimaryKey(autoGenerate = true)
        val headToHeadArrowScoreId: Int,
        val shootId: Int,
        val matchNumber: Int,
        val type: HeadToHeadArcherType,
        val isTotal: Boolean,
        val setNumber: Int,
        val arrowNumber: Int,
        val score: Int?,
        val isX: Boolean,
) {
    companion object {
        const val TABLE_NAME = "head_to_head_detail"
    }
}
