package eywa.projectcodex.model.scorePadData

import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import eywa.projectcodex.R
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.database.arrows.DatabaseArrowScore
import eywa.projectcodex.database.arrows.asString
import eywa.projectcodex.database.arrows.getGolds
import eywa.projectcodex.database.arrows.getHits
import eywa.projectcodex.database.arrows.getScore
import eywa.projectcodex.model.GoldsType

sealed class ScorePadRow {
    internal abstract val hits: Int
    internal abstract val score: Int
    internal abstract val golds: Int
    internal open val runningTotal: Int? = null

    abstract fun getRowHeader(): ResOrActual<String>
    open fun getRowHeaderContentDescription(): ResOrActual<String>? = null
    abstract fun getArrowsString(): ResOrActual<String>

    /**
     * @param endNumber 1-indexed
     */
    data class End(
            internal val endNumber: Int,
            val arrowScores: List<ResOrActual<String>>,
            override val hits: Int,
            override val score: Int,
            override val golds: Int,
            override val runningTotal: Int,
    ) : ScorePadRow() {
        internal constructor(
                endNumber: Int,
                arrows: List<DatabaseArrowScore>,
                goldsType: GoldsType,
                runningTotal: Int,
        ) : this(
                endNumber,
                arrows.map { it.asString() },
                arrows.getHits(),
                arrows.getScore(),
                arrows.getGolds(goldsType),
                runningTotal
        )

        override fun getArrowsString(): ResOrActual<String> =
                object : ResOrActual<String>() {
                    @Composable
                    override fun get(): String = get(LocalContext.current.resources)

                    override fun get(resources: Resources): String {
                        val delimiter = resources.getString(R.string.end_to_string_arrow_deliminator)
                        return arrowScores.joinToString(delimiter) { it.get(resources) }
                    }
                }


        override fun getRowHeaderContentDescription() = ResOrActual.StringResource(
                resId = R.string.score_pad__end_row_header_accessibility,
                args = listOf(endNumber)
        )

        override fun getRowHeader() = ResOrActual.Actual(endNumber.toString())
    }

    data class DistanceTotal(
            private val distance: Int,
            private val distanceUnit: ResOrActual<String>,
            override val hits: Int,
            override val score: Int,
            override val golds: Int,
    ) : ScorePadRow() {
        internal constructor(
                arrows: List<DatabaseArrowScore>,
                goldsType: GoldsType,
                distance: Int,
                distanceUnit: ResOrActual<String>,
        ) : this(distance, distanceUnit, arrows.getHits(), arrows.getScore(), arrows.getGolds(goldsType))

        override fun getArrowsString(): ResOrActual<String> = ResOrActual.StringResource(
                R.string.score_pad__distance_total,
                listOf(distance, distanceUnit),
        )

        override fun getRowHeader() = ResOrActual.StringResource(R.string.score_pad__distance_total_row_header)
    }

    data class HalfDistanceTotal(
            private val distance: Int,
            private val distanceUnit: ResOrActual<String>,
            private val isFirstHalf: Boolean,
            override val hits: Int,
            override val score: Int,
            override val golds: Int,
    ) : ScorePadRow() {
        internal constructor(
                arrows: List<DatabaseArrowScore>,
                goldsType: GoldsType,
                distance: Int,
                distanceUnit: ResOrActual<String>,
                isFirstHalf: Boolean,
        ) : this(
                distance,
                distanceUnit,
                isFirstHalf,
                arrows.getHits(),
                arrows.getScore(),
                arrows.getGolds(goldsType)
        )

        override fun getArrowsString(): ResOrActual<String> = ResOrActual.StringResource(
                if (isFirstHalf) R.string.score_pad__half_way_first_total
                else R.string.score_pad__half_way_second_total,
        )

        override fun getRowHeader() = ResOrActual.StringResource(R.string.score_pad__distance_total_row_header)
    }

    data class SurplusTotal(
            override val hits: Int,
            override val score: Int,
            override val golds: Int,
    ) : ScorePadRow() {
        constructor(arrows: List<DatabaseArrowScore>, goldsType: GoldsType)
                : this(arrows.getHits(), arrows.getScore(), arrows.getGolds(goldsType))

        override fun getArrowsString(): ResOrActual<String> =
                ResOrActual.StringResource(R.string.score_pad__surplus_total)

        override fun getRowHeader() = ResOrActual.StringResource(R.string.score_pad__distance_total_row_header)
    }

    data class GrandTotal(
            override val hits: Int,
            override val score: Int,
            override val golds: Int,
    ) : ScorePadRow() {
        constructor(arrows: List<DatabaseArrowScore>, goldsType: GoldsType)
                : this(arrows.getHits(), arrows.getScore(), arrows.getGolds(goldsType))

        override fun getArrowsString(): ResOrActual<String> =
                ResOrActual.StringResource(R.string.score_pad__grand_total)

        override fun getRowHeader() = ResOrActual.StringResource(R.string.score_pad__grand_total_row_header)
    }
}
