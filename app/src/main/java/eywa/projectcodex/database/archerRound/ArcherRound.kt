package eywa.projectcodex.database.archerRound

import androidx.room.*
import eywa.projectcodex.database.archerRound.ArcherRound.Companion.TABLE_NAME
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import eywa.projectcodex.database.rounds.RoundSubType
import java.util.*


/**
 * Main information about a round/session an archer has shot
 */
@Entity(
        tableName = TABLE_NAME,
        foreignKeys = [
            ForeignKey(
                    entity = Round::class,
                    parentColumns = ["roundId"],
                    childColumns = ["roundId"],
                    onDelete = ForeignKey.SET_NULL,
            ),
            ForeignKey(
                    entity = RoundSubType::class,
                    parentColumns = ["roundId", "subTypeId"],
                    childColumns = ["roundId", "roundSubTypeId"],
                    onDelete = ForeignKey.SET_NULL,
            ),
        ],
)
data class ArcherRound(
        @PrimaryKey(autoGenerate = true) val archerRoundId: Int,
        // TODO Use Calendar
        val dateShot: Date,
        val archerId: Int,
        val countsTowardsHandicap: Boolean = true,
        val bowId: Int? = null,
        @ColumnInfo(index = true) val roundId: Int? = null,
        val roundSubTypeId: Int? = null,
        val goalScore: Int? = null,
        val shootStatus: String? = null
) {
    companion object {
        const val TABLE_NAME = "archer_rounds"
    }
}

data class DatabaseFullArcherRoundInfo(
        @Embedded val archerRound: ArcherRound,

        @Relation(
                parentColumn = "archerRoundId",
                entityColumn = "archerRoundId",
        )
        val arrows: List<ArrowValue>? = null,

        @Relation(
                parentColumn = "roundId",
                entityColumn = "roundId",
        )
        val round: Round? = null,

        @Relation(
                parentColumn = "roundId",
                entityColumn = "roundId",
        )
        val roundArrowCounts: List<RoundArrowCount>? = null,

        /**
         * Note this is all subtypes relating to [round] as composite keys are not supported with @Relation.
         * It might be better to do this as part of the query rather than retrieving all subtypes
         * but we don't expect more than ~5 subtypes for any given round
         */
        @Relation(
                parentColumn = "roundId",
                entityColumn = "roundId",
        )
        private val allRoundSubTypes: List<RoundSubType>? = null,

        /**
         * Note this is all distances relating to [round] as composite keys are not supported with @Relation.
         * It might be better to do this as part of the query rather than retrieving all distances
         * but we don't expect more than ~5 subtypes for any given round
         */
        @Relation(
                parentColumn = "roundId",
                entityColumn = "roundId",
        )
        private val allRoundDistances: List<RoundDistance>? = null,
) {
    val roundSubType
        get() = allRoundSubTypes?.find { it.subTypeId == archerRound.roundSubTypeId }
    val roundDistances
        get() = allRoundDistances?.filter { it.subTypeId == (archerRound.roundSubTypeId ?: 1) }
}

@Deprecated("Use FullArcherRoundInfo?")
data class ArcherRoundWithRoundInfoAndName(
        @Embedded val archerRound: ArcherRound,

        @Relation(
                parentColumn = "roundId",
                entityColumn = "roundId",
        )
        val round: Round? = null,

        /**
         * Note this is all subtypes relating to [round] as composite keys are not supported with @Relation.
         * It might be better to do this as part of the query rather than retrieving all subtypes
         * but we don't expect more than ~5 subtypes for any given round
         */
        @Relation(
                parentColumn = "roundId",
                entityColumn = "roundId",
        )
        val roundSubTypes: List<RoundSubType>? = null,
) {
    val roundSubType
        get() = roundSubTypes?.find { it.subTypeId == archerRound.roundSubTypeId }

    val displayName: String?
        get() = roundSubType?.name ?: round?.displayName

    val id: Int
        get() = archerRound.archerRoundId

    init {
        // TODO Why is this causing crashes?
//        require(archerRound.roundId == round?.roundId) {
//            "Mismatched round id. ${archerRound.roundId} ${round?.roundId} $displayName"
//        }
//        require(
//                roundSubTypeName == null && archerRound.roundSubTypeId == null
//                        || roundSubTypeName != null && archerRound.roundSubTypeId != null
//        ) { "Mismatched subtype nullness" }
    }
}
