package eywa.projectcodex.components.handicapTables.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.grid.CodexGridColumnMetadata
import eywa.projectcodex.common.sharedUi.grid.CodexGridWithHeaders
import eywa.projectcodex.common.sharedUi.numberField.PartialNumberFieldState
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogState
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.common.utils.asDecimalFormat
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsStatePreviewHelper
import eywa.projectcodex.components.handicapTables.DetailedHandicapBreakdown
import eywa.projectcodex.components.handicapTables.HandicapScore
import eywa.projectcodex.components.handicapTables.HandicapTablesIntent
import eywa.projectcodex.components.handicapTables.HandicapTablesState
import eywa.projectcodex.components.handicapTables.InputType

@Composable
fun DetailedHandicapDisplay(
        state: HandicapTablesState,
        listener: (HandicapTablesIntent) -> Unit,
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(HandicapTablesIntent.HelpShowcaseAction(it)) }
    val detailedHandicaps = state.detailedHandicaps

    if (detailedHandicaps.isNullOrEmpty() || state.distanceUnitRes == null) return

    Column(
            verticalArrangement = Arrangement.spacedBy(15.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
                text = stringResource(
                        R.string.handicap_tables__breakdown_header,
                        state.highlightedHandicap!!.handicap,
                ),
                style = CodexTypography.NORMAL.copy(CodexTheme.colors.onAppBackground),
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = CodexTheme.dimens.screenPadding)
        )
        ProvideTextStyle(value = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onListItemAppOnBackground)) {
            CodexGridWithHeaders(
                    data = detailedHandicaps,
                    columnMetadata = DetailedHandicapTableColumn.entries.toList(),
                    extraData = ResOrActual.StringResource(state.distanceUnitRes),
                    helpListener = helpListener,
                    modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = CodexTheme.dimens.screenPadding)
            )
        }
    }
}

private enum class DetailedHandicapTableColumn(
        override val primaryTitle: ResOrActual<String>?,
        override val primaryTitleHorizontalSpan: Int,
        override val primaryTitleVerticalSpan: Int,
        override val secondaryTitle: ResOrActual<String>?,
        override val testTag: HandicapTablesTestTag,
        override val helpTitle: ResOrActual<String>,
        override val helpBody: ResOrActual<String>,
        override val mapping: (DetailedHandicapBreakdown) -> ResOrActual<String>,
        override val cellContentDescription: (DetailedHandicapBreakdown, ResOrActual<String>) -> ResOrActual<String>,
) : CodexGridColumnMetadata<DetailedHandicapBreakdown, ResOrActual<String>> {
    DISTANCE(
            primaryTitle = ResOrActual.StringResource(R.string.handicap_tables__distance_field),
            primaryTitleHorizontalSpan = 1,
            primaryTitleVerticalSpan = 2,
            secondaryTitle = null,
            testTag = HandicapTablesTestTag.DETAIL_TABLE_DISTANCE,
            helpTitle = ResOrActual.StringResource(R.string.help_handicap_tables__table_distance_title),
            helpBody = ResOrActual.StringResource(R.string.help_handicap_tables__table_distance_body),
            mapping = { ResOrActual.Actual(it.distance.toString()) },
            cellContentDescription = { it, distanceUnit ->
                ResOrActual.StringResource(
                        R.string.handicap_tables__distance_semantics,
                        listOf(it.distance, distanceUnit),
                )
            },
    ),
    SCORE(
            primaryTitle = ResOrActual.StringResource(R.string.handicap_tables__score_field),
            primaryTitleHorizontalSpan = 3,
            primaryTitleVerticalSpan = 1,
            secondaryTitle = ResOrActual.StringResource(R.string.handicap_tables__total_score_field),
            testTag = HandicapTablesTestTag.DETAIL_TABLE_SCORE,
            helpTitle = ResOrActual.StringResource(R.string.help_handicap_tables__table_score_total_title),
            helpBody = ResOrActual.StringResource(R.string.help_handicap_tables__table_score_total_body),
            mapping = { it.score.asDecimalFormat() },
            cellContentDescription = { it, _ ->
                ResOrActual.StringResource(
                        R.string.handicap_tables__score_semantics,
                        listOf(it.score.asDecimalFormat()),
                )
            },
    ),
    SCORE_PER_END(
            primaryTitle = null,
            primaryTitleHorizontalSpan = 1,
            primaryTitleVerticalSpan = 1,
            secondaryTitle = ResOrActual.StringResource(R.string.handicap_tables__average_end_field),
            testTag = HandicapTablesTestTag.DETAIL_TABLE_AVERAGE_END,
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
            testTag = HandicapTablesTestTag.DETAIL_TABLE_AVERAGE_ARROW,
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
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun DetailedHandicapDisplay_Preview() {
    CodexTheme {
        DetailedHandicapDisplay(
                HandicapTablesState(
                        input = PartialNumberFieldState().onTextChanged("31"),
                        inputType = InputType.HANDICAP,
                        use2023System = false,
                        selectRoundDialogState = SelectRoundDialogState(
                                selectedRoundId = RoundPreviewHelper.wa1440RoundData.round.roundId,
                                allRounds = listOf(RoundPreviewHelper.wa1440RoundData),
                        ),
                        handicaps = listOf(
                                HandicapScore(26, 333, 60, 3),
                                HandicapScore(27, 331, 60, 3),
                                HandicapScore(28, 330, 60, 3),
                                HandicapScore(29, 328, 60, 3),
                                HandicapScore(30, 326, 60, 3),
                                HandicapScore(31, 324, 60, 3, true),
                                HandicapScore(32, 322, 60, 3),
                                HandicapScore(33, 319, 60, 3),
                                HandicapScore(34, 317, 60, 3),
                                HandicapScore(35, 315, 60, 3),
                                HandicapScore(36, 312, 60, 3),
                        ),
                        updateDefaultRoundsState = UpdateDefaultRoundsStatePreviewHelper.complete,
                        useSimpleHandicapView = false,
                )
        ) {}
    }
}
