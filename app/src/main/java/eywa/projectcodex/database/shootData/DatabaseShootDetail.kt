package eywa.projectcodex.database.shootData

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import eywa.projectcodex.database.RoundFace
import eywa.projectcodex.database.shootData.DatabaseShootDetail.Companion.TABLE_NAME

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
data class DatabaseShootDetail(
        @PrimaryKey val shootId: Int,
        val face: RoundFace? = null,
        val distance: Int? = null,
        val isDistanceInMeters: Boolean = true,
        val faceSizeInCm: Double? = null,
) {
    companion object {
        const val TABLE_NAME = "shoot_details"
    }
}
