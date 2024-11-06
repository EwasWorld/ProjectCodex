package eywa.projectcodex.components.shootDetails.headToHeadEnd.grid

import android.content.res.Resources
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadArcherType
import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadResult

enum class HeadToHeadGridColumn {
    SET_NUMBER {
        override val primaryTitle: ResOrActual<String>?
            get() = null
        override val mapping: (HeadToHeadGridRowData, HeadToHeadSetData) -> ResOrActual<String>?
            get() = { _, _ -> null }
    },

    TYPE {
        override val primaryTitle: ResOrActual<String>
            get() = ResOrActual.Actual("Archer")
        override val mapping: (HeadToHeadGridRowData, HeadToHeadSetData) -> ResOrActual<String>
            get() = { data, _ -> data.type.text }
    },

    ARROWS {
        override val primaryTitle: ResOrActual<String>
            get() = ResOrActual.Actual("Arrows")
        override val mapping: (HeadToHeadGridRowData, HeadToHeadSetData) -> ResOrActual<String>
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
                else {
                    ResOrActual.StringResource(R.string.score_pad__running_total_placeholder)
                }
            }
    },

    END_TOTAL {
        override val primaryTitle: ResOrActual<String>
            get() = ResOrActual.Actual("S")
        override val mapping: (HeadToHeadGridRowData, HeadToHeadSetData) -> ResOrActual<String>
            get() = { data, _ -> ResOrActual.Actual(data.totalScore.toString()) }
    },

    TEAM_TOTAL {
        override val primaryTitle: ResOrActual<String>
            get() = ResOrActual.Actual("T")
        override val mapping: (HeadToHeadGridRowData, HeadToHeadSetData) -> ResOrActual<String>?
            get() = { data, extra ->
                when (data.type) {
                    HeadToHeadArcherType.SELF -> ResOrActual.Actual(extra.teamEndTotal.toString())
                    HeadToHeadArcherType.OPPONENT -> ResOrActual.Actual(extra.opponentEndTotal.toString())
                    else -> null
                }
            }

        override fun cellVerticalSpan(row: HeadToHeadGridRowData, extra: HeadToHeadSetData): Int {
            return if (row.type == HeadToHeadArcherType.SELF) 2 else 1
        }
    },

    POINTS {
        override val primaryTitle: ResOrActual<String>
            get() = ResOrActual.Actual("P")
        override val mapping: (HeadToHeadGridRowData, HeadToHeadSetData) -> ResOrActual<String>?
            get() = { data, extra ->
                if (extra.result == HeadToHeadResult.INCOMPLETE) {
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

                        HeadToHeadArcherType.RESULT ->
                            ResOrActual.Actual((data as HeadToHeadGridRowData.Total).total.toString())
                    }
                }
            }

        override fun cellVerticalSpan(row: HeadToHeadGridRowData, extra: HeadToHeadSetData): Int {
            return if (row.type == HeadToHeadArcherType.SELF && extra.hasSelfAndTeamRows) 2 else 1
        }
    },
    ;

    abstract val primaryTitle: ResOrActual<String>?
    abstract val mapping: (HeadToHeadGridRowData, HeadToHeadSetData) -> ResOrActual<String>?
    val cellContentDescription: (HeadToHeadGridRowData, HeadToHeadSetData) -> ResOrActual<String>?
        get() = { _, _ -> null }
    val helpTitle: ResOrActual<String>?
        get() = null
    val helpBody: ResOrActual<String>?
        get() = null
    val testTag: CodexTestTag?
        get() = null

    open fun cellVerticalSpan(row: HeadToHeadGridRowData, extra: HeadToHeadSetData): Int = 1

    fun getHelpState(resources: Resources): HelpShowcaseItem? {
        if (helpTitle == null || helpBody == null) return null
        return HelpShowcaseItem(
                helpTitle = helpTitle!!.get(resources),
                helpBody = helpBody!!.get(resources),
        )
    }
}
