package eywa.projectcodex.database.shootData

import java.util.Calendar

sealed class ShootFilter {
    data class DateRange(val from: Calendar? = null, val to: Calendar? = null) : ShootFilter() {
        init {
            require(from != null || to != null) { "No dates given" }
            require(from == null || to == null || from.before(to)) { "Invalid date range" }
        }
    }

    data class Round(val roundId: Int?, private val subtypeId: Int?) : ShootFilter() {
        val nonNullSubtypeId = subtypeId ?: 1
    }

    object PersonalBests : ShootFilter()

    object ArrowCounts : ShootFilter()

    object Scores : ShootFilter()

    data class Handicap(val from: Int? = null, val to: Int? = null) : ShootFilter() {
        init {
            require(from != null || to != null) { "No handicaps given" }
            require(from == null || to == null || from <= to) { "Invalid handicap range" }
        }
    }

    object FirstRoundOfDay : ShootFilter()

    object CompleteRounds : ShootFilter()
}
