package eywa.projectcodex.components.shootDetails.stats

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import eywa.projectcodex.R
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.navigation.NavArgument
import eywa.projectcodex.common.sharedUi.CodexIconButton
import eywa.projectcodex.common.sharedUi.DataRow
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.selectRoundFaceDialog.SelectFaceRow
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.common.utils.DateTimeFormat
import eywa.projectcodex.components.shootDetails.ShootDetailsResponse
import eywa.projectcodex.components.shootDetails.commonUi.HandleMainEffects
import eywa.projectcodex.components.shootDetails.commonUi.ShootDetailsMainScreen
import eywa.projectcodex.components.shootDetails.commonUi.ShootDetailsStatePreviewHelper
import eywa.projectcodex.components.shootDetails.getData
import eywa.projectcodex.components.shootDetails.stats.StatsIntent.*
import eywa.projectcodex.components.shootDetails.stats.StatsTestTag.*
import eywa.projectcodex.database.RoundFace
import kotlin.math.abs

@Composable
private fun style(textAlign: TextAlign = TextAlign.Start) =
        CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground, textAlign = textAlign)

@Composable
fun StatsScreen(
        navController: NavController,
        viewModel: StatsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val listener = { it: StatsIntent -> viewModel.handle(it) }

    ShootDetailsMainScreen(
            currentScreen = CodexNavRoute.SHOOT_DETAILS_STATS,
            state = state,
            listener = { listener(ShootDetailsAction(it)) },
    ) { it, modifier -> StatsScreen(it, modifier, listener) }

    HandleMainEffects(
            navController = navController,
            state = state,
            listener = { listener(ShootDetailsAction(it)) },
    )
    HandleEffects(navController, state, listener)
}

@Composable
fun HandleEffects(
        navController: NavController,
        state: ShootDetailsResponse<StatsState>,
        listener: (StatsIntent) -> Unit,
) {
    val loadedState = state.getData() ?: return

    LaunchedEffect(loadedState.openEditScoreScreen) {
        if (loadedState.openEditScoreScreen) {
            CodexNavRoute.NEW_SCORE.navigate(
                    navController,
                    mapOf(NavArgument.SHOOT_ID to loadedState.fullShootInfo.id.toString()),
            )
            listener(EditHandled)
        }
    }
}

