package eywa.projectcodex.components.handicapTables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.DEFAULT_HELP_PRIORITY
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.helpShowcase.asHelpState
import eywa.projectcodex.common.helpShowcase.updateHelpDialogPosition
import eywa.projectcodex.common.sharedUi.DataRow
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.codexTheme.asClickableStyle
import eywa.projectcodex.common.sharedUi.grid.CodexGridColumn
import eywa.projectcodex.common.sharedUi.grid.CodexGridColumnMetadata
import eywa.projectcodex.common.sharedUi.grid.CodexGridWithHeaders
import eywa.projectcodex.common.sharedUi.numberField.CodexNumberField
import eywa.projectcodex.common.sharedUi.numberField.CodexNumberFieldErrorText
import eywa.projectcodex.common.sharedUi.numberField.PartialNumberFieldState
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.selectRoundDialog.RoundsUpdatingWrapper
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogState
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundRows
import eywa.projectcodex.common.sharedUi.selectRoundFaceDialog.SelectRoundFaceDialog
import eywa.projectcodex.common.sharedUi.testTag
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.common.utils.asDecimalFormat
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsStatePreviewHelper
import eywa.projectcodex.components.handicapTables.HandicapTablesIntent.*

@Composable
fun HandicapTablesScreen(
        viewModel: HandicapTablesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    HandicapTablesScreen(state = state, listener = { viewModel.handle(it) })
}

@Composable
fun HandicapTablesScreen(
        state: HandicapTablesState,
        listener: (HandicapTablesIntent) -> Unit,
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(HelpShowcaseAction(it)) }
    helpListener(HelpShowcaseIntent.Clear)

    Column(
            verticalArrangement = Arrangement.spacedBy(15.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                    .fillMaxSize()
                    .background(CodexTheme.colors.appBackground)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 20.dp)
                    .testTag(HandicapTablesTestTag.SCREEN)
    ) {
        ProvideTextStyle(value = CodexTypography.NORMAL.copy(CodexTheme.colors.onAppBackground)) {
            Selections(state, listener)
            RoundSelector(state, listener)
            HandicapDisplay(state, state.useSimpleHandicapView, listener)
            Text(
                    text = stringResource(
                            if (state.useSimpleHandicapView) R.string.archer_round_stats__show_advanced_view
                            else R.string.archer_round_stats__show_simple_view
                    ),
                    style = LocalTextStyle.current.asClickableStyle(),
                    modifier = Modifier
                            .clickable { listener(ToggleSimpleView) }
                            .testTag(HandicapTablesTestTag.SIMPLE_TOGGLE)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Selections(
        state: HandicapTablesState,
        listener: (HandicapTablesIntent) -> Unit,
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(HelpShowcaseAction(it)) }

    DataRow(
            title = stringResource(R.string.handicap_tables__handicap_system_title),
            text = stringResource(
                    if (state.use2023System) R.string.handicap_tables__handicap_system_agb_2023
                    else R.string.handicap_tables__handicap_system_david_lane
            ),
            helpState = HelpShowcaseItem(
                    helpTitle = stringResource(R.string.help_handicap_tables__2023_system_title),
                    helpBody = stringResource(R.string.help_handicap_tables__2023_system_body),
            ).asHelpState(helpListener),
            onClick = { listener(ToggleHandicapSystem) },
            accessibilityRole = Role.Switch,
            modifier = Modifier.testTag(HandicapTablesTestTag.SYSTEM_SELECTOR)
    )

    Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
            modifier = Modifier.padding(vertical = 2.dp)
    ) {
        FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        ) {
            Text(
                    text = stringResource(state.inputType.labelId),
                    style = LocalTextStyle.current.asClickableStyle(),
                    modifier = Modifier
                            .testTag(HandicapTablesTestTag.INPUT_SELECTOR)
                            .clickable(
                                    onClickLabel = stringResource(R.string.handicap_tables__input_selector_click_label),
                                    onClick = { listener(ToggleInput) },
                                    role = Role.Switch,
                            )
                            .updateHelpDialogPosition(
                                    HelpShowcaseItem(
                                            helpTitle = stringResource(R.string.help_handicap_tables__input_type_title),
                                            helpBody = stringResource(state.inputType.typeHelpId),
                                    ).asHelpState(helpListener)
                            )
                            .align(Alignment.CenterVertically)
            )
            CodexNumberField(
                    currentValue = state.inputFull.text,
                    placeholder = "50",
                    contentDescription = stringResource(state.inputType.labelId),
                    errorMessage = state.inputFull.error,
                    testTag = HandicapTablesTestTag.INPUT_TEXT,
                    onValueChanged = { listener(InputChanged(it)) },
                    modifier = Modifier
                            .updateHelpDialogPosition(
                                    HelpShowcaseItem(
                                            helpTitle = stringResource(R.string.help_handicap_tables__input_title),
                                            helpBody = stringResource(state.inputType.inputHelpId),
                                    ).asHelpState(helpListener)
                            )
                            .align(Alignment.CenterVertically)
            )
        }
        CodexNumberFieldErrorText(
                errorText = state.inputFull.error,
                testTag = HandicapTablesTestTag.INPUT_ERROR,
        )
    }
}

