package eywa.projectcodex.database.rounds

import androidx.room.Entity
import androidx.room.ForeignKey
import eywa.projectcodex.database.rounds.RoundArrowCount.Companion.TABLE_NAME

/**
 * The number of arrows and the face sizes for each distance
 */
@Entity(
        tableName = TABLE_NAME,
        primaryKeys = ["roundId", "distanceNumber"],
        foreignKeys = [
            ForeignKey(
                    entity = Round::class,
                    parentColumns = ["roundId"],
                    childColumns = ["roundId"],
                    onDelete = ForeignKey.CASCADE,
            ),
        ]
)
data class RoundArrowCount(
        val roundId: Int,
        /**
         * distanceNumber 1 is the first distance shot
         */
        val distanceNumber: Int,
        val faceSizeInCm: Double,
        val arrowCount: Int
) {
    companion object {
        const val TABLE_NAME = "round_arrow_counts"
    }
}
