package eywa.projectcodex.database.rounds

import androidx.room.Entity

/**
 * Distinguishes distance variations of the same round. E.g. Long National, National, Short National, etc.
 */
@Entity(tableName = "round_sub_types", primaryKeys = ["roundId", "subTypeId"])
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
)