package eywa.projectcodex.database.entities

import androidx.room.Entity

/**
 * Distinguishes distance variations of the same round. E.g. Long National, National, Short National, etc.
 */
@Entity(tableName = "round_sub_types", primaryKeys = ["roundId", "subTypeId"])
data class RoundSubType(
        val roundId: Int,
        val subTypeId: Int,
        val name: String? = null,
        val gents: Int? = null,
        val ladies: Int? = null
)