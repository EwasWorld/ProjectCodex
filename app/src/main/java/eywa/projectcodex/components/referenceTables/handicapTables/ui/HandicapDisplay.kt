package eywa.projectcodex.components.referenceTables.handicapTables.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.DEFAULT_HELP_PRIORITY
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.helpShowcase.asHelpState
import eywa.projectcodex.common.helpShowcase.updateHelpDialogPosition
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.grid.CodexGridColumn
import eywa.projectcodex.common.sharedUi.grid.CodexGridColumnMetadata
import eywa.projectcodex.common.sharedUi.grid.CodexGridWithHeaders
import eywa.projectcodex.common.sharedUi.testTag
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.common.utils.asDecimalFormat
import eywa.projectcodex.components.referenceTables.handicapTables.HandicapScore
import eywa.projectcodex.components.referenceTables.handicapTables.HandicapTablesIntent
import eywa.projectcodex.components.referenceTables.handicapTables.HandicapTablesState

@Composable
internal fun HandicapDisplay(
        state: HandicapTablesState,
        simpleView: Boolean,
        listener: (HandicapTablesIntent) -> Unit,
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(HandicapTablesIntent.HelpShowcaseAction(it)) }
    ProvideTextStyle(value = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onListItemAppOnBackground)) {
        if (state.handicaps.isEmpty()) {
            Surface(
                    shape = RoundedCornerShape(CodexTheme.dimens.smallCornerRounding),
                    color = CodexTheme.colors.listItemOnAppBackground,
                    modifier = Modifier.padding(20.dp)
            ) {
                val helpState = HelpShowcaseItem(
                        helpTitle = stringResource(R.string.help_handicap_tables__table_title),
                        helpBody = stringResource(R.string.help_handicap_tables__table_body),
                        priority = DEFAULT_HELP_PRIORITY + 1,
                ).asHelpState(helpListener)
                Text(
                        text = stringResource(R.string.handicap_tables__no_tables),
                        modifier = Modifier
                                .updateHelpDialogPosition(helpState)
                                .testTag(HandicapTablesTestTag.TABLE_EMPTY_TEXT)
                                .padding(10.dp)
                )
            }
        }
        else {
            val columnMetadata =
                    if (simpleView) SimpleHandicapTableColumn.entries else HandicapTableColumn.entries
            val columns = List(columnMetadata.size) {
                if (simpleView) CodexGridColumn.Match(1) else CodexGridColumn.WrapContent
            }

            CodexGridWithHeaders(
                    data = state.handicaps,
                    columnMetadata = columnMetadata.toList(),
                    columns = columns,
                    extraData = Unit,
                    helpListener = helpListener,
                    modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(CodexTheme.dimens.screenPadding)
            )
        }
    }
}

private enum class SimpleHandicapTableColumn(
        val title: ResOrActual<String>,
        val metadata: HandicapTableColumn,
) : CodexGridColumnMetadata<HandicapScore, Unit> {
    HANDICAP(
            title = ResOrActual.StringResource(R.string.handicap_tables__handicap_field),
            metadata = HandicapTableColumn.HANDICAP,
    ),
    SCORE(
            title = ResOrActual.StringResource(R.string.handicap_tables__score_field),
            metadata = HandicapTableColumn.SCORE,
    ),
    ALLOWANCE(
            title = ResOrActual.StringResource(R.string.handicap_tables__allowance_field),
            metadata = HandicapTableColumn.ALLOWANCE,
    ),
    ;

    override val primaryTitle: ResOrActual<String>?
        get() = title

    override val primaryTitleHorizontalSpan: Int
        get() = 1

    override val primaryTitleVerticalSpan: Int
        get() = 1

    override val secondaryTitle: ResOrActual<String>?
        get() = null

    override val helpTitle: ResOrActual<String>
        get() = metadata.helpTitle

    override val helpBody: ResOrActual<String>
        get() = metadata.helpBody

    override val testTag: CodexTestTag?
        get() = metadata.testTag

    override val mapping: (HandicapScore) -> ResOrActual<String>
        get() = metadata.mapping

    override val cellContentDescription: (HandicapScore, Unit) -> ResOrActual<String>
        get() = metadata.cellContentDescription
}

