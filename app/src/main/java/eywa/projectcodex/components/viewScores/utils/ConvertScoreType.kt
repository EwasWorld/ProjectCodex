package eywa.projectcodex.components.viewScores.utils

import androidx.annotation.StringRes
import eywa.projectcodex.R
import eywa.projectcodex.common.sharedUi.HasDisplayTitle
import eywa.projectcodex.database.arrows.DatabaseArrowScore

enum class ConvertScoreType(
        @StringRes override val displayTitle: Int,
        private val convert: (DatabaseArrowScore) -> DatabaseArrowScore
) : HasDisplayTitle {
    XS_TO_TENS(
            displayTitle = R.string.view_scores__convert_xs_to_tens,
            convert = { DatabaseArrowScore(it.archerRoundId, it.arrowNumber, it.score, false) }
    ),
    TO_FIVE_ZONE(
            displayTitle = R.string.view_scores__convert_to_five_zone,
            convert = {
                val scoreChange = when {
                    it.score == 0 -> 0
                    it.score % 2 == 0 -> -1
                    else -> 0
                }
                DatabaseArrowScore(it.archerRoundId, it.arrowNumber, it.score + scoreChange, false)
            }
    ),
    ;

    /**
     * @return updated arrow values, if any
     */
    fun convertScore(arrows: List<DatabaseArrowScore>) = arrows.mapNotNull { arrow ->
        convert(arrow).takeIf { it != arrow }
    }
}
