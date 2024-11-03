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
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
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
import eywa.projectcodex.components.shootDetails.addEnd.AddEndTestTag
import eywa.projectcodex.components.shootDetails.addEnd.SightMark
import eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.ArrowInputEditButtons
import eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.arrowButton.ArrowButtonGroup
import eywa.projectcodex.components.shootDetails.headToHeadEnd.grid.HeadToHeadAddEndGrid

@Composable
fun HeadToHeadAddEndScreen() {
    val state = HeadToHeadAddEndState()
    val listener = { _: HeadToHeadAddEndIntent -> }
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
            Column(
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
            ) {
                DataRow(
                        title = "Heat:",
                        text = "1/32",
                        textStyle = CodexTypography.NORMAL_PLUS.copy(color = CodexTheme.colors.onAppBackground),
                        titleStyle = CodexTypography.SMALL_PLUS.copy(color = CodexTheme.colors.onAppBackground),
                        modifier = Modifier.padding(bottom = 5.dp)
                )
                ProvideTextStyle(CodexTypography.SMALL_PLUS.copy(color = CodexTheme.colors.onAppBackground)) {
                    Text(
                            text = "Opponent (rank 1):",
                    )
                    Text(
                            text = "Jessica Summers",
                    )
                }
            }
        }

        Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
        ) {
            DataRow(
                    title = "Sighters:",
                    text = "6",
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
                        title = "Score:",
                        text = "${state.teamScore}-${state.opponentScore}",
                        textStyle = CodexTypography.LARGE.copy(color = CodexTheme.colors.onAppBackground),
                        titleStyle = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground),
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 20.dp)
                )
            }
        }

        Column(
                verticalArrangement = Arrangement.spacedBy(15.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
        ) {
            Text(
                    text = "Set 1: ${state.result.title.get()}",
                    style = CodexTypography.NORMAL_PLUS,
                    color = CodexTheme.colors.onAppBackground,
            )

            HeadToHeadAddEndGrid(
                    state = state.toGridState(),
                    rowClicked = { _, _ -> },
                    helpListener = helpListener,
            )

            Row(
                    horizontalArrangement = Arrangement.spacedBy(15.dp),
            ) {
                CodexChip(
                        text = "Shoot off",
                        selected = state.isShootOff,
                        testTag = HeadToHeadAddEndTestTag.IS_SHOOT_OFF_CHECKBOX,
                        style = CodexTypography.NORMAL,
                ) {
                }
                CodexChip(
                        text = "Win",
                        selected = state.isShootOffWin,
                        testTag = HeadToHeadAddEndTestTag.IS_SHOOT_OFF_CHECKBOX,
                        enabled = state.isShootOff,
                        style = CodexTypography.NORMAL,
                ) {
                }
            }
        }

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

            CodexButton(text = "Next end") {

            }
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
        HeadToHeadAddEndScreen()
    }
}
