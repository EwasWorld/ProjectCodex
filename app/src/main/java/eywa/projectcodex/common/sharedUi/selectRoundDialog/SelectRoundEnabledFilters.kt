package eywa.projectcodex.common.sharedUi.selectRoundDialog

import eywa.projectcodex.database.rounds.Round

/**
 * Contains a set of [SelectRoundFilter] and dictates how items can be added to that set.
 * e.g. prevents [SelectRoundFilter.METRIC] and [SelectRoundFilter.IMPERIAL] from being set together as they
 * are mutually exclusive
 */
class SelectRoundEnabledFilters private constructor(private val filters: Set<SelectRoundFilter>) {
    constructor() : this(setOf())

    fun plus(add: SelectRoundFilter) = plus(setOf(add))
    fun plus(add: Set<SelectRoundFilter>): SelectRoundEnabledFilters {
        val newFilters = filters.toMutableSet()

        SelectRoundFilter.mutuallyExclusiveSets.forEach { meSet ->
            val count = add.count { meSet.contains(it) }

            require(count <= 1) {
                "Items to add contains more than one of: " + meSet.joinToString { it.name }
            }
            if (count != 0) {
                newFilters.removeAll(meSet)
            }
        }

        return SelectRoundEnabledFilters(newFilters.plus(add))
    }

    fun minus(remove: SelectRoundFilter) = minus(setOf(remove))
    fun minus(remove: Set<SelectRoundFilter>) = SelectRoundEnabledFilters(filters.minus(remove))

    fun toggle(filter: SelectRoundFilter) = if (filters.contains(filter)) minus(filter) else plus(filter)

    fun contains(element: SelectRoundFilter) = filters.contains(element)

    fun filter(rounds: Iterable<Round>) = rounds.filter { round -> filters.all { it.predicate(round) } }

    override fun equals(other: Any?): Boolean {
        if (other !is SelectRoundEnabledFilters) return false
        return filters == other.filters
    }

    override fun hashCode(): Int {
        return filters.hashCode()
    }
}
