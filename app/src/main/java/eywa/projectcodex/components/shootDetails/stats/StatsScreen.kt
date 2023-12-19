package eywa.projectcodex.components.shootDetails.stats

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpState
import eywa.projectcodex.common.helpShowcase.updateHelpDialogPosition
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.navigation.NavArgument
import eywa.projectcodex.common.sharedUi.ButtonState
import eywa.projectcodex.common.sharedUi.CodexIconButton
import eywa.projectcodex.common.sharedUi.CodexIconInfo
import eywa.projectcodex.common.sharedUi.CodexTabSwitcher
import eywa.projectcodex.common.sharedUi.ComposeUtils.modifierIf
import eywa.projectcodex.common.sharedUi.DataRow
import eywa.projectcodex.common.sharedUi.SimpleDialog
import eywa.projectcodex.common.sharedUi.SimpleDialogContent
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.codexTheme.asClickableStyle
import eywa.projectcodex.common.sharedUi.helperInterfaces.NamedItem
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelperDsl
import eywa.projectcodex.common.sharedUi.selectRoundFaceDialog.SelectFaceRow
import eywa.projectcodex.common.sharedUi.testTag
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.common.utils.DateTimeFormat
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.components.archerHandicaps.ArcherHandicapsPreviewHelper
import eywa.projectcodex.components.shootDetails.ShootDetailsResponse
import eywa.projectcodex.components.shootDetails.ShootDetailsState
import eywa.projectcodex.components.shootDetails.commonUi.HandleMainEffects
import eywa.projectcodex.components.shootDetails.commonUi.ShootDetailsMainScreen
import eywa.projectcodex.components.shootDetails.getData
import eywa.projectcodex.components.shootDetails.stats.StatsIntent.EditArcherInfoClicked
import eywa.projectcodex.components.shootDetails.stats.StatsIntent.EditArcherInfoHandled
import eywa.projectcodex.components.shootDetails.stats.StatsIntent.EditShootClicked
import eywa.projectcodex.components.shootDetails.stats.StatsIntent.EditShootHandled
import eywa.projectcodex.components.shootDetails.stats.StatsIntent.HelpShowcaseAction
import eywa.projectcodex.components.shootDetails.stats.StatsIntent.PastRoundRecordsClicked
import eywa.projectcodex.components.shootDetails.stats.StatsIntent.PastRoundRecordsDismissed
import eywa.projectcodex.components.shootDetails.stats.StatsIntent.ShootDetailsAction
import eywa.projectcodex.components.shootDetails.stats.StatsTestTag.ADJUSTED_SCORE_TEXT
import eywa.projectcodex.components.shootDetails.stats.StatsTestTag.ALLOWANCE_TEXT
import eywa.projectcodex.components.shootDetails.stats.StatsTestTag.ARCHER_HANDICAP_TEXT
import eywa.projectcodex.components.shootDetails.stats.StatsTestTag.DATE_TEXT
import eywa.projectcodex.components.shootDetails.stats.StatsTestTag.GOLDS_TEXT
import eywa.projectcodex.components.shootDetails.stats.StatsTestTag.HANDICAP_TEXT
import eywa.projectcodex.components.shootDetails.stats.StatsTestTag.HITS_TEXT
import eywa.projectcodex.components.shootDetails.stats.StatsTestTag.PAST_RECORDS_DIALOG_ITEM
import eywa.projectcodex.components.shootDetails.stats.StatsTestTag.PAST_RECORDS_LINK_TEXT
import eywa.projectcodex.components.shootDetails.stats.StatsTestTag.PB_TEXT
import eywa.projectcodex.components.shootDetails.stats.StatsTestTag.PREDICTED_SCORE_TEXT
import eywa.projectcodex.components.shootDetails.stats.StatsTestTag.REMAINING_ARROWS_TEXT
import eywa.projectcodex.components.shootDetails.stats.StatsTestTag.ROUND_TEXT
import eywa.projectcodex.components.shootDetails.stats.StatsTestTag.SCORE_TEXT
import eywa.projectcodex.components.shootDetails.stats.StatsTestTag.SCREEN
import eywa.projectcodex.database.RoundFace
import eywa.projectcodex.database.shootData.DatabaseShootShortRecord
import eywa.projectcodex.model.FullShootInfo
import java.util.Calendar
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

    LaunchedEffect(loadedState.openEditShootScreen, loadedState.openEditArcherInfoScreen) {
        if (loadedState.openEditShootScreen) {
            CodexNavRoute.NEW_SCORE.navigate(
                    navController,
                    mapOf(NavArgument.SHOOT_ID to loadedState.fullShootInfo.id.toString()),
            )
            listener(EditShootHandled)
        }
        if (loadedState.openEditArcherInfoScreen) {
            CodexNavRoute.ARCHER_HANDICAPS.navigate(navController)
            listener(EditArcherInfoHandled)
        }
    }
}

