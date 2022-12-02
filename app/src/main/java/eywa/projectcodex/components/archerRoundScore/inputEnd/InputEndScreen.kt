package eywa.projectcodex.components.archerRoundScore.inputEnd

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.archeryObjects.FullArcherRoundInfo
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.components.archerRoundScore.ArcherRoundIntent
import eywa.projectcodex.components.archerRoundScore.ArcherRoundState
import eywa.projectcodex.components.archerRoundScore.ArcherRoundsPreviewHelper

// TODO No more arrows to add - prevent accidental opening of this screen
@Composable
fun InputEndScreen(
        state: ArcherRoundState.Loaded,
        listener: (ArcherRoundIntent) -> Unit,
) {
    InputEndScaffold(
            showReset = false,
            inputArrows = state.currentScreenInputArrows,
            round = state.fullArcherRoundInfo.round,
            endSize = state.currentScreenEndSize,
            showCancelButton = false,
            submitButtonText = stringResource(R.string.input_end__next_end),
            listener = listener,
    ) {
        ScoreIndicator(
                state.fullArcherRoundInfo.score,
                state.fullArcherRoundInfo.arrowsShot,
        )
        RemainingArrowsIndicator(state.fullArcherRoundInfo)
    }
}

@Composable
private fun ScoreIndicator(
        totalScore: Int,
        arrowsShot: Int,
) {
    Row {
        Column(
                modifier = Modifier.width(IntrinsicSize.Max)
        ) {
            ScoreIndicatorCell(
                    text = stringResource(R.string.input_end__archer_score_header),
                    isHeader = true,
            )
            ScoreIndicatorCell(
                    text = totalScore.toString(),
                    isHeader = false,
            )
        }
        Column(
                modifier = Modifier.width(IntrinsicSize.Max)
        ) {
            ScoreIndicatorCell(
                    text = stringResource(R.string.input_end__archer_arrows_count_header),
                    isHeader = true,
            )
            ScoreIndicatorCell(
                    text = arrowsShot.toString(),
                    isHeader = false,
            )
        }
    }
}

@Composable
private fun ScoreIndicatorCell(
        text: String,
        isHeader: Boolean,
) {
    Text(
            text = text,
            style = CodexTypography.LARGE,
            color = CodexTheme.colors.onAppBackground,
            textAlign = TextAlign.Center,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CodexTheme.colors.onAppBackground)
                    .padding(vertical = 5.dp, horizontal = 10.dp)
    )
}

@Composable
private fun RemainingArrowsIndicator(
        fullArcherRoundInfo: FullArcherRoundInfo
) {
    fullArcherRoundInfo.remainingArrowsAtDistances?.let {
        val remainingStrings = it.map { (count, distance) ->
            stringResource(
                    R.string.input_end__round_indicator_at,
                    count,
                    distance,
                    stringResource(fullArcherRoundInfo.distanceUnit!!)
            )
        }

        Column(
                horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                    text = stringResource(R.string.input_end__round_indicator_label),
                    style = CodexTypography.NORMAL,
                    color = CodexTheme.colors.onAppBackground,
            )
            Text(
                    text = remainingStrings.first(),
                    style = CodexTypography.LARGE,
                    color = CodexTheme.colors.onAppBackground,
            )
            if (it.size > 1) {
                Text(
                        text = remainingStrings
                                .drop(1)
                                .joinToString(stringResource(R.string.general_comma_separator)),
                        style = CodexTypography.NORMAL,
                        color = CodexTheme.colors.onAppBackground,
                )
            }
        }
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
        heightDp = 700,
)
@Composable
fun InputEndScreen_Preview() {
    CodexTheme {
        InputEndScreen(ArcherRoundsPreviewHelper.FEW_ARROWS) {}
    }
}