private enum class HandicapTableColumn(
        override val primaryTitle: ResOrActual<String>?,
        override val primaryTitleHorizontalSpan: Int,
        override val primaryTitleVerticalSpan: Int,
        override val secondaryTitle: ResOrActual<String>?,
        override val testTag: HandicapTablesTestTag,
        override val helpTitle: ResOrActual<String>,
        override val helpBody: ResOrActual<String>,
        override val mapping: (HandicapScore) -> ResOrActual<String>,
        override val cellContentDescription: (HandicapScore, Unit) -> ResOrActual<String>,
) : CodexGridColumnMetadata<HandicapScore, Unit> {
    HANDICAP(
            primaryTitle = ResOrActual.StringResource(R.string.handicap_tables__handicap_field_short),
            primaryTitleHorizontalSpan = 1,
            primaryTitleVerticalSpan = 2,
            secondaryTitle = null,
            testTag = HandicapTablesTestTag.TABLE_HANDICAP,
            helpTitle = ResOrActual.StringResource(R.string.help_handicap_tables__table_handicap_title),
            helpBody = ResOrActual.StringResource(R.string.help_handicap_tables__table_handicap_body),
            mapping = { ResOrActual.Actual(it.handicap.toString()) },
            cellContentDescription = { it, _ ->
                ResOrActual.StringResource(
                        R.string.handicap_tables__handicap_semantics,
                        listOf(it.handicap),
                )
            },
    ),
    SCORE(
            primaryTitle = ResOrActual.StringResource(R.string.handicap_tables__score_field),
            primaryTitleHorizontalSpan = 3,
            primaryTitleVerticalSpan = 1,
            secondaryTitle = ResOrActual.StringResource(R.string.handicap_tables__total_score_field),
            testTag = HandicapTablesTestTag.TABLE_SCORE,
            helpTitle = ResOrActual.StringResource(R.string.help_handicap_tables__table_score_total_title),
            helpBody = ResOrActual.StringResource(R.string.help_handicap_tables__table_score_total_body),
            mapping = { ResOrActual.Actual(it.score.toString()) },
            cellContentDescription = { it, _ ->
                ResOrActual.StringResource(
                        R.string.handicap_tables__score_semantics,
                        listOf(it.score.toString()),
                )
            },
    ),
    SCORE_PER_END(
            primaryTitle = null,
            primaryTitleHorizontalSpan = 1,
            primaryTitleVerticalSpan = 1,
            secondaryTitle = ResOrActual.StringResource(R.string.handicap_tables__average_end_field),
            testTag = HandicapTablesTestTag.TABLE_AVERAGE_END,
            helpTitle = ResOrActual.StringResource(R.string.help_handicap_tables__table_score_end_title),
            helpBody = ResOrActual.StringResource(R.string.help_handicap_tables__table_score_end_body),
            mapping = { it.averageEnd.asDecimalFormat() },
            cellContentDescription = { it, _ ->
                ResOrActual.StringResource(
                        R.string.handicap_tables__average_end_semantics,
                        listOf(it.averageEnd.asDecimalFormat()),
                )
            },
    ),
    SCORE_PER_ARROW(
            primaryTitle = null,
            primaryTitleHorizontalSpan = 1,
            primaryTitleVerticalSpan = 1,
            secondaryTitle = ResOrActual.StringResource(R.string.handicap_tables__average_arrow_field),
            testTag = HandicapTablesTestTag.TABLE_AVERAGE_ARROW,
            helpTitle = ResOrActual.StringResource(R.string.help_handicap_tables__table_score_arrow_title),
            helpBody = ResOrActual.StringResource(R.string.help_handicap_tables__table_score_arrow_body),
            mapping = { it.averageArrow.asDecimalFormat() },
            cellContentDescription = { it, _ ->
                ResOrActual.StringResource(
                        R.string.handicap_tables__average_arrow_semantics,
                        listOf(it.averageArrow.asDecimalFormat()),
                )
            },
    ),
    ALLOWANCE(
            primaryTitle = ResOrActual.StringResource(R.string.handicap_tables__allowance_field_two_lines),
            primaryTitleHorizontalSpan = 1,
            primaryTitleVerticalSpan = 2,
            secondaryTitle = null,
            testTag = HandicapTablesTestTag.TABLE_ALLOWANCE,
            helpTitle = ResOrActual.StringResource(R.string.help_handicap_tables__table_allowance_title),
            helpBody = ResOrActual.StringResource(R.string.help_handicap_tables__table_allowance_body),
            mapping = { ResOrActual.Actual(it.allowance.toString()) },
            cellContentDescription = { it, _ ->
                ResOrActual.StringResource(
                        R.string.handicap_tables__allowance_semantics,
                        listOf(it.allowance),
                )
            },
    ),
}
