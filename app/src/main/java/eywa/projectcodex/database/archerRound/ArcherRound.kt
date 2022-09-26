package eywa.projectcodex.database.archerRound

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import eywa.projectcodex.database.rounds.Round
import java.util.*

const val ARCHER_ROUNDS_TABLE_NAME = "archer_rounds"

/**
 * Main information about a round/session an archer has shot
 */
@Entity(tableName = ARCHER_ROUNDS_TABLE_NAME)
data class ArcherRound(
        @PrimaryKey(autoGenerate = true)
        val archerRoundId: Int,
        val dateShot: Date,
        val archerId: Int,
        val countsTowardsHandicap: Boolean = true,
        val bowId: Int? = null,
        val roundId: Int? = null,
        val roundSubTypeId: Int? = null,
        val goalScore: Int? = null,
        val shootStatus: String? = null
)

data class ArcherRoundWithRoundInfoAndName(
        @Embedded(prefix = "ar_") val archerRound: ArcherRound,
        @Embedded val round: Round? = null,
        val roundSubTypeName: String? = null
) {
    val displayName: String?
        get() = roundSubTypeName ?: round?.displayName
    val id: Int
        get() = archerRound.archerRoundId

    init {
        // TODO_CURRENT Why is this causing crashes?
//        require(archerRound.roundId == round?.roundId) {
//            "Mismatched round id. ${archerRound.roundId} ${round?.roundId} $displayName"
//        }
//        require(
//                roundSubTypeName == null && archerRound.roundSubTypeId == null
//                        || roundSubTypeName != null && archerRound.roundSubTypeId != null
//        ) { "Mismatched subtype nullness" }
    }
}