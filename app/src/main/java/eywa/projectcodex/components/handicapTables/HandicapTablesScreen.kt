package eywa.projectcodex.components.handicapTables

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.ActionBarHelp
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.helpShowcase.updateHelpDialogPosition
import eywa.projectcodex.common.sharedUi.CodexCheckbox
import eywa.projectcodex.common.sharedUi.ComposeUtils.modifierIf
import eywa.projectcodex.common.sharedUi.NumberSetting
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundEnabledFilters
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundRows
import eywa.projectcodex.components.handicapTables.HandicapTablesIntent.*

class HandicapTablesScreen : ActionBarHelp {

    @Composable
    fun ComposeContent(
            state: HandicapTablesState,
            listener: (HandicapTablesIntent) -> Unit,
    ) {
        val helpListener = { it: HelpShowcaseIntent -> listener(HelpShowcaseAction(it)) }
        HelpInfo(state.inputHandicap, helpListener)

        Column(
                verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                        .fillMaxSize()
                        .background(CodexTheme.colors.appBackground)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 20.dp)
                        .testTag(TestTag.SCREEN)
        ) {
            ProvideTextStyle(value = CodexTypography.NORMAL.copy(CodexTheme.colors.onAppBackground)) {
                CodexCheckbox(
                        text = stringResource(R.string.handicap_tables__new_system_toggle),
                        checked = state.use2023Tables,
                        onToggle = { listener(ToggleHandicapSystem) },
                        modifier = Modifier
                                .updateHelpDialogPosition(
                                        helpListener,
                                        R.string.help_handicap_tables__2023_system_title
                                )
                )
            }

            Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier
                            .clickable { listener(ToggleInput) }
                            .updateHelpDialogPosition(helpListener, R.string.help_handicap_tables__input_type_title)
            ) {
                @Composable
                fun getStyle(isEnabled: Boolean) =
                        (if (isEnabled) CodexTypography.NORMAL else CodexTypography.SMALL).copy(
                                color = CodexTheme.colors.onAppBackground,
                                textDecoration = if (isEnabled) TextDecoration.None else TextDecoration.LineThrough
                        )
                Text(
                        text = stringResource(R.string.handicap_tables__input),
                        style = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground),
                )
                Text(
                        text = stringResource(R.string.handicap_tables__handicap_field),
                        style = getStyle(state.inputHandicap),
                )
                Switch(checked = !state.inputHandicap, onCheckedChange = { listener(ToggleInput) })
                Text(
                        text = stringResource(R.string.handicap_tables__score_field),
                        style = getStyle(!state.inputHandicap),
                )
            }

            ProvideTextStyle(value = CodexTypography.NORMAL.copy(CodexTheme.colors.onAppBackground)) {
                NumberSetting(
                        clazz = Int::class,
                        title = (
                                if (state.inputHandicap) R.string.handicap_tables__handicap_field
                                else R.string.handicap_tables__score_field
                                ),
                        currentValue = state.input,
                        placeholder = 50,
                        testTag = "",
                        onValueChanged = { listener(InputChanged(it)) },
                        modifier = Modifier
                                .updateHelpDialogPosition(helpListener, R.string.help_handicap_tables__input_title)
                )
            }

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
                            isSelectRoundDialogOpen = state.isSelectRoundDialogOpen,
                            isSelectSubtypeDialogOpen = state.isSelectSubtypeDialogOpen,
                            selectedRound = state.round?.info,
                            selectedSubtypeId = state.subType ?: 1,
                            rounds = state.allRounds,
                            filters = state.roundFilters,
                            helpListener = helpListener,
                            listener = { listener(SelectRoundDialogAction(it)) },
                    )
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
    private fun HelpInfo(
            inputHandicap: Boolean,
            helpListener: (HelpShowcaseIntent) -> Unit,
    ) {
        helpListener(
                HelpShowcaseIntent.Add(
                        HelpShowcaseItem(
                                helpTitle = R.string.help_handicap_tables__2023_system_title,
                                helpBody = R.string.help_handicap_tables__2023_system_body,
                        ),
                )
        )

        helpListener(
                HelpShowcaseIntent.Add(
                        HelpShowcaseItem(
                                helpTitle = R.string.help_handicap_tables__input_type_title,
                                helpBody = (
                                        if (inputHandicap) R.string.help_handicap_tables__input_type_body_handicap
                                        else R.string.help_handicap_tables__input_type_body_score
                                        ),
                        ),
                )
        )
        helpListener(
                HelpShowcaseIntent.Add(
                        HelpShowcaseItem(
                                helpTitle = R.string.help_handicap_tables__input_title,
                                helpBody = (
                                        if (inputHandicap) R.string.help_handicap_tables__input_body_handicap
                                        else R.string.help_handicap_tables__input_body_score
                                        ),
                        ),
                )
        )
        helpListener(
                HelpShowcaseIntent.Add(
                        HelpShowcaseItem(
                                helpTitle = R.string.help_handicap_tables__table_title,
                                helpBody = R.string.help_handicap_tables__table_body,
                        ),
                )
        )
    }

    @Composable
    private fun Table(
            handicaps: List<HandicapScore>,
            highlighted: HandicapScore?,
            helpListener: (HelpShowcaseIntent) -> Unit,
    ) {
        ProvideTextStyle(value = CodexTypography.NORMAL) {
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
                                                                R.string.help_handicap_tables__table_title
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
                                    .updateHelpDialogPosition(helpListener, R.string.help_handicap_tables__table_title)
                    )
                }
            }
        }
    }

    object TestTag {
        private const val PREFIX = "HANDICAP_TABLES_"

        const val SCREEN = "${PREFIX}SCREEN"
    }

    @Preview
    @Composable
    fun PreviewMainMenuScreen() {
        ComposeContent(
                HandicapTablesState(
                        input = 31,
                        inputHandicap = true,
                        round = HandicapTablesState.RoundInfo.Round(RoundPreviewHelper.outdoorImperialRoundData),
                        roundFilters = SelectRoundEnabledFilters(),
                        subType = 1,
                        use2023Tables = false,
                        allRounds = listOf(RoundPreviewHelper.outdoorImperialRoundData),
                        isSelectRoundDialogOpen = false,
                        isSelectSubtypeDialogOpen = false,
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
}
