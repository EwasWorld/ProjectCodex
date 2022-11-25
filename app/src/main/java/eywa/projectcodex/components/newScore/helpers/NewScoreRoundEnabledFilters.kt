package eywa.projectcodex.components.newScore.helpers

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
        val newFilters = filters.toMutableSet()

        NewScoreRoundFilter.mutuallyExclusiveSets.forEach { meSet ->
            val count = add.count { meSet.contains(it) }

            require(count <= 1) {
                "Items to add contains more than one of: " + meSet.joinToString { it.name }
            }
            if (count != 0) {
                newFilters.removeAll(meSet)
            }
        }

        return NewScoreRoundEnabledFilters(newFilters.plus(add))
    }

    fun minus(remove: NewScoreRoundFilter) = minus(setOf(remove))
    fun minus(remove: Set<NewScoreRoundFilter>) = NewScoreRoundEnabledFilters(filters.minus(remove))

    fun toggle(filter: NewScoreRoundFilter) = if (filters.contains(filter)) minus(filter) else plus(filter)

    fun contains(element: NewScoreRoundFilter) = filters.contains(element)

    fun filter(rounds: Iterable<Round>) = rounds.filter { round -> filters.all { it.predicate(round) } }
}