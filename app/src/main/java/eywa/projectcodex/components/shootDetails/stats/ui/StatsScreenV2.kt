package eywa.projectcodex.components.shootDetails.stats.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.navigation.NavArgument
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.codexTheme.asClickableStyle
import eywa.projectcodex.common.sharedUi.helperInterfaces.NamedItem
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelperDsl
import eywa.projectcodex.common.sharedUi.testTag
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.common.utils.classificationTables.ClassificationTableEntry
import eywa.projectcodex.common.utils.classificationTables.ClassificationTablesUseCase
import eywa.projectcodex.components.archerHandicaps.ArcherHandicapsPreviewHelper
import eywa.projectcodex.components.shootDetails.ShootDetailsIntent
import eywa.projectcodex.components.shootDetails.ShootDetailsResponse
import eywa.projectcodex.components.shootDetails.ShootDetailsState
import eywa.projectcodex.components.shootDetails.commonUi.HandleMainEffects
import eywa.projectcodex.components.shootDetails.commonUi.ShootDetailsMainScreen
import eywa.projectcodex.components.shootDetails.getData
import eywa.projectcodex.components.shootDetails.stats.StatsExtras
import eywa.projectcodex.components.shootDetails.stats.StatsIntent
import eywa.projectcodex.components.shootDetails.stats.StatsIntent.*
import eywa.projectcodex.components.shootDetails.stats.StatsState
import eywa.projectcodex.components.shootDetails.stats.StatsViewModel
import eywa.projectcodex.components.shootDetails.stats.ui.StatsTestTag.SCREEN
import eywa.projectcodex.database.RoundFace
import eywa.projectcodex.database.archer.DatabaseArcherPreviewHelper
import eywa.projectcodex.database.bow.DatabaseBowPreviewHelper
import eywa.projectcodex.database.shootData.DatabaseShootShortRecord
import java.util.Calendar

@Composable
fun StatsScreenV2(
        navController: NavController,
        viewModel: StatsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val listener = { it: StatsIntent -> viewModel.handle(it) }

    ShootDetailsMainScreen(
            currentScreen = CodexNavRoute.SHOOT_DETAILS_STATS_V2,
            state = state,
            listener = { listener(ShootDetailsAction(it)) },
    ) { it, modifier ->
        if (it.useSimpleView) {
            StatsScreenSimple(it, modifier, listener)
        }
        else {
            StatsScreenFull(it, modifier, listener)
        }
    }

    HandleMainEffects(
            navController = navController,
            state = state,
            listener = { listener(ShootDetailsAction(it)) },
    )
    HandleEffectsV2(navController, state, listener)
}

@Composable
fun HandleEffectsV2(
        navController: NavController,
        state: ShootDetailsResponse<StatsState>,
        listener: (StatsIntent) -> Unit,
) {
    val loadedState = state.getData() ?: return

    LaunchedEffect(
            loadedState.openEditShootScreen,
            loadedState.openEditHandicapInfoScreen,
            loadedState.openEditArcherInfoScreen,
            loadedState.openHandicapTablesScreen,
            loadedState.openClassificationTablesScreen,
    ) {
        if (loadedState.openEditShootScreen) {
            CodexNavRoute.NEW_SCORE.navigate(
                    navController,
                    mapOf(NavArgument.SHOOT_ID to loadedState.fullShootInfo.id.toString()),
            )
            listener(EditShootHandled)
        }
        if (loadedState.openEditHandicapInfoScreen) {
            CodexNavRoute.ARCHER_HANDICAPS.navigate(navController)
            listener(EditHandicapInfoHandled)
        }
        if (loadedState.openEditArcherInfoScreen) {
            CodexNavRoute.ARCHER_INFO.navigate(navController)
            listener(EditArcherInfoHandled)
        }
        if (loadedState.openHandicapTablesScreen) {
            CodexNavRoute.HANDICAP_TABLES.navigate(
                    navController,
                    mapOf(
                            NavArgument.HANDICAP to loadedState.fullShootInfo.handicap,
                            NavArgument.ROUND_ID to loadedState.fullShootInfo.round?.roundId,
                            NavArgument.ROUND_SUB_TYPE_ID to loadedState.fullShootInfo.roundSubType?.subTypeId,
                    ).filter { it.value != null }.mapValues { (_, value) -> value.toString() },
            )
            listener(ExpandHandicapsHandled)
        }
        if (loadedState.openClassificationTablesScreen) {
            CodexNavRoute.CLASSIFICATION_TABLES.navigate(
                    navController,
                    mapOf(
                            NavArgument.ROUND_ID to loadedState.fullShootInfo.round?.roundId,
                            NavArgument.ROUND_SUB_TYPE_ID to loadedState.fullShootInfo.roundSubType?.subTypeId,
                            // Passing handicap so that it's available when tab switching between reference tables
                            NavArgument.HANDICAP to loadedState.fullShootInfo.handicap,
                    ).filter { it.value != null }.mapValues { (_, value) -> value.toString() },
            )
            listener(ExpandClassificationsHandled)
        }
    }
}

