package eywa.projectcodex.database.arrows

import androidx.room.Entity
import androidx.room.ForeignKey
import eywa.projectcodex.R
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.database.arrows.DatabaseArrowScore.Companion.TABLE_NAME
import eywa.projectcodex.database.shootData.DatabaseShoot
import eywa.projectcodex.model.GoldsType
import eywa.projectcodex.model.getArrowScoreString

@Entity(
        tableName = TABLE_NAME,
        primaryKeys = ["shootId", "arrowNumber"],
        foreignKeys = [
            ForeignKey(
                    entity = DatabaseShoot::class,
                    parentColumns = ["shootId"],
                    childColumns = ["shootId"],
                    onDelete = ForeignKey.CASCADE,
            ),
        ]
)
data class DatabaseArrowScore(
        val shootId: Int,
        val arrowNumber: Int,
        val score: Int,
        val isX: Boolean = false,
) {
    init {
        require(!isX || score == 10) { "For isX to be true, score must be 10" }
    }

    val isHit
        get() = score > 0

    override fun toString(): String {
        return "$shootId-$arrowNumber: " + getArrowScoreString(score, isX)
    }

    companion object {
        const val TABLE_NAME = "arrow_values"
    }
}

fun Iterable<DatabaseArrowScore>.getHits() = count { it.isHit }
fun Iterable<DatabaseArrowScore>.getScore() = sumOf { it.score }
fun Iterable<DatabaseArrowScore>.getGolds(goldsType: GoldsType) = count { goldsType.isGold(it) }

fun DatabaseArrowScore.asString() = arrowScoreAsString(score, isX)

fun arrowScoreAsString(score: Int, isX: Boolean) = when {
    score == 0 -> ResOrActual.StringResource(R.string.arrow_value_m)
    isX -> ResOrActual.StringResource(R.string.arrow_value_x)
    else -> ResOrActual.Actual(score.toString())
}
