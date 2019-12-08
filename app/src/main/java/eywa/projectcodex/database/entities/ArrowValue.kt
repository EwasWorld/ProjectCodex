package eywa.projectcodex.database.entities

import androidx.room.Entity

@Entity(tableName = "arrow_value_table", primaryKeys = ["archerRoundsID", "arrowNumber"])
data class ArrowValue(
        val archerRoundsID: Int,
        val arrowNumber: Int,
        var score: Int,
        var isX: Boolean
)