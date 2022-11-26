package eywa.projectcodex.database.rounds

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import eywa.projectcodex.common.sharedUi.helperInterfaces.NamedItem
import eywa.projectcodex.database.rounds.Round.Companion.TABLE_NAME

/**
 * Main round information
 */
@Entity(tableName = TABLE_NAME)
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
) : NamedItem {
    override val label: String
        get() = displayName

    companion object {
        const val TABLE_NAME = "rounds"
    }
}

data class FullRoundInfo(
        @Embedded val round: Round,

        @Relation(parentColumn = "roundId", entityColumn = "roundId")
        val roundSubTypes: List<RoundSubType>? = null,

        @Relation(parentColumn = "roundId", entityColumn = "roundId")
        val roundArrowCounts: List<RoundArrowCount>? = null,

        @Relation(parentColumn = "roundId", entityColumn = "roundId")
        val roundDistances: List<RoundDistance>? = null,
)
