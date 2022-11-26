package eywa.projectcodex.database.rounds

import androidx.room.Entity
import androidx.room.ForeignKey
import eywa.projectcodex.common.sharedUi.helperInterfaces.NamedItem
import eywa.projectcodex.database.rounds.RoundSubType.Companion.TABLE_NAME

/**
 * Distinguishes distance variations of the same round. E.g. Long National, National, Short National, etc.
 */
@Entity(
        tableName = TABLE_NAME,
        primaryKeys = ["roundId", "subTypeId"],
        foreignKeys = [
            ForeignKey(
                    entity = Round::class,
                    parentColumns = ["roundId"],
                    childColumns = ["roundId"],
                    onDelete = ForeignKey.CASCADE,
            ),
        ]
)
data class RoundSubType(
        val roundId: Int,
        val subTypeId: Int,
        /**
         * Should be recognisable as a standalone name
         */
        val name: String? = null,
        /**
         * null -> no age restriction
         * 0 -> invalid (e.g. gents no matter their age cannot shoot a Bristol V
         *               as their closest is the under 12s Bristol IV
         * x -> valid only for archers under the age x
         */
        val gents: Int? = null,
        /**
         * @see gents
         */
        val ladies: Int? = null
) : NamedItem {
    override val label: String
        get() = name!!

    companion object {
        const val TABLE_NAME = "round_sub_types"
    }
}