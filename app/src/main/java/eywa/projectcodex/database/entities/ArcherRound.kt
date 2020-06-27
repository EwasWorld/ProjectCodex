package eywa.projectcodex.database.entities

import androidx.room.*
import java.util.*

private const val TABLE_NAME = "archer_rounds"

/**
 * Main information about a round/session an archer has shot
 */
@Entity(tableName = TABLE_NAME)
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

class ArcherRoundWithName(
        @Embedded var archerRound: ArcherRound,
        var roundName: String? = null,
        var roundSubTypeName: String? = null
)