@Composable
private fun StatsScreen(
        state: StatsState,
        modifier: Modifier = Modifier,
        listener: (StatsIntent) -> Unit,
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(HelpShowcaseAction(it)) }

    ProvideTextStyle(value = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground)) {
        Column(
                verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = modifier
                        .padding(25.dp)
                        .testTag(SCREEN.getTestTag())
        ) {
            NewScoreSection(
                    fullShootInfo = state.fullShootInfo,
                    editClickedListener = { listener(EditShootClicked) },
                    helpListener = helpListener,
            )
            HsgSection(state, helpListener)

            RoundStatsSection(state, helpListener)
            PastRecordsSection(state, listener)
            AllowanceSection(state, listener)

            if (state.useBetaFeatures) {
                NumberBreakdownSection(state)
            }
        }
    }
}

@Composable
private fun EditBox(
        editContentDescription: String,
        editListener: () -> Unit,
        content: @Composable () -> Unit,
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
                    modifier = Modifier.padding(horizontal = 35.dp, vertical = 20.dp)
            ) {
                content()
            }
        }
        CodexIconButton(
                icon = CodexIconInfo.VectorIcon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = editContentDescription,
                ),
                onClick = editListener,
                modifier = Modifier
                        .padding(horizontal = 0.dp, vertical = 0.dp)
                        .testTag(StatsTestTag.EDIT_SHOOT_INFO)
        )
    }
}

@Composable
fun NewScoreSection(
        fullShootInfo: FullShootInfo,
        editClickedListener: () -> Unit,
        helpListener: (HelpShowcaseIntent) -> Unit,
) {
    EditBox(
            editContentDescription = stringResource(R.string.archer_round_stats__edit_shoot_content_description),
            editListener = editClickedListener,
    ) {
        DataRow(
                title = stringResource(R.string.archer_round_stats__date),
                text = DateTimeFormat.LONG_DATE_TIME.format(fullShootInfo.shoot.dateShot),
                helpState = HelpState(
                        helpListener = helpListener,
                        helpTitle = stringResource(R.string.help_archer_round_stats__date_title),
                        helpBody = stringResource(R.string.help_archer_round_stats__date_body),
                ),
                textModifier = Modifier.testTag(DATE_TEXT.getTestTag()),
        )
        DataRow(
                title = stringResource(R.string.archer_round_stats__round),
                text = fullShootInfo.displayName
                        ?: stringResource(R.string.archer_round_stats__no_round),
                textModifier = Modifier.testTag(ROUND_TEXT.getTestTag()),
                helpState = HelpState(
                        helpListener = helpListener,
                        helpTitle = stringResource(R.string.help_archer_round_stats__round_title),
                        helpBody = stringResource(R.string.help_archer_round_stats__round_body),
                ),
        )
        SelectFaceRow(
                selectedFaces = fullShootInfo.faces,
                helpListener = helpListener,
                onClick = null,
        )
    }
}

@Composable
private fun HsgSection(
        state: StatsState,
        helpListener: (HelpShowcaseIntent) -> Unit,
) {
    val hits = state.fullShootInfo.hits
    val arrowsShot = state.fullShootInfo.arrowsShot

    Section {
        DataRow(
                title = stringResource(R.string.archer_round_stats__hits),
                text = (
                        if (hits == arrowsShot) hits.toString()
                        else stringResource(R.string.archer_round_stats__hits_of, hits, arrowsShot)
                        ),
                helpState = HelpState(
                        helpListener = helpListener,
                        helpTitle = stringResource(R.string.help_archer_round_stats__hits_title),
                        helpBody = stringResource(R.string.help_archer_round_stats__hits_body),
                ),
                textModifier = Modifier.testTag(HITS_TEXT.getTestTag()),
        )
        DataRow(
                title = stringResource(R.string.archer_round_stats__score),
                text = state.fullShootInfo.score.toString(),
                helpState = HelpState(
                        helpListener = helpListener,
                        helpTitle = stringResource(R.string.help_archer_round_stats__score_title),
                        helpBody = stringResource(R.string.help_archer_round_stats__score_body),
                ),
                textModifier = Modifier.testTag(SCORE_TEXT.getTestTag()),
        )
        DataRow(
                title = stringResource(state.fullShootInfo.goldsType.longStringId) + ":",
                text = state.fullShootInfo.golds().toString(),
                helpState = HelpState(
                        helpListener = helpListener,
                        helpTitle = stringResource(R.string.help_archer_round_stats__golds_title),
                        helpBody = stringResource(R.string.help_archer_round_stats__golds_body),
                ),
                textModifier = Modifier.testTag(GOLDS_TEXT.getTestTag()),
        )
    }
}

