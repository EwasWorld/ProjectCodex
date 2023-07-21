package eywa.projectcodex.database.arrowValue

import androidx.room.Entity
import androidx.room.ForeignKey
import eywa.projectcodex.R
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.database.archerRound.ArcherRound
import eywa.projectcodex.database.arrowValue.ArrowValue.Companion.TABLE_NAME
import eywa.projectcodex.model.GoldsType
import eywa.projectcodex.model.getArrowValueString

@Entity(
        tableName = TABLE_NAME,
        primaryKeys = ["archerRoundId", "arrowNumber"],
        foreignKeys = [
            ForeignKey(
                    entity = ArcherRound::class,
                    parentColumns = ["archerRoundId"],
                    childColumns = ["archerRoundId"],
                    onDelete = ForeignKey.CASCADE,
            ),
        ]
)
data class ArrowValue(
        val archerRoundId: Int,
        val arrowNumber: Int,
        val score: Int,
        val isX: Boolean
) {
    init {
        require(!isX || score == 10) { "For isX to be true, score must be 10" }
    }

    val isHit
        get() = score > 0

    override fun toString(): String {
        return "$archerRoundId-$arrowNumber: " + getArrowValueString(score, isX)
    }

    companion object {
        const val TABLE_NAME = "arrow_values"
    }
}

fun Iterable<ArrowValue>.getHits() = count { it.isHit }
fun Iterable<ArrowValue>.getScore() = sumOf { it.score }
fun Iterable<ArrowValue>.getGolds(goldsType: GoldsType) = count { goldsType.isGold(it) }

fun ArrowValue.asString() = arrowScoreAsString(score, isX)

fun arrowScoreAsString(score: Int, isX: Boolean) = when {
    score == 0 -> ResOrActual.fromRes(R.string.arrow_value_m)
    isX -> ResOrActual.fromRes(R.string.arrow_value_x)
    else -> ResOrActual.fromActual(score.toString())
}
