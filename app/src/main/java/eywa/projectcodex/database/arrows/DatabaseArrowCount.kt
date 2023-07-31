package eywa.projectcodex.database.arrows

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import eywa.projectcodex.database.archerRound.ArcherRound
import eywa.projectcodex.database.arrows.DatabaseArrowCount.Companion.TABLE_NAME

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
data class DatabaseArrowCount(
        @PrimaryKey val archerRoundId: Int,
        val shotCount: Int,
) {
    init {
        require(shotCount >= 0) { "Shot count cannot be negative" }
    }

    companion object {
        const val TABLE_NAME = "arrow_counts"
    }
}
