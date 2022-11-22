package eywa.projectcodex.components.newScore

import androidx.annotation.StringRes
import eywa.projectcodex.R
import eywa.projectcodex.components.newScore.NewScoreRoundFilter.IMPERIAL
import eywa.projectcodex.components.newScore.NewScoreRoundFilter.METRIC
import eywa.projectcodex.database.rounds.Round

/**
 * Contains a set of [NewScoreRoundFilter] and dictates how items can be added to that set.
 * e.g. prevents [NewScoreRoundFilter.METRIC] and [NewScoreRoundFilter.IMPERIAL] from being set together as they
 * are mutually exclusive
 */
class NewScoreRoundEnabledFilters private constructor(private val filters: Set<NewScoreRoundFilter>) {
    constructor() : this(setOf())

    fun plus(add: NewScoreRoundFilter) = plus(setOf(add))
    fun plus(add: Set<NewScoreRoundFilter>): NewScoreRoundEnabledFilters {
        require(!(add.contains(METRIC) && add.contains(IMPERIAL)))

        val newFilters = filters.toMutableSet()

        if (add.contains(METRIC) || add.contains(IMPERIAL)) {
            newFilters.remove(METRIC)
            newFilters.remove(IMPERIAL)
        }

        newFilters.addAll(add)
        return NewScoreRoundEnabledFilters(newFilters)
    }

    fun minus(remove: NewScoreRoundFilter) = minus(setOf(remove))
    fun minus(remove: Set<NewScoreRoundFilter>) = NewScoreRoundEnabledFilters(filters.minus(remove))

    fun toggle(filter: NewScoreRoundFilter) = if (filters.contains(filter)) minus(filter) else plus(filter)

    fun contains(element: NewScoreRoundFilter) = filters.contains(element)

    fun filter(rounds: Iterable<Round>) = rounds.filter { round -> filters.all { it.predicate(round) } }
}

/**
 * Filters which can be applied to the [NewScoreScreen]'s round selection dialog
 */
enum class NewScoreRoundFilter(
        @StringRes val chipText: Int,
        val predicate: (Round) -> Boolean,
) {
    METRIC(R.string.create_round__select_a_round_filter_metric, { it.isMetric }),
    IMPERIAL(R.string.create_round__select_a_round_filter_imperial, { !it.isMetric }),
}