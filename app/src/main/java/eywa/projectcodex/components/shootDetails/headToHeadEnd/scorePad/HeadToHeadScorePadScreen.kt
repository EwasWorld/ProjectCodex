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
import eywa.projectcodex.common.sharedUi.DataRow
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.components.referenceTables.headToHead.HeadToHeadUseCase
import eywa.projectcodex.components.shootDetails.headToHeadEnd.grid.HeadToHeadGrid
import eywa.projectcodex.components.shootDetails.headToHeadEnd.grid.HeadToHeadGridRowDataPreviewHelper
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHeadHeat
import eywa.projectcodex.model.FullHeadToHeadHeat
import eywa.projectcodex.model.FullHeadToHeadSet

@Composable
fun HeadToHeadScorePadScreen(
        viewModel: HeadToHeadScorePadViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val listener = { it: HeadToHeadScorePadIntent -> viewModel.handle(it) }

    HeadToHeadScorePadScreen(state, listener)
}

@Composable
fun HeadToHeadScorePadScreen(
        state: HeadToHeadScorePadState,
        listener: (HeadToHeadScorePadIntent) -> Unit,
) {
    when (state) {
        is HeadToHeadScorePadState.Loaded -> {
            if (state.entries.isNotEmpty()) {
                Loaded(state, listener)
            }
            else {
                Column(
                        verticalArrangement = Arrangement.spacedBy(40.dp, alignment = Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                                .fillMaxSize()
                                .background(CodexTheme.colors.appBackground)
                                .verticalScroll(rememberScrollState())
                                .padding(vertical = CodexTheme.dimens.screenPadding)
                ) {
                    Text(text = "Empty")
                }
            }
        }

        else -> {
            Column(
                    verticalArrangement = Arrangement.spacedBy(40.dp, alignment = Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                            .fillMaxSize()
                            .background(CodexTheme.colors.appBackground)
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = CodexTheme.dimens.screenPadding)
            ) {
                Text(text = state::class.java.simpleName)
            }
        }
    }
}

@Composable
private fun Loaded(
        state: HeadToHeadScorePadState.Loaded,
        listener: (HeadToHeadScorePadIntent) -> Unit,
) {
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
                            title = stringResource(R.string.head_to_head_add_heat__heat),
                            text = HeadToHeadUseCase.roundName(entry.heat.heat).get(),
                            modifier = Modifier
                                    .align(Alignment.Start)
                                    .padding(horizontal = CodexTheme.dimens.screenPadding)
                    )

                    entry.heat.opponentString()?.get()?.let {
                        if (!entry.heat.opponent.isNullOrBlank()) {
                            Text(
                                    text = it,
                                    modifier = Modifier
                                            .align(Alignment.Start)
                                            .padding(horizontal = CodexTheme.dimens.screenPadding)
                            )
                        }
                    }
                    DataRow(
                            title = stringResource(R.string.add_count__sighters),
                            text = entry.heat.sightersCount.toString(),
                            modifier = Modifier
                                    .align(Alignment.Start)
                                    .padding(horizontal = CodexTheme.dimens.screenPadding)
                    )
                }
                HeadToHeadGrid(
                        state = entry.toGridState(),
                        rowClicked = { _, _ -> },
                        onTextValueChanged = { _, _ -> },
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
        HeadToHeadScorePadScreen(
                HeadToHeadScorePadState.Loaded(
                        entries = listOf(
                                FullHeadToHeadHeat(
                                        heat = DatabaseHeadToHeadHeat(
                                                heat = 1,
                                                opponent = "Jessica Summers",
                                                opponentQualificationRank = 1,
                                                shootId = 1,
                                                sightersCount = 0,
                                                isBye = false,
                                                isShootOffWin = false,
                                        ),
                                        isRecurveMatch = true,
                                        teamSize = 1,
                                        sets = listOf(
                                                FullHeadToHeadSet(
                                                        data = HeadToHeadGridRowDataPreviewHelper.create(),
                                                        teamSize = 1,
                                                        isShootOff = false,
                                                        isShootOffWin = false,
                                                        setNumber = 1,
                                                ),
                                                FullHeadToHeadSet(
                                                        data = HeadToHeadGridRowDataPreviewHelper.create(),
                                                        teamSize = 1,
                                                        isShootOff = false,
                                                        isShootOffWin = false,
                                                        setNumber = 2,
                                                ),
                                        ),
                                ),
                                FullHeadToHeadHeat(
                                        heat = DatabaseHeadToHeadHeat(
                                                heat = 1,
                                                opponent = "Jessica Summers",
                                                opponentQualificationRank = 1,
                                                shootId = 1,
                                                sightersCount = 0,
                                                isBye = false,
                                                isShootOffWin = false,
                                        ),
                                        isRecurveMatch = true,
                                        teamSize = 1,
                                        sets = listOf(
                                                FullHeadToHeadSet(
                                                        data = HeadToHeadGridRowDataPreviewHelper.create(),
                                                        teamSize = 1,
                                                        isShootOff = false,
                                                        isShootOffWin = false,
                                                        setNumber = 1,
                                                ),
                                                FullHeadToHeadSet(
                                                        data = HeadToHeadGridRowDataPreviewHelper.create(),
                                                        teamSize = 1,
                                                        isShootOff = false,
                                                        isShootOffWin = false,
                                                        setNumber = 2,
                                                ),
                                        ),
                                ),
                        ),
                )
        ) {}
    }
}
