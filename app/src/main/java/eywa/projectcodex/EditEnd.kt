package eywa.projectcodex

import android.content.res.Resources
import java.lang.NullPointerException

// TODO is there a way to not pass in resources? Feels silly, hard to mock...
class EditEnd(_resources: Resources) {
    private var resources: Resources = _resources
    private var arrowDeliminator = resources.getString(R.string.arrow_deliminator)
    private var arrowPlaceholder = resources.getString(R.string.arrow_placeholder)

    /**
     * @param end the string representation of the end
     * @return the total score for the end
     */
    fun getEndScore(end: String): Int {
        val scores = end.split(arrowDeliminator)
        var total = 0
        for (score in scores) {
            total += getArrowScore(score)
        }
        return total
    }

    /**
     * @param arrow the arrow value as a string
     * @return the arrow value as an integer
     */
    fun getArrowScore(arrow: String): Int {
        return when (arrow) {
            resources.getString(R.string.arrow_value_x) -> 10
            arrowPlaceholder -> 0
            resources.getString(R.string.arrow_value_m) -> 0
            else -> Integer.parseInt(arrow)
        }
    }

    /**
     * @param end the string representation of the end
     * @param arrow the arrow value to add to the end
     * @return the end with the new arrow added
     * @throws NullPointerException if the end is full
     */
    fun addArrowToEnd(end: String, arrow: String): String {
        if (!end.contains(arrowPlaceholder)) {
            throw NullPointerException("End full")
        }
        return end.replaceFirst("\\.".toRegex(), arrow)
    }

    /**
     * @param end the string representation of the end
     * @return the end with the last arrow score removed
     * @throws NullPointerException if the end is empty
     */
    fun removeLastArrowFromEnd(end: String): String {
        if (end[0] == arrowPlaceholder[0]) {
            throw NullPointerException("End empty")
        }
        val scores = end.split(arrowDeliminator).toMutableList()
        for (i in (scores.size - 1) downTo 0) {
            if (scores[i] != arrowPlaceholder) {
                scores[i] = arrowPlaceholder
                break
            }
        }
        return scores.joinToString(arrowDeliminator)
    }

    /**
     * @param end the string representation of the end
     * @return the end sorted numerically with all placeholders at the end
     */
    fun rewriteScores(end: String): String {
        val mString = resources.getString(R.string.arrow_value_m)

        val scores = end.split(arrowDeliminator).sorted().toMutableList()
        scores.removeAll { arrowString -> arrowString == arrowPlaceholder || arrowString == mString }

        // Move 'm's to the start
        val mCount = scores.count { arrowString -> arrowString == mString }
        for (i in 1..mCount) {
            scores.add(0, mString)
        }

        // Fill remaining placeholders
        while (scores.size < 6) {
            scores.add(arrowPlaceholder)
        }
        return scores.joinToString(arrowDeliminator)
    }
}