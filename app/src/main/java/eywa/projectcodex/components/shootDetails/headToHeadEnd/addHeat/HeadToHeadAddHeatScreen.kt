package eywa.projectcodex.components.shootDetails.headToHeadEnd.addHeat

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
import androidx.compose.material.LocalTextStyle
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
import eywa.projectcodex.common.sharedUi.codexTheme.asClickableStyle
import eywa.projectcodex.common.sharedUi.numberField.CodexLabelledNumberFieldWithErrorMessage
import eywa.projectcodex.common.sharedUi.testTag
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.components.archerHandicaps.ArcherHandicapsTestTag
import eywa.projectcodex.components.referenceTables.headToHead.HeadToHeadUseCase
import eywa.projectcodex.components.shootDetails.addEnd.SightMark
import eywa.projectcodex.components.shootDetails.commonUi.HandleMainEffects
import eywa.projectcodex.components.shootDetails.commonUi.ShootDetailsMainScreen
import eywa.projectcodex.components.shootDetails.getData
import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadResult
import eywa.projectcodex.components.shootDetails.headToHeadEnd.addEnd.HeadToHeadRoundInfo
import eywa.projectcodex.components.shootDetails.headToHeadEnd.addHeat.HeadToHeadAddHeatIntent.*
import eywa.projectcodex.components.sightMarks.SightMarksPreviewHelper
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHeadHeat
import eywa.projectcodex.model.SightMark

@Composable
fun HeadToHeadAddHeatScreen(
        navController: NavController,
        viewModel: HeadToHeadAddHeatViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val listener = { it: HeadToHeadAddHeatIntent -> viewModel.handle(it) }

    ShootDetailsMainScreen(
            currentScreen = CodexNavRoute.HEAD_TO_HEAD_ADD_HEAT,
            state = state,
            listener = { listener(ShootDetailsAction(it)) },
    ) { it, modifier -> HeadToHeadAddHeatScreen(it, modifier, listener) }

    HandleMainEffects(
            navController = navController,
            state = state,
            listener = { listener(ShootDetailsAction(it)) },
    )

    val data = state.getData()
    LaunchedEffect(
            data?.extras?.openAllSightMarks,
            data?.extras?.openEditSightMark,
            data?.extras?.openAddEndScreen,
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

            if (data.extras.openAddEndScreen) {
                CodexNavRoute.HEAD_TO_HEAD_ADD_END.navigate(
                        navController,
                        mapOf(NavArgument.SHOOT_ID to viewModel.shootId.toString()),
                        popCurrentRoute = true,
                )
                listener(OpenAddEndScreenHandled)
            }
        }
    }
}

@Composable
fun HeadToHeadAddHeatScreen(
        state: HeadToHeadAddHeatState,
        modifier: Modifier = Modifier,
        listener: (HeadToHeadAddHeatIntent) -> Unit,
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(HelpShowcaseAction(it)) }

    ProvideTextStyle(CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground)) {
        Column(
                verticalArrangement = Arrangement.spacedBy(40.dp, alignment = Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = modifier
                        .background(CodexTheme.colors.appBackground)
                        .padding(vertical = CodexTheme.dimens.screenPadding)
        ) {
            if (state.roundInfo != null) {
                SightMark(
                        distance = state.roundInfo.distance,
                        isMetric = state.roundInfo.isMetric,
                        sightMark = state.roundInfo.sightMark,
                        helpListener = helpListener,
                        onExpandClicked = { listener(ExpandSightMarkClicked) },
                        onEditClicked = { listener(EditSightMarkClicked) },
                )
            }

            PreviousHeat(state)
            EditingHeat(state)

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
private fun PreviousHeat(
        state: HeadToHeadAddHeatState,
        modifier: Modifier = Modifier,
) {
    if (state.previousHeat == null) return
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
                    text = if (state.previousHeat.heat != null) {
                        stringResource(
                                R.string.head_to_head_add_heat__match_header_with_heat,
                                state.previousHeat.matchNumber,
                                HeadToHeadUseCase.shortRoundName(state.previousHeat.heat).get(),
                        )
                    }
                    else {
                        stringResource(
                                R.string.head_to_head_add_heat__match_header,
                                state.previousHeat.matchNumber,
                        )
                    },
            )
            val setScore =
                    if (state.previousHeat.runningTotal != null) {
                        stringResource(
                                R.string.head_to_head_add_end__score_text,
                                state.previousHeat.runningTotal.first,
                                state.previousHeat.runningTotal.second,
                        )
                    }
                    else {
                        stringResource(R.string.head_to_head_add_heat__result)
                    }
            DataRow(
                    title = setScore,
                    text = state.previousHeat.result.title.get(),
            )
        }
    }
}

