package eywa.projectcodex.database.entities

import androidx.room.Entity

/**
 * The number of arrows and the face sizes for each distance
 */
@Entity(tableName = "round_arrow_counts", primaryKeys = ["roundId", "distanceNumber"])
data class RoundArrowCount(
        val roundId: Int,
        /**
         * distanceNumber 1 is the first distance shot
         */
        val distanceNumber: Int,
        val faceSizeInCm: Double,
        val arrowCount: Int
)