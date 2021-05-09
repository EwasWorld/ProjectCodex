package eywa.projectcodex.database.rounds

import androidx.room.Entity

const val ROUND_DISTANCES_TABLE_NAME = "round_distances"

/**
 * The actual distances for each round subtype
 */
@Entity(tableName = ROUND_DISTANCES_TABLE_NAME, primaryKeys = ["roundId", "distanceNumber", "subTypeId"])
data class RoundDistance(
        val roundId: Int,
        val distanceNumber: Int,
        val subTypeId: Int = 1,
        /**
         * In meters if the round is metric, else it's in yards
         * @see Round.isMetric
         */
        val distance: Int
)