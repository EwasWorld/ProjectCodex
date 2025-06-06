package eywa.projectcodex.components.shootDetails.headToHead.addMatch

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.helpShowcase.asHelpState
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.navigation.DEFAULT_INT_NAV_ARG
import eywa.projectcodex.common.navigation.NavArgument
import eywa.projectcodex.common.sharedUi.ButtonState
import eywa.projectcodex.common.sharedUi.ChipColours
import eywa.projectcodex.common.sharedUi.CodexButton
import eywa.projectcodex.common.sharedUi.CodexChip
import eywa.projectcodex.common.sharedUi.CodexIconButton
import eywa.projectcodex.common.sharedUi.CodexIconInfo
import eywa.projectcodex.common.sharedUi.CodexTextField
import eywa.projectcodex.common.sharedUi.DataRow
import eywa.projectcodex.common.sharedUi.SimpleDialog
import eywa.projectcodex.common.sharedUi.SimpleDialogContent
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.numberField.CodexLabelledNumberFieldWithErrorMessage
import eywa.projectcodex.common.sharedUi.testTag
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.components.referenceTables.headToHead.HeadToHeadUseCase
import eywa.projectcodex.components.shootDetails.addEnd.SightMark
import eywa.projectcodex.components.shootDetails.commonUi.HandleMainEffects
import eywa.projectcodex.components.shootDetails.commonUi.ShootDetailsMainScreen
import eywa.projectcodex.components.shootDetails.getData
import eywa.projectcodex.components.shootDetails.headToHead.HeadToHeadResult
import eywa.projectcodex.components.shootDetails.headToHead.addEnd.HeadToHeadRoundInfo
import eywa.projectcodex.components.shootDetails.headToHead.addMatch.HeadToHeadAddMatchIntent.*
import eywa.projectcodex.components.sightMarks.SightMarksPreviewHelper
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHeadMatch
import eywa.projectcodex.model.SightMark

@Composable
fun HeadToHeadAddMatchScreen(
        navController: NavController,
        viewModel: HeadToHeadAddMatchViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val listener = { it: HeadToHeadAddMatchIntent -> viewModel.handle(it) }

    val data = state.getData()

    ShootDetailsMainScreen(
            currentScreen = CodexNavRoute.HEAD_TO_HEAD_ADD_MATCH
                    .takeIf { data?.editing == null && data?.isInserting != true },
            state = state,
            listener = { listener(ShootDetailsAction(it)) },
    ) { it, modifier -> HeadToHeadAddMatchScreen(it, modifier, listener) }

    HandleMainEffects(
            navController = navController,
            state = state,
            listener = { listener(ShootDetailsAction(it)) },
    )

    LaunchedEffect(
            data?.extras?.openAllSightMarks,
            data?.extras?.openEditSightMark,
            data?.extras?.openAddEndScreenForMatch,
            data?.extras?.pressBack,
    ) {
        if (data != null) {
            if (data.extras.openAllSightMarks) {
                CodexNavRoute.SIGHT_MARKS.navigate(navController)
                listener(ExpandSightMarkHandled)
            }

            if (data.extras.openEditSightMark) {
                val args = if (data.roundInfo?.sightMark != null) {
                    mapOf(NavArgument.SIGHT_MARK_ID to data.roundInfo.sightMark.id.toString())
                }
                else {
                    val distance = data.roundInfo?.distance ?: DEFAULT_INT_NAV_ARG
                    val isMetric = data.roundInfo?.isMetric ?: true
                    mapOf(NavArgument.DISTANCE to distance.toString(), NavArgument.IS_METRIC to isMetric.toString())
                }
                CodexNavRoute.SIGHT_MARK_DETAIL.navigate(navController, args)
                listener(EditSightMarkHandled)
            }

            if (data.extras.openAddEndScreenForMatch != null) {
                CodexNavRoute.HEAD_TO_HEAD_ADD_END.navigate(
                        navController,
                        mapOf(
                                NavArgument.SHOOT_ID to viewModel.shootId.toString(),
                                NavArgument.MATCH_NUMBER to data.extras.openAddEndScreenForMatch.toString(),
                        ),
                        popCurrentRoute = true,
                )
                listener(OpenAddEndScreenHandled)
            }

            if (data.extras.pressBack) {
                navController.popBackStack()
                listener(BackPressedHandled)
            }
        }
    }
}

@Composable
fun HeadToHeadAddMatchScreen(
        state: HeadToHeadAddMatchState,
        modifier: Modifier = Modifier,
        listener: (HeadToHeadAddMatchIntent) -> Unit,
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(HelpShowcaseAction(it)) }

    ProvideTextStyle(CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground)) {
        Column(
                verticalArrangement = Arrangement.spacedBy(40.dp, alignment = Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = modifier
                        .background(CodexTheme.colors.appBackground)
                        .padding(vertical = CodexTheme.dimens.screenPadding)
                        .testTag(HeadToHeadAddMatchTestTag.SCREEN)
        ) {
            if (state.roundInfo != null && state.editing == null && !state.isInserting) {
                SightMark(
                        distance = state.roundInfo.distance,
                        isMetric = state.roundInfo.isMetric,
                        sightMark = state.roundInfo.sightMark,
                        helpListener = helpListener,
                        onExpandClicked = { listener(ExpandSightMarkClicked) },
                        onEditClicked = { listener(EditSightMarkClicked) },
                )
            }

            PreviousMatch(state)

            Surface(
                    shape = RoundedCornerShape(CodexTheme.dimens.cornerRounding),
                    border = BorderStroke(1.dp, CodexTheme.colors.listItemOnAppBackground),
                    color = CodexTheme.colors.appBackground,
                    modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = CodexTheme.dimens.screenPadding)
            ) {
                MatchDetails(
                        state = state,
                        listener = { listener(it) },
                        modifier = Modifier.padding(vertical = 20.dp, horizontal = 25.dp)
                )
            }
        }
    }
}

@Composable
private fun PreviousMatch(
        state: HeadToHeadAddMatchState,
        modifier: Modifier = Modifier,
) {
    if (state.previousMatch == null) return
    Surface(
            border = BorderStroke(1.dp, CodexTheme.colors.listItemOnAppBackground),
            color = CodexTheme.colors.appBackground,
            modifier = modifier.padding(horizontal = CodexTheme.dimens.screenPadding)
    ) {
        Column(
                verticalArrangement = Arrangement.spacedBy(5.dp, alignment = Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 20.dp, horizontal = 25.dp)
        ) {
            Text(
                    text = if (state.previousMatch.heat != null) {
                        stringResource(
                                R.string.head_to_head_add_heat__match_header_with_heat,
                                state.previousMatch.matchNumber,
                                HeadToHeadUseCase.shortHeatName(state.previousMatch.heat).get(),
                        )
                    }
                    else {
                        stringResource(
                                R.string.head_to_head_add_heat__match_header,
                                state.previousMatch.matchNumber,
                        )
                    },
                    modifier = Modifier.testTag(HeadToHeadAddMatchTestTag.PREVIOUS_MATCH_INFO)
            )

            val setScore =
                    if (state.previousMatch.runningTotal != null && !state.previousMatch.isBye) {
                        stringResource(
                                R.string.head_to_head_add_end__score_text,
                                state.previousMatch.runningTotal.first,
                                state.previousMatch.runningTotal.second,
                        )
                    }
                    else {
                        stringResource(R.string.head_to_head_add_heat__result)
                    }
            val result =
                    if (state.previousMatch.isBye) {
                        stringResource(R.string.head_to_head_score_pad__is_bye)
                    }
                    else {
                        state.previousMatch.result.title.get()
                    }

            DataRow(
                    title = setScore,
                    text = result,
                    modifier = Modifier.testTag(HeadToHeadAddMatchTestTag.PREVIOUS_MATCH_RESULT)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MatchDetails(
        state: HeadToHeadAddMatchState,
        modifier: Modifier = Modifier,
        listener: (HeadToHeadAddMatchIntent) -> Unit,
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(HelpShowcaseAction(it)) }

    SimpleDialog(
            isShown = state.extras.showSelectHeatDialog,
            onDismissListener = { listener(CloseSelectHeatDialog) },
    ) {
        SimpleDialogContent(
                title = stringResource(R.string.head_to_head_add_heat__heat_match),
                positiveButton = ButtonState(
                        text = stringResource(R.string.head_to_head_add_heat__heat_no_heat_button),
                        onClick = { listener(SelectHeatDialogItemClicked(null)) },
                ),
                negativeButton = ButtonState(
                        text = stringResource(R.string.general_cancel),
                        onClick = { listener(CloseSelectHeatDialog) },
                ),
        ) {
            Column {
                (0..HeadToHeadUseCase.MAX_HEAT).forEach { heat ->
                    Text(
                            text = HeadToHeadUseCase.heatName(heat).get(),
                            style = CodexTypography.NORMAL,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                    .clickable { listener(SelectHeatDialogItemClicked(heat)) }
                                    .fillMaxWidth()
                                    .padding(10.dp)
                                    .testTag(HeadToHeadAddMatchTestTag.HEAT_SELECTOR_DIALOG_ITEM)
                    )
                }
            }
        }
    }

    Column(
            verticalArrangement = Arrangement.spacedBy(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
    ) {
        Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically,
        ) {
            DataRow(
                    title = stringResource(R.string.head_to_head_add_heat__heat, state.extras.matchNumber),
                    text = state.extras.heat?.let { HeadToHeadUseCase.shortHeatName(it).get() }
                            ?: stringResource(R.string.head_to_head_add_heat__heat_null),
                    onClick = { listener(HeatClicked) },
                    modifier = Modifier.testTag(HeadToHeadAddMatchTestTag.HEAT)
            )
            CodexChip(
                    text = stringResource(R.string.head_to_head_ref__bye),
                    selected = state.extras.isBye,
                    testTag = HeadToHeadAddMatchTestTag.IS_BYE_CHECKBOX,
                    onToggle = { listener(ToggleIsBye) },
                    colours = ChipColours.Defaults.onPrimary(),
            )
        }
        AnimatedVisibility(
                visible = state.editingMatchWithSetsToBye,
                enter = expandIn(expandFrom = Alignment.TopCenter) + fadeIn(),
                exit = shrinkOut(shrinkTowards = Alignment.TopCenter) + fadeOut(),
        ) {
            Text(
                    text = stringResource(R.string.head_to_head_add_heat__bye_with_sets_warning),
                    color = CodexTheme.colors.warningOnAppBackground,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                            .padding(bottom = 3.dp)
                            .testTag(HeadToHeadAddMatchTestTag.BYE_WITH_SETS_WARNING_TEXT)
            )
        }

        DataRow(
                title = stringResource(R.string.head_to_head_add_heat__opponent),
                modifier = Modifier.padding(vertical = 10.dp)
        ) {
            CodexTextField(
                    text = state.extras.opponent,
                    enabled = !state.extras.isBye,
                    onValueChange = { listener(OpponentUpdated(it)) },
                    placeholderText = stringResource(R.string.head_to_head_add_heat__opponent_placeholder),
                    modifier = Modifier.testTag(HeadToHeadAddMatchTestTag.OPPONENT_INPUT)
            )
        }
        CodexLabelledNumberFieldWithErrorMessage(
                title = stringResource(R.string.head_to_head_add_heat__opponent_quali_rank),
                enabled = !state.extras.isBye,
                currentValue = state.extras.opponentQualiRank.text,
                errorMessage = state.extras.opponentQualiRank.error,
                fieldTestTag = HeadToHeadAddMatchTestTag.OPPONENT_QUALI_RANK_INPUT,
                errorMessageTestTag = HeadToHeadAddMatchTestTag.OPPONENT_QUALI_RANK_ERROR,
                placeholder = stringResource(R.string.head_to_head_add_heat__quali_rank_placeholder),
                onValueChanged = { listener(OpponentQualiRankUpdated(it)) },
        )
        CodexLabelledNumberFieldWithErrorMessage(
                title = stringResource(R.string.head_to_head_add_heat__max_rank),
                currentValue = state.extras.maxPossibleRank.text,
                errorMessage = state.extras.maxPossibleRank.error,
                fieldTestTag = HeadToHeadAddMatchTestTag.MAX_RANK_INPUT,
                errorMessageTestTag = HeadToHeadAddMatchTestTag.MAX_RANK_ERROR,
                placeholder = stringResource(R.string.head_to_head_add_heat__quali_rank_placeholder),
                onValueChanged = { listener(MaxPossibleRankUpdated(it)) },
        )

        if (state.editing == null)
            CodexButton(
                    text = stringResource(
                            if (state.isInserting) R.string.head_to_head_add_heat__submit_insert
                            else R.string.head_to_head_add_heat__submit
                    ),
                    onClick = { listener(SubmitClicked) },
                    helpState = null,
                    modifier = Modifier
                            .padding(top = 8.dp)
                            .testTag(HeadToHeadAddMatchTestTag.SAVE_BUTTON)
            )
        else {
            EditButtons(
                    state = state,
                    listener = listener,
                    modifier = Modifier.padding(top = 10.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EditButtons(
        state: HeadToHeadAddMatchState,
        modifier: Modifier = Modifier,
        listener: (HeadToHeadAddMatchIntent) -> Unit,
) {
    if (state.editing == null) return
    val helpListener = { it: HelpShowcaseIntent -> listener(HelpShowcaseAction(it)) }
    var isDeleteConfirmationShown by remember { mutableStateOf(false) }

    SimpleDialog(
            isShown = isDeleteConfirmationShown,
            onDismissListener = { isDeleteConfirmationShown = false },
    ) {
        SimpleDialogContent(
                title = stringResource(R.string.head_to_head_add_heat__delete_dialog_title),
                message = stringResource(
                        R.string.head_to_head_add_heat__delete_dialog_body,
                        state.editing.matchNumber.toString()
                ),
                positiveButton = ButtonState(
                        text = stringResource(R.string.general_delete),
                        onClick = { listener(DeleteClicked) },
                ),
                negativeButton = ButtonState(
                        text = stringResource(R.string.general_cancel),
                        onClick = { isDeleteConfirmationShown = false },
                ),
        )
    }

    FlowRow(
            horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
            modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
    ) {
        CodexIconButton(
                icon = CodexIconInfo.VectorIcon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.general_delete),
                ),
                captionBelow = stringResource(R.string.general_delete),
                onClick = { isDeleteConfirmationShown = true },
                helpState = HelpShowcaseItem(
                        helpTitle = stringResource(R.string.help_head_to_head_add_heat__delete_title),
                        helpBody = stringResource(R.string.help_head_to_head_add_heat__delete_body),
                ).asHelpState(helpListener),
                modifier = Modifier
                        .testTag(HeadToHeadAddMatchTestTag.DELETE_BUTTON)
                        .align(Alignment.CenterVertically)
        )
        CodexIconButton(
                icon = CodexIconInfo.VectorIcon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.general__reset_edits),
                ),
                captionBelow = stringResource(R.string.general__reset_edits),
                onClick = { listener(ResetClicked) },
                helpState = HelpShowcaseItem(
                        helpTitle = stringResource(R.string.help_sight_marks__reset_title),
                        helpBody = stringResource(R.string.help_sight_marks__reset_body),
                ).asHelpState(helpListener),
                modifier = Modifier
                        .testTag(HeadToHeadAddMatchTestTag.RESET_BUTTON)
                        .align(Alignment.CenterVertically)
        )
        CodexIconButton(
                icon = CodexIconInfo.VectorIcon(
                        imageVector = Icons.Default.Check,
                        contentDescription = stringResource(R.string.general_save),
                ),
                captionBelow = stringResource(R.string.general_save),
                onClick = { listener(SubmitClicked) },
                helpState = HelpShowcaseItem(
                        helpTitle = stringResource(R.string.help_head_to_head_add_heat__save_title),
                        helpBody = stringResource(R.string.help_head_to_head_add_heat__save_body),
                ).asHelpState(helpListener),
                modifier = Modifier
                        .testTag(HeadToHeadAddMatchTestTag.SAVE_BUTTON)
                        .align(Alignment.CenterVertically)
        )
    }
}

enum class HeadToHeadAddMatchTestTag : CodexTestTag {
    SCREEN,
    HEAT,
    OPPONENT_INPUT,
    OPPONENT_QUALI_RANK_INPUT,
    OPPONENT_QUALI_RANK_ERROR,
    MAX_RANK_INPUT,
    MAX_RANK_ERROR,
    IS_BYE_CHECKBOX,
    BYE_WITH_SETS_WARNING_TEXT,
    HEAT_SELECTOR_DIALOG_ITEM,
    PREVIOUS_MATCH_INFO,
    PREVIOUS_MATCH_RESULT,
    DELETE_BUTTON,
    RESET_BUTTON,
    SAVE_BUTTON,
    ;

    override val screenName: String
        get() = "HEAD_TO_HEAD_ADD_MATCH"

    override fun getElement(): String = name
}

@Preview
@Composable
fun HeadToHeadAddMatchScreen_Preview() {
    CodexTheme {
        HeadToHeadAddMatchScreen(
                state = HeadToHeadAddMatchState(
                        previousMatch = HeadToHeadAddMatchState.PreviousMatch(
                                matchNumber = 1,
                                heat = 0,
                                result = HeadToHeadResult.WIN,
                                runningTotal = 6 to 0,
                                isBye = false,
                        ),
                        roundInfo = SightMark(SightMarksPreviewHelper.sightMarks[0]).let {
                            HeadToHeadRoundInfo(sightMark = it, distance = it.distance, isMetric = it.isMetric)
                        },
                ),
        ) {}
    }
}

@Preview
@Composable
fun Insert_HeadToHeadAddMatchScreen_Preview() {
    CodexTheme {
        HeadToHeadAddMatchScreen(
                state = HeadToHeadAddMatchState(
                        roundInfo = SightMark(SightMarksPreviewHelper.sightMarks[0]).let {
                            HeadToHeadRoundInfo(sightMark = it, distance = it.distance, isMetric = it.isMetric)
                        },
                        isInserting = true,
                ),
        ) {}
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun Editing_HeadToHeadAddMatchScreen_Preview() {
    CodexTheme {
        HeadToHeadAddMatchScreen(
                state = HeadToHeadAddMatchState(
                        editing = DatabaseHeadToHeadMatch(
                                shootId = 0,
                                matchNumber = 1,
                                heat = 0,
                                opponent = null,
                                opponentQualificationRank = null,
                                sightersCount = 0,
                                isBye = false,
                                maxPossibleRank = 1,
                        ),
                        extras = HeadToHeadAddMatchExtras(
                                heat = 0,
                                opponent = "",
                                isBye = false
                        ),
                        editingMatchWithSetsToBye = true,
                ),
        ) {}
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun Bye_HeadToHeadAddMatchScreen_Preview() {
    CodexTheme {
        HeadToHeadAddMatchScreen(
                state = HeadToHeadAddMatchState(
                        extras = HeadToHeadAddMatchExtras(isBye = true),
                ),
        ) {}
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
        widthDp = 300,
)
@Composable
fun Short_HeadToHeadAddMatchScreen_Preview() {
    CodexTheme {
        HeadToHeadAddMatchScreen(state = HeadToHeadAddMatchState()) {}
    }
}
