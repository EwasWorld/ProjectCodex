package eywa.projectcodex.components.shootDetails.headToHeadEnd.addEnd

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.helpShowcase.asHelpState
import eywa.projectcodex.common.helpShowcase.updateHelpDialogPosition
import eywa.projectcodex.common.sharedUi.CodexButton
import eywa.projectcodex.common.sharedUi.CodexChip
import eywa.projectcodex.common.sharedUi.DataRow
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.codexTheme.asClickableStyle
import eywa.projectcodex.common.sharedUi.testTag
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.components.referenceTables.headToHead.HeadToHeadUseCase
import eywa.projectcodex.components.shootDetails.addEnd.AddEndTestTag
import eywa.projectcodex.components.shootDetails.addEnd.SightMark
import eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.ArrowInputEditButtons
import eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.arrowButton.ArrowButtonGroup
import eywa.projectcodex.components.shootDetails.headToHeadEnd.addHeat.HeadToHeadAddHeatContent
import eywa.projectcodex.components.shootDetails.headToHeadEnd.addHeat.HeadToHeadAddHeatState
import eywa.projectcodex.components.shootDetails.headToHeadEnd.grid.HeadToHeadAddEndGrid
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHeadHeatPreviewHelper

@Composable
fun HeadToHeadAddEndScreen() {
    val state = HeadToHeadAddEndState()
    val listener = { _: HeadToHeadAddEndIntent -> }

    HeadToHeadAddEndScreen(state, listener)

//    LaunchedEffect(state.openAddDialog) {
//        if (state.openAddDialog) {
//            ArcherHandicapsBottomSheetAdd.navigate(navController)
//            viewModel.handle(ArcherHandicapsIntent.AddHandled)
//        }
//    }
}

@Composable
fun HeadToHeadAddEndScreen(
        state: HeadToHeadAddEndState,
        listener: (HeadToHeadAddEndIntent) -> Unit
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(HeadToHeadAddEndIntent.HelpShowcaseAction(it)) }

    if (state.heat != null) {
        EnterEnd(state, listener)
    }
    else {
        Column(
                verticalArrangement = Arrangement.spacedBy(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                        .fillMaxSize()
                        .background(CodexTheme.colors.appBackground)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = CodexTheme.dimens.screenPadding)
        ) {
            SightMark(
                    distance = state.distance,
                    isMetric = state.isMetric,
                    sightMark = state.sightMark,
                    helpListener = helpListener,
                    onExpandClicked = { },
                    onEditClicked = { },
            )

            HeadToHeadAddHeatContent(
                    state = HeadToHeadAddHeatState()
            ) {}
//            CodexButton(text = stringResource(R.string.head_to_head_add_end__add_heat)) {}
        }
    }
}

@Composable
fun EnterEnd(
        state: HeadToHeadAddEndState,
        listener: (HeadToHeadAddEndIntent) -> Unit
) {
    require(state.heat != null)
    val helpListener = { it: HelpShowcaseIntent -> listener(HeadToHeadAddEndIntent.HelpShowcaseAction(it)) }

    Column(
            verticalArrangement = Arrangement.spacedBy(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                    .fillMaxSize()
                    .background(CodexTheme.colors.appBackground)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = CodexTheme.dimens.screenPadding)
    ) {
        Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = CodexTheme.dimens.screenPadding)
        ) {
            SightMark(
                    distance = state.distance,
                    isMetric = state.isMetric,
                    sightMark = state.sightMark,
                    helpListener = helpListener,
                    onExpandClicked = { },
                    onEditClicked = { },
            )
            Text(
                    text = stringResource(R.string.input_end__section_delimiter),
                    style = CodexTypography.NORMAL,
                    color = CodexTheme.colors.onAppBackground,
            )
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
    require(state.heat != null)
    val opponent =
            when {
                state.heat.opponentQualificationRank != null && state.heat.opponent != null -> {
                    stringResource(
                            R.string.head_to_head_add_end__opponent_rank_and_name,
                            state.heat.opponentQualificationRank,
                            state.heat.opponent,
                    )
                }

                state.heat.opponentQualificationRank != null -> {
                    stringResource(
                            R.string.head_to_head_add_end__opponent_rank,
                            state.heat.opponentQualificationRank,
                    )
                }

                state.heat.opponent != null -> {
                    stringResource(
                            R.string.head_to_head_add_end__opponent_name,
                            state.heat.opponent,
                    )
                }

                else -> null
            }

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
    require(state.heat != null)
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
                textStyle = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground)
                        .asClickableStyle(),
                textModifier = Modifier.testTag(AddEndTestTag.SIGHTERS),
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
                            state.teamScore,
                            state.opponentScore,
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
    require(state.heat != null)
    val helpListener = { it: HelpShowcaseIntent -> listener(HeadToHeadAddEndIntent.HelpShowcaseAction(it)) }

    Column(
            verticalArrangement = Arrangement.spacedBy(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
    ) {
        Text(
                text = stringResource(
                        R.string.head_to_head_add_end__set,
                        state.setNumber,
                        state.result.title.get(),
                ),
                style = CodexTypography.NORMAL_PLUS,
                color = CodexTheme.colors.onAppBackground,
        )

        HeadToHeadAddEndGrid(
                state = state.toGridState()!!,
                rowClicked = { _, _ -> },
                helpListener = helpListener,
        )

        Row(
                horizontalArrangement = Arrangement.spacedBy(15.dp),
        ) {
            CodexChip(
                    text = stringResource(R.string.head_to_head_add_end__shoot_off),
                    selected = state.heat.hasShootOff,
                    testTag = HeadToHeadAddEndTestTag.IS_SHOOT_OFF_CHECKBOX,
                    style = CodexTypography.NORMAL,
            ) {
            }
            CodexChip(
                    text = stringResource(R.string.head_to_head_add_end__shoot_off_win),
                    selected = state.heat.isShootOffWin,
                    testTag = HeadToHeadAddEndTestTag.IS_SHOOT_OFF_CHECKBOX,
                    enabled = state.heat.hasShootOff,
                    style = CodexTypography.NORMAL,
            ) {
            }
        }
    }
}

@Composable
private fun Buttons(
        state: HeadToHeadAddEndState,
        listener: (HeadToHeadAddEndIntent) -> Unit,
) {
    require(state.heat != null)
    val helpListener = { it: HelpShowcaseIntent -> listener(HeadToHeadAddEndIntent.HelpShowcaseAction(it)) }

    Column(
            horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ArrowButtonGroup(
                round = state.round,
                roundFace = state.face,
                onClick = { listener(HeadToHeadAddEndIntent.ArrowInputted(it)) },
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

        CodexButton(text = stringResource(R.string.head_to_head_add_end__submit)) {

        }
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
fun HeadToHeadEnd_Preview() {
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
fun NoHeat_HeadToHeadEnd_Preview() {
    CodexTheme {
        HeadToHeadAddEndScreen(
                state = HeadToHeadAddEndState(),
        ) {}
    }
}