@Composable
private fun StatsScreenFull(
        state: StatsState,
        modifier: Modifier = Modifier,
        listener: (StatsIntent) -> Unit,
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(HelpShowcaseAction(it)) }

    Column(
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                    .padding(vertical = CodexTheme.dimens.screenPadding)
                    .testTag(SCREEN)
    ) {
        DateAndRoundSection(
                fullShootInfo = state.fullShootInfo,
                editClickedListener = { listener(EditShootClicked) },
                helpListener = helpListener,
                modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = CodexTheme.dimens.screenPadding)
        )
        HsgPbAndIncompleteRoundInfoSection(
                fullShootInfo = state.fullShootInfo,
                helpListener = helpListener,
                modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = CodexTheme.dimens.screenPadding)
        )

        if (state.fullShootInfo.round != null) {
            PastRecordsSection(state, listener)
            StatsDivider(modifier = Modifier.padding(vertical = 10.dp))

            HandicapAndClassificationSection(
                    state = state,
                    helpListener = helpListener,
                    listener = listener,
                    modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = CodexTheme.dimens.screenPadding)
            )
            StatsDivider(modifier = Modifier.padding(vertical = 10.dp))

            AllowanceSection(
                    state = state,
                    listener = listener,
                    modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = CodexTheme.dimens.screenPadding)
            )
            if (state.fullShootInfo.arrowsShot > 0) {
                StatsDivider(modifier = Modifier.padding(vertical = 10.dp))

                NumberBreakdownSection(
                        state = state,
                        modifier = Modifier
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = CodexTheme.dimens.screenPadding)
                )
            }
        }

        StatsDivider(modifier = Modifier.padding(vertical = 10.dp))
        Text(
                text = stringResource(R.string.archer_round_stats__show_simple_view),
                style = CodexTypography.SMALL_PLUS.asClickableStyle(),
                modifier = Modifier.clickable { listener(ShootDetailsAction(ShootDetailsIntent.ToggleSimpleView)) }
        )
    }
}

@Composable
private fun StatsScreenSimple(
        state: StatsState,
        modifier: Modifier = Modifier,
        listener: (StatsIntent) -> Unit,
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(HelpShowcaseAction(it)) }

    Column(
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                    .padding(vertical = CodexTheme.dimens.screenPadding)
                    .testTag(SCREEN)
    ) {
        DateAndRoundSection(
                fullShootInfo = state.fullShootInfo,
                editClickedListener = { listener(EditShootClicked) },
                helpListener = helpListener,
                modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = CodexTheme.dimens.screenPadding)
        )
        HsgPbAndIncompleteRoundInfoSection(
                fullShootInfo = state.fullShootInfo,
                helpListener = helpListener,
                simpleView = true,
                modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = CodexTheme.dimens.screenPadding)
        )

        if (state.fullShootInfo.round != null) {
            PastRecordsSection(state, listener)

            StatsDivider(modifier = Modifier.padding(vertical = 10.dp))

            HandicapAndClassificationSection(
                    state = state,
                    helpListener = helpListener,
                    listener = listener,
                    modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = CodexTheme.dimens.screenPadding)
            )
        }

        StatsDivider(modifier = Modifier.padding(vertical = 10.dp))

        Text(
                text = stringResource(R.string.archer_round_stats__show_advanced_view),
                style = LocalTextStyle.current.asClickableStyle(),
                modifier = Modifier.clickable { listener(ShootDetailsAction(ShootDetailsIntent.ToggleSimpleView)) }
        )
    }
}

enum class StatsScreenPastRecordsTabs(override val label: ResOrActual<String>) : NamedItem {
    BEST(ResOrActual.StringResource(R.string.archer_round_stats__past_records_best_tab)),
    RECENT(ResOrActual.StringResource(R.string.archer_round_stats__past_records_recent_tab)),
}

