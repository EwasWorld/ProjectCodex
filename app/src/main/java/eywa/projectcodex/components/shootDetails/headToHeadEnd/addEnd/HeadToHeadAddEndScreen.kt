package eywa.projectcodex.components.shootDetails.headToHeadEnd.addEnd

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.helpShowcase.asHelpState
import eywa.projectcodex.common.helpShowcase.updateHelpDialogPosition
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.navigation.DEFAULT_INT_NAV_ARG
import eywa.projectcodex.common.navigation.NavArgument
import eywa.projectcodex.common.sharedUi.ButtonState
import eywa.projectcodex.common.sharedUi.CodexButton
import eywa.projectcodex.common.sharedUi.CodexChip
import eywa.projectcodex.common.sharedUi.DataRow
import eywa.projectcodex.common.sharedUi.SimpleDialog
import eywa.projectcodex.common.sharedUi.SimpleDialogContent
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.codexTheme.asClickableStyle
import eywa.projectcodex.common.sharedUi.previewHelpers.HeadToHeadSetPreviewHelperDsl
import eywa.projectcodex.common.sharedUi.testTag
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.common.utils.ToastSpamPrevention
import eywa.projectcodex.components.referenceTables.headToHead.HeadToHeadUseCase
import eywa.projectcodex.components.shootDetails.addEnd.AddEndTestTag
import eywa.projectcodex.components.shootDetails.addEnd.SightMark
import eywa.projectcodex.components.shootDetails.commonUi.HandleMainEffects
import eywa.projectcodex.components.shootDetails.commonUi.ShootDetailsMainScreen
import eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.ArrowInputEditButtons
import eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.ArrowInputsIntent.ArrowInputted
import eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.arrowButton.ArrowButtonGroup
import eywa.projectcodex.components.shootDetails.getData
import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadArcherType
import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadResult
import eywa.projectcodex.components.shootDetails.headToHeadEnd.addEnd.HeadToHeadAddEndIntent.*
import eywa.projectcodex.components.shootDetails.headToHeadEnd.grid.HeadToHeadGrid
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHeadHeatPreviewHelper
import eywa.projectcodex.model.headToHead.FullHeadToHeadSet

@Composable
fun HeadToHeadAddEndScreen(
        navController: NavController,
        viewModel: HeadToHeadAddEndViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val listener = { it: HeadToHeadAddEndIntent -> viewModel.handle(it) }

    ShootDetailsMainScreen(
            currentScreen = CodexNavRoute.HEAD_TO_HEAD_ADD_END,
            state = state,
            listener = { listener(ShootDetailsAction(it)) },
    ) { it, modifier -> HeadToHeadAddEndScreen(it, modifier, listener) }

    HandleMainEffects(
            navController = navController,
            state = state,
            listener = { listener(ShootDetailsAction(it)) },
    )

    val context = LocalContext.current
    val data = state.getData()
    LaunchedEffect(
            data?.extras?.openAllSightMarks,
            data?.extras?.openEditSightMark,
            data?.extras?.openSighters,
            data?.extras?.openAddHeatScreen,
            data?.extras?.arrowInputsError,
            data?.extras?.openCreateNextMatch,
    ) {
        if (data != null) {
            data.extras.arrowInputsError.forEach {
                ToastSpamPrevention.displayToast(context, context.resources.getString(it.messageId))
                listener(ArrowInputsErrorHandled(it))
            }

            if (data.extras.openAllSightMarks) {
                CodexNavRoute.SIGHT_MARKS.navigate(navController)
                listener(ExpandSightMarkHandled)
            }

            if (data.extras.openEditSightMark) {
                val args = if (data.roundInfo?.sightMark != null) {
                    mapOf(NavArgument.SIGHT_MARK_ID to data.roundInfo.sightMark.id.toString())
                }
                else {
                    val distance = data.roundInfo?.distance ?: DEFAULT_INT_NAV_ARG
                    val isMetric = data.roundInfo?.isMetric ?: true
                    mapOf(NavArgument.DISTANCE to distance.toString(), NavArgument.IS_METRIC to isMetric.toString())
                }
                CodexNavRoute.SIGHT_MARK_DETAIL.navigate(navController, args)
                listener(EditSightMarkHandled)
            }

            if (data.extras.openSighters) {
                CodexNavRoute.SHOOT_DETAILS_ADD_COUNT.navigate(
                        navController,
                        mapOf(
                                NavArgument.SHOOT_ID to data.heat.shootId.toString(),
                                NavArgument.HEAT_ID to data.heat.heat.toString(),
                                NavArgument.IS_SIGHTERS to true.toString(),
                        ),
                )
                listener(SightersHandled)
            }

            if (data.extras.openAddHeatScreen) {
                CodexNavRoute.HEAD_TO_HEAD_ADD_HEAT.navigate(
                        navController,
                        mapOf(NavArgument.SHOOT_ID to viewModel.shootId.toString()),
                        popCurrentRoute = true,
                )
                listener(OpenAddHeatScreenHandled)
            }

            if (data.extras.openCreateNextMatch) {
                CodexNavRoute.HEAD_TO_HEAD_ADD_HEAT.navigate(
                        navController,
                        mapOf(
                                NavArgument.SHOOT_ID to viewModel.shootId.toString(),
                                NavArgument.HEAT_ID to (data.heat.heat - 1).toString(),
                        ),
                        popCurrentRoute = true,
                )
                listener(OpenAddHeatScreenHandled)
            }
        }
    }
}

