package eywa.projectcodex.model

import eywa.projectcodex.database.arrows.DatabaseArrowScore
import eywa.projectcodex.database.arrows.arrowScoreAsString

/**
 * @param arrow the arrow value as a string
 * @return the arrow value as an integer
 * @throws NumberFormatException if the string is not parsable or the score is negative
 */
fun getArrowScore(arrow: String): Int {
    if (isX(arrow)) {
        return 10
    }

    // TODO Hardcoded string
    val score = when (arrow) {
        "M", "m" -> 0
        else -> Integer.parseInt(arrow)
    }
    if (score < 0) {
        throw NumberFormatException("Arrow score is invalid")
    }
    return score
}

fun isX(arrowScore: String): Boolean {
    // TODO Hardcoded string
    return arrowScore == "X" || arrowScore == "x"
}

@Deprecated(
        "Use arrowScoreAs String",
        ReplaceWith(
                "arrowScoreAsString(score = score, isX = isX)",
                "eywa.projectcodex.model.getArrowScoreString"
        )
)
fun getArrowScoreString(score: Int, isX: Boolean): String {
    // TODO Hardcoded string
    return when {
        score == 10 && isX -> "X"
        score == 0 -> "m"
        else -> score.toString()
    }
}

data class Arrow(val score: Int, val isX: Boolean = false) {
    constructor(scoreString: String) : this(
            getArrowScore(scoreString),
            isX(scoreString)
    )

    override fun toString(): String {
        return getArrowScoreString(score, isX)
    }

    fun asArrowScore(shootId: Int, arrowNumber: Int): DatabaseArrowScore {
        return DatabaseArrowScore(shootId, arrowNumber, score, isX)
    }

    fun asString() = arrowScoreAsString(score, isX)
}
