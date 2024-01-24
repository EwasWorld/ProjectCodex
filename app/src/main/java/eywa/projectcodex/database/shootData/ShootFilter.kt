package eywa.projectcodex.database.shootData

import eywa.projectcodex.database.DatabaseConverters
import java.util.Calendar

sealed class ShootFilter {
    data class DateRange(
            override val from: Calendar? = null,
            override val to: Calendar? = null,
    ) : ShootFilter(), Range<Calendar, Long> {
        override val errorName: String
            get() = "date"

        override fun convert(value: Calendar): Long = DatabaseConverters().calendarToTimestamp(value)!!
    }

    /**
     * @param [roundId] null means entries without around
     * @param [subtypeId] null means all subtypes
     */
    data class Round(val roundId: Int?, val subtypeId: Int?) : ShootFilter()

    object PersonalBests : ShootFilter()

    /**
     * @param only true if only entries with arrow counts are desired, false if only non-arrow counts are desired
     */
    data class ArrowCounts(val only: Boolean) : ShootFilter()

    data class ScoreRange(
            override val from: Int? = null,
            override val to: Int? = null,
    ) : ShootFilter(), Range<Int, Int> {
        override val errorName: String
            get() = "score"

        override fun convert(value: Int): Int = value
    }

    object FirstRoundOfDay : ShootFilter()

    object CompleteRounds : ShootFilter()
}

interface Range<T : Comparable<T>, R> {
    val errorName: String

    val from: T?
    val to: T?

    val fromDb: R?
        get() = from?.let { convert(it) }
    val toDb: R?
        get() = to?.let { convert(it) }

    fun validate() {
        require(from != null || to != null) { "No $errorName(s) given" }
        require(from == null || to == null || from!! <= to!!) { "Invalid $errorName range" }
    }

    fun convert(value: T): R
}