@Composable
fun HeadToHeadAddEndScreen(
        state: HeadToHeadAddEndState,
        modifier: Modifier = Modifier,
        listener: (HeadToHeadAddEndIntent) -> Unit,
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(HelpShowcaseAction(it)) }

    Column(
            verticalArrangement = Arrangement.spacedBy(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                    .background(CodexTheme.colors.appBackground)
                    .padding(vertical = CodexTheme.dimens.screenPadding)
    ) {
        Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = CodexTheme.dimens.screenPadding)
        ) {
            if (state.roundInfo != null) {
                SightMark(
                        distance = state.roundInfo.distance,
                        isMetric = state.roundInfo.isMetric,
                        sightMark = state.roundInfo.sightMark,
                        helpListener = helpListener,
                        onExpandClicked = { listener(ExpandSightMarkClicked) },
                        onEditClicked = { listener(EditSightMarkClicked) },
                )
                Text(
                        text = stringResource(R.string.input_end__section_delimiter),
                        style = CodexTypography.NORMAL,
                        color = CodexTheme.colors.onAppBackground,
                )
            }
            HeatFixedInfo(state, listener)
        }

        if (state.heat.isBye) {
            Sighters(state, listener)
            Text(
                    text = stringResource(R.string.head_to_head_score_pad__is_bye),
                    style = CodexTypography.NORMAL_PLUS,
                    color = CodexTheme.colors.onAppBackground,
            )
            CodexButton(
                    text = stringResource(R.string.head_to_head_add_end__next_match),
                    onClick = { listener(CreateNextMatchClicked) },
            )
        }
        else {
            HeatTransitiveInfo(state, listener)
            SetInfo(state, listener)
            Buttons(state, listener)
        }
    }
}

@Composable
private fun HeatFixedInfo(
        state: HeadToHeadAddEndState,
        listener: (HeadToHeadAddEndIntent) -> Unit,
) {
    val opponent = state.heat.opponentString(true)?.get()

    Column(
            verticalArrangement = Arrangement.spacedBy(0.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
    ) {
        DataRow(
                title = stringResource(R.string.head_to_head_add_heat__heat),
                text = HeadToHeadUseCase.shortRoundName(state.heat.heat).get(),
                textStyle = CodexTypography.NORMAL_PLUS.copy(color = CodexTheme.colors.onAppBackground),
                titleStyle = CodexTypography.SMALL_PLUS.copy(color = CodexTheme.colors.onAppBackground),
                modifier = Modifier.padding(bottom = 5.dp)
        )

        if (opponent != null) {
            Text(
                    text = opponent,
                    style = CodexTypography.SMALL_PLUS,
                    color = CodexTheme.colors.onAppBackground,
                    textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun Sighters(
        state: HeadToHeadAddEndState,
        listener: (HeadToHeadAddEndIntent) -> Unit,
) {
    DataRow(
            title = stringResource(R.string.add_count__sighters),
            text = state.heat.sightersCount.toString(),
            titleStyle = CodexTypography.SMALL_PLUS.copy(color = CodexTheme.colors.onAppBackground),
            textStyle = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground),
            textModifier = Modifier.testTag(AddEndTestTag.SIGHTERS),
            onClick = { listener(SightersClicked) },
            modifier = Modifier
                    .updateHelpDialogPosition(
                            HelpShowcaseItem(
                                    helpTitle = stringResource(R.string.help_input_end__sighters_title),
                                    helpBody = stringResource(R.string.help_input_end__sighters_body),
                            ).asHelpState { listener(HelpShowcaseAction(it)) },
                    )
    )
}

@Composable
private fun HeatTransitiveInfo(
        state: HeadToHeadAddEndState,
        listener: (HeadToHeadAddEndIntent) -> Unit,
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(HelpShowcaseAction(it)) }

    Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
    ) {
        Sighters(state, listener)

        Surface(
                color = Color.Transparent,
                border = BorderStroke(1.dp, CodexTheme.colors.onAppBackground),
                modifier = Modifier.padding(horizontal = CodexTheme.dimens.screenPadding)
        ) {
            DataRow(
                    title = stringResource(R.string.head_to_head_add_end__score),
                    text = stringResource(
                            R.string.head_to_head_add_end__score_text,
                            state.teamRunningTotal,
                            state.opponentRunningTotal,
                    ),
                    textStyle = CodexTypography.LARGE.copy(color = CodexTheme.colors.onAppBackground),
                    titleStyle = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground),
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 20.dp)
            )
        }
    }
}

