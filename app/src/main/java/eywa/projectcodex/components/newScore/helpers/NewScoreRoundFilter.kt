package eywa.projectcodex.components.newScore.helpers

import androidx.annotation.StringRes
import eywa.projectcodex.R
import eywa.projectcodex.components.newScore.NewScoreScreen
import eywa.projectcodex.database.rounds.Round

/**
 * Filters which can be applied to the [NewScoreScreen]'s round selection dialog
 */
enum class NewScoreRoundFilter(
        @StringRes val chipText: Int,
        val predicate: (Round) -> Boolean,
) {
    METRIC(R.string.create_round__select_a_round_filter_metric, { it.isMetric }),
    IMPERIAL(R.string.create_round__select_a_round_filter_imperial, { !it.isMetric }),
    INDOOR(R.string.create_round__select_a_round_filter_indoor, { !it.isOutdoor }),
    OUTDOOR(R.string.create_round__select_a_round_filter_outdoor, { it.isOutdoor }),
    ;

    companion object {
        /**
         * Each item is a set of filters that can't happen at the same time. E.g. a round cannot be metric and imperial
         */
        val mutuallyExclusiveSets = setOf(
                setOf(METRIC, IMPERIAL),
                setOf(INDOOR, OUTDOOR),
        )
    }
}