@Composable
private fun EditingHeat(
        state: HeadToHeadAddHeatState,
        modifier: Modifier = Modifier,
) {
    if (state.editing != null) {
        Text(
                text = stringResource(
                        R.string.head_to_head_add_heat__editing,
                        state.editing.matchNumber,
                ),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MatchDetails(
        state: HeadToHeadAddHeatState,
        modifier: Modifier = Modifier,
        listener: (HeadToHeadAddHeatIntent) -> Unit,
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(HelpShowcaseAction(it)) }

    SimpleDialog(
            isShown = state.extras.showSelectHeatDialog,
            onDismissListener = { listener(CloseSelectHeatDialog) },
    ) {
        SimpleDialogContent(
                title = stringResource(R.string.head_to_head_add_heat__heat_match),
                negativeButton = ButtonState(
                        text = stringResource(R.string.general_cancel),
                        onClick = { listener(CloseSelectHeatDialog) },
                ),
        ) {
            Column {
                (0..HeadToHeadUseCase.MAX_HEAT).forEach { heat ->
                    Text(
                            text = HeadToHeadUseCase.roundName(heat).get(),
                            style = CodexTypography.NORMAL,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                    .clickable { listener(SelectHeatDialogItemClicked(heat)) }
                                    .fillMaxWidth()
                                    .padding(10.dp)
                                    .testTag(HeadToHeadAddHeatTestTag.SELECTOR_DIALOG_ITEM)
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
                    title = stringResource(R.string.head_to_head_add_heat__heat, state.matchNumber),
            ) {
                val requiredErrorString = stringResource(R.string.err__required_field)
                Text(
                        text = state.extras.heat?.let { HeadToHeadUseCase.shortRoundName(it).get() }
                                ?: stringResource(R.string.head_to_head_add_heat__heat_null),
                        style = LocalTextStyle.current
                                .asClickableStyle()
                                .copy(color = CodexTheme.colors.linkText),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                                .clickable { listener(HeatClicked) }
                                .align(Alignment.CenterVertically)
                )
            }
            CodexChip(
                    text = stringResource(R.string.head_to_head_ref__bye),
                    selected = state.extras.isBye,
                    testTag = HeadToHeadAddHeatTestTag.IS_BYE_CHECKBOX,
                    onToggle = { listener(ToggleIsBye) },
                    colours = ChipColours.Defaults.onPrimary(),
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
            )
        }
        CodexLabelledNumberFieldWithErrorMessage(
                title = stringResource(R.string.head_to_head_add_heat__opponent_quali_rank),
                enabled = !state.extras.isBye,
                currentValue = state.extras.opponentQualiRank.text,
                fieldTestTag = HeadToHeadAddHeatTestTag.OPPONENT_QUALI_RANK_INPUT,
                errorMessageTestTag = HeadToHeadAddHeatTestTag.OPPONENT_QUALI_RANK_ERROR,
                placeholder = stringResource(R.string.head_to_head_add_heat__quali_rank_placeholder),
                onValueChanged = { listener(OpponentQualiRankUpdated(it)) },
        )
        CodexLabelledNumberFieldWithErrorMessage(
                title = stringResource(R.string.head_to_head_add_heat__max_rank),
                currentValue = state.extras.maxPossibleRank.text,
                fieldTestTag = HeadToHeadAddHeatTestTag.MAX_RANK_INPUT,
                errorMessageTestTag = HeadToHeadAddHeatTestTag.MAX_RANK_ERROR,
                placeholder = stringResource(R.string.head_to_head_add_heat__quali_rank_placeholder),
                onValueChanged = { listener(MaxPossibleRankUpdated(it)) },
        )

        if (state.editing == null)
            CodexButton(
                    text = stringResource(R.string.head_to_head_add_heat__submit),
                    onClick = { listener(SubmitClicked) },
                    helpState = null,
                    modifier = Modifier
                            .padding(top = 8.dp)
                            .testTag(ArcherHandicapsTestTag.ADD_HANDICAP_SUBMIT)
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
        state: HeadToHeadAddHeatState,
        modifier: Modifier = Modifier,
        listener: (HeadToHeadAddHeatIntent) -> Unit,
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
                        .testTag(HeadToHeadAddHeatTestTag.DELETE_BUTTON)
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
                        .testTag(HeadToHeadAddHeatTestTag.RESET_BUTTON)
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
                        .testTag(HeadToHeadAddHeatTestTag.SAVE_BUTTON)
                        .align(Alignment.CenterVertically)
        )
    }
}

enum class HeadToHeadAddHeatTestTag : CodexTestTag {
    SCREEN,
    OPPONENT_QUALI_RANK_INPUT,
    OPPONENT_QUALI_RANK_ERROR,
    MAX_RANK_INPUT,
    MAX_RANK_ERROR,
    IS_BYE_CHECKBOX,
    SELECTOR_DIALOG_ITEM,
    DELETE_BUTTON,
    RESET_BUTTON,
    SAVE_BUTTON,
    ;

    override val screenName: String
        get() = "HEAD_TO_HEAD_ADD_HEAT"

    override fun getElement(): String = name
}

@Preview
@Composable
fun Heat_HeadToHeadAddScreen_Preview() {
    CodexTheme {
        HeadToHeadAddHeatScreen(
                state = HeadToHeadAddHeatState(
                        previousHeat = HeadToHeadAddHeatState.PreviousHeat(
                                matchNumber = 1,
                                heat = 0,
                                result = HeadToHeadResult.WIN,
                                runningTotal = 6 to 0,
                        ),
                        roundInfo = SightMark(SightMarksPreviewHelper.sightMarks[0]).let {
                            HeadToHeadRoundInfo(sightMark = it, distance = it.distance, isMetric = it.isMetric)
                        },
                ),
        ) {}
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun Editing_HeadToHeadAddHeatScreen_Preview() {
    CodexTheme {
        HeadToHeadAddHeatScreen(
                state = HeadToHeadAddHeatState(
                        editing = DatabaseHeadToHeadHeat(
                                shootId = 0,
                                matchNumber = 1,
                                heat = 0,
                                opponent = null,
                                opponentQualificationRank = null,
                                isShootOffWin = false,
                                sightersCount = 0,
                                isBye = false,
                                maxPossibleRank = 1,
                        ),
                        extras = HeadToHeadAddHeatExtras(
                                heat = 0,
                                opponent = "",
                                isBye = false
                        ),
                ),
        ) {}
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun Bye_HeadToHeadAddHeatScreen_Preview() {
    CodexTheme {
        HeadToHeadAddHeatScreen(
                state = HeadToHeadAddHeatState(
                        extras = HeadToHeadAddHeatExtras(isBye = true),
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
fun Short_HeadToHeadAddHeatScreen_Preview() {
    CodexTheme {
        HeadToHeadAddHeatScreen(state = HeadToHeadAddHeatState()) {}
    }
}
