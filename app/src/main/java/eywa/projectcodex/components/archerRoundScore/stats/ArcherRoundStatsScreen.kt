package eywa.projectcodex.components.archerRoundScore.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.ProvideTextStyle
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
import eywa.projectcodex.common.sharedUi.DataRow
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.utils.DateTimeFormat
import eywa.projectcodex.components.archerRoundScore.ArcherRoundIntent
import eywa.projectcodex.components.archerRoundScore.ArcherRoundSubScreen
import eywa.projectcodex.components.archerRoundScore.state.ArcherRoundState
import eywa.projectcodex.components.archerRoundScore.state.ArcherRoundStatePreviewHelper
import kotlin.math.abs

@Composable
private fun style(textAlign: TextAlign = TextAlign.Start) =
        CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground, textAlign = textAlign)

class ArcherRoundStatsScreen : ArcherRoundSubScreen() {
    @Composable
    override fun ComposeContent(
            state: ArcherRoundState.Loaded,
            modifier: Modifier,
            listener: (ArcherRoundIntent) -> Unit,
    ) {
        ScreenContent(state, modifier)
    }

    @Composable
    private fun ScreenContent(
            state: ArcherRoundStatsState,
            modifier: Modifier = Modifier,
    ) {
        ProvideTextStyle(value = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground)) {
            Column(
                    verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = modifier
                            .padding(25.dp)
                            .testTag(TestTag.SCREEN)
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

                val hits = state.fullArcherRoundInfo.hits
                val arrowsShot = state.fullArcherRoundInfo.arrowsShot

                Section {
                    DataRow(
                            title = R.string.archer_round_stats__hits,
                            text = (
                                    if (hits == arrowsShot) hits.toString()
                                    else stringResource(R.string.archer_round_stats__hits_of, hits, arrowsShot)
                                    ),
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
                        if (state.fullArcherRoundInfo.handicap != null) {
                            DataRow(
                                    title = R.string.archer_round_stats__handicap,
                                    text = state.fullArcherRoundInfo.handicap.toString(),
                                    textModifier = Modifier.testTag(TestTag.HANDICAP_TEXT),
                            )
                        }
                        if (state.fullArcherRoundInfo.predictedScore != null) {
                            DataRow(
                                    title = R.string.archer_round_stats__predicted_score,
                                    text = state.fullArcherRoundInfo.predictedScore.toString(),
                                    textModifier = Modifier.testTag(TestTag.PREDICTED_SCORE_TEXT),
                            )
                        }
                    }
                }

                if (state.useBetaFeatures) {
                    state.extras?.let { extras ->
                        Spacer(modifier = Modifier)

                        Text(
                                text = "Beta Feature:",
                                fontWeight = FontWeight.Bold,
                                style = CodexTypography.LARGE,
                                color = CodexTheme.colors.onAppBackground,
                        )
                        Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.horizontalScroll(rememberScrollState())
                        ) {
                            DataColumn(
                                    "dist",
                                    extras.map {
                                        when (it) {
                                            is DistanceExtra -> it.distance.distance.toString()
                                            is GrandTotalExtra -> "Total"
                                            else -> throw NotImplementedError()
                                        }
                                    },
                            )
                            DoubleDataColumn("HC", extras.map { it.handicap })
                            FloatDataColumn("avgEnd", extras.map { it.averageEnd })
                            FloatDataColumn("endStD", extras.map { it.endStDev }, 2)
                            FloatDataColumn("avgArr", extras.map { it.averageArrow })
                            FloatDataColumn("arrStD", extras.map { it.arrowStdDev }, 2)
                        }

                        Text(
                                "HC: handicap, avgEnd: average end score, endStD: end standard deviation," +
                                        " avgArr: average arrow score, arrStD: arrow standard deviation"
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun DoubleDataColumn(title: String, strings: List<Double?>, decimalPlaces: Int = 1) =
            DataColumn(title, strings.map { it?.let { "%.${decimalPlaces}f".format(it) } ?: "-" })

    @Composable
    private fun FloatDataColumn(title: String, strings: List<Float?>, decimalPlaces: Int = 1) =
            DataColumn(title, strings.map { it?.let { "%.${decimalPlaces}f".format(it) } ?: "-" })

    @Composable
    private fun DataColumn(title: String, strings: List<String>) {
        Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(IntrinsicSize.Min)
        ) {
            listOf(title).plus(strings)
                    .forEachIndexed { index, it ->
                        val isBold = index == 0 || index == strings.size
                        Text(
                                text = it,
                                color = CodexTheme.colors.onListItemAppOnBackground,
                                textAlign = TextAlign.Center,
                                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier
                                        .background(CodexTheme.colors.listItemOnAppBackground)
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                        )
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

        const val SCREEN = "${PREFIX}SCREEN"
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
                    ArcherRoundStatePreviewHelper.SIMPLE,
            )
        }
    }
}
