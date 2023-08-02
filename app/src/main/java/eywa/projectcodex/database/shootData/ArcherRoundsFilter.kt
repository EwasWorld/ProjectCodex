package eywa.projectcodex.database.shootData

import java.util.*

sealed class ArcherRoundsFilter {
    data class DateRange(val from: Calendar? = null, val to: Calendar? = null) : ArcherRoundsFilter() {
        init {
            require(from != null || to != null) { "No dates given" }
            require(from == null || to == null || from.before(to)) { "Invalid date range" }
        }
    }

    data class Round(val roundId: Int, private val subtypeId: Int?) : ArcherRoundsFilter() {
        val nonNullSubtypeId = subtypeId ?: 1
    }

    object PersonalBests : ArcherRoundsFilter()
}
