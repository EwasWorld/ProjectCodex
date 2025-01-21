package eywa.projectcodex.components.shootDetails.headToHead.stats.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.navigation.NavArgument
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.codexTheme.asClickableStyle
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelperDsl
import eywa.projectcodex.common.sharedUi.testTag
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.common.utils.classificationTables.ClassificationTablesPreviewHelper
import eywa.projectcodex.components.shootDetails.commonUi.HandleMainEffects
import eywa.projectcodex.components.shootDetails.commonUi.ShootDetailsMainScreen
import eywa.projectcodex.components.shootDetails.getData
import eywa.projectcodex.components.shootDetails.headToHead.stats.HeadToHeadStatsIntent
import eywa.projectcodex.components.shootDetails.headToHead.stats.HeadToHeadStatsIntent.*
import eywa.projectcodex.components.shootDetails.headToHead.stats.HeadToHeadStatsState
import eywa.projectcodex.components.shootDetails.headToHead.stats.HeadToHeadStatsViewModel
import eywa.projectcodex.components.shootDetails.stats.ui.DateAndRoundSection
import eywa.projectcodex.components.shootDetails.stats.ui.HandicapAndClassificationSection
import eywa.projectcodex.components.shootDetails.stats.ui.StatsDivider
import eywa.projectcodex.database.archer.DatabaseArcherPreviewHelper
import eywa.projectcodex.database.bow.DatabaseBowPreviewHelper
import eywa.projectcodex.model.roundHandicap

@Composable
fun HeadToHeadStatsScreen(
        navController: NavController,
        viewModel: HeadToHeadStatsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val listener = { it: HeadToHeadStatsIntent -> viewModel.handle(it) }

    ShootDetailsMainScreen(
            currentScreen = CodexNavRoute.HEAD_TO_HEAD_STATS,
            state = state,
            listener = { listener(ShootDetailsAction(it)) },
    ) { it, modifier -> HeadToHeadStatsScreen(it, modifier, listener) }

    HandleMainEffects(
            navController = navController,
            state = state,
            listener = { listener(ShootDetailsAction(it)) },
    )

    val data = state.getData()
    val extras = data?.extras
    LaunchedEffect(extras) {
        if (extras != null) {
            if (extras.editMainInfo) {
                CodexNavRoute.NEW_SCORE.navigate(
                        navController,
                        mapOf(NavArgument.SHOOT_ID to data.fullShootInfo.id.toString()),
                )
                listener(EditMainInfoHandled)
            }
            if (extras.editArcherInfo) {
                CodexNavRoute.ARCHER_INFO.navigate(navController)
                listener(EditArcherInfoHandled)
            }
            if (extras.expandHandicaps) {
                CodexNavRoute.HANDICAP_TABLES.navigate(
                        navController,
                        mapOf(
                                NavArgument.HANDICAP to data.fullShootInfo.h2hHandicapToIsSelf?.first,
                                NavArgument.ROUND_ID to data.fullShootInfo.round?.roundId,
                                NavArgument.ROUND_SUB_TYPE_ID to data.fullShootInfo.roundSubType?.subTypeId,
                        ).filter { it.value != null }.mapValues { (_, value) -> value.toString() },
                )
                listener(ExpandHandicapsHandled)
            }
            if (extras.expandClassifications) {
                CodexNavRoute.CLASSIFICATION_TABLES.navigate(
                        navController,
                        mapOf(
                                NavArgument.ROUND_ID to data.fullShootInfo.round?.roundId,
                                NavArgument.ROUND_SUB_TYPE_ID to data.fullShootInfo.roundSubType?.subTypeId,
                                // Passing handicap so that it's available when tab switching between reference tables
                                NavArgument.HANDICAP to data.fullShootInfo.h2hHandicapToIsSelf?.first,
                        ).filter { it.value != null }.mapValues { (_, value) -> value.toString() },
                )
                listener(ExpandClassificationsHandled)
            }
            if (extras.viewQualifyingRound) {
                CodexNavRoute.SHOOT_DETAILS_STATS.navigate(
                        navController,
                        mapOf(NavArgument.SHOOT_ID to extras.qualifyingRoundId!!.toString()),
                )
                listener(ViewQuailfyingRoundHandled)
            }
        }
    }
}

@Composable
fun HeadToHeadStatsScreen(
        state: HeadToHeadStatsState,
        modifier: Modifier = Modifier,
        listener: (HeadToHeadStatsIntent) -> Unit,
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(HelpShowcaseAction(it)) }
    ProvideTextStyle(CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground)) {
        Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = modifier
                        .background(CodexTheme.colors.appBackground)
                        .padding(vertical = CodexTheme.dimens.screenPadding)
                        .testTag(HeadToHeadStatsTestTag.SCREEN)
        ) {
            DateAndRoundSection(
                    fullShootInfo = state.fullShootInfo,
                    modifier = Modifier,
                    editClickedListener = { listener(EditMainInfoClicked) },
                    helpListener = helpListener,
            )
            MatchInfoTable(
                    state = state,
                    listener = listener,
                    modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = CodexTheme.dimens.screenPadding)
            )

            if (state.extras.qualifyingRoundId != null) {
                Text(
                        text = "View qualifying round",
                        style = CodexTypography.SMALL_PLUS.asClickableStyle(),
                        modifier = Modifier.clickable { listener(ViewQuailfyingRoundClicked) }
                )
            }

            HandicapClassification(
                    state = state,
                    listener = listener,
                    modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = CodexTheme.dimens.screenPadding)
            )

            NumbersBreakdown(
                    state = state,
                    listener = listener,
                    modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = CodexTheme.dimens.screenPadding)
            )
        }
    }
}

