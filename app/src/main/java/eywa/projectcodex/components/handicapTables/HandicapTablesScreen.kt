package eywa.projectcodex.components.handicapTables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.DEFAULT_HELP_PRIORITY
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.helpShowcase.HelpState
import eywa.projectcodex.common.helpShowcase.updateHelpDialogPosition
import eywa.projectcodex.common.sharedUi.CodexGrid
import eywa.projectcodex.common.sharedUi.ComposeUtils.modifierIf
import eywa.projectcodex.common.sharedUi.DataRow
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.numberField.CodexLabelledNumberField
import eywa.projectcodex.common.sharedUi.numberField.PartialNumberFieldState
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogState
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundRows
import eywa.projectcodex.common.sharedUi.selectRoundFaceDialog.SelectRoundFaceDialog
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.components.handicapTables.HandicapTablesIntent.HelpShowcaseAction
import eywa.projectcodex.components.handicapTables.HandicapTablesIntent.InputChanged
import eywa.projectcodex.components.handicapTables.HandicapTablesIntent.SelectFaceDialogAction
import eywa.projectcodex.components.handicapTables.HandicapTablesIntent.SelectRoundDialogAction
import eywa.projectcodex.components.handicapTables.HandicapTablesIntent.ToggleHandicapSystem
import eywa.projectcodex.components.handicapTables.HandicapTablesIntent.ToggleInput

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
                    .testTag(HandicapTablesTestTag.SCREEN.getTestTag())
    ) {
        ProvideTextStyle(value = CodexTypography.NORMAL.copy(CodexTheme.colors.onAppBackground)) {
            Selections(state, listener)
            RoundSelector(state, listener)
            HandicapDisplay(state, listener)
        }
    }
}

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
            helpState = HelpState(
                    helpListener = helpListener,
                    helpTitle = stringResource(R.string.help_handicap_tables__2023_system_title),
                    helpBody = stringResource(R.string.help_handicap_tables__2023_system_body),
            ),
            onClick = { listener(ToggleHandicapSystem) },
            accessibilityRole = Role.Switch,
            modifier = Modifier
                    .padding(bottom = 2.dp)
                    .testTag(HandicapTablesTestTag.SYSTEM_SELECTOR.getTestTag())
    )

    DataRow(
            title = stringResource(R.string.handicap_tables__input),
            text = stringResource(state.inputType.labelId),
            helpState = HelpState(
                    helpListener = helpListener,
                    helpTitle = stringResource(R.string.help_handicap_tables__input_type_title),
                    helpBody = stringResource(state.inputType.typeHelpId),
            ),
            onClick = { listener(ToggleInput) },
            accessibilityRole = Role.Switch,
            modifier = Modifier.testTag(HandicapTablesTestTag.INPUT_SELECTOR.getTestTag())
    )

    CodexLabelledNumberField(
            title = stringResource(state.inputType.labelId),
            currentValue = state.inputFull.text,
            placeholder = "50",
            testTag = HandicapTablesTestTag.INPUT_TEXT,
            helpState = HelpState(
                    helpListener = helpListener,
                    helpTitle = stringResource(R.string.help_handicap_tables__input_title),
                    helpBody = stringResource(state.inputType.inputHelpId),
            ),
            onValueChanged = { listener(InputChanged(it)) },
    )
}

