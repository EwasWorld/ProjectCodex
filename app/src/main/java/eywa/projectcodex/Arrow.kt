package eywa.projectcodex

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

/**
 * @return the arrow as a string
 */
fun getArrowString(arrowScore: Int, isX: Boolean): String {
    // TODO Hardcoded string
    return when {
        arrowScore == 10 && isX -> "X"
        arrowScore == 0 -> "m"
        else -> arrowScore.toString()
    }
}

fun isX(arrowScore: String): Boolean {
    // TODO Hardcoded string
    return arrowScore == "X" || arrowScore == "x"
}


class Arrow(val score: Int, val isX: Boolean) {
    constructor(scoreString: String) : this(getArrowScore(scoreString), isX(scoreString))

    override fun toString(): String {
        return getArrowString(score, isX)
    }
}