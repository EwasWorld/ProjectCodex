package eywa.projectcodex.database.shootData

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import eywa.projectcodex.common.utils.classificationTables.model.ClassificationBow
import eywa.projectcodex.database.arrows.DatabaseArrowCounter
import eywa.projectcodex.database.arrows.DatabaseArrowScore
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import eywa.projectcodex.database.rounds.RoundSubType
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHead
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHeadDetail
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHeadMatch
import java.util.Calendar

data class DatabaseFullShootInfo(
        @Embedded val shoot: DatabaseShoot,

        @Relation(
                parentColumn = "shootId",
                entityColumn = "shootId",
        )
        val shootRound: DatabaseShootRound? = null,

        @Relation(
                parentColumn = "shootId",
                entityColumn = "shootId",
        )
        val shootDetail: DatabaseShootDetail? = null,

        @Relation(
                parentColumn = "shootId",
                entityColumn = "shootId",
        )
        val arrowCounter: DatabaseArrowCounter? = null,

        @Relation(
                parentColumn = "shootId",
                entityColumn = "shootId",
        )
        val arrows: List<DatabaseArrowScore>? = null,

        @Relation(
                parentColumn = "shootId",
                entityColumn = "roundId",
                associateBy = Junction(
                        value = DatabaseShootRound::class,
                        parentColumn = "shootId",
                        entityColumn = "roundId",
                ),
        )
        val round: Round? = null,

        @Relation(
                parentColumn = "shootId",
                entityColumn = "roundId",
                associateBy = Junction(
                        value = DatabaseShootRound::class,
                        parentColumn = "shootId",
                        entityColumn = "roundId",
                ),
        )
        val roundArrowCounts: List<RoundArrowCount>? = null,

        /**
         * Note this is all subtypes relating to [round] as composite keys are not supported with @Relation.
         * It might be better to do this as part of the query rather than retrieving all subtypes
         * but we don't expect more than ~5 subtypes for any given round
         */
        @Relation(
                parentColumn = "shootId",
                entityColumn = "roundId",
                associateBy = Junction(
                        value = DatabaseShootRound::class,
                        parentColumn = "shootId",
                        entityColumn = "roundId",
                ),
        )
        private val allRoundSubTypes: List<RoundSubType>? = null,

        /**
         * Note this is all distances relating to [round] as composite keys are not supported with @Relation.
         * It might be better to do this as part of the query rather than retrieving all distances
         * but we don't expect more than ~5 subtypes for any given round
         */
        @Relation(
                parentColumn = "shootId",
                entityColumn = "roundId",
                associateBy = Junction(
                        value = DatabaseShootRound::class,
                        parentColumn = "shootId",
                        entityColumn = "roundId",
                ),
        )
        private val allRoundDistances: List<RoundDistance>? = null,

        @Relation(
                parentColumn = "shootId",
                entityColumn = "shootId",
        )
        val headToHead: DatabaseHeadToHead? = null,

        @Relation(
                parentColumn = "shootId",
                entityColumn = "shootId",
        )
        val headToHeadMatches: List<DatabaseHeadToHeadMatch>? = null,

        @Relation(
                parentColumn = "shootId",
                entityColumn = "shootId",
        )
        val headToHeadDetails: List<DatabaseHeadToHeadDetail>? = null,

        val isPersonalBest: Boolean? = null,

        /**
         * True if this is a PB that has been matched in another shoot
         * Only valid if [isPersonalBest] is true
         */
        val isTiedPersonalBest: Boolean? = null,

        val joinedDate: Calendar? = null,

        val bow: ClassificationBow = ClassificationBow.RECURVE,
) {
    val roundSubType
        get() = allRoundSubTypes
                ?.takeIf { it.isNotEmpty() && shootRound != null }
                ?.find { it.subTypeId == (shootRound!!.roundSubTypeId ?: 1) }
    val roundDistances
        get() = allRoundDistances
                ?.takeIf { it.isNotEmpty() && shootRound != null }
                ?.filter { it.subTypeId == (shootRound!!.roundSubTypeId ?: 1) }
}
