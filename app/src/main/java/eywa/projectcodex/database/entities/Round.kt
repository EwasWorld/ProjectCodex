package eywa.projectcodex.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Main round information
 */
@Entity(tableName = "rounds")
data class Round(
        @PrimaryKey(autoGenerate = true)
        val roundId: Int,
        val name: String, // Warwick, National, etc.
        val isOutdoor: Boolean,
        val isMetric: Boolean,
        val fiveArrowEnd: Boolean,
        val permittedFaces: List<String>,
        val isDefaultRound: Boolean = false
)