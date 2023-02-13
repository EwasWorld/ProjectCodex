package eywa.projectcodex.components.archerRoundScore.arrowInputs.inputEnd

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
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
import eywa.projectcodex.components.archerRoundScore.ArcherRoundSubScreen
import eywa.projectcodex.components.archerRoundScore.ArcherRoundsPreviewHelper
import eywa.projectcodex.components.archerRoundScore.arrowInputs.ArrowInputsScaffold
import eywa.projectcodex.components.archerRoundScore.state.ArcherRoundState

// TODO_CURRENT Help info for table and remaining arrows
class InputEndScreen : ArcherRoundSubScreen() {
    @Composable
    override fun ComposeContent(
            state: ArcherRoundState.Loaded,
            listener: (ArcherRoundIntent) -> Unit,
    ) {
        ScreenContent(state, listener)
    }

    // TODO No more arrows to add - prevent accidental opening of this screen
    @Composable
    private fun ScreenContent(
            state: InputEndState,
            listener: (ArcherRoundIntent) -> Unit,
    ) {
        ArrowInputsScaffold(
                state = state,
                showCancelButton = false,
                showResetButton = false,
                submitButtonText = stringResource(R.string.input_end__next_end),
                helpListener = { listener(ArcherRoundIntent.HelpShowcaseAction(it)) },
                submitHelpInfoTitle = stringResource(R.string.help_input_end__next_end_title),
                submitHelpInfoBody = stringResource(R.string.help_input_end__next_end_body),
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
                        modifier = Modifier.testTag(TestTag.ROUND_SCORE)
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
                        modifier = Modifier.testTag(TestTag.ROUND_ARROWS)
                )
            }
        }
    }

    @Composable
    private fun ScoreIndicatorCell(
            text: String,
            isHeader: Boolean,
            modifier: Modifier = Modifier,
    ) {
        Text(
                text = text,
                style = CodexTypography.LARGE,
                color = CodexTheme.colors.onAppBackground,
                textAlign = TextAlign.Center,
                fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
                modifier = modifier
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
                        modifier = Modifier.testTag(TestTag.REMAINING_ARROWS_CURRENT)
                )
                if (it.size > 1) {
                    Text(
                            text = remainingStrings
                                    .drop(1)
                                    .joinToString(stringResource(R.string.general_comma_separator)),
                            style = CodexTypography.NORMAL,
                            color = CodexTheme.colors.onAppBackground,
                            modifier = Modifier.testTag(TestTag.REMAINING_ARROWS_LATER)
                    )
                }
            }
        }
    }

    object TestTag {
        private const val PREFIX = "ARCHER_ROUND_INPUT_END_"

        const val REMAINING_ARROWS_CURRENT = "${PREFIX}REMAINING_ARROWS_CURRENT"
        const val REMAINING_ARROWS_LATER = "${PREFIX}REMAINING_ARROWS_LATER"
        const val ROUND_SCORE = "${PREFIX}ROUND_SCORE"
        const val ROUND_ARROWS = "${PREFIX}ROUND_ARROWS"
    }

    @Preview(
            showBackground = true,
            backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
            heightDp = 700,
    )
    @Composable
    fun InputEndScreen_Preview() {
        CodexTheme {
            ScreenContent(ArcherRoundsPreviewHelper.WITH_SHOT_ARROWS) {}
        }
    }
}
