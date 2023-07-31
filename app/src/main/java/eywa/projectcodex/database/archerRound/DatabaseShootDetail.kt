package eywa.projectcodex.database.archerRound

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import eywa.projectcodex.database.RoundFace
import eywa.projectcodex.database.archerRound.DatabaseShootDetail.Companion.TABLE_NAME

@Entity(
        tableName = TABLE_NAME,
        foreignKeys = [
            ForeignKey(
                    entity = ArcherRound::class,
                    parentColumns = ["archerRoundId"],
                    childColumns = ["archerRoundId"],
                    onDelete = ForeignKey.CASCADE,
            ),
        ]
)
data class DatabaseShootDetail(
        @PrimaryKey val archerRoundId: Int,
        val face: RoundFace? = null,
        val distance: Int? = null,
        val isDistanceInMeters: Boolean = true,
        val faceSizeInCm: Double? = null,
) {
    companion object {
        const val TABLE_NAME = "shoot_detail"
    }
}
