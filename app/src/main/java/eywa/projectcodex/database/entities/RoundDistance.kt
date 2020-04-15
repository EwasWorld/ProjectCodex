package eywa.projectcodex.database.entities

import androidx.room.Entity

/**
 * The actual distances for each round subtype
 */
@Entity(tableName = "round_distances", primaryKeys = ["roundId", "distanceNumber", "subTypeId"])
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