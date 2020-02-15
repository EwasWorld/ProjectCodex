package eywa.projectcodex.database.entities

import androidx.room.Entity

/**
 * The actual distances for each round subtype
 */
@Entity(tableName = "round_sub_type_counts", primaryKeys = ["roundId", "distanceNumber", "subTypeId"])
data class RoundSubTypeCount(
        val roundId: Int,
        val distanceNumber: Int,
        val subTypeId: Int = 1,
        // This is in meters if the round isMetric, else it's in yards
        val distance: Int
)