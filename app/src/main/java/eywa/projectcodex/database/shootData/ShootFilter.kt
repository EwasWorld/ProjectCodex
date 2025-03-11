package eywa.projectcodex.database.shootData

import eywa.projectcodex.components.viewScores.actionBar.filters.ViewScoresFiltersTypes
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

    data object PersonalBests : ShootFilter()

    data class Type(val type: ViewScoresFiltersTypes) : ShootFilter()

    data class ScoreRange(
            override val from: Int? = null,
            override val to: Int? = null,
    ) : ShootFilter(), Range<Int, Int> {
        override val errorName: String
            get() = "score"

        override fun convert(value: Int): Int = value
    }

    data object FirstRoundOfDay : ShootFilter()

    /**
     * Note head to head entries will never show up as completed
     */
    data object CompleteRounds : ShootFilter()
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
