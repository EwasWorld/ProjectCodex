package eywa.projectcodex.components.shootDetails.headToHeadEnd.addEnd

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
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
import eywa.projectcodex.common.sharedUi.CodexButton
import eywa.projectcodex.common.sharedUi.CodexChip
import eywa.projectcodex.common.sharedUi.DataRow
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.testTag
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.components.referenceTables.headToHead.HeadToHeadUseCase
import eywa.projectcodex.components.shootDetails.addEnd.AddEndTestTag
import eywa.projectcodex.components.shootDetails.addEnd.SightMark
import eywa.projectcodex.components.shootDetails.commonUi.HandleMainEffects
import eywa.projectcodex.components.shootDetails.commonUi.ShootDetailsMainScreen
import eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.ArrowInputEditButtons
import eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.ArrowInputsIntent.ArrowInputted
import eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.arrowButton.ArrowButtonGroup
import eywa.projectcodex.components.shootDetails.getData
import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadResult
import eywa.projectcodex.components.shootDetails.headToHeadEnd.grid.HeadToHeadGrid
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHeadHeatPreviewHelper

@Composable
fun HeadToHeadAddScreen(
        navController: NavController,
        viewModel: HeadToHeadAddViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val listener = { it: HeadToHeadAddIntent -> viewModel.handle(it) }

    ShootDetailsMainScreen(
            currentScreen = CodexNavRoute.HEAD_TO_HEAD_ADD,
            state = state,
            listener = { listener(HeadToHeadAddIntent.ShootDetailsAction(it)) },
    ) { it, modifier -> HeadToHeadAddScreen(it, modifier, listener) }

    HandleMainEffects(
            navController = navController,
            state = state,
            listener = { listener(HeadToHeadAddIntent.ShootDetailsAction(it)) },
    )

    val addEndState = state.getData() as? HeadToHeadAddState.AddEnd
    LaunchedEffect(addEndState?.extras?.effects, addEndState?.extras?.openSighters) {
        if (addEndState != null) {
            if (addEndState.extras.effects.openAllSightMarks) {
                CodexNavRoute.SIGHT_MARKS.navigate(navController)
                listener(HeadToHeadAddIntent.ExpandSightMarkHandled)
            }

            if (addEndState.extras.effects.openEditSightMark) {
                val args = if (addEndState.roundCommon?.sightMark != null) {
                    mapOf(NavArgument.SIGHT_MARK_ID to addEndState.roundCommon.sightMark.id.toString())
                }
                else {
                    val distance = addEndState.roundCommon?.distance ?: DEFAULT_INT_NAV_ARG
                    val isMetric = addEndState.roundCommon?.isMetric ?: true
                    mapOf(NavArgument.DISTANCE to distance.toString(), NavArgument.IS_METRIC to isMetric.toString())
                }
                CodexNavRoute.SIGHT_MARK_DETAIL.navigate(navController, args)
                listener(HeadToHeadAddIntent.EditSightMarkHandled)
            }

            if (addEndState.extras.openSighters) {
                CodexNavRoute.SHOOT_DETAILS_ADD_COUNT.navigate(
                        navController,
                        mapOf(
                                NavArgument.SHOOT_ID to addEndState.heat.shootId.toString(),
                                NavArgument.HEAT_ID to addEndState.heat.heat.toString(),
                                NavArgument.IS_SIGHTERS to true.toString(),
                        ),
                )
                listener(HeadToHeadAddIntent.AddEndAction(HeadToHeadAddEndIntent.SightersHandled))
            }
        }
    }

    val addHeatState = state.getData() as? HeadToHeadAddState.AddHeat
    LaunchedEffect(addHeatState?.extras?.effects) {
        if (addHeatState != null) {
            if (addHeatState.extras.effects.openAllSightMarks) {
                CodexNavRoute.SIGHT_MARKS.navigate(navController)
                listener(HeadToHeadAddIntent.ExpandSightMarkHandled)
            }

            if (addHeatState.extras.effects.openEditSightMark) {
                val args = if (addHeatState.roundCommon?.sightMark != null) {
                    mapOf(NavArgument.SIGHT_MARK_ID to addHeatState.roundCommon.sightMark.id.toString())
                }
                else {
                    val distance = addHeatState.roundCommon?.distance ?: DEFAULT_INT_NAV_ARG
                    val isMetric = addHeatState.roundCommon?.isMetric ?: true
                    mapOf(NavArgument.DISTANCE to distance.toString(), NavArgument.IS_METRIC to isMetric.toString())
                }
                CodexNavRoute.SIGHT_MARK_DETAIL.navigate(navController, args)
                listener(HeadToHeadAddIntent.EditSightMarkHandled)
            }
        }
    }
}