enum class StatsTestTag : CodexTestTag {
    SCREEN,
    DATE_TEXT,
    ROUND_TEXT,
    HITS_TEXT,
    SCORE_TEXT,
    GOLDS_TEXT,
    REMAINING_ARROWS_TEXT,
    SURPLUS_ARROWS_TEXT,
    PAST_RECORDS_LINK_TEXT,
    PAST_RECORDS_DIALOG_TAB,
    PAST_RECORDS_DIALOG_ITEM,
    PB_TEXT,
    HANDICAP_TEXT,
    PREDICTED_SCORE_TEXT,
    ARCHER_HANDICAP_TEXT,
    ALLOWANCE_TEXT,
    ADJUSTED_SCORE_TEXT,
    EDIT_SHOOT_INFO,
    EXPAND_SHOOT_INFO,
    CLASSIFICATION_CATEGORY,
    CLASSIFICATION,
    SHOOT_DETAIL_SECTION,
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
fun NoRound_StatsScreenV2_Preview() {
    CodexTheme {
        StatsScreenFull(
                StatsState(
                        extras = StatsExtras(),
                        main = ShootDetailsState(
                                fullShootInfo = ShootPreviewHelperDsl.create {
                                    addFullSetOfArrows()
                                },
                        ),
                        classificationTablesUseCase = ClassificationTablesUseCase(listOf()),
                )
        ) {}
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
        heightDp = 1000,
)
@Composable
fun RoundIncomplete_StatsScreenV2_Preview() {
    CodexTheme {
        StatsScreenFull(
                StatsState(
                        main = ShootDetailsState(
                                fullShootInfo = ShootPreviewHelperDsl.create {
                                    round = RoundPreviewHelper.wa1440RoundData
                                    addIdenticalArrows(20, 7)
                                    faces = listOf(RoundFace.FULL, RoundFace.FULL, RoundFace.HALF, RoundFace.HALF)
                                },
                                archerHandicaps = ArcherHandicapsPreviewHelper.handicaps,
                                pastRoundRecords = listOf(
                                        DatabaseShootShortRecord(2, Calendar.getInstance(), 400, true),
                                        DatabaseShootShortRecord(1, Calendar.getInstance(), 700, true),
                                ),
                                archerInfo = DatabaseArcherPreviewHelper.default.copy(isGent = false),
                                bow = DatabaseBowPreviewHelper.default,
                        ),
                        extras = StatsExtras(pastRoundScoresTab = StatsScreenPastRecordsTabs.RECENT),
                        classificationTablesUseCase = ClassificationTablesUseCase(
                                data = listOf(
                                        ClassificationTableEntry
                                                .fromString("5,Women,Recurve,Senior,WA 1440 (90m),907")!!
                                ),
                        ),
                )
        ) {}
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
        heightDp = 1100,
)
@Composable
fun RoundComplete_StatsScreenV2_Preview() {
    CodexTheme {
        StatsScreenFull(
                StatsState(
                        main = ShootDetailsState(
                                fullShootInfo = ShootPreviewHelperDsl.create {
                                    round = RoundPreviewHelper.wa1440RoundData
                                    completeRound(arrowScore = 8, isX = false)
                                    isPersonalBest = true
                                },
                                archerHandicaps = ArcherHandicapsPreviewHelper.handicaps,
                                pastRoundRecords = listOf(
                                        DatabaseShootShortRecord(2, Calendar.getInstance(), 400, true),
                                        DatabaseShootShortRecord(1, Calendar.getInstance(), 700, true),
                                ),
                                archerInfo = DatabaseArcherPreviewHelper.default.copy(isGent = false),
                                bow = DatabaseBowPreviewHelper.default,
                        ),
                        extras = StatsExtras(pastRoundScoresTab = StatsScreenPastRecordsTabs.RECENT),
                        classificationTablesUseCase = ClassificationTablesUseCase(
                                data = listOf(
                                        ClassificationTableEntry
                                                .fromString("5,Women,Recurve,Senior,WA 1440 (90m),907")!!
                                ),
                        ),
                )
        ) {}
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun NoArrows_StatsScreenV2_Preview() {
    CodexTheme {
        StatsScreenFull(
                StatsState(
                        main = ShootDetailsState(
                                fullShootInfo = ShootPreviewHelperDsl.create {
                                    round = RoundPreviewHelper.wa1440RoundData
                                },
                                archerHandicaps = ArcherHandicapsPreviewHelper.handicaps,
                                pastRoundRecords = listOf(
                                        DatabaseShootShortRecord(2, Calendar.getInstance(), 400, true),
                                        DatabaseShootShortRecord(1, Calendar.getInstance(), 700, true),
                                ),
                                archerInfo = DatabaseArcherPreviewHelper.default.copy(isGent = false),
                                bow = DatabaseBowPreviewHelper.default,
                        ),
                        extras = StatsExtras(pastRoundScoresTab = StatsScreenPastRecordsTabs.RECENT),
                        classificationTablesUseCase = ClassificationTablesUseCase(
                                data = listOf(
                                        ClassificationTableEntry
                                                .fromString("5,Women,Recurve,Senior,WA 1440 (90m),907")!!
                                ),
                        ),
                )
        ) {}
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun SimpleRoundIncomplete_StatsScreenV2_Preview() {
    CodexTheme {
        StatsScreenSimple(
                StatsState(
                        main = ShootDetailsState(
                                fullShootInfo = ShootPreviewHelperDsl.create {
                                    round = RoundPreviewHelper.wa1440RoundData
                                    addIdenticalArrows(20, 7)
                                    faces = listOf(RoundFace.FULL, RoundFace.FULL, RoundFace.HALF, RoundFace.HALF)
                                },
                                archerHandicaps = ArcherHandicapsPreviewHelper.handicaps,
                                pastRoundRecords = listOf(
                                        DatabaseShootShortRecord(2, Calendar.getInstance(), 400, true),
                                        DatabaseShootShortRecord(1, Calendar.getInstance(), 700, true),
                                ),
                                archerInfo = DatabaseArcherPreviewHelper.default.copy(isGent = false),
                                bow = DatabaseBowPreviewHelper.default,
                        ),
                        extras = StatsExtras(pastRoundScoresTab = StatsScreenPastRecordsTabs.RECENT),
                        classificationTablesUseCase = ClassificationTablesUseCase(
                                data = listOf(
                                        ClassificationTableEntry
                                                .fromString("5,Women,Recurve,Senior,WA 1440 (90m),907")!!
                                ),
                        ),
                )
        ) {}
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun SimpleRoundComplete_StatsScreenV2_Preview() {
    CodexTheme {
        StatsScreenSimple(
                StatsState(
                        main = ShootDetailsState(
                                fullShootInfo = ShootPreviewHelperDsl.create {
                                    round = RoundPreviewHelper.wa1440RoundData
                                    completeRound(arrowScore = 8, isX = false)
                                    isPersonalBest = true
                                },
                                archerHandicaps = ArcherHandicapsPreviewHelper.handicaps,
                                pastRoundRecords = listOf(
                                        DatabaseShootShortRecord(2, Calendar.getInstance(), 400, true),
                                        DatabaseShootShortRecord(1, Calendar.getInstance(), 700, true),
                                ),
                                archerInfo = DatabaseArcherPreviewHelper.default.copy(isGent = false),
                                bow = DatabaseBowPreviewHelper.default,
                        ),
                        extras = StatsExtras(pastRoundScoresTab = StatsScreenPastRecordsTabs.RECENT),
                        classificationTablesUseCase = ClassificationTablesUseCase(
                                data = listOf(
                                        ClassificationTableEntry
                                                .fromString("5,Women,Recurve,Senior,WA 1440 (90m),907")!!
                                ),
                        ),
                )
        ) {}
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun SimpleNoArrows_StatsScreenV2_Preview() {
    CodexTheme {
        StatsScreenSimple(
                StatsState(
                        main = ShootDetailsState(
                                fullShootInfo = ShootPreviewHelperDsl.create {
                                    round = RoundPreviewHelper.wa1440RoundData
                                },
                                archerHandicaps = ArcherHandicapsPreviewHelper.handicaps,
                                pastRoundRecords = listOf(
                                        DatabaseShootShortRecord(2, Calendar.getInstance(), 400, true),
                                        DatabaseShootShortRecord(1, Calendar.getInstance(), 700, true),
                                ),
                                archerInfo = DatabaseArcherPreviewHelper.default.copy(isGent = false),
                                bow = DatabaseBowPreviewHelper.default,
                        ),
                        extras = StatsExtras(pastRoundScoresTab = StatsScreenPastRecordsTabs.RECENT),
                        classificationTablesUseCase = ClassificationTablesUseCase(
                                data = listOf(
                                        ClassificationTableEntry
                                                .fromString("5,Women,Recurve,Senior,WA 1440 (90m),907")!!
                                ),
                        ),
                )
        ) {}
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun PastRecords_StatsScreenV2_Preview() {
    CodexTheme {
        StatsScreenFull(
                StatsState(
                        main = ShootDetailsState(
                                fullShootInfo = ShootPreviewHelperDsl.create {
                                    round = RoundPreviewHelper.outdoorImperialRoundData
                                    completeRound(arrowScore = 7, isX = false)
                                },
                                archerHandicaps = ArcherHandicapsPreviewHelper.handicaps,
                                roundPbs = listOf(
                                        DatabaseShootShortRecord(3, Calendar.getInstance(), 500, true),
                                ),
                                pastRoundRecords = listOf(
                                        DatabaseShootShortRecord(2, Calendar.getInstance(), 400, true),
                                        DatabaseShootShortRecord(1, Calendar.getInstance(), 700, true),
                                ),
                        ),
                        extras = StatsExtras(
                                isPastRoundRecordsDialogOpen = true,
                                pastRoundScoresTab = StatsScreenPastRecordsTabs.RECENT,
                        ),
                        classificationTablesUseCase = ClassificationTablesUseCase(listOf()),
                )
        ) {}
    }
}
