package eywa.projectcodex.components.shootDetails.headToHead.grid

import android.content.res.Resources
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.components.shootDetails.headToHead.HeadToHeadArcherType
import eywa.projectcodex.components.shootDetails.headToHead.HeadToHeadResult
import eywa.projectcodex.model.headToHead.FullHeadToHeadSet

enum class HeadToHeadGridColumn {
    SET_NUMBER {
        override val primaryTitle: ResOrActual<String>?
            get() = null
        override val mapping: (HeadToHeadGridRowData, HeadToHeadSetData) -> ResOrActual<String>?
            get() = { _, _ -> null }
        override val testTag: HeadToHeadGridColumnTestTag
            get() = HeadToHeadGridColumnTestTag.SET_NUMBER_CELL
    },

    TYPE {
        override val primaryTitle: ResOrActual<String>
            get() = ResOrActual.StringResource(R.string.head_to_head_grid__column_type)
        override val mapping: (HeadToHeadGridRowData, HeadToHeadSetData) -> ResOrActual<String>
            get() = { data, _ -> data.type.text }
        override val testTag: HeadToHeadGridColumnTestTag
            get() = HeadToHeadGridColumnTestTag.TYPE_CELL
    },

    ARROWS {
        override val primaryTitle: ResOrActual<String>
            get() = ResOrActual.StringResource(R.string.head_to_head_grid__column_arrows)
        override val mapping: (HeadToHeadGridRowData, HeadToHeadSetData) -> ResOrActual<String>?
            get() = { data, _ ->
                if (data is HeadToHeadGridRowData.Arrows) {
                    val missing = data.expectedArrowCount - data.arrows.size
                    val text = data.arrows.map { it.asString() }
                            .plus(
                                    List(missing) {
                                        ResOrActual.StringResource(R.string.end_to_string_arrow_placeholder)
                                    },
                            )
                    ResOrActual.JoinToStringResource(text, R.string.end_to_string_arrow_deliminator)
                }
                else if (data.type == HeadToHeadArcherType.RESULT) {
                    null
                }
                else {
                    ResOrActual.StringResource(R.string.score_pad__running_total_placeholder)
                }
            }
        override val testTag: HeadToHeadGridColumnTestTag
            get() = HeadToHeadGridColumnTestTag.ARROW_CELL
    },

    END_TOTAL {
        override val primaryTitle: ResOrActual<String>
            get() = ResOrActual.StringResource(R.string.head_to_head_grid__column_end_total)
        override val mapping: (HeadToHeadGridRowData, HeadToHeadSetData) -> ResOrActual<String>
            get() = { data, _ ->
                if (data.type == HeadToHeadArcherType.RESULT) {
                    FullHeadToHeadSet.getResult(data.totalScore).title
                }
                else {
                    ResOrActual.Actual(data.totalScore.toString())
                }
            }
        override val testTag: HeadToHeadGridColumnTestTag
            get() = HeadToHeadGridColumnTestTag.END_TOTAL_CELL

        override fun cellHorizontalSpan(row: HeadToHeadGridRowData, extra: HeadToHeadSetData): Int {
            return if (row.type == HeadToHeadArcherType.RESULT) extra.resultColumnSpan else 1
        }
    },

    TEAM_TOTAL {
        override val primaryTitle: ResOrActual<String>
            get() = ResOrActual.StringResource(R.string.head_to_head_grid__column_team_total)
        override val mapping: (HeadToHeadGridRowData, HeadToHeadSetData) -> ResOrActual<String>?
            get() = { data, extra ->
                when (data.type) {
                    HeadToHeadArcherType.SELF -> ResOrActual.Actual(extra.teamEndTotal.toString())
                    HeadToHeadArcherType.OPPONENT -> ResOrActual.Actual(extra.opponentEndTotal.toString())
                    else -> null
                }
            }
        override val testTag: HeadToHeadGridColumnTestTag
            get() = HeadToHeadGridColumnTestTag.TEAM_TOTAL_CELL

        override fun cellVerticalSpan(row: HeadToHeadGridRowData, extra: HeadToHeadSetData): Int {
            return if (row.type == HeadToHeadArcherType.SELF) extra.teamTotalColumnSpan else 1
        }
    },

    POINTS {
        override val primaryTitle: ResOrActual<String>
            get() = ResOrActual.StringResource(R.string.head_to_head_grid__column_points)
        override val mapping: (HeadToHeadGridRowData, HeadToHeadSetData) -> ResOrActual<String>?
            get() = { data, extra ->
                if (extra.result == HeadToHeadResult.INCOMPLETE || extra.result == HeadToHeadResult.UNKNOWN) {
                    ResOrActual.StringResource(R.string.score_pad__running_total_placeholder)
                }
                else {
                    fun HeadToHeadResult.getPoints() =
                            ResOrActual.Actual((if (extra.isShootOff) shootOffPoints else defaultPoints).toString())

                    when (data.type) {
                        HeadToHeadArcherType.SELF -> extra.result.getPoints()

                        HeadToHeadArcherType.TEAM_MATE ->
                            if (extra.hasSelfAndTeamRows) null else extra.result.getPoints()

                        HeadToHeadArcherType.TEAM -> extra.result.getPoints()

                        HeadToHeadArcherType.OPPONENT -> {
                            val opponent = when (extra.result) {
                                HeadToHeadResult.WIN -> HeadToHeadResult.LOSS
                                HeadToHeadResult.LOSS -> HeadToHeadResult.WIN
                                else -> extra.result
                            }
                            opponent.getPoints()
                        }

                        HeadToHeadArcherType.RESULT -> {
                            check(data.isTotalRow)
                            val points = when (data.totalScore) {
                                0 -> 0
                                1 -> if (extra.isShootOff) throw IllegalStateException() else 1
                                2 -> if (extra.isShootOff) 1 else 2
                                else -> throw IllegalStateException()
                            }
                            ResOrActual.Actual(points.toString())
                        }
                    }
                }
            }
        override val testTag: HeadToHeadGridColumnTestTag
            get() = HeadToHeadGridColumnTestTag.POINTS_CELL

        override fun cellVerticalSpan(row: HeadToHeadGridRowData, extra: HeadToHeadSetData): Int {
            return if (row.type == HeadToHeadArcherType.SELF && extra.hasSelfAndTeamRows) 2 else 1
        }
    },
    ;

    abstract val primaryTitle: ResOrActual<String>?
    abstract val mapping: (HeadToHeadGridRowData, HeadToHeadSetData) -> ResOrActual<String>?
    abstract val testTag: HeadToHeadGridColumnTestTag
    val cellContentDescription: (HeadToHeadGridRowData, HeadToHeadSetData) -> ResOrActual<String>?
        get() = { _, _ -> null }
    val helpTitle: ResOrActual<String>?
        get() = null
    val helpBody: ResOrActual<String>?
        get() = null

    open fun cellVerticalSpan(row: HeadToHeadGridRowData, extra: HeadToHeadSetData): Int = 1
    open fun cellHorizontalSpan(row: HeadToHeadGridRowData, extra: HeadToHeadSetData): Int = 1

    fun getHelpState(resources: Resources): HelpShowcaseItem? {
        if (helpTitle == null || helpBody == null) return null
        return HelpShowcaseItem(
                helpTitle = helpTitle!!.get(resources),
                helpBody = helpBody!!.get(resources),
        )
    }
}
