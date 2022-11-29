package eywa.projectcodex.common.archeryObjects

import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.arrowValue.arrowScoreAsString

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

@Deprecated("Use arrowScoreAs String")
fun getArrowValueString(score: Int, isX: Boolean): String {
    // TODO Hardcoded string
    return when {
        score == 10 && isX -> "X"
        score == 0 -> "m"
        else -> score.toString()
    }
}

class Arrow(val score: Int, val isX: Boolean) {
    constructor(scoreString: String) : this(
            getArrowScore(scoreString),
            isX(scoreString)
    )

    override fun toString(): String {
        return getArrowValueString(score, isX)
    }

    fun toArrowValue(archerRoundId: Int, arrowNumber: Int): ArrowValue {
        return ArrowValue(archerRoundId, arrowNumber, score, isX)
    }

    fun asString() = arrowScoreAsString(score, isX)
}