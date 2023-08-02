package eywa.projectcodex.database.shootData

import java.util.*

sealed class ShootFilter {
    data class DateRange(val from: Calendar? = null, val to: Calendar? = null) : ShootFilter() {
        init {
            require(from != null || to != null) { "No dates given" }
            require(from == null || to == null || from.before(to)) { "Invalid date range" }
        }
    }

    data class Round(val roundId: Int, private val subtypeId: Int?) : ShootFilter() {
        val nonNullSubtypeId = subtypeId ?: 1
    }

    object PersonalBests : ShootFilter()
}
