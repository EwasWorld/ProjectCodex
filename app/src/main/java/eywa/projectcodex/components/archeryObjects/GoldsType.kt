package eywa.projectcodex.components.archeryObjects

import eywa.projectcodex.R
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.rounds.Round

/**
 * Represents how golds will be calculated
 *
 * @param score the minimum score required for an arrow to be counted as a gold
 * @param isX whether an X is required for the arrow to be counted as a gold
 * @param colHeaderStringId the string to display in the score pad for the golds column
 */
enum class GoldsType(private val score: Int, private val isX: Boolean, val colHeaderStringId: Int) {
    NINES(9, false, R.string.table_golds_nines_header),
    TENS(10, false, R.string.table_golds_tens_header),
    XS(10, true, R.string.table_golds_xs_header);

    companion object {
        /**
         * @return which golds type should be used based on whether the round [isOutdoor] and/or [isMetric]
         */
        fun getGoldsType(isOutdoor: Boolean, isMetric: Boolean): GoldsType {
            return when {
                !isOutdoor -> TENS
                isMetric -> TENS
                else -> NINES
            }
        }

        /**
         * @return which golds type should be used based on [round]
         */
        fun getGoldsType(round: Round): GoldsType {
            return getGoldsType(round.isOutdoor, round.isMetric)
        }
    }

    fun isGold(arrow: Arrow): Boolean {
        return isGold(arrow.score, arrow.isX)
    }

    fun isGold(arrow: ArrowValue): Boolean {
        return isGold(arrow.score, arrow.isX)
    }

    fun isGold(arrowScore: Int, arrowIsX: Boolean): Boolean {
        return arrowScore >= score && (arrowIsX || !isX)
    }
}