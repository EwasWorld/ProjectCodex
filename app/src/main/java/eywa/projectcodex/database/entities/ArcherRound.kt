package eywa.projectcodex.database.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

const val ARCHER_ROUNDS_TABLE_NAME = "archer_rounds"

/**
 * Main information about a round/session an archer has shot
 */
@Entity(tableName = ARCHER_ROUNDS_TABLE_NAME)
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

data class ArcherRoundWithRoundInfoAndName(
        @Embedded(prefix = "ar_") var archerRound: ArcherRound,
        @Embedded var round: Round? = null,
        var roundSubTypeName: String? = null
)