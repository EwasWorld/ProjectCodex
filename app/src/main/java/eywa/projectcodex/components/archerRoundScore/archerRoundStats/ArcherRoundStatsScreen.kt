package eywa.projectcodex.components.archerRoundScore.archerRoundStats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.archeryObjects.FullArcherRoundInfo
import eywa.projectcodex.common.archeryObjects.GoldsType
import eywa.projectcodex.common.sharedUi.ButtonState
import eywa.projectcodex.common.sharedUi.SimpleDialog
import eywa.projectcodex.common.sharedUi.SimpleDialogContent
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.utils.DateTimeFormat
import eywa.projectcodex.components.archerRoundScore.ArcherRoundsPreviewHelper
import eywa.projectcodex.components.archerRoundScore.DataRow
import kotlin.math.abs

@Composable
private fun style(textAlign: TextAlign = TextAlign.Start) =
        CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground, textAlign = textAlign)

@Composable
fun ArcherRoundStatsScreen(
        state: FullArcherRoundInfo,
        goldsType: GoldsType,
        noArrowsListener: () -> Unit,
) {
    SimpleDialog(isShown = state.arrowsShot <= 0, onDismissListener = noArrowsListener) {
        SimpleDialogContent(
                title = stringResource(R.string.archer_round_stats__no_arrows_dialog_title),
                positiveButton = ButtonState(
                        text = stringResource(R.string.archer_round_stats__no_arrows_dialog_button),
                        onClick = noArrowsListener,
                ),
        )
    }

    Column(
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(25.dp)
    ) {
        Section {
            DataRow(
                    title = R.string.archer_round_stats__date,
                    text = DateTimeFormat.LONG_DATE_TIME.format(state.archerRound.dateShot),
            )
            DataRow(
                    title = R.string.archer_round_stats__round,
                    text = state.round?.displayName ?: stringResource(R.string.archer_round_stats__no_round)
            )
        }

        if (state.arrowsShot > 0) {
            Section {
                DataRow(
                        title = R.string.archer_round_stats__hits,
                        text = state.hits.toString(),
                )
                DataRow(
                        title = R.string.archer_round_stats__score,
                        text = state.score.toString(),
                )
                DataRow(
                        title = R.string.archer_round_stats__golds,
                        text = state.golds(goldsType).toString(),
                )
            }

            if (state.round != null) {
                Section {
                    state.remainingArrows!!.let { remaining ->
                        if (remaining == 0) {
                            Text(
                                    text = stringResource(R.string.input_end__round_complete),
                                    style = style(),
                            )
                        }
                        else {
                            val heading = if (remaining >= 0) R.string.archer_round_stats__remaining_arrows
                            else R.string.archer_round_stats__surplus_arrows
                            DataRow(
                                    title = heading,
                                    text = abs(remaining).toString(),
                            )
                        }
                    }
                    DataRow(
                            title = R.string.archer_round_stats__handicap,
                            text = state.handicap.toString(),
                    )
                    DataRow(
                            title = R.string.archer_round_stats__predicted_score,
                            text = state.predictedScore.toString(),
                    )
                }
            }
        }
    }
}

@Composable
private fun Section(
        content: @Composable () -> Unit
) {
    Column(
            verticalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
    ) {
        content()
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun ArcherRoundStatsScreen_Preview() {
    CodexTheme {
        ArcherRoundStatsScreen(
                ArcherRoundsPreviewHelper.SIMPLE.fullArcherRoundInfo,
                GoldsType.NINES,
        ) {}
    }
}