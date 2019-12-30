package eywa.projectcodex.database.entities

import androidx.room.Entity

@Entity(tableName = "arrow_values", primaryKeys = ["archerRoundId", "arrowNumber"])
data class ArrowValue(
        val archerRoundId: Int,
        val arrowNumber: Int,
        var score: Int,
        var isX: Boolean
)