@Composable
fun RoundSelector(
        state: HandicapTablesState,
        listener: (HandicapTablesIntent) -> Unit,
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(HelpShowcaseAction(it)) }

    Surface(
            shape = RoundedCornerShape(
                    if (!state.updateDefaultRoundsState.hasTaskFinished) CodexTheme.dimens.cornerRounding
                    else if (state.selectRoundDialogState.selectedRound == null) CodexTheme.dimens.smallCornerRounding
                    else CodexTheme.dimens.cornerRounding
            ),
            border = BorderStroke(1.dp, CodexTheme.colors.listItemOnAppBackground),
            color = CodexTheme.colors.appBackground,
            modifier = Modifier.padding(horizontal = 20.dp)
    ) {
        RoundsUpdatingWrapper(
                state = state.updateDefaultRoundsState,
                warningModifier = Modifier.padding(10.dp)
        ) {
            Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                SelectRoundRows(
                        state = state.selectRoundDialogState,
                        helpListener = helpListener,
                        listener = { listener(SelectRoundDialogAction(it)) },
                )
                if (state.selectRoundDialogState.selectedRound != null) {
                    SelectRoundFaceDialog(
                            state = state.selectFaceDialogState,
                            helpListener = helpListener,
                            listener = { listener(SelectFaceDialogAction(it)) },
                    )
                }
            }
        }
    }
}

@Composable
fun HandicapDisplay(
        state: HandicapTablesState,
        simpleView: Boolean,
        listener: (HandicapTablesIntent) -> Unit,
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(HelpShowcaseAction(it)) }
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
                            .padding(20.dp)
            )
        }
    }
}

enum class SimpleHandicapTableColumn(
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

enum class HandicapTableColumn(
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
                        listOf(it.score),
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

enum class HandicapTablesTestTag : CodexTestTag {
    SCREEN,
    SYSTEM_SELECTOR,
    INPUT_SELECTOR,
    INPUT_TEXT,
    INPUT_ERROR,
    TABLE_EMPTY_TEXT,
    TABLE_HANDICAP,
    TABLE_SCORE,
    TABLE_AVERAGE_ARROW,
    TABLE_AVERAGE_END,
    TABLE_ALLOWANCE,
    SIMPLE_TOGGLE,
    ;

    override val screenName: String
        get() = "HANDICAP_TABLES"

    override fun getElement(): String = name
}

@Preview
@Composable
fun Simple_HandicapTablesScreen_Preview() {
    HandicapTablesScreen(
            HandicapTablesState(
                    input = PartialNumberFieldState().onTextChanged("31"),
                    inputType = InputType.HANDICAP,
                    use2023System = false,
                    selectRoundDialogState = SelectRoundDialogState(
                            selectedRoundId = RoundPreviewHelper.indoorMetricRoundData.round.roundId,
                            allRounds = listOf(RoundPreviewHelper.indoorMetricRoundData),
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
                    highlightedHandicap = HandicapScore(31, 324, 60, 3),
                    updateDefaultRoundsState = UpdateDefaultRoundsStatePreviewHelper.complete,
            )
    ) {}
}

@Preview
@Composable
fun HandicapTablesScreen_Preview() {
    HandicapTablesScreen(
            HandicapTablesState(
                    input = PartialNumberFieldState().onTextChanged("31"),
                    inputType = InputType.HANDICAP,
                    use2023System = false,
                    selectRoundDialogState = SelectRoundDialogState(
                            selectedRoundId = RoundPreviewHelper.indoorMetricRoundData.round.roundId,
                            allRounds = listOf(RoundPreviewHelper.indoorMetricRoundData),
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
                    highlightedHandicap = HandicapScore(31, 324, 60, 3),
                    updateDefaultRoundsState = UpdateDefaultRoundsStatePreviewHelper.complete,
                    useSimpleHandicapView = false,
            )
    ) {}
}

@Preview
@Composable
fun Error_HandicapTablesScreen_Preview() {
    HandicapTablesScreen(
            HandicapTablesState(
                    input = PartialNumberFieldState().onTextChanged("-1"),
                    inputType = InputType.HANDICAP,
                    selectRoundDialogState = SelectRoundDialogState(
                            allRounds = listOf(RoundPreviewHelper.indoorMetricRoundData),
                    ),
                    updateDefaultRoundsState = UpdateDefaultRoundsStatePreviewHelper.complete,
            )
    ) {}
}
