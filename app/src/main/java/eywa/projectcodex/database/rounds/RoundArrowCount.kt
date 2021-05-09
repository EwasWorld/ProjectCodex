package eywa.projectcodex.database.rounds

import androidx.room.Entity

const val ROUND_ARROW_COUNTS_TABLE_NAME = "round_arrow_counts"

/**
 * The number of arrows and the face sizes for each distance
 */
@Entity(tableName = ROUND_ARROW_COUNTS_TABLE_NAME, primaryKeys = ["roundId", "distanceNumber"])
data class RoundArrowCount(
        val roundId: Int,
        /**
         * distanceNumber 1 is the first distance shot
         */
        val distanceNumber: Int,
        val faceSizeInCm: Double,
        val arrowCount: Int
)