@Composable
fun HeadToHeadAddScreen(
        state: HeadToHeadAddState,
        modifier: Modifier = Modifier,
        listener: (HeadToHeadAddIntent) -> Unit,
) {
    when (state) {
        is HeadToHeadAddState.AddEnd -> AddEnd(state, modifier, listener)
        is HeadToHeadAddState.AddHeat -> AddHeat(state, modifier, listener)
    }
}

@Composable
fun AddHeat(
        state: HeadToHeadAddState.AddHeat,
        modifier: Modifier = Modifier,
        listener: (HeadToHeadAddIntent) -> Unit,
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(HeadToHeadAddIntent.HelpShowcaseAction(it)) }

    Column(
            verticalArrangement = Arrangement.spacedBy(40.dp, alignment = Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                    .background(CodexTheme.colors.appBackground)
                    .padding(vertical = CodexTheme.dimens.screenPadding)
    ) {
        if (state.roundCommon != null) {
            SightMark(
                    distance = state.roundCommon.distance,
                    isMetric = state.roundCommon.isMetric,
                    sightMark = state.roundCommon.sightMark,
                    helpListener = helpListener,
                    onExpandClicked = { listener(HeadToHeadAddIntent.ExpandSightMarkClicked) },
                    onEditClicked = { listener(HeadToHeadAddIntent.EditSightMarkClicked) },
            )
        }

        if (state.previousHeat != null) {
            Surface(
                    border = BorderStroke(1.dp, CodexTheme.colors.listItemOnAppBackground),
                    color = CodexTheme.colors.appBackground,
                    modifier = Modifier.padding(horizontal = CodexTheme.dimens.screenPadding)
            ) {
                ProvideTextStyle(CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground)) {
                    Column(
                            verticalArrangement = Arrangement.spacedBy(5.dp, alignment = Alignment.CenterVertically),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 20.dp, horizontal = 25.dp)
                    ) {
                        DataRow(
                                title = stringResource(R.string.head_to_head_add_heat__heat),
                                text = HeadToHeadUseCase.shortRoundName(state.previousHeat.heat).get(),
                        )
                        DataRow(
                                title = stringResource(
                                        R.string.head_to_head_add_end__score_text,
                                        state.previousHeat.teamRunningTotal,
                                        state.previousHeat.opponentRunningTotal,
                                ),
                                text = state.previousHeat.result.title.get(),
                        )
                    }
                }
            }
        }

        Surface(
                shape = RoundedCornerShape(CodexTheme.dimens.cornerRounding),
                border = BorderStroke(1.dp, CodexTheme.colors.listItemOnAppBackground),
                color = CodexTheme.colors.appBackground,
                modifier = Modifier.padding(horizontal = CodexTheme.dimens.screenPadding)
        ) {
            HeadToHeadAddHeatContent(
                    state = state,
                    listener = { listener(HeadToHeadAddIntent.AddHeatAction(it)) },
                    modifier = Modifier.padding(vertical = 20.dp, horizontal = 25.dp)
            )
        }
    }
}

@Composable
fun AddEnd(
        state: HeadToHeadAddState.AddEnd,
        modifier: Modifier = Modifier,
        listener: (HeadToHeadAddIntent) -> Unit,
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(HeadToHeadAddIntent.HelpShowcaseAction(it)) }
    val addEndListener = { it: HeadToHeadAddEndIntent -> listener(HeadToHeadAddIntent.AddEndAction(it)) }

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
            if (state.roundCommon != null) {
                SightMark(
                        distance = state.roundCommon.distance,
                        isMetric = state.roundCommon.isMetric,
                        sightMark = state.roundCommon.sightMark,
                        helpListener = helpListener,
                        onExpandClicked = { listener(HeadToHeadAddIntent.ExpandSightMarkClicked) },
                        onEditClicked = { listener(HeadToHeadAddIntent.EditSightMarkClicked) },
                )
                Text(
                        text = stringResource(R.string.input_end__section_delimiter),
                        style = CodexTypography.NORMAL,
                        color = CodexTheme.colors.onAppBackground,
                )
            }
            HeatFixedInfo(state, addEndListener)
        }

        HeatTransitiveInfo(state, addEndListener)
        SetInfo(state, addEndListener)
        Buttons(state, addEndListener)
    }
}

