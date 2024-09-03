package eywa.projectcodex.components.referenceTables.handicapTables.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.codexTheme.asClickableStyle
import eywa.projectcodex.common.sharedUi.numberField.PartialNumberFieldState
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogState
import eywa.projectcodex.common.sharedUi.testTag
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsStatePreviewHelper
import eywa.projectcodex.components.referenceTables.handicapTables.HandicapScore
import eywa.projectcodex.components.referenceTables.handicapTables.HandicapTablesIntent
import eywa.projectcodex.components.referenceTables.handicapTables.HandicapTablesIntent.HelpShowcaseAction
import eywa.projectcodex.components.referenceTables.handicapTables.HandicapTablesIntent.ToggleSimpleView
import eywa.projectcodex.components.referenceTables.handicapTables.HandicapTablesState
import eywa.projectcodex.components.referenceTables.handicapTables.HandicapTablesViewModel
import eywa.projectcodex.components.referenceTables.handicapTables.InputType

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
                    .padding(vertical = CodexTheme.dimens.screenPadding)
                    .testTag(HandicapTablesTestTag.SCREEN)
    ) {
        ProvideTextStyle(value = CodexTypography.NORMAL.copy(CodexTheme.colors.onAppBackground)) {
            Selections(state, listener)
            RoundSelector(state, listener)
            HandicapDisplay(state, state.useSimpleHandicapView, listener)
            if (!state.useSimpleHandicapView) {
                DetailedHandicapDisplay(state, listener)
            }
            Text(
                    text = stringResource(
                            if (state.useSimpleHandicapView) R.string.archer_round_stats__show_advanced_view
                            else R.string.archer_round_stats__show_simple_view,
                    ),
                    style = LocalTextStyle.current.asClickableStyle(),
                    modifier = Modifier
                            .clickable { listener(ToggleSimpleView) }
                            .testTag(HandicapTablesTestTag.SIMPLE_TOGGLE)
                            .padding(vertical = 10.dp)
            )
        }
    }
}

enum class HandicapTablesTestTag : CodexTestTag {
    SCREEN,
    SYSTEM_SELECTOR,
    COMPOUND_SELECTOR,
    INPUT_SELECTOR,
    INPUT_TEXT,
    INPUT_ERROR,
    TABLE_EMPTY_TEXT,
    TABLE_HANDICAP,
    TABLE_SCORE,
    TABLE_AVERAGE_ARROW,
    TABLE_AVERAGE_END,
    TABLE_ALLOWANCE,
    DETAIL_TABLE_DISTANCE,
    DETAIL_TABLE_SCORE,
    DETAIL_TABLE_AVERAGE_ARROW,
    DETAIL_TABLE_AVERAGE_END,
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
                    updateDefaultRoundsState = UpdateDefaultRoundsStatePreviewHelper.complete,
            ),
    ) {}
}

@Preview(
        heightDp = 1200
)
@Composable
fun HandicapTablesScreen_Preview() {
    HandicapTablesScreen(
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
            ),
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
            ),
    ) {}
}
