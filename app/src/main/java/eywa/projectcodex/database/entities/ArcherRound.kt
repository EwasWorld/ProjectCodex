package eywa.projectcodex.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/**
 * Main information about a round/session an archer has shot
 */
@Entity(tableName = "archer_rounds")
data class ArcherRound(
        @PrimaryKey(autoGenerate = true)
        val archerRoundId: Int,
        var dateShot: Date,
        val archerId: Int,
        val countsTowardsHandicap: Boolean,
        val bowId: Int? = null,
        val roundId: Int? = null,
        val roundSubTypeId: Int? = null,
        val goalScore: Int? = null,
        var shootStatus: String? = null
)