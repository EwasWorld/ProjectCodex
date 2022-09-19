package eywa.projectcodex.database.arrowValue

import androidx.room.Entity
import eywa.projectcodex.common.archeryObjects.getArrowValueString

@Entity(tableName = "arrow_values", primaryKeys = ["archerRoundId", "arrowNumber"])
data class ArrowValue(
        val archerRoundId: Int,
        val arrowNumber: Int,
        val score: Int,
        val isX: Boolean
) {
    override fun toString(): String {
        return "$archerRoundId-$arrowNumber: " + getArrowValueString(score, isX)
    }
}