package eywa.projectcodex.components.shootDetails.headToHeadEnd.scorePad

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.sharedUi.DataRow
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.components.shootDetails.headToHeadEnd.grid.HeadToHeadAddEndGrid
import eywa.projectcodex.components.shootDetails.headToHeadEnd.grid.HeadToHeadGridRowDataPreviewHelper

@Composable
fun HeadToHeadScorePadScreen() {
    val state = HeadToHeadScorePadState(
            endSize = 3,
            teamSize = 1,
            entries = listOf(
                    HeadToHeadScorePadHeatState(
                            heat = 1,
                            opponent = "Jessica Summers",
                            opponentRank = 1,
                            enteredArrows = listOf(
                                    HeadToHeadGridRowDataPreviewHelper.create(),
                                    HeadToHeadGridRowDataPreviewHelper.create(),
                            ),
                            hasShootOff = false,
                            isShootOffWin = false,
                    ),
                    HeadToHeadScorePadHeatState(
                            heat = 1,
                            opponent = "Jessica Summers",
                            opponentRank = 1,
                            enteredArrows = listOf(
                                    HeadToHeadGridRowDataPreviewHelper.create(),
                                    HeadToHeadGridRowDataPreviewHelper.create(),
                            ),
                            hasShootOff = false,
                            isShootOffWin = false,
                    ),
            ),
    )
    val listener = { _: HeadToHeadScorePadIntent -> }
    val helpListener = { it: HelpShowcaseIntent -> listener(HeadToHeadScorePadIntent.HelpShowcaseAction(it)) }

    Column(
            verticalArrangement = Arrangement.spacedBy(50.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                    .fillMaxSize()
                    .background(CodexTheme.colors.appBackground)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = CodexTheme.dimens.screenPadding)
    ) {
        state.entries.forEach { entry ->
            Column(
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
            ) {
                ProvideTextStyle(CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground)) {
                    DataRow(
                            title = "Heat:",
                            text = "1/32",
                            modifier = Modifier
                                    .align(Alignment.Start)
                                    .padding(horizontal = CodexTheme.dimens.screenPadding)
                    )
                    DataRow(
                            title = "Opponent (rank 1):",
                            text = "Jessica Summers",
                            modifier = Modifier
                                    .align(Alignment.Start)
                                    .padding(horizontal = CodexTheme.dimens.screenPadding)
                    )
                    DataRow(
                            title = "Sighters:",
                            text = "6",
                            modifier = Modifier
                                    .align(Alignment.Start)
                                    .padding(horizontal = CodexTheme.dimens.screenPadding)
                    )
                }
                HeadToHeadAddEndGrid(
                        state = entry.toGridState(state.endSize, state.teamSize),
                        rowClicked = { _, _ -> },
                        helpListener = helpListener,
                        modifier = Modifier.padding(top = 10.dp)
                )
            }
        }
    }
}

enum class HeadToHeadScorePadTestTag : CodexTestTag {
    SCREEN,
    ;

    override val screenName: String
        get() = "HEAD_TO_HEAD_SCORE_PAD"

    override fun getElement(): String = name
}

@Preview
@Composable
fun HeadToHeadScorePadScreen_Preview() {
    CodexTheme {
        HeadToHeadScorePadScreen()
    }
}
