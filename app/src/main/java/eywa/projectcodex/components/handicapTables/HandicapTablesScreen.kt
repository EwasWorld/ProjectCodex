package eywa.projectcodex.components.handicapTables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import eywa.projectcodex.common.helpShowcase.*
import eywa.projectcodex.common.sharedUi.ComposeUtils.modifierIf
import eywa.projectcodex.common.sharedUi.DataRow
import eywa.projectcodex.common.sharedUi.numberField.CodexLabelledNumberField
import eywa.projectcodex.common.sharedUi.numberField.PartialNumberFieldState
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogState
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundRows
import eywa.projectcodex.common.sharedUi.selectRoundFaceDialog.SelectRoundFaceDialog
import eywa.projectcodex.common.utils.CodexTestTag
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
            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                    .fillMaxSize()
                    .background(CodexTheme.colors.appBackground)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 20.dp)
                    .testTag(HandicapTablesTestTag.SCREEN.getTestTag())
    ) {
        ProvideTextStyle(value = CodexTypography.NORMAL.copy(CodexTheme.colors.onAppBackground)) {
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
                    style = CodexTypography.NORMAL,
                    modifier = Modifier.padding(bottom = 2.dp)
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
                    style = CodexTypography.NORMAL,
            )
        }

        ProvideTextStyle(value = CodexTypography.NORMAL.copy(CodexTheme.colors.onAppBackground)) {
            CodexLabelledNumberField(
                    title = stringResource(state.inputType.labelId),
                    currentValue = state.inputFull.text,
                    placeholder = "50",
                    testTag = "",
                    helpState = HelpState(
                            helpListener = helpListener,
                            helpTitle = stringResource(R.string.help_handicap_tables__input_title),
                            helpBody = stringResource(state.inputType.inputHelpId),
                    ),
                    onValueChanged = { listener(InputChanged(it)) },
            )

            Surface(
                    shape = RoundedCornerShape(20),
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
                    SelectRoundFaceDialog(
                            state = state.selectFaceDialogState,
                            helpListener = helpListener,
                            listener = { listener(SelectFaceDialogAction(it)) },
                    )
                }
            }
        }

        Surface(
                shape = RoundedCornerShape(10),
                color = CodexTheme.colors.listItemOnAppBackground,
                modifier = Modifier.padding(20.dp)
        ) {
            Table(state.handicaps, state.highlightedHandicap, helpListener)
        }
    }
}

@Composable
private fun Table(
        handicaps: List<HandicapScore>,
        highlighted: HandicapScore?,
        helpListener: (HelpShowcaseIntent) -> Unit,
) {
    helpListener(
            HelpShowcaseIntent.Add(
                    HelpShowcaseItem(
                            helpTitle = stringResource(R.string.help_handicap_tables__table_title),
                            helpBody = stringResource(R.string.help_handicap_tables__table_body),
                            priority = DEFAULT_HELP_PRIORITY + 1,
                    ),
            )
    )

    ProvideTextStyle(value = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onListItemAppOnBackground)) {
        Column(
                verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterVertically),
                modifier = Modifier.padding(10.dp)
        ) {
            if (handicaps.isNotEmpty()) {
                Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
                        modifier = Modifier.fillMaxWidth(0.6f)
                ) {
                    Text(
                            text = stringResource(R.string.handicap_tables__handicap_field),
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                    )
                    Text(
                            text = stringResource(R.string.handicap_tables__score_field),
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                    )
                }

                handicaps.forEach {
                    Row(
                            horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
                            modifier = Modifier
                                    .modifierIf(
                                            predicate = it == highlighted,
                                            modifier = Modifier
                                                    .background(CodexTheme.colors.appBackground)
                                                    .updateHelpDialogPosition(
                                                            helpListener,
                                                            stringResource(R.string.help_handicap_tables__table_title),
                                                    )
                                    )
                                    .fillMaxWidth(0.6f)
                    ) {
                        Text(
                                text = it.handicap.toString(),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                        )
                        Text(
                                text = it.score.toString(),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            else {
                Text(
                        text = stringResource(R.string.handicap_tables__no_tables),
                        modifier = Modifier
                                .updateHelpDialogPosition(
                                        helpListener,
                                        stringResource(R.string.help_handicap_tables__table_title),
                                )
                )
            }
        }
    }
}

enum class HandicapTablesTestTag : CodexTestTag {
    SCREEN,
    ;

    override val screenName: String
        get() = "HANDICAP_TABLES"

    override fun getElement(): String = name
}

@Preview
@Composable
fun PreviewMainMenuScreen() {
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
