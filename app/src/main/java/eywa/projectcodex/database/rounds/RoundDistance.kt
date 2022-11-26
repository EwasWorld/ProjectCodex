package eywa.projectcodex.database.rounds

import androidx.room.Entity
import androidx.room.ForeignKey
import eywa.projectcodex.database.rounds.RoundDistance.Companion.TABLE_NAME

/**
 * The actual distances for each round subtype
 */
@Entity(
        tableName = TABLE_NAME,
        primaryKeys = ["roundId", "distanceNumber", "subTypeId"],
        foreignKeys = [
            ForeignKey(
                    entity = Round::class,
                    parentColumns = ["roundId"],
                    childColumns = ["roundId"],
                    onDelete = ForeignKey.CASCADE,
            ),
        ],
)
data class RoundDistance(
        val roundId: Int,
        val distanceNumber: Int,
        val subTypeId: Int = 1,
        /**
         * In meters if the round is metric, else it's in yards
         * @see Round.isMetric
         */
        val distance: Int
) {
    companion object {
        const val TABLE_NAME = "round_distances"
    }
}