@Composable
private fun EditRowTypesDialog(
        state: HeadToHeadAddEndState,
        listener: (HeadToHeadAddEndIntent) -> Unit,
) {
    val dialogState = state.extras.selectRowTypesDialogState
    SimpleDialog(
            isShown = dialogState != null,
            onDismissListener = { listener(CloseEditTypesDialog) },
    ) {
        SimpleDialogContent(
                title = stringResource(R.string.head_to_head_add_end__type_dialog_title),
                message = stringResource(R.string.head_to_head_add_end__type_dialog_message),
                negativeButton = ButtonState(
                        text = stringResource(R.string.general_cancel),
                        onClick = { listener(CloseEditTypesDialog) },
                ),
        ) {
            dialogState ?: return@SimpleDialogContent
            Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
            ) {
                val isTeam = state.extras.set.teamSize > 1
                HeadToHeadArcherType.entries.forEach {
                    if (it.showForSelectorDialog(isTeam)) {
                        val enabled = it.enabledOnSelectorDialog(isTeam, dialogState.keys.toList())
                        val text =
                                if (!enabled) {
                                    stringResource(R.string.head_to_head_add_end__type_dialog_unavailable)
                                }
                                else {
                                    when (dialogState[it]) {
                                        true -> stringResource(R.string.head_to_head_add_end__type_dialog_total)
                                        false -> stringResource(R.string.head_to_head_add_end__type_dialog_arrows)
                                        null -> stringResource(R.string.head_to_head_add_end__type_dialog_off)
                                    }
                                }
                        val textClickableStyle =
                                if (enabled) LocalTextStyle.current.asClickableStyle()
                                else LocalTextStyle.current.asClickableStyle()
                                        .copy(color = CodexTheme.colors.disabledButton)
                        DataRow(
                                title = stringResource(
                                        R.string.head_to_head_add_end__type_dialog_row_title,
                                        it.selectorText.get(),
                                ),
                                text = text,
                                onClick = { listener(EditTypesItemClicked(it)) }.takeIf { enabled },
                                textClickableStyle = textClickableStyle,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.SetInfo(
        state: HeadToHeadAddEndState,
        listener: (HeadToHeadAddEndIntent) -> Unit,
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(HelpShowcaseAction(it)) }
    val result = state.extras.set.result

    val setText =
            if (state.extras.set.isShootOff) {
                stringResource(
                        R.string.head_to_head_add_end__shoot_off,
                        result.title.get(),
                )
            }
            else {
                stringResource(
                        R.string.head_to_head_add_end__set,
                        state.extras.set.setNumber,
                        result.title.get(),
                )
            }

    EditRowTypesDialog(state, listener)

    Column(
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
                text = setText,
                style = CodexTypography.NORMAL_PLUS,
                color = CodexTheme.colors.onAppBackground,
        )
        if (result == HeadToHeadResult.UNKNOWN) {
            Text(
                    text = stringResource(
                            R.string.head_to_head_add_end__unknown_result_warning,
                            state.extras.set.requiredRowsString.get(),
                    ),
                    style = CodexTypography.SMALL_PLUS,
                    color = CodexTheme.colors.onAppBackground,
                    textAlign = TextAlign.Center,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier.padding(bottom = 10.dp)
            )
        }

        HeadToHeadGrid(
                state = state.toGridState(),
                errorOnIncompleteRows = state.extras.incompleteError,
                rowClicked = { _, row -> listener(GridRowClicked(row)) },
                onTextValueChanged = { type, text -> listener(GridTextValueChanged(type, text)) },
                editTypesClicked = { listener(EditTypesClicked) },
                helpListener = helpListener,
                modifier = Modifier.padding(vertical = 10.dp)
        )

        if (state.extras.set.isShootOff) {
            CodexChip(
                    text = stringResource(R.string.head_to_head_add_end__shoot_off_win),
                    selected = state.heat.isShootOffWin,
                    testTag = HeadToHeadAddEndTestTag.IS_SHOOT_OFF_CHECKBOX,
                    enabled = state.extras.set.isShootOff,
                    style = CodexTypography.NORMAL,
                    onToggle = { listener(ToggleShootOffWin) },
            )
        }
    }
}

@Composable
private fun Buttons(
        state: HeadToHeadAddEndState,
        listener: (HeadToHeadAddEndIntent) -> Unit,
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(HelpShowcaseAction(it)) }

    Column(
            horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (state.extras.selectedData?.isTotalRow == false) {
            ArrowButtonGroup(
                    round = state.roundInfo?.round,
                    roundFace = state.roundInfo?.face,
                    onClick = { listener(ArrowInputAction(ArrowInputted(it))) },
                    modifier = Modifier.updateHelpDialogPosition(
                            HelpShowcaseItem(
                                    helpTitle = stringResource(R.string.help_input_end__arrow_inputs_title),
                                    helpBody = stringResource(R.string.help_input_end__arrow_inputs_body),
                            ).asHelpState(helpListener),
                    )
            )

            ArrowInputEditButtons(showResetButton = false, helpListener = helpListener) {
                listener(ArrowInputAction(it))
            }
        }

        if (state.extras.set.result == HeadToHeadResult.UNKNOWN && state.extras.set.setNumber >= 3) {
            CodexButton(
                    text = stringResource(R.string.head_to_head_add_end__next_match),
                    onClick = { listener(CreateNextMatchClicked) },
            )
        }
        CodexButton(
                text = stringResource(R.string.head_to_head_add_end__submit),
                onClick = { listener(SubmitClicked) },
        )
    }
}

enum class HeadToHeadAddEndTestTag : CodexTestTag {
    SCREEN,
    IS_SHOOT_OFF_CHECKBOX,
    ;

    override val screenName: String
        get() = "HEAD_TO_HEAD_ADD_END"

    override fun getElement(): String = name
}

@Preview
@Composable
fun HeadToHeadAddScreen_Preview() {
    CodexTheme {
        HeadToHeadAddEndScreen(
                state = HeadToHeadAddEndState(
                        heat = DatabaseHeadToHeadHeatPreviewHelper.data,
                ),
        ) {}
    }
}

@Preview
@Composable
fun EditRowTypes_HeadToHeadAddScreen_Preview() {
    CodexTheme {
        HeadToHeadAddEndScreen(
                state = HeadToHeadAddEndState(
                        heat = DatabaseHeadToHeadHeatPreviewHelper.data,
                        extras = HeadToHeadAddEndExtras(
                                set = FullHeadToHeadSet(
                                        setNumber = 1,
                                        data = listOf(),
                                        teamSize = 2,
                                        isShootOffWin = false,
                                        isRecurveStyle = false,
                                ),
                                selectRowTypesDialogState = mapOf(
                                        HeadToHeadArcherType.SELF to false,
                                        HeadToHeadArcherType.TEAM_MATE to true,
                                        HeadToHeadArcherType.OPPONENT to true,
                                ),
                        ),
                ),
        ) {}
    }
}

@Preview
@Composable
fun Unknown_HeadToHeadAddScreen_Preview() {
    CodexTheme {
        HeadToHeadAddEndScreen(
                state = HeadToHeadAddEndState(
                        heat = DatabaseHeadToHeadHeatPreviewHelper.data,
                        extras = HeadToHeadAddEndExtras(
                                set = HeadToHeadSetPreviewHelperDsl(
                                        setNumber = 3,
                                        teamSize = 1,
                                        isShootOffWin = false,
                                        isRecurveStyle = true,
                                ).apply {
                                    addRows(HeadToHeadResult.WIN)
                                    removeRow(HeadToHeadArcherType.OPPONENT)
                                }.asFull(),
                        ),
                ),
        ) {}
    }
}

@Preview
@Composable
fun ByeHeadToHeadAddScreen_Preview() {
    CodexTheme {
        HeadToHeadAddEndScreen(
                state = HeadToHeadAddEndState(
                        heat = DatabaseHeadToHeadHeatPreviewHelper.data.copy(isBye = true),
                ),
        ) {}
    }
}