@Composable
fun RoundSelector(
        state: HandicapTablesState,
        listener: (HandicapTablesIntent) -> Unit,
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(HelpShowcaseAction(it)) }

    Surface(
            shape = RoundedCornerShape(
                    if (state.selectRoundDialogState.selectedRound == null) CodexTheme.dimens.smallCornerRounding
                    else CodexTheme.dimens.cornerRounding
            ),
            border = BorderStroke(1.dp, CodexTheme.colors.listItemOnAppBackground),
            color = CodexTheme.colors.appBackground,
            modifier = Modifier.padding(horizontal = 20.dp)
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

@Composable
fun HandicapDisplay(
        state: HandicapTablesState,
        listener: (HandicapTablesIntent) -> Unit,
) {
    Surface(
            shape = RoundedCornerShape(
                    if (state.handicaps.isEmpty()) CodexTheme.dimens.smallCornerRounding
                    else CodexTheme.dimens.cornerRounding
            ),
            color = CodexTheme.colors.listItemOnAppBackground,
            modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(20.dp)
    ) {
        val helpState = HelpState(
                helpListener = { listener(HelpShowcaseAction(it)) },
                helpShowcaseItem = HelpShowcaseItem(
                        helpTitle = stringResource(R.string.help_handicap_tables__table_title),
                        helpBody = stringResource(R.string.help_handicap_tables__table_body),
                        priority = DEFAULT_HELP_PRIORITY + 1,
                ),
        )
        ProvideTextStyle(value = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onListItemAppOnBackground)) {
            if (state.handicaps.isEmpty()) {
                Text(
                        text = stringResource(R.string.handicap_tables__no_tables),
                        modifier = Modifier
                                .updateHelpDialogPosition(helpState)
                                .testTag(HandicapTablesTestTag.TABLE_EMPTY_TEXT.getTestTag())
                                .padding(10.dp)
                )
            }
            else {
                Table(state.handicaps, state.highlightedHandicap, helpState)
            }
        }
    }
}

@Composable
private fun Table(
        handicaps: List<HandicapScore>,
        highlighted: HandicapScore?,
        helpState: HelpState,
) {
    CodexGrid(
            columns = 3,
            alignment = Alignment.Center,
            modifier = Modifier
                    .updateHelpDialogPosition(helpState)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        listOf(
                R.string.handicap_tables__handicap_field,
                R.string.handicap_tables__score_field,
                R.string.handicap_tables__allowance_field,
        ).forEach {
            item {
                Text(
                        text = stringResource(it),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                )
            }
        }

        handicaps.forEach {
            val isHighlighted = it == highlighted

            listOf(
                    it.handicap to HandicapTablesTestTag.TABLE_HANDICAP,
                    it.score to HandicapTablesTestTag.TABLE_SCORE,
                    it.allowance to HandicapTablesTestTag.TABLE_ALLOWANCE,
            ).forEach { (it, testTag) ->
                item(fillBox = isHighlighted) {
                    Text(
                            text = it.toString(),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                    .modifierIf(
                                            predicate = isHighlighted,
                                            modifier = Modifier.background(CodexTheme.colors.appBackground)
                                    )
                                    .padding(3.dp)
                                    .testTag(testTag.getTestTag())
                    )
                }
            }
        }
    }
}

enum class HandicapTablesTestTag : CodexTestTag {
    SCREEN,
    SYSTEM_SELECTOR,
    INPUT_SELECTOR,
    INPUT_TEXT,
    TABLE_EMPTY_TEXT,
    TABLE_HANDICAP,
    TABLE_SCORE,
    TABLE_ALLOWANCE,
    ;

    override val screenName: String
        get() = "HANDICAP_TABLES"

    override fun getElement(): String = name
}

@Preview
@Composable
fun HandicapTablesScreen_Preview() {
    HandicapTablesScreen(
            HandicapTablesState(
                    input = PartialNumberFieldState().onValueChanged("31"),
                    inputType = InputType.HANDICAP,
                    use2023System = false,
                    selectRoundDialogState = SelectRoundDialogState(
                            selectedRoundId = RoundPreviewHelper.indoorMetricRoundData.round.roundId,
                            allRounds = listOf(RoundPreviewHelper.indoorMetricRoundData),
                    ),
                    handicaps = listOf(
                            HandicapScore(26, 333),
                            HandicapScore(27, 331),
                            HandicapScore(28, 330),
                            HandicapScore(29, 328),
                            HandicapScore(30, 326),
                            HandicapScore(31, 324),
                            HandicapScore(32, 322),
                            HandicapScore(33, 319),
                            HandicapScore(34, 317),
                            HandicapScore(35, 315),
                            HandicapScore(36, 312),
                    ),
                    highlightedHandicap = HandicapScore(31, 324),
            )
    ) {}
}
