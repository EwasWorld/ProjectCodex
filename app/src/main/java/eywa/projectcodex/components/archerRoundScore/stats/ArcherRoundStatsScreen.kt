package eywa.projectcodex.components.archerRoundScore.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.sharedUi.ButtonState
import eywa.projectcodex.common.sharedUi.DataRow
import eywa.projectcodex.common.sharedUi.SimpleDialog
import eywa.projectcodex.common.sharedUi.SimpleDialogContent
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.utils.DateTimeFormat
import eywa.projectcodex.components.archerRoundScore.ArcherRoundIntent
import eywa.projectcodex.components.archerRoundScore.ArcherRoundSubScreen
import eywa.projectcodex.components.archerRoundScore.ArcherRoundsPreviewHelper
import eywa.projectcodex.components.archerRoundScore.state.ArcherRoundState
import kotlin.math.abs

@Composable
private fun style(textAlign: TextAlign = TextAlign.Start) =
        CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground, textAlign = textAlign)

class ArcherRoundStatsScreen : ArcherRoundSubScreen() {
    @Composable
    override fun ComposeContent(
            state: ArcherRoundState.Loaded,
            listener: (ArcherRoundIntent) -> Unit,
    ) {
        ScreenContent(state) { listener(ArcherRoundIntent.NoArrowsDialogOkClicked) }
    }

    override fun getHelpShowcases(): List<HelpShowcaseItem> {
        // TODO_CURRENT Help info
        return listOf()
    }

    override fun getHelpPriority(): Int? = null

    @Composable
    private fun ScreenContent(
            state: ArcherRoundStatsState,
            noArrowsListener: () -> Unit,
    ) {
        ProvideTextStyle(value = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground)) {
            SimpleDialog(isShown = state.fullArcherRoundInfo.arrowsShot <= 0, onDismissListener = noArrowsListener) {
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
                            text = DateTimeFormat.LONG_DATE_TIME.format(state.fullArcherRoundInfo.archerRound.dateShot),
                            textModifier = Modifier.testTag(TestTag.DATE_TEXT),
                    )
                    DataRow(
                            title = R.string.archer_round_stats__round,
                            text = state.fullArcherRoundInfo.displayName
                                    ?: stringResource(R.string.archer_round_stats__no_round),
                            textModifier = Modifier.testTag(TestTag.ROUND_TEXT),
                    )
                }

                if (state.fullArcherRoundInfo.arrowsShot > 0) {
                    val hits = state.fullArcherRoundInfo.hits
                    val arrowsShot = state.fullArcherRoundInfo.arrowsShot

                    Section {
                        DataRow(
                                title = R.string.archer_round_stats__hits,
                                text = if (hits == arrowsShot) hits.toString()
                                else stringResource(R.string.archer_round_stats__hits_of, hits, arrowsShot),
                                textModifier = Modifier.testTag(TestTag.HITS_TEXT),
                        )
                        DataRow(
                                title = R.string.archer_round_stats__score,
                                text = state.fullArcherRoundInfo.score.toString(),
                                textModifier = Modifier.testTag(TestTag.SCORE_TEXT),
                        )
                        DataRow(
                                title = R.string.archer_round_stats__golds,
                                text = state.fullArcherRoundInfo.golds(state.goldsType).toString(),
                                textModifier = Modifier.testTag(TestTag.GOLDS_TEXT),
                        )
                    }

                    if (state.fullArcherRoundInfo.round != null) {
                        Section {
                            state.fullArcherRoundInfo.remainingArrows!!.let { remaining ->
                                if (remaining == 0) {
                                    Text(
                                            text = stringResource(R.string.input_end__round_complete),
                                            style = style(),
                                            modifier = Modifier.testTag(TestTag.REMAINING_ARROWS_TEXT),
                                    )
                                }
                                else {
                                    val heading = if (remaining >= 0) R.string.archer_round_stats__remaining_arrows
                                    else R.string.archer_round_stats__surplus_arrows
                                    DataRow(
                                            title = heading,
                                            text = abs(remaining).toString(),
                                            textModifier = Modifier.testTag(TestTag.REMAINING_ARROWS_TEXT),
                                    )
                                }
                            }
                            DataRow(
                                    title = R.string.archer_round_stats__handicap,
                                    text = state.fullArcherRoundInfo.handicap.toString(),
                                    textModifier = Modifier.testTag(TestTag.HANDICAP_TEXT),
                            )
                            if (state.fullArcherRoundInfo.predictedScore != null) {
                                DataRow(
                                        title = R.string.archer_round_stats__predicted_score,
                                        text = state.fullArcherRoundInfo.predictedScore.toString(),
                                        textModifier = Modifier.testTag(TestTag.PREDICTED_SCORE_TEXT),
                                )
                            }
                        }
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

    object TestTag {
        private const val PREFIX = "ARCHER_ROUND_STATS_"

        const val DATE_TEXT = "${PREFIX}DATE_TEXT"
        const val ROUND_TEXT = "${PREFIX}ROUND_TEXT"
        const val HITS_TEXT = "${PREFIX}HITS_TEXT"
        const val SCORE_TEXT = "${PREFIX}SCORE_TEXT"
        const val GOLDS_TEXT = "${PREFIX}GOLDS_TEXT"
        const val REMAINING_ARROWS_TEXT = "${PREFIX}REMAINING_ARROWS_TEXT"
        const val HANDICAP_TEXT = "${PREFIX}HANDICAP_TEXT"
        const val PREDICTED_SCORE_TEXT = "${PREFIX}PREDICTED_SCORE_TEXT"
    }

    @Preview(
            showBackground = true,
            backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
    )
    @Composable
    fun ArcherRoundStatsScreen_Preview() {
        CodexTheme {
            ScreenContent(
                    ArcherRoundsPreviewHelper.SIMPLE,
            ) {}
        }
    }
}