@Composable
private fun RoundStatsSection(
        state: StatsState,
        helpListener: (HelpShowcaseIntent) -> Unit,
) {
    if (state.fullShootInfo.round == null) return

    val remaining = state.fullShootInfo.remainingArrows!!
    Section {
        if (remaining == 0) {
            Text(
                    text = stringResource(R.string.input_end__round_complete),
                    style = style(),
                    modifier = Modifier
                            .testTag(REMAINING_ARROWS_TEXT.getTestTag())
                            .updateHelpDialogPosition(
                                    HelpState(
                                            helpListener = helpListener,
                                            helpTitle = stringResource(R.string.help_archer_round_stats__round_complete_title),
                                            helpBody = stringResource(R.string.help_archer_round_stats__round_complete_body),
                                    ),
                            )
            )
        }
        else {
            DataRow(
                    title = stringResource(
                            if (remaining >= 0) R.string.archer_round_stats__remaining_arrows
                            else R.string.archer_round_stats__surplus_arrows
                    ),
                    text = abs(remaining).toString(),
                    helpState = HelpState(
                            helpListener = helpListener,
                            helpTitle = stringResource(R.string.help_archer_round_stats__remaining_arrows_title),
                            helpBody = stringResource(R.string.help_archer_round_stats__remaining_arrows_body),
                    ),
                    textModifier = Modifier.testTag(REMAINING_ARROWS_TEXT.getTestTag()),
            )
        }
        if (state.fullShootInfo.isPersonalBest) {
            val pbStringId =
                    if (state.fullShootInfo.isTiedPersonalBest) R.string.archer_round_stats__is_tied_pb
                    else R.string.archer_round_stats__is_pb
            Text(
                    text = stringResource(pbStringId),
                    style = style(),
                    color = CodexTheme.colors.onPersonalBestTag,
                    modifier = Modifier
                            .padding(vertical = 4.dp)
                            .background(color = CodexTheme.colors.personalBestTag, shape = RoundedCornerShape(100))
                            .padding(horizontal = 10.dp)
                            .testTag(PB_TEXT.getTestTag())
                            .updateHelpDialogPosition(
                                    HelpState(
                                            helpListener = helpListener,
                                            helpTitle = stringResource(R.string.help_archer_round_stats__personal_best_title),
                                            helpBody = stringResource(R.string.help_archer_round_stats__personal_best_body),
                                    ),
                            )
            )
        }
        if (state.fullShootInfo.handicap != null) {
            DataRow(
                    title = stringResource(R.string.archer_round_stats__handicap),
                    text = state.fullShootInfo.handicap.toString(),
                    helpState = HelpState(
                            helpListener = helpListener,
                            helpTitle = stringResource(R.string.help_archer_round_stats__round_handicap_title),
                            helpBody = stringResource(R.string.help_archer_round_stats__round_handicap_body),
                    ),
                    textModifier = Modifier.testTag(HANDICAP_TEXT.getTestTag()),
            )
        }
        if (state.fullShootInfo.predictedScore != null) {
            DataRow(
                    title = stringResource(R.string.archer_round_stats__predicted_score),
                    text = state.fullShootInfo.predictedScore.toString(),
                    helpState = HelpState(
                            helpListener = helpListener,
                            helpTitle = stringResource(R.string.help_archer_round_stats__predicted_score_title),
                            helpBody = stringResource(R.string.help_archer_round_stats__predicted_score_body),
                    ),
                    textModifier = Modifier.testTag(PREDICTED_SCORE_TEXT.getTestTag()),
            )
        }
    }
}

