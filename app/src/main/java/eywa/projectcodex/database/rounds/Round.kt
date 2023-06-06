package eywa.projectcodex.database.rounds

import androidx.room.*
import eywa.projectcodex.R
import eywa.projectcodex.common.sharedUi.helperInterfaces.NamedItem
import eywa.projectcodex.database.rounds.Round.Companion.TABLE_NAME

/**
 * Main round information
 */
@Entity(tableName = TABLE_NAME)
data class Round(
        @PrimaryKey(autoGenerate = true) val roundId: Int,
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
        val fiveArrowEnd: Boolean = false,
        @ColumnInfo(defaultValue = "NULL") val legacyName: String? = null,
        @ColumnInfo(defaultValue = "NULL") val defaultRoundId: Int? = null,
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

/**
 * Resource id of the unit for a [Round]'s distances (e.g. yd/m)
 */
fun Round?.distanceUnitStringRes() = when {
    this == null -> null
    isMetric -> R.string.units_meters_short
    else -> R.string.units_yards_short
}