@Composable
private fun ColumnScope.HandicapClassification(
        state: HeadToHeadStatsState,
        modifier: Modifier = Modifier,
        listener: (HeadToHeadStatsIntent) -> Unit,
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(HelpShowcaseAction(it)) }
    val (handicap, isSelf) = state.fullShootInfo.h2hHandicapToIsSelf ?: (null to true)
    val classification = state.classification

    if (state.fullShootInfo.round != null) {
        StatsDivider(modifier = Modifier.padding(top = 10.dp, bottom = 3.dp))
        HandicapAndClassificationSection(
                prefix = if (isSelf) "Self" else "Team",
                classification = classification,
                isOfficialClassification = false,
                archerInfo = state.archerInfo,
                bow = state.bow,
                handicap = handicap?.roundHandicap(),
                helpListener = helpListener,
                handicapTablesClicked = { listener(ExpandHandicapsClicked) },
                classificationTablesClicked = { listener(ExpandClassificationsClicked) },
                archerCategoryClicked = { listener(EditArcherInfoClicked) },
        )
    }
}

enum class HeadToHeadStatsTestTag : CodexTestTag {
    SCREEN,
    MATCHES_TABLE,
    MATCHES_TABLE_MATCH_CELL,
    MATCHES_TABLE_OPPONENT_CELL,
    MATCHES_TABLE_RANK_CELL,
    MATCHES_TABLE_RESULT_CELL,
    NO_MATCHES_TEXT,
    NUMBERS_BREAKDOWN_TABLE,
    NUMBERS_BREAKDOWN_TABLE_MATCH_CELL,
    NUMBERS_BREAKDOWN_TABLE_SELF_HC_CELL,
    NUMBERS_BREAKDOWN_TABLE_TEAM_HC_CELL,
    NUMBERS_BREAKDOWN_TABLE_SELF_ARROW_AVG_CELL,
    NUMBERS_BREAKDOWN_TABLE_TEAM_ARROW_AVG_CELL,
    NUMBERS_BREAKDOWN_TABLE_OPPONENT_ARROW_AVG_CELL,
    NUMBERS_BREAKDOWN_TABLE_DIFF_ARROW_AVG_CELL,
    NUMBERS_BREAKDOWN_TABLE_SELF_END_AVG_CELL,
    NUMBERS_BREAKDOWN_TABLE_TEAM_END_AVG_CELL,
    NUMBERS_BREAKDOWN_TABLE_OPPONENT_END_AVG_CELL,
    NUMBERS_BREAKDOWN_TABLE_DIFF_END_AVG_CELL,
    NO_NUMBERS_BREAKDOWN_TEXT,
    ;

    override val screenName: String
        get() = "HEAD_TO_HEAD_STATS"

    override fun getElement(): String = name
}

@Preview(showBackground = true)
@Composable
fun HeadToHeadStatsScreen_Preview() {
    CodexTheme {
        HeadToHeadStatsScreen(
                state = HeadToHeadStatsState(
                        fullShootInfo = ShootPreviewHelperDsl.create {
                            round = RoundPreviewHelper.wa70RoundData
                            addH2h {
                                headToHead = headToHead.copy(qualificationRank = 5, totalArchers = 20)
                                addMatch {
                                    match = match.copy(opponentQualificationRank = 10, opponent = "Tess", isBye = true)
                                }
                                addMatch {
                                    match = match.copy(opponentQualificationRank = 13, opponent = "Adya")
                                    addSet { addRows() }
                                    addSet { addRows() }
                                    addSet { addRows() }
                                }
                            }
                        },
                        classificationTablesUseCase = ClassificationTablesPreviewHelper.get(),
                        archerInfo = DatabaseArcherPreviewHelper.default,
                        bow = DatabaseBowPreviewHelper.default,
                        extras = HeadToHeadStatsState.Extras(
                                qualifyingRoundId = 1,
                        ),
                ),
        ) {}
    }
}

@Preview(showBackground = true)
@Composable
fun NoMatches_HeadToHeadStatsScreen_Preview() {
    CodexTheme {
        HeadToHeadStatsScreen(
                state = HeadToHeadStatsState(
                        fullShootInfo = ShootPreviewHelperDsl.create {
                            addH2h {
                                headToHead = headToHead.copy(totalArchers = 5, isStandardFormat = false)
                            }
                        },
                        classificationTablesUseCase = ClassificationTablesPreviewHelper.get(),
                        archerInfo = DatabaseArcherPreviewHelper.default,
                        bow = DatabaseBowPreviewHelper.default,
                ),
        ) {}
    }
}
