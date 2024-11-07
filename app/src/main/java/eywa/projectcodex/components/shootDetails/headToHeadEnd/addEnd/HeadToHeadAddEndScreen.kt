package eywa.projectcodex.components.shootDetails.headToHeadEnd.addEnd

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import eywa.projectcodex.components.shootDetails.headToHeadEnd.grid.HeadToHeadGrid
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHeadHeatPreviewHelper

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
            listener = { listener(HeadToHeadAddEndIntent.ShootDetailsAction(it)) },
    ) { it, modifier -> HeadToHeadAddEndScreen(it, modifier, listener) }

    HandleMainEffects(
            navController = navController,
            state = state,
            listener = { listener(HeadToHeadAddEndIntent.ShootDetailsAction(it)) },
    )

    val data = state.getData()
    LaunchedEffect(
            data?.extras?.openAllSightMarks,
            data?.extras?.openEditSightMark,
            data?.extras?.openSighters,
            data?.extras?.openAddHeatScreen,
    ) {
        if (data != null) {
            if (data.extras.openAllSightMarks) {
                CodexNavRoute.SIGHT_MARKS.navigate(navController)
                listener(HeadToHeadAddEndIntent.ExpandSightMarkHandled)
            }

            if (data.extras.openEditSightMark) {
                val args = if (data.headToHeadRoundInfo?.sightMark != null) {
                    mapOf(NavArgument.SIGHT_MARK_ID to data.headToHeadRoundInfo.sightMark.id.toString())
                }
                else {
                    val distance = data.headToHeadRoundInfo?.distance ?: DEFAULT_INT_NAV_ARG
                    val isMetric = data.headToHeadRoundInfo?.isMetric ?: true
                    mapOf(NavArgument.DISTANCE to distance.toString(), NavArgument.IS_METRIC to isMetric.toString())
                }
                CodexNavRoute.SIGHT_MARK_DETAIL.navigate(navController, args)
                listener(HeadToHeadAddEndIntent.EditSightMarkHandled)
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
                listener(HeadToHeadAddEndIntent.SightersHandled)
            }

            if (data.extras.openAddHeatScreen) {
                CodexNavRoute.HEAD_TO_HEAD_ADD_HEAT.navigate(
                        navController,
                        mapOf(NavArgument.SHOOT_ID to viewModel.shootId.toString()),
                        popCurrentRoute = true,
                )
                listener(HeadToHeadAddEndIntent.OpenAddHeatScreenHandled)
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
    val helpListener = { it: HelpShowcaseIntent -> listener(HeadToHeadAddEndIntent.HelpShowcaseAction(it)) }

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
            if (state.headToHeadRoundInfo != null) {
                SightMark(
                        distance = state.headToHeadRoundInfo.distance,
                        isMetric = state.headToHeadRoundInfo.isMetric,
                        sightMark = state.headToHeadRoundInfo.sightMark,
                        helpListener = helpListener,
                        onExpandClicked = { listener(HeadToHeadAddEndIntent.ExpandSightMarkClicked) },
                        onEditClicked = { listener(HeadToHeadAddEndIntent.EditSightMarkClicked) },
                )
                Text(
                        text = stringResource(R.string.input_end__section_delimiter),
                        style = CodexTypography.NORMAL,
                        color = CodexTheme.colors.onAppBackground,
                )
            }
            HeatFixedInfo(state, listener)
        }

        HeatTransitiveInfo(state, listener)
        SetInfo(state, listener)
        Buttons(state, listener)
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
private fun HeatTransitiveInfo(
        state: HeadToHeadAddEndState,
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
        state: HeadToHeadAddEndState,
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
        state: HeadToHeadAddEndState,
        listener: (HeadToHeadAddEndIntent) -> Unit,
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(HeadToHeadAddEndIntent.HelpShowcaseAction(it)) }

    Column(
            horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ArrowButtonGroup(
                round = state.headToHeadRoundInfo?.round,
                roundFace = state.headToHeadRoundInfo?.face,
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
fun HeadToHeadAddScreen_Preview() {
    CodexTheme {
        HeadToHeadAddEndScreen(
                state = HeadToHeadAddEndState(
                        heat = DatabaseHeadToHeadHeatPreviewHelper.data,
                ),
        ) {}
    }
}