@Composable
private fun StatsScreen(
        state: StatsState,
        modifier: Modifier = Modifier,
        listener: (StatsIntent) -> Unit,
) {
    ProvideTextStyle(value = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground)) {
        Column(
                verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = modifier
                        .padding(25.dp)
                        .testTag(SCREEN.getTestTag())
        ) {
            Box(
                    contentAlignment = Alignment.BottomEnd,
            ) {
                Surface(
                        shape = RoundedCornerShape(20),
                        border = BorderStroke(1.dp, CodexTheme.colors.listItemOnAppBackground),
                        color = CodexTheme.colors.appBackground,
                        modifier = Modifier.padding(5.dp)
                ) {
                    Section(
                            modifier = Modifier.padding(horizontal = 25.dp, vertical = 20.dp)
                    ) {
                        DataRow(
                                title = stringResource(R.string.archer_round_stats__date),
                                text = DateTimeFormat.LONG_DATE_TIME.format(state.fullShootInfo.shoot.dateShot),
                                textModifier = Modifier.testTag(DATE_TEXT.getTestTag()),
                        )
                        DataRow(
                                title = stringResource(R.string.archer_round_stats__round),
                                text = state.fullShootInfo.displayName
                                        ?: stringResource(R.string.archer_round_stats__no_round),
                                textModifier = Modifier.testTag(ROUND_TEXT.getTestTag()),
                        )
                        SelectFaceRow(
                                selectedFaces = state.fullShootInfo.faces,
                                helpListener = { listener(HelpShowcaseAction(it)) },
                                onClick = null,
                        )
                    }
                }
                CodexIconButton(
                        icon = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.archer_round_stats__edit_content_description),
                        onClick = { listener(EditClicked) },
                        modifier = Modifier.padding(horizontal = 0.dp, vertical = 0.dp)
                )
            }

            val hits = state.fullShootInfo.hits
            val arrowsShot = state.fullShootInfo.arrowsShot

            Section {
                DataRow(
                        title = stringResource(R.string.archer_round_stats__hits),
                        text = (
                                if (hits == arrowsShot) hits.toString()
                                else stringResource(R.string.archer_round_stats__hits_of, hits, arrowsShot)
                                ),
                        textModifier = Modifier.testTag(HITS_TEXT.getTestTag()),
                )
                DataRow(
                        title = stringResource(R.string.archer_round_stats__score),
                        text = state.fullShootInfo.score.toString(),
                        textModifier = Modifier.testTag(SCORE_TEXT.getTestTag()),
                )
                DataRow(
                        title = stringResource(state.fullShootInfo.goldsType.longStringId) + ":",
                        text = state.fullShootInfo.golds().toString(),
                        textModifier = Modifier.testTag(GOLDS_TEXT.getTestTag()),
                )
            }

            if (state.fullShootInfo.round != null) {
                Section {
                    state.fullShootInfo.remainingArrows!!.let { remaining ->
                        if (remaining == 0) {
                            Text(
                                    text = stringResource(R.string.input_end__round_complete),
                                    style = style(),
                                    modifier = Modifier.testTag(REMAINING_ARROWS_TEXT.getTestTag()),
                            )
                        }
                        else {
                            val heading = if (remaining >= 0) R.string.archer_round_stats__remaining_arrows
                            else R.string.archer_round_stats__surplus_arrows
                            DataRow(
                                    title = stringResource(heading),
                                    text = abs(remaining).toString(),
                                    textModifier = Modifier.testTag(REMAINING_ARROWS_TEXT.getTestTag()),
                            )
                        }
                    }
                    if (state.fullShootInfo.handicap != null) {
                        DataRow(
                                title = stringResource(R.string.archer_round_stats__handicap),
                                text = state.fullShootInfo.handicap.toString(),
                                textModifier = Modifier.testTag(HANDICAP_TEXT.getTestTag()),
                        )
                    }
                    if (state.fullShootInfo.predictedScore != null) {
                        DataRow(
                                title = stringResource(R.string.archer_round_stats__predicted_score),
                                text = state.fullShootInfo.predictedScore.toString(),
                                textModifier = Modifier.testTag(PREDICTED_SCORE_TEXT.getTestTag()),
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
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit,
) {
    Column(
            verticalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
    ) {
        content()
    }
}

enum class StatsTestTag : CodexTestTag {
    SCREEN,
    DATE_TEXT,
    ROUND_TEXT,
    HITS_TEXT,
    SCORE_TEXT,
    GOLDS_TEXT,
    REMAINING_ARROWS_TEXT,
    HANDICAP_TEXT,
    PREDICTED_SCORE_TEXT,
    ;

    override val screenName: String
        get() = "SHOOT_DETAILS_STATS"

    override fun getElement(): String = name
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun NoRound_StatsScreen_Preview() {
    CodexTheme {
        StatsScreen(
                StatsState(
                        main = ShootDetailsStatePreviewHelper.SIMPLE,
                        extras = StatsExtras(),
                )
        ) {}
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun Round_StatsScreen_Preview() {
    CodexTheme {
        StatsScreen(
                StatsState(
                        main = ShootDetailsStatePreviewHelper.WITH_SHOT_ARROWS.let { state ->
                            val faces = listOf(RoundFace.TRIPLE, RoundFace.FITA_SIX)
                            val far = state.fullShootInfo!!
                            state.copy(fullShootInfo = far.copy(shootRound = far.shootRound!!.copy(faces = faces)))
                        },
                        extras = StatsExtras(),
                )
        ) {}
    }
}
