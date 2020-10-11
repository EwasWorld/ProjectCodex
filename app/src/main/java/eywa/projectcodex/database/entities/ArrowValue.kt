package eywa.projectcodex.database.entities

import androidx.room.Entity
import eywa.projectcodex.logic.getArrowValueString

@Entity(tableName = "arrow_values", primaryKeys = ["archerRoundId", "arrowNumber"])
data class ArrowValue(
        val archerRoundId: Int,
        val arrowNumber: Int,
        var score: Int,
        var isX: Boolean
) {
    override fun toString(): String {
        return "$archerRoundId-$arrowNumber: " + getArrowValueString(score, isX)
    }
}