@Composable
private fun HeatFixedInfo(
        state: HeadToHeadAddState.AddEnd,
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
private fun HeatTransitiveInfo(
        state: HeadToHeadAddState.AddEnd,
        listener: (HeadToHeadAddEndIntent) -> Unit,
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(HeadToHeadAddEndIntent.HelpShowcaseAction(it)) }

    Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
    ) {
        DataRow(
                title = stringResource(R.string.add_count__sighters),
                text = state.heat.sightersCount.toString(),
                titleStyle = CodexTypography.SMALL_PLUS.copy(color = CodexTheme.colors.onAppBackground),
                textStyle = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground),
                textModifier = Modifier.testTag(AddEndTestTag.SIGHTERS),
                onClick = { listener(HeadToHeadAddEndIntent.SightersClicked) },
                modifier = Modifier
                        .updateHelpDialogPosition(
                                HelpShowcaseItem(
                                        helpTitle = stringResource(R.string.help_input_end__sighters_title),
                                        helpBody = stringResource(R.string.help_input_end__sighters_body),
                                ).asHelpState(helpListener),
                        )
        )

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
private fun SetInfo(
        state: HeadToHeadAddState.AddEnd,
        listener: (HeadToHeadAddEndIntent) -> Unit,
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(HeadToHeadAddEndIntent.HelpShowcaseAction(it)) }

    val setText =
            if (state.extras.set.isShootOff) {
                stringResource(
                        R.string.head_to_head_add_end__shoot_off,
                        state.extras.set.result.title.get(),
                )
            }
            else {
                stringResource(
                        R.string.head_to_head_add_end__set,
                        state.extras.set.setNumber,
                        state.extras.set.result.title.get(),
                )
            }

    Column(
            verticalArrangement = Arrangement.spacedBy(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
    ) {
        Text(
                text = setText,
                style = CodexTypography.NORMAL_PLUS,
                color = CodexTheme.colors.onAppBackground,
        )

        HeadToHeadGrid(
                state = state.toGridState(),
                rowClicked = { _, row -> listener(HeadToHeadAddEndIntent.GridRowClicked(row)) },
                onTextValueChanged = { type, text ->
                    listener(
                            HeadToHeadAddEndIntent.GridTextValueChanged(
                                    type,
                                    text
                            )
                    )
                },
                helpListener = helpListener,
        )

        if (state.extras.set.isShootOff) {
            CodexChip(
                    text = stringResource(R.string.head_to_head_add_end__shoot_off_win),
                    selected = state.heat.isShootOffWin,
                    testTag = HeadToHeadAddTestTag.IS_SHOOT_OFF_CHECKBOX,
                    enabled = state.extras.set.isShootOff,
                    style = CodexTypography.NORMAL,
                    onToggle = { listener(HeadToHeadAddEndIntent.ToggleShootOffWin) },
            )
        }
    }
}

@Composable
private fun Buttons(
        state: HeadToHeadAddState.AddEnd,
        listener: (HeadToHeadAddEndIntent) -> Unit,
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(HeadToHeadAddEndIntent.HelpShowcaseAction(it)) }

    Column(
            horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ArrowButtonGroup(
                round = state.roundCommon?.round,
                roundFace = state.roundCommon?.face,
                onClick = { listener(HeadToHeadAddEndIntent.ArrowInputAction(ArrowInputted(it))) },
                modifier = Modifier.updateHelpDialogPosition(
                        HelpShowcaseItem(
                                helpTitle = stringResource(R.string.help_input_end__arrow_inputs_title),
                                helpBody = stringResource(R.string.help_input_end__arrow_inputs_body),
                        ).asHelpState(helpListener),
                )
        )

        ArrowInputEditButtons(showResetButton = false, helpListener = helpListener) {
            listener(HeadToHeadAddEndIntent.ArrowInputAction(it))
        }

        CodexButton(
                text = stringResource(R.string.head_to_head_add_end__submit),
                onClick = { listener(HeadToHeadAddEndIntent.SubmitClicked) },
        )
    }
}

enum class HeadToHeadAddTestTag : CodexTestTag {
    SCREEN,
    IS_SHOOT_OFF_CHECKBOX,
    ;

    override val screenName: String
        get() = "HEAD_TO_HEAD_ADD_END"

    override fun getElement(): String = name
}

@Preview
@Composable
fun End_HeadToHeadAddScreen_Preview() {
    CodexTheme {
        HeadToHeadAddScreen(
                state = HeadToHeadAddState.AddEnd(
                        heat = DatabaseHeadToHeadHeatPreviewHelper.data,
                ),
        ) {}
    }
}

@Preview
@Composable
fun Heat_HeadToHeadAddScreen_Preview() {
    CodexTheme {
        HeadToHeadAddScreen(
                state = HeadToHeadAddState.AddHeat(
                        previousHeat = HeadToHeadAddState.AddHeat.PreviousHeat(
                                heat = 0,
                                result = HeadToHeadResult.WIN,
                                teamRunningTotal = 6,
                                opponentRunningTotal = 0,
                        ),
                ),
        ) {}
    }
}
