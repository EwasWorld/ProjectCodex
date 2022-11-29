package eywa.projectcodex.components.archerRoundScore.archerRoundStats

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import eywa.projectcodex.common.helpShowcase.ComposeHelpShowcaseItem
import eywa.projectcodex.common.helpShowcase.ComposeHelpShowcaseMap
import eywa.projectcodex.common.helpShowcase.updateHelpDialogPosition
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.utils.DateTimeFormat
import eywa.projectcodex.components.archerRoundScore.ArcherRoundPreviewHelper
import eywa.projectcodex.components.archerRoundScore.ArcherRoundState
import kotlin.math.abs

@Composable
private fun style(textAlign: TextAlign = TextAlign.Start) =
        CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground, textAlign = textAlign)

@Composable
fun ArcherRoundStatsScreen(
        state: ArcherRoundState
) {
    Column(
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxSize()
                    .background(CodexTheme.colors.appBackground)
                    .padding(25.dp)
    ) {
        Section {
            DataRow(
                    title = R.string.archer_round_stats__date,
                    text = DateTimeFormat.LONG_DATE_TIME.format(state.fullArcherRoundInfo.archerRound.dateShot),
            )
            if (state.fullArcherRoundInfo.round != null) {
                DataRow(
                        title = R.string.archer_round_stats__round,
                        text = state.fullArcherRoundInfo.round.displayName
                )
            }
        }

        Section {
            DataRow(
                    title = R.string.archer_round_stats__hits,
                    text = state.fullArcherRoundInfo.hits.toString(),
            )
            DataRow(
                    title = R.string.archer_round_stats__score,
                    text = state.fullArcherRoundInfo.score.toString(),
            )
            DataRow(
                    title = R.string.archer_round_stats__golds,
                    text = state.fullArcherRoundInfo.golds(state.goldsType).toString(),
            )
        }

        if (state.fullArcherRoundInfo.round != null) {
            Section {
                state.fullArcherRoundInfo.remainingArrows!!.let { remaining ->
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
                        text = state.fullArcherRoundInfo.handicap.toString(),
                )
                DataRow(
                        title = R.string.archer_round_stats__predicted_score,
                        text = state.fullArcherRoundInfo.predictedScore.toString(),
                )
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

@Composable
private fun DataRow(
        @StringRes title: Int,
        text: String,
        helpInfo: ComposeHelpShowcaseMap? = null,
        @StringRes helpTitle: Int? = null,
        @StringRes helpBody: Int? = null,
        modifier: Modifier = Modifier,
) {
    require(helpTitle == null || (helpBody != null || helpInfo == null)) { "If a title is given, a map and body must be given too" }
    var rowModifier = modifier

    if (helpTitle != null) {
        helpInfo!!.add(ComposeHelpShowcaseItem(helpTitle = helpTitle, helpBody = helpBody!!))
        rowModifier = rowModifier.then(Modifier.updateHelpDialogPosition(helpInfo, helpTitle))
    }

    Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            modifier = rowModifier
    ) {
        Text(
                text = stringResource(title),
                style = style(textAlign = TextAlign.End),
        )
        Text(
                text = text,
                style = style(textAlign = TextAlign.Start),
        )
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun ArcherRoundStatsScreen_Preview() {
    CodexTheme {
        ArcherRoundStatsScreen(ArcherRoundPreviewHelper.SIMPLE)
    }
}