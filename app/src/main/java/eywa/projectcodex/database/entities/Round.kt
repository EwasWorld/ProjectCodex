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
        /**
         * @see displayName
         *
         * Contains lower case alphanumerics only. Must be unique
         */
        val name: String,
        /**
         * Round display name e.g. Warwick, National, etc.
         */
        val displayName: String,
        val isOutdoor: Boolean,
        val isMetric: Boolean,
        val permittedFaces: List<String>,
        val isDefaultRound: Boolean = false,
        val fiveArrowEnd: Boolean = false
)