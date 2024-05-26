package eywa.projectcodex.components.shootDetails.stats

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.helpShowcase.HelpState
import eywa.projectcodex.common.helpShowcase.asHelpState
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
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelperDsl
import eywa.projectcodex.common.sharedUi.selectRoundFaceDialog.SelectFaceRow
import eywa.projectcodex.common.sharedUi.testTag
import eywa.projectcodex.common.utils.DateTimeFormat
import eywa.projectcodex.common.utils.classificationTables.ClassificationTableEntry
import eywa.projectcodex.common.utils.classificationTables.ClassificationTablesUseCase
import eywa.projectcodex.components.archerHandicaps.ArcherHandicapsPreviewHelper
import eywa.projectcodex.components.shootDetails.ShootDetailsResponse
import eywa.projectcodex.components.shootDetails.ShootDetailsState
import eywa.projectcodex.components.shootDetails.commonUi.HandleMainEffects
import eywa.projectcodex.components.shootDetails.commonUi.ShootDetailsMainScreen
import eywa.projectcodex.components.shootDetails.getData
import eywa.projectcodex.components.shootDetails.stats.StatsIntent.*
import eywa.projectcodex.components.shootDetails.stats.StatsTestTag.*
import eywa.projectcodex.database.RoundFace
import eywa.projectcodex.database.archer.DatabaseArcherPreviewHelper
import eywa.projectcodex.database.bow.DatabaseBowPreviewHelper
import eywa.projectcodex.database.shootData.DatabaseShootShortRecord
import eywa.projectcodex.model.FullShootInfo
import java.util.Calendar

@Composable
private fun style(textAlign: TextAlign = TextAlign.Start) =
        CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground, textAlign = textAlign)

@Composable
fun StatsScreenV2(
        navController: NavController,
        viewModel: StatsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val listener = { it: StatsIntent -> viewModel.handle(it) }

    ShootDetailsMainScreen(
            currentScreen = CodexNavRoute.SHOOT_DETAILS_STATS,
            state = state,
            listener = { listener(ShootDetailsAction(it)) },
    ) { it, modifier -> StatsScreenV2(it, modifier, listener) }

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
            listener(EditArcherInfoHandled)
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
                    ).filter { it.value != null }.mapValues { (_, value) -> value.toString() },
            )
            listener(ExpandClassificationsHandled)
        }
    }
}

@Composable
private fun StatsScreenV2(
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
            NewScoreSectionV2(
                    fullShootInfo = state.fullShootInfo,
                    editClickedListener = { listener(EditShootClicked) },
                    helpListener = helpListener,
            )
            Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                HsgSection(state, helpListener)
                if (state.fullShootInfo.isPersonalBest) {
                    val pbStringId =
                            if (state.fullShootInfo.isTiedPersonalBest) R.string.archer_round_stats__is_tied_pb
                            else R.string.archer_round_stats__is_pb
                    Text(
                            text = stringResource(pbStringId),
                            color = CodexTheme.colors.onPersonalBestTag,
                            style = CodexTypography.NORMAL,
                            modifier = Modifier
                                    .padding(top = 10.dp)
                                    .background(
                                            color = CodexTheme.colors.personalBestTag,
                                            shape = RoundedCornerShape(100)
                                    )
                                    .padding(horizontal = 10.dp)
                                    .testTag(PB_TEXT.getTestTag())
                                    .updateHelpDialogPosition(
                                            helpState = HelpShowcaseItem(
                                                    helpTitle = stringResource(R.string.help_archer_round_stats__personal_best_title),
                                                    helpBody = stringResource(R.string.help_archer_round_stats__personal_best_body),
                                            ).asHelpState(helpListener),
                                    )
                    )
                }
            }
            RoundStatsSection(state, helpListener)
            Divider(thickness = 1.dp, color = CodexTheme.colors.onAppBackground)
            NewRoundStatsSection(state, helpListener)
            PastRecordsSection(state, listener)
//            ClassificationSection(state, listener)
            AllowanceSection(state, listener)

            if (state.useBetaFeatures) {
                NumberBreakdownSection(state)
            }
        }
    }
}

@Composable
private fun EditBox(
        testTag: StatsTestTag,
        editContentDescription: String? = null,
        editHelpState: HelpState? = null,
        editListener: (() -> Unit)? = null,
        expandContentDescription: String? = null,
        expandListener: (() -> Unit)? = null,
        expandHelpState: HelpState? = null,
        content: @Composable () -> Unit,
) {
    Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.testTag(testTag)
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
        if (editListener != null && editContentDescription != null) {
            CodexIconButton(
                    icon = CodexIconInfo.VectorIcon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = editContentDescription,
                    ),
                    onClick = editListener,
                    modifier = Modifier
                            .testTag(EDIT_SHOOT_INFO)
                            .align(Alignment.BottomEnd)
                            .updateHelpDialogPosition(editHelpState)
            )
        }
        if (expandListener != null && expandContentDescription != null) {
            CodexIconButton(
                    icon = CodexIconInfo.VectorIcon(
                            imageVector = Icons.Default.OpenInFull,
                            contentDescription = expandContentDescription,
                            modifier = Modifier.scale(-1f, 1f)
                    ),
                    onClick = expandListener,
                    modifier = Modifier
                            .testTag(EXPAND_SHOOT_INFO)
                            .align(Alignment.BottomStart)
                            .updateHelpDialogPosition(expandHelpState)
            )
        }
    }
}

@Composable
fun NewScoreSectionV2(
        fullShootInfo: FullShootInfo,
        editClickedListener: () -> Unit,
        helpListener: (HelpShowcaseIntent) -> Unit,
) {
    EditBox(
            testTag = SHOOT_DETAIL_SECTION,
            editContentDescription = stringResource(R.string.archer_round_stats__edit_shoot_content_description),
            editListener = editClickedListener,
            editHelpState = HelpShowcaseItem(
                    helpTitle = stringResource(R.string.help_archer_round_stats__edit_round_info_title),
                    helpBody = stringResource(R.string.help_archer_round_stats__edit_round_info_body),
            ).asHelpState(helpListener),
    ) {
        Text(
                text = DateTimeFormat.LONG_DATE_TIME.format(fullShootInfo.shoot.dateShot),
                modifier = Modifier
                        .testTag(DATE_TEXT.getTestTag())
                        .updateHelpDialogPosition(
                                helpState = HelpShowcaseItem(
                                        helpTitle = stringResource(R.string.help_archer_round_stats__date_title),
                                        helpBody = stringResource(R.string.help_archer_round_stats__date_body),
                                ).asHelpState(helpListener),
                        )
        )
        DataRow(
                title = stringResource(R.string.archer_round_stats__round),
                text = fullShootInfo.displayName
                        ?: stringResource(R.string.archer_round_stats__no_round),
                textModifier = Modifier.testTag(ROUND_TEXT.getTestTag()),
                helpState = HelpShowcaseItem(
                        helpTitle = stringResource(R.string.help_archer_round_stats__round_title),
                        helpBody = stringResource(R.string.help_archer_round_stats__round_body),
                ).asHelpState(helpListener),
                titleStyle = CodexTypography.SMALL.copy(color = CodexTheme.colors.onAppBackground),
        )
        ProvideTextStyle(value = CodexTypography.SMALL.copy(color = CodexTheme.colors.onAppBackground)) {
            SelectFaceRow(
                    selectedFaces = fullShootInfo.faces,
                    helpListener = helpListener,
                    onClick = null,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HsgSection(
        state: StatsState,
        helpListener: (HelpShowcaseIntent) -> Unit,
) {
    val hits = state.fullShootInfo.hits
    val arrowsShot = state.fullShootInfo.arrowsShot

    val separator = ":"

    ProvideTextStyle(value = CodexTypography.SMALL.copy(color = CodexTheme.colors.onAppBackground)) {
        FlowRow(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        ) {
            Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                        text = stringResource(R.string.archer_round_stats__hits),
                )
                Text(
                        text = hits.toString(),
                        style = CodexTypography.LARGE,
                        color = CodexTheme.colors.onAppBackground,
                )
                if (hits != arrowsShot) {
                    Text(
                            text = stringResource(
                                    R.string.archer_round_stats__hits_of_2,
                                    arrowsShot,
                            ),
                    )
                }
            }
            Text(
                    text = separator,
                    style = CodexTypography.NORMAL,
                    color = CodexTheme.colors.onAppBackground,
            )
            Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                        text = stringResource(R.string.archer_round_stats__score),
                )
                Text(
                        text = state.fullShootInfo.score.toString(),
                        style = CodexTypography.X_LARGE,
                        color = CodexTheme.colors.onAppBackground,
                        fontWeight = FontWeight.Bold,
                )
            }
            Text(
                    text = separator,
                    style = CodexTypography.NORMAL,
                    color = CodexTheme.colors.onAppBackground,
            )
            Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                        text = stringResource(state.fullShootInfo.goldsType.longStringId),
                )
                Text(
                        text = state.fullShootInfo.golds().toString(),
                        style = CodexTypography.LARGE,
                        color = CodexTheme.colors.onAppBackground,
                )
            }
        }
    }
}

@Composable
private fun RoundStatsSection(
        state: StatsState,
        helpListener: (HelpShowcaseIntent) -> Unit,
) {
    if (state.fullShootInfo.round == null) return

    val remaining = state.fullShootInfo.remainingArrows ?: 0
    ProvideTextStyle(value = CodexTypography.SMALL.copy(color = CodexTheme.colors.onAppBackground)) {
        Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
        ) {
            if (remaining != 0) {
                Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                            text = remaining.toString(),
                            style = CodexTypography.NORMAL,
                            color = CodexTheme.colors.onAppBackground,
                    )
                    Text(
                            text = "arrows"
                    )
                    Text(
                            text = if (remaining > 0) "remaining" else "surplus"
                    )
                }
            }
            if (state.fullShootInfo.predictedScore != null) {
                Text(
                        text = ":",
                        style = CodexTypography.NORMAL,
                        color = CodexTheme.colors.onAppBackground,
                )
                Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (remaining > 0) {
                        Text(
                                text = stringResource(R.string.archer_round_stats__predicted_score)
                        )
                    }
                    Text(
                            text = state.fullShootInfo.predictedScore.toString(),
                            style = CodexTypography.NORMAL,
                            color = CodexTheme.colors.onAppBackground,
                    )
                }
            }
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

    ProvideTextStyle(value = CodexTypography.SMALL.copy(color = CodexTheme.colors.onAppBackground)) {
        Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                        text = stringResource(R.string.archer_round_stats__archer_handicap),
                )
                Row(
                        verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                            text = state.archerHandicap.toString(),
                            style = CodexTypography.LARGE,
                            color = CodexTheme.colors.onAppBackground,
                    )
                    CodexIconButton(
                            icon = CodexIconInfo.VectorIcon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                            ),
                            onClick = { },
                    )
                }
            }
            Text(
                    text = ":",
                    style = CodexTypography.NORMAL,
                    color = CodexTheme.colors.onAppBackground,
            )
            Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                        text = stringResource(R.string.archer_round_stats__allowance),
                )
                Text(
                        text = state.allowance.toString(),
                        style = CodexTypography.NORMAL,
                        color = CodexTheme.colors.onAppBackground,
                )
            }
            if ((state.adjustedFinalScore ?: state.predictedAdjustedScore) != null) {
                Text(
                        text = ":",
                        style = CodexTypography.NORMAL,
                        color = CodexTheme.colors.onAppBackground,
                )
                Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                            text = "Adjusted score",
                    )
                    Text(
                            text = (state.adjustedFinalScore ?: state.predictedAdjustedScore).toString(),
                            style = CodexTypography.NORMAL,
                            color = CodexTheme.colors.onAppBackground,
                    )
                    if (state.adjustedFinalScore == null) {
                        Text(
                                text = "predicted",
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClassificationSection(
        state: StatsState,
        listener: (StatsIntent) -> Unit,
) {
    if (
        state.fullShootInfo.round == null
        || state.fullShootInfo.arrowsShot == 0
        || state.archerInfo == null
        || state.bow == null
    ) return
    val helpListener = { it: HelpShowcaseIntent -> listener(HelpShowcaseAction(it)) }

    val unofficialSuffix =
            if (state.classification?.second == true) ""
            else stringResource(R.string.archer_round_stats__archer_info_classification_unofficial).let { " $it" }

    Section {
        EditBox(
                testTag = CLASSIFICATION_SECTION,
                editContentDescription = stringResource(R.string.archer_round_stats__archer_info_edit),
                editListener = { listener(EditArcherInfoClicked) },
                editHelpState = HelpShowcaseItem(
                        helpTitle = stringResource(R.string.help_archer_round_stats__edit_archer_info_handicaps_title),
                        helpBody = stringResource(R.string.help_archer_round_stats__edit_archer_info_handicaps_body),
                ).asHelpState(helpListener),
                expandContentDescription = stringResource(R.string.archer_round_stats__archer_info_expand),
                expandListener = { listener(ExpandClassificationsClicked) },
                expandHelpState = HelpShowcaseItem(
                        helpTitle = stringResource(R.string.help_archer_round_stats__expand_handicaps_title),
                        helpBody = stringResource(R.string.help_archer_round_stats__expand_handicaps_body),
                ).asHelpState(helpListener),
        ) {
            DataRow(
                    title = stringResource(R.string.archer_round_stats__archer_info_category),
                    text = listOf(
                            state.archerInfo.age.rawName,
                            state.archerInfo.genderString.get(),
                            state.bow.type.rawName,
                    ).joinToString(" "),
                    helpState = HelpShowcaseItem(
                            helpTitle = stringResource(R.string.help_archer_round_stats__archer_info_title),
                            helpBody = stringResource(R.string.help_archer_round_stats__archer_info_body),
                    ).asHelpState(helpListener),
                    textModifier = Modifier.testTag(CLASSIFICATION_CATEGORY.getTestTag()),
            )
            DataRow(
                    title = stringResource(
                            if (state.fullShootInfo.isRoundComplete) {
                                R.string.archer_round_stats__archer_info_classification
                            }
                            else {
                                R.string.archer_round_stats__archer_info_classification_predicted
                            }
                    ),
                    text = state.classification?.first?.fullStringId?.get()
                            ?.plus(unofficialSuffix)
                            ?: stringResource(R.string.archer_round_stats__archer_info_classification_none),
                    helpState = HelpShowcaseItem(
                            helpTitle = stringResource(R.string.help_archer_round_stats__archer_info_classification_title),
                            helpBody = stringResource(R.string.help_archer_round_stats__archer_info_classification_body),
                    ).asHelpState(helpListener),
                    textModifier = Modifier.testTag(CLASSIFICATION.getTestTag()),
            )
        }
    }
}

@Composable
private fun PastRecordsSection(
        state: StatsState,
        listener: (StatsIntent) -> Unit,
) {
    if (state.pastRoundScores.isNullOrEmpty()) return
    val helpListener = { it: HelpShowcaseIntent -> listener(HelpShowcaseAction(it)) }

    Section {
        Text(
                text = stringResource(R.string.archer_round_stats__past_records),
                style = style().asClickableStyle(),
                modifier = Modifier
                        .clickable { listener(PastRoundRecordsClicked) }
                        .testTag(PAST_RECORDS_LINK_TEXT.getTestTag())
                        .updateHelpDialogPosition(
                                helpState = HelpShowcaseItem(
                                        helpTitle = stringResource(R.string.help_archer_round_stats__past_records_title),
                                        helpBody = stringResource(R.string.help_archer_round_stats__past_records_body),
                                ).asHelpState(helpListener),
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
            Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(25.dp),
                    modifier = Modifier.fillMaxWidth()
            ) {
                CodexTabSwitcher(
                        items = StatsScreenPastRecordsTabs.values().toList(),
                        selectedItem = state.pastRoundScoresTab,
                        itemClickedListener = { listener(PastRecordsTabClicked(it)) },
                        itemColor = CodexTheme.colors.tabSwitcherOnDialogSelected,
                        dividerColor = CodexTheme.colors.tabSwitcherOnDialogDivider,
                        modifier = Modifier.testTag(PAST_RECORDS_DIALOG_TAB)
                )

                Crossfade(
                        targetState = state.pastRoundScores,
                        label = "recentBestRecordsTextFade"
                ) { records ->
                    Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                    ) {
                        records.forEach { PastScore(state, it) }
                    }
                }
            }
        }
    }
}


@Composable
private fun PastScore(
        state: StatsState,
        shootRecord: DatabaseShootShortRecord,
) {
    val delim = stringResource(R.string.archer_round_stats__past_record_item_delim).let { " $it " }

    val isPb = shootRecord.score == state.pastRoundScoresPb
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
                if (state.pastRoundScoresPbIsTied) R.string.archer_round_stats__is_tied_pb
                else R.string.archer_round_stats__is_pb
        )
    }
    if (isCurrentShoot) {
        extraSemanticTextIds.add(R.string.archer_round_stats__past_records_current)
    }
    if (!shootRecord.isComplete) {
        extraSemanticTextIds.add(R.string.archer_round_stats__past_records_incomplete)
    }
    val extraSemanticText = extraSemanticTextIds.map { stringResource(it) }

    Text(
            text = text.joinToString(delim),
            style = style().copy(color = CodexTheme.colors.onDialogBackground),
            textDecoration = (
                    if (shootRecord.isComplete) TextDecoration.None else TextDecoration.LineThrough
                    ),
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

@Composable
private fun NewRoundStatsSection(
        state: StatsState,
        helpListener: (HelpShowcaseIntent) -> Unit,
) {
    ProvideTextStyle(value = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground)) {
        Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.horizontalScroll(rememberScrollState())
//                horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                        text = "Round Handicap",
                        style = CodexTypography.SMALL,
                        color = CodexTheme.colors.onAppBackground,
                )
                Row(
                        verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                            text = state.fullShootInfo.handicap.toString(),
                            style = CodexTypography.LARGE,
                            color = CodexTheme.colors.onAppBackground,
                    )
                    CodexIconButton(
                            icon = CodexIconInfo.VectorIcon(
                                    imageVector = Icons.Default.OpenInFull,
                                    contentDescription = null,
                            ),
                            onClick = { },
                    )
                }
            }
            Text(
                    text = ":",
                    modifier = Modifier.padding(horizontal = 10.dp)
            )
            Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                        text = "Classification (predicted, unofficial)",
                        textAlign = TextAlign.Center,
                        style = CodexTypography.SMALL,
                        color = CodexTheme.colors.onAppBackground,
                )
                Text(
                        text = listOf(
                                state.archerInfo?.age?.rawName,
                                state.archerInfo?.genderString?.get(),
                                state.bow?.type?.rawName,
                        ).joinToString(" "),
                        textAlign = TextAlign.Center,
                        style = CodexTypography.SMALL.asClickableStyle(),
//                        color = CodexTheme.colors.onAppBackground,
                )
                Row(
                        verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                            text = state.classification?.first?.shortStringId?.get() ?: "",
                            textAlign = TextAlign.Center,
                            style = CodexTypography.NORMAL_PLUS,
                            color = CodexTheme.colors.onAppBackground,
                    )
                    CodexIconButton(
                            icon = CodexIconInfo.VectorIcon(
                                    imageVector = Icons.Default.OpenInFull,
                                    contentDescription = null,
                            ),
                            onClick = { },
                    )
                }
            }
        }
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun NoRound_StatsScreenV2_Preview() {
    CodexTheme {
        StatsScreenV2(
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
)
@Composable
fun RoundIncomplete_StatsScreenV2_Preview() {
    CodexTheme {
        StatsScreenV2(
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
fun RoundComplete_StatsScreenV2_Preview() {
    CodexTheme {
        StatsScreenV2(
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
fun PastRecords_StatsScreenV2_Preview() {
    CodexTheme {
        StatsScreenV2(
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
