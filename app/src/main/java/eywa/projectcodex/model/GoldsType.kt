package eywa.projectcodex.model

import androidx.annotation.StringRes
import eywa.projectcodex.R
import eywa.projectcodex.database.arrows.DatabaseArrowScore
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
        @StringRes val shortStringId: Int,
        @StringRes val longStringId: Int,
        @StringRes val helpString: Int,
) {
    NINES(
            score = 9,
            isX = false,
            shortStringId = R.string.table_golds_nines_header,
            longStringId = R.string.table_golds_nines_full,
            helpString = R.string.help_score_pad__golds_column_body_nines,
    ),
    TENS(
            score = 10,
            isX = false,
            shortStringId = R.string.table_golds_tens_header,
            longStringId = R.string.table_golds_tens_full,
            helpString = R.string.help_score_pad__golds_column_body_tens,
    ),
    XS(
            score = 10,
            isX = true,
            shortStringId = R.string.table_golds_xs_header,
            longStringId = R.string.table_golds_xs_full,
            helpString = R.string.help_score_pad__golds_column_body_xs,
    ),
    ;

    companion object {
        val defaultGoldsType = TENS

        /**
         * @return which golds type should be used based on [round]
         */
        fun getGoldsType(round: Round): List<GoldsType> {
            return when {
                !round.isOutdoor -> listOf(TENS)
                !round.isMetric -> listOf(NINES)
                else -> listOf(TENS, XS)
            }
        }
    }

    fun isGold(arrow: Arrow): Boolean {
        return isGold(arrow.score, arrow.isX)
    }

    fun isGold(arrow: DatabaseArrowScore): Boolean {
        return isGold(arrow.score, arrow.isX)
    }

    fun isGold(arrowScore: Int, arrowIsX: Boolean): Boolean {
        return arrowScore >= score && (arrowIsX || !isX)
    }
}