@Composable
private fun AllowanceSection(
        state: StatsState,
        listener: (StatsIntent) -> Unit,
) {
    if (state.fullShootInfo.round == null || state.allowance == null) return
    val helpListener = { it: HelpShowcaseIntent -> listener(HelpShowcaseAction(it)) }

    Section {
        EditBox(
                editContentDescription = stringResource(R.string.archer_round_stats__archer_handicap_edit),
                editListener = { listener(EditArcherInfoClicked) }
        ) {
            DataRow(
                    title = stringResource(R.string.archer_round_stats__archer_handicap),
                    text = state.archerHandicap.toString(),
                    helpState = HelpState(
                            helpListener = helpListener,
                            helpTitle = stringResource(R.string.help_archer_round_stats__archer_handicap_title),
                            helpBody = stringResource(R.string.help_archer_round_stats__archer_handicap_body),
                    ),
                    textModifier = Modifier.testTag(ARCHER_HANDICAP_TEXT.getTestTag()),
            )
            DataRow(
                    title = stringResource(R.string.archer_round_stats__allowance),
                    text = state.allowance.toString(),
                    helpState = HelpState(
                            helpListener = helpListener,
                            helpTitle = stringResource(R.string.help_archer_round_stats__allowance_title),
                            helpBody = stringResource(R.string.help_archer_round_stats__allowance_body),
                    ),
                    textModifier = Modifier.testTag(ALLOWANCE_TEXT.getTestTag()),
            )
            if (state.adjustedFinalScore != null) {
                DataRow(
                        title = stringResource(R.string.archer_round_stats__adjusted_score),
                        text = (state.adjustedFinalScore).toString(),
                        helpState = HelpState(
                                helpListener = helpListener,
                                helpTitle = stringResource(R.string.help_archer_round_stats__adjusted_score_title),
                                helpBody = stringResource(R.string.help_archer_round_stats__adjusted_score_body),
                        ),
                        textModifier = Modifier.testTag(ADJUSTED_SCORE_TEXT.getTestTag()),
                )
            }
            else if (state.predictedAdjustedScore != null) {
                DataRow(
                        title = stringResource(R.string.archer_round_stats__predicted_adjusted_score),
                        text = state.predictedAdjustedScore.toString(),
                        helpState = HelpState(
                                helpListener = helpListener,
                                helpTitle = stringResource(R.string.help_archer_round_stats__adjusted_score_title),
                                helpBody = stringResource(R.string.help_archer_round_stats__adjusted_score_body),
                        ),
                        textModifier = Modifier.testTag(ADJUSTED_SCORE_TEXT.getTestTag()),
                )
            }
        }
    }
}

