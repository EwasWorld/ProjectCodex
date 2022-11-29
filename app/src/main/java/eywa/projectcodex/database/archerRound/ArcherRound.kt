package eywa.projectcodex.database.archerRound

import androidx.room.*
import eywa.projectcodex.common.archeryObjects.GoldsType
import eywa.projectcodex.components.archerRoundScore.Handicap
import eywa.projectcodex.database.archerRound.ArcherRound.Companion.TABLE_NAME
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.arrowValue.getGolds
import eywa.projectcodex.database.arrowValue.getHits
import eywa.projectcodex.database.arrowValue.getScore
import eywa.projectcodex.database.rounds.*
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

data class FullArcherRoundInfo(
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
    val roundSubType by lazy {
        allRoundSubTypes?.find { it.subTypeId == archerRound.roundSubTypeId }
    }

    val roundDistances by lazy {
        allRoundDistances?.filter { it.subTypeId == (archerRound.roundSubTypeId ?: 1) }
    }

    val displayName by lazy { roundSubType?.name ?: round?.displayName }

    val distanceUnit by lazy { round?.distanceUnitStringRes() }

    val id: Int by lazy { archerRound.archerRoundId }

    val hits by lazy { arrows?.getHits() ?: 0 }

    val score by lazy { arrows?.getScore() ?: 0 }

    fun golds(type: GoldsType) = arrows?.getGolds(type) ?: 0

    val arrowsShot by lazy { arrows?.size ?: 0 }

    val remainingArrows by lazy {
        roundArrowCounts?.sumOf { it.arrowCount }?.minus(arrowsShot)
    }

    /**
     * Pairs of arrow counts to distances in order (earlier distances first)
     */
    val remainingArrowsAtDistances: List<Pair<Int, Int>>? by lazy {
        if ((remainingArrows ?: 0) <= 0) return@lazy null

        var shotCount = arrowsShot
        val arrowCounts = roundArrowCounts!!.toMutableList()

        while (shotCount > 0) {
            val nextCount = arrowCounts.first()
            if (nextCount.arrowCount < shotCount) {
                shotCount -= nextCount.arrowCount
                arrowCounts.removeAt(0)
            }
            else {
                shotCount = 0
                arrowCounts[0] = nextCount.copy(arrowCount = nextCount.arrowCount - shotCount)
            }
        }

        arrowCounts.map { count ->
            count.arrowCount to roundDistances!!.find { it.distanceNumber == count.distanceNumber }!!.distance
        }
    }

    val hasSurplusArrows by lazy { remainingArrows?.let { it < 0 } }

    private val isInnerTenArcher by lazy { false }

    val handicap by lazy {
        if (round == null) return@lazy null
        if (listOf(roundArrowCounts, roundDistances, arrows).any { it.isNullOrEmpty() }) return@lazy null

        Handicap.getHandicapForRound(
                round,
                roundArrowCounts!!,
                roundDistances!!,
                arrows!!.sumOf { it.score },
                isInnerTenArcher,
                arrows.count()
        )
    }

    val predictedScore by lazy {
        if (handicap == null) return@lazy null
        // No need to predict a score if round is already completed
        if (remainingArrows!! == 0) return@lazy null

        Handicap.getScoreForRound(
                round!!, roundArrowCounts!!, roundDistances!!, handicap!!, isInnerTenArcher, null
        )
    }
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