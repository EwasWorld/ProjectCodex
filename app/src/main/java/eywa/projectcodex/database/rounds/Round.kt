package eywa.projectcodex.database.rounds

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import eywa.projectcodex.R
import eywa.projectcodex.common.sharedUi.helperInterfaces.NamedItem
import eywa.projectcodex.common.utils.ResOrActual
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
        val fiveArrowEnd: Boolean = false,
        @ColumnInfo(defaultValue = "NULL") val legacyName: String? = null,
        @ColumnInfo(defaultValue = "NULL") val defaultRoundId: Int? = null,
) : NamedItem {
    override val label
        get() = ResOrActual.Actual(displayName)

    val isImperial
        get() = !isMetric

    companion object {
        const val TABLE_NAME = "rounds"
    }
}

/**
 * Resource id of the unit for a [Round]'s distances (e.g. yd/m)
 */
fun Round?.getDistanceUnitRes() = getDistanceUnitRes(this?.isMetric)

fun getDistanceUnitRes(isMetric: Boolean?) = when {
    isMetric == null -> null
    isMetric -> R.string.units_meters_short
    else -> R.string.units_yards_short
}