@Composable
private fun PastRecordsSection(
        state: StatsState,
        listener: (StatsIntent) -> Unit,
) {
    if (state.recentPastRoundScores.isNullOrEmpty()) return
    val helpListener = { it: HelpShowcaseIntent -> listener(HelpShowcaseAction(it)) }

    Section {
        Text(
                text = stringResource(R.string.archer_round_stats__past_records),
                style = style().asClickableStyle(),
                modifier = Modifier
                        .clickable { listener(PastRoundRecordsClicked) }
                        .testTag(PAST_RECORDS_LINK_TEXT.getTestTag())
                        .updateHelpDialogPosition(
                                HelpState(
                                        helpListener = helpListener,
                                        helpTitle = stringResource(R.string.help_archer_round_stats__past_records_title),
                                        helpBody = stringResource(R.string.help_archer_round_stats__past_records_body),
                                ),
                        )
        )
    }

    SimpleDialog(
            isShown = state.isPastRoundRecordsDialogOpen,
            onDismissListener = { listener(PastRoundRecordsDismissed) },
    ) {
        SimpleDialogContent(
                title = stringResource(R.string.archer_round_stats__past_records),
                negativeButton = ButtonState(
                        text = stringResource(R.string.general_close),
                        onClick = { listener(PastRoundRecordsDismissed) },
                ),
        ) {
            val records =
                    if (state.pastRoundScoresTab == StatsScreenPastRecordsTabs.RECENT) state.recentPastRoundScores
                    else state.bestPastRoundScores
            val pbScore = state.bestPastRoundScores!![0].score
            val isTied = state.bestPastRoundScores.getOrNull(1)?.score?.let { it == pbScore } ?: false
            val delim = stringResource(R.string.archer_round_stats__past_record_item_delim).let { " $it " }
            Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
            ) {
                CodexTabSwitcher(
                        items = StatsScreenPastRecordsTabs.values().toList(),
                        selectedItem = state.pastRoundScoresTab,
                        itemClickedListener = { listener(StatsIntent.PastRecordsTabClicked(it)) },
                        itemColor = CodexTheme.colors.tabSwitcherOnDialogSelected,
                        dividerColor = CodexTheme.colors.tabSwitcherOnDialogDivider,
                        modifier = Modifier.padding(bottom = 15.dp)
                )

                records!!.forEach { shootRecord ->
                    val isPb = shootRecord.score == pbScore
                    val isCurrentShoot = shootRecord.shootId == state.fullShootInfo.id

                    val text = listOfNotNull(
                            DateTimeFormat.SHORT_DATE.format(shootRecord.dateShot),
                            shootRecord.score.toString(),
                    )

                    val background = when {
                        isPb -> CodexTheme.colors.personalBestTag
                        isCurrentShoot -> CodexTheme.colors.dialogBackgroundAccent
                        else -> null
                    }

                    val extraSemanticTextIds = mutableListOf<Int>()
                    if (isPb) {
                        extraSemanticTextIds.add(
                                if (isTied) R.string.archer_round_stats__is_tied_pb
                                else R.string.archer_round_stats__is_pb
                        )
                    }
                    if (isCurrentShoot) {
                        extraSemanticTextIds.add(R.string.archer_round_stats__past_records_current)
                    }
                    val extraSemanticText = extraSemanticTextIds.map { stringResource(it) }

                    Text(
                            text = text.joinToString(delim),
                            style = style().copy(color = CodexTheme.colors.onDialogBackground),
                            modifier = Modifier
                                    .testTag(PAST_RECORDS_DIALOG_ITEM.getTestTag())
                                    .modifierIf(
                                            predicate = background != null,
                                            modifier = Modifier
                                                    .background(
                                                            color = background ?: Color.Transparent,
                                                            shape = RoundedCornerShape(100),
                                                    )
                                                    .padding(horizontal = 10.dp)
                                    )
                                    .semantics {
                                        contentDescription = text
                                                .plus(extraSemanticText)
                                                .joinToString(delim)
                                    }
                    )
                }
            }
        }
    }
}

@Composable
private fun NumberBreakdownSection(
        state: StatsState,
) {
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
    PAST_RECORDS_LINK_TEXT,
    PAST_RECORDS_DIALOG_ITEM,
    PB_TEXT,
    HANDICAP_TEXT,
    PREDICTED_SCORE_TEXT,
    ARCHER_HANDICAP_TEXT,
    ALLOWANCE_TEXT,
    ADJUSTED_SCORE_TEXT,
    EDIT_SHOOT_INFO,
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
                        main = ShootDetailsState(
                                fullShootInfo = ShootPreviewHelperDsl.create {
                                    addFullSetOfArrows()
                                }
                        ),
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
fun RoundIncomplete_StatsScreen_Preview() {
    CodexTheme {
        StatsScreen(
                StatsState(
                        main = ShootDetailsState(
                                fullShootInfo = ShootPreviewHelperDsl.create {
                                    round = RoundPreviewHelper.outdoorImperialRoundData
                                    addIdenticalArrows(20, 7)
                                    faces = listOf(RoundFace.TRIPLE, RoundFace.FITA_SIX)
                                },
                                archerHandicaps = ArcherHandicapsPreviewHelper.handicaps,
                                pastRoundRecords = listOf(
                                        DatabaseShootShortRecord(2, Calendar.getInstance(), 400, true),
                                        DatabaseShootShortRecord(1, Calendar.getInstance(), 700, true),
                                ),
                        ),
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
fun RoundComplete_StatsScreen_Preview() {
    CodexTheme {
        StatsScreen(
                StatsState(
                        main = ShootDetailsState(
                                fullShootInfo = ShootPreviewHelperDsl.create {
                                    round = RoundPreviewHelper.outdoorImperialRoundData
                                    completeRound(arrowScore = 7, isX = false)
                                    isPersonalBest = true
                                },
                                archerHandicaps = ArcherHandicapsPreviewHelper.handicaps,
                                pastRoundRecords = listOf(
                                        DatabaseShootShortRecord(2, Calendar.getInstance(), 400, true),
                                        DatabaseShootShortRecord(1, Calendar.getInstance(), 700, true),
                                ),
                        ),
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
fun PastRecords_StatsScreen_Preview() {
    CodexTheme {
        StatsScreen(
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
                        extras = StatsExtras(isPastRoundRecordsDialogOpen = true),
                )
        ) {}
    }
}
