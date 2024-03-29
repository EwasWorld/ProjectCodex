package eywa.projectcodex.model

import eywa.projectcodex.R
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.rounds.Round

/**
 * Represents how golds will be calculated
 *
 * @param score the minimum score required for an arrow to be counted as a gold
 * @param isX whether an X is required for the arrow to be counted as a gold
 * @param shortStringId the string to display in the score pad for the golds column
 */
enum class GoldsType(
        private val score: Int,
        private val isX: Boolean,
        val shortStringId: Int,
        val longStringId: Int,
        val helpString: Int,
) {
    NINES(
            9,
            false,
            R.string.table_golds_nines_header,
            R.string.table_golds_nines_full,
            R.string.help_score_pad__golds_column_body_nines,
    ),
    TENS(
            10,
            false,
            R.string.table_golds_tens_header,
            R.string.table_golds_tens_full,
            R.string.help_score_pad__golds_column_body_tens,
    ),
    XS(
            10,
            true,
            R.string.table_golds_xs_header,
            R.string.table_golds_xs_full,
            R.string.help_score_pad__golds_column_body_xs,
    ),
    ;

    companion object {
        val defaultGoldsType = NINES

        /**
         * @return which golds type should be used based on [round]
         */
        fun getGoldsType(round: Round): GoldsType {
            return when {
                !round.isMetric && round.isOutdoor -> NINES
                else -> TENS
            }
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
