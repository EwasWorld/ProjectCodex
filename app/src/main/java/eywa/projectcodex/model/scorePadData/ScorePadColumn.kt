package eywa.projectcodex.model.scorePadData

import android.content.res.Resources
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import eywa.projectcodex.R
import eywa.projectcodex.common.sharedUi.grid.CodexGridColumnMetadata
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.components.shootDetails.scorePad.ScorePadTestTag
import eywa.projectcodex.model.GoldsType
import eywa.projectcodex.model.endAsAccessibilityString

interface ScorePadColumn : CodexGridColumnMetadata<ScorePadRow, Unit> {
    override val primaryTitleHorizontalSpan: Int
        get() = 1
    override val primaryTitleVerticalSpan: Int
        get() = 1
    override val secondaryTitle: ResOrActual<String>?
        get() = null
    override val testTag: CodexTestTag?
        get() = ScorePadTestTag.CELL

    class Golds(private val goldsType: GoldsType) : ScorePadColumn {
        override val primaryTitle: ResOrActual<String>
            get() = ResOrActual.StringResource(goldsType.shortStringId)
        override val helpTitle: ResOrActual<String>
            get() = ResOrActual.StringResource(R.string.help_score_pad__golds_column_title)
        override val helpBody: ResOrActual<String>
            get() = ResOrActual.StringResource(
                    R.string.help_score_pad__golds_column_body,
                    listOf(ResOrActual.StringResource(goldsType.helpString)),
            )
        override val mapping: (ScorePadRow) -> ResOrActual<String>
            get() = { ResOrActual.Actual(it.golds[goldsType].toString()) }
        override val cellContentDescription: (ScorePadRow, Unit) -> ResOrActual<String>
            get() = { row, _ ->
                ResOrActual.StringResource(
                        R.string.score_pad__golds_accessibility,
                        listOf(row.golds[goldsType].toString(), ResOrActual.StringResource(goldsType.longStringId))
                )
            }
    }

    data object Header : ScorePadColumn {
        override val primaryTitle: ResOrActual<String>?
            get() = null
        override val helpTitle: ResOrActual<String>?
            get() = null
        override val helpBody: ResOrActual<String>?
            get() = null
        override val mapping: (ScorePadRow) -> ResOrActual<String>
            get() = { it.getRowHeader() }
        override val cellContentDescription: (ScorePadRow, Unit) -> ResOrActual<String>?
            get() = { row, _ -> row.getRowHeaderContentDescription() }
    }

    enum class FixedData(
            val headingId: Int,
            @StringRes val helpTitleId: Int,
            @StringRes val helpBodyId: Int,
    ) : ScorePadColumn {
        ARROWS(
                headingId = R.string.score_pad__end_string_header,
                helpTitleId = R.string.help_score_pad__arrow_column_title,
                helpBodyId = R.string.help_score_pad__arrow_column_body,
        ) {
            override val mapping: (ScorePadRow) -> ResOrActual<String>
                get() = { it.getArrowsString() }
            override val cellContentDescription: (ScorePadRow, Unit) -> ResOrActual<String>?
                get() = { row, _ ->
                    if (row !is ScorePadRow.End) {
                        mapping(row)
                    }
                    else {
                        object : ResOrActual<String>() {
                            @Composable
                            override fun get(): String = get(LocalContext.current.resources)

                            override fun get(resources: Resources): String {
                                val mainStr = row.arrowScores.map { it.get(resources) }.endAsAccessibilityString()
                                return resources.getString(R.string.score_pad__arrow_string_accessibility, mainStr)
                            }
                        }
                    }
                }
        },
        HITS(
                headingId = R.string.table_hits_header,
                helpTitleId = R.string.help_score_pad__hits_column_title,
                helpBodyId = R.string.help_score_pad__hits_column_body,
        ) {
            override val mapping: (ScorePadRow) -> ResOrActual<String>
                get() = { ResOrActual.Actual(it.hits.toString()) }
            override val cellContentDescription: (ScorePadRow, Unit) -> ResOrActual<String>?
                get() = { row, _ ->
                    ResOrActual.StringResource(
                            R.string.score_pad__hits_accessibility,
                            listOf(mapping(row))
                    )
                }
        },
        SCORE(
                headingId = R.string.table_score_header,
                helpTitleId = R.string.help_score_pad__score_column_title,
                helpBodyId = R.string.help_score_pad__score_column_body,
        ) {
            override val mapping: (ScorePadRow) -> ResOrActual<String>
                get() = { ResOrActual.Actual(it.score.toString()) }
            override val cellContentDescription: (ScorePadRow, Unit) -> ResOrActual<String>?
                get() = { row, _ ->
                    ResOrActual.StringResource(
                            R.string.score_pad__score_accessibility,
                            listOf(mapping(row))
                    )
                }
        },
        RUNNING_TOTAL(
                headingId = R.string.score_pad__running_total_header,
                helpTitleId = R.string.help_score_pad__running_column_title,
                helpBodyId = R.string.help_score_pad__running_column_body,
        ) {
            override val mapping: (ScorePadRow) -> ResOrActual<String>
                get() = { row ->
                    row.runningTotal?.let { ResOrActual.Actual(it.toString()) }
                            ?: ResOrActual.StringResource(R.string.score_pad__running_total_placeholder)
                }
            override val cellContentDescription: (ScorePadRow, Unit) -> ResOrActual<String>?
                get() = { row, _ ->
                    if (row !is ScorePadRow.End) null
                    else ResOrActual.StringResource(
                            R.string.score_pad__running_total_accessibility,
                            listOf(mapping(row))
                    )
                }
        },
        ;

        override val primaryTitle: ResOrActual<String>?
            get() = ResOrActual.StringResource(headingId)
        override val helpTitle: ResOrActual<String>
            get() = ResOrActual.StringResource(helpTitleId)
        override val helpBody: ResOrActual<String>
            get() = ResOrActual.StringResource(helpBodyId)
    }
}
