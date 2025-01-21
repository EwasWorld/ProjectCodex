package eywa.projectcodex.components.newScore

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.helpShowcase.asHelpState
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.navigation.NavArgument
import eywa.projectcodex.common.sharedUi.CodexButton
import eywa.projectcodex.common.sharedUi.CodexDateTimeSelectorRow
import eywa.projectcodex.common.sharedUi.DataRow
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.numberField.CodexLabelledNumberFieldWithErrorMessage
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelperDsl
import eywa.projectcodex.common.sharedUi.selectRoundDialog.RoundsUpdatingWrapper
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogState
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundRows
import eywa.projectcodex.common.sharedUi.selectRoundFaceDialog.SelectRoundFaceDialog
import eywa.projectcodex.common.sharedUi.testTag
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsState
import eywa.projectcodex.components.newScore.NewScoreIntent.*


@Composable
fun NewScoreScreen(
        navController: NavController,
        viewModel: NewScoreViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val listener = { it: NewScoreIntent -> viewModel.handle(it) }

    NewScoreScreen(state, listener)

    handleEffects(navController, state, listener)
}

private fun handleEffects(
        navController: NavController,
        state: NewScoreState,
        listener: (NewScoreIntent) -> Unit,
) {
    if (state.navigateToAddEnd != null) {
        when (state.type) {
            NewScoreType.SCORING -> {
                CodexNavRoute.SHOOT_DETAILS_ADD_END.navigate(
                        navController,
                        mapOf(NavArgument.SHOOT_ID to state.navigateToAddEnd.toString()),
                        popCurrentRoute = true,
                )
            }

            NewScoreType.COUNTING -> {
                CodexNavRoute.SHOOT_DETAILS_ADD_COUNT.navigate(
                        navController,
                        mapOf(NavArgument.SHOOT_ID to state.navigateToAddEnd.toString()),
                        popCurrentRoute = true,
                )
            }

            NewScoreType.HEAD_TO_HEAD -> {
                CodexNavRoute.HEAD_TO_HEAD_ADD_MATCH.navigate(
                        navController,
                        mapOf(NavArgument.SHOOT_ID to state.navigateToAddEnd.toString()),
                        popCurrentRoute = true,
                )
            }
        }
        listener(HandleNavigate)
    }
    if (state.popBackstack) {
        navController.popBackStack()
        listener(HandlePopBackstack)
    }
}

@Composable
fun NewScoreScreen(
        state: NewScoreState,
        listener: (NewScoreIntent) -> Unit,
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(HelpShowcaseAction(it)) }
    helpListener(HelpShowcaseIntent.Clear)

    ProvideTextStyle(CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground)) {
        Column(
                verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .background(CodexTheme.colors.appBackground)
                        .padding(25.dp)
                        .testTag(NewScoreTestTag.SCREEN)
        ) {
            CodexDateTimeSelectorRow(
                    date = state.dateShot,
                    helpState = HelpShowcaseItem(
                            helpTitle = stringResource(R.string.help_create_round__date_title),
                            helpBody = stringResource(R.string.help_create_round__date_body),
                    ).asHelpState(helpListener),
                    updateDateListener = { listener(DateChanged(it)) },
            )
            RoundSelectionSection(state, listener)
            ScoringTypeSection(state, listener)
            AnimatedVisibility(
                    visible = state.type == NewScoreType.HEAD_TO_HEAD,
            ) {
                HeadToHeadSection(state, listener)
            }

            if (state.isEditing) EditingEndButtons(state, listener) else NewScoreEndButtons(listener)
        }
    }
}

@Composable
private fun HeadToHeadSection(
        state: NewScoreState,
        listener: (NewScoreIntent) -> Unit,
) {
    Surface(
            shape = RoundedCornerShape(CodexTheme.dimens.cornerRounding),
            border = BorderStroke(1.dp, CodexTheme.colors.listItemOnAppBackground),
            color = CodexTheme.colors.appBackground,
            modifier = Modifier.padding(horizontal = CodexTheme.dimens.screenPadding)
    ) {
        Column(
                verticalArrangement = Arrangement.spacedBy(15.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                        .padding(horizontal = 25.dp, vertical = 20.dp)
                        .padding(bottom = 5.dp)
        ) {
            DataRow(
                    title = stringResource(R.string.create_round__h2h_style),
                    text = stringResource(
                            if (state.h2hStyleIsRecurve) R.string.create_round__h2h_style_recurve
                            else R.string.create_round__h2h_style_compound,
                    ),
                    helpState = HelpShowcaseItem(
                            helpTitle = stringResource(R.string.help_create_round__h2h_style_title),
                            helpBody = stringResource(R.string.help_create_round__h2h_style_body),
                    ).asHelpState { listener(HelpShowcaseAction(it)) },
                    onClick = { listener(H2hStyleChanged) }.takeIf { !state.isEditing },
                    modifier = Modifier.testTag(NewScoreTestTag.H2H_STYLE_SWITCH)
            )
            DataRow(
                    title = stringResource(R.string.create_round__h2h_format),
                    text = stringResource(
                            if (state.h2hFormatIsStandard) R.string.create_round__h2h_format_standard
                            else R.string.create_round__h2h_format_non_standard,
                    ),
                    helpState = HelpShowcaseItem(
                            helpTitle = stringResource(R.string.help_create_round__h2h_format_title),
                            helpBody = stringResource(R.string.help_create_round__h2h_format_body),
                    ).asHelpState { listener(HelpShowcaseAction(it)) },
                    onClick = { listener(H2hFormatChanged) }.takeIf { !state.isEditing },
                    modifier = Modifier.testTag(NewScoreTestTag.H2H_FORMAT_SWITCH)
            )
            CodexLabelledNumberFieldWithErrorMessage(
                    title = stringResource(R.string.create_round__h2h_team_size),
                    currentValue = state.h2hTeamSize.text,
                    fieldTestTag = NewScoreTestTag.H2H_TEAM_SIZE_INPUT,
                    errorMessageTestTag = NewScoreTestTag.H2H_TEAM_SIZE_ERROR,
                    errorMessage = state.h2hTeamSize.error,
                    placeholder = stringResource(R.string.create_round__h2h_team_size_placeholder),
                    onValueChanged = { listener(H2hTeamSizeChanged(it)) },
                    helpState = HelpShowcaseItem(
                            helpTitle = stringResource(R.string.help_create_round__h2h_team_size_title),
                            helpBody = stringResource(R.string.help_create_round__h2h_team_size_body),
                    ).asHelpState { listener(HelpShowcaseAction(it)) },
            )
            CodexLabelledNumberFieldWithErrorMessage(
                    title = stringResource(R.string.create_round__h2h_qualification_rank),
                    currentValue = state.h2hQualificationRank.text,
                    fieldTestTag = NewScoreTestTag.H2H_QUALI_RANK_INPUT,
                    errorMessageTestTag = NewScoreTestTag.H2H_QUALI_RANK_ERROR,
                    errorMessage = state.h2hQualificationRank.error,
                    placeholder = stringResource(R.string.head_to_head_add_heat__quali_rank_placeholder),
                    onValueChanged = { listener(H2hQualiRankChanged(it)) },
                    helpState = HelpShowcaseItem(
                            helpTitle = stringResource(R.string.help_create_round__h2h_quali_rank_title),
                            helpBody = stringResource(R.string.help_create_round__h2h_quali_rank_body),
                    ).asHelpState { listener(HelpShowcaseAction(it)) },
            )
            CodexLabelledNumberFieldWithErrorMessage(
                    title = stringResource(R.string.create_round__h2h_total_archers),
                    currentValue = state.h2hTotalArchers.text,
                    fieldTestTag = NewScoreTestTag.H2H_TOTAL_ARCHERS_INPUT,
                    errorMessageTestTag = NewScoreTestTag.H2H_TOTAL_ARCHERS_ERROR,
                    errorMessage = state.h2hTotalArchers.error,
                    placeholder = stringResource(R.string.create_round__h2h_team_size_placeholder),
                    onValueChanged = { listener(H2hTotalArchersChanged(it)) },
                    helpState = HelpShowcaseItem(
                            helpTitle = stringResource(R.string.help_create_round__h2h_total_archers_title),
                            helpBody = stringResource(R.string.help_create_round__h2h_total_archers_body),
                    ).asHelpState { listener(HelpShowcaseAction(it)) },
            )
        }
    }
}

@Composable
private fun ScoringTypeSection(
        state: NewScoreState,
        listener: (NewScoreIntent) -> Unit,
) {
    Column(
            verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 5.dp)
    ) {
        DataRow(
                title = stringResource(R.string.create_round__score_type),
                text = state.type.title.get(),
                helpState = HelpShowcaseItem(
                        helpTitle = stringResource(R.string.help_create_round__score_type_title),
                        helpBody = stringResource(R.string.help_create_round__score_type_body),
                ).asHelpState { listener(HelpShowcaseAction(it)) },
                onClick = { listener(TypeChanged) }.takeIf { !state.isEditing },
                modifier = Modifier.testTag(NewScoreTestTag.TYPE_SWITCH)
        )
        if (state.isEditing) {
            Text(
                    text = stringResource(R.string.create_round__score_type_cannot_change),
                    textAlign = TextAlign.Center,
                    fontStyle = FontStyle.Italic,
                    style = CodexTypography.SMALL,
                    color = CodexTheme.colors.onAppBackground,
                    modifier = Modifier.padding(horizontal = 20.dp)
            )
        }
    }
}

@Composable
private fun RoundSelectionSection(
        state: NewScoreState,
        listener: (NewScoreIntent) -> Unit,
) {
    val isRoundsUpdateCompleteAndNoneSelected =
            state.selectRoundDialogState.selectedRound == null && state.updateDefaultRoundsState.hasTaskFinished

    Surface(
            shape = RoundedCornerShape(
                    if (isRoundsUpdateCompleteAndNoneSelected) CodexTheme.dimens.smallCornerRounding
                    else CodexTheme.dimens.cornerRounding,
            ),
            border = BorderStroke(1.dp, CodexTheme.colors.listItemOnAppBackground),
            color = CodexTheme.colors.appBackground,
            modifier = Modifier.padding(horizontal = CodexTheme.dimens.screenPadding)
    ) {
        Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            RoundsUpdatingWrapper(
                    state = state.updateDefaultRoundsState,
                    errorText = stringResource(R.string.create_round__default_rounds_updating_warning),
            ) {
                SelectRoundRows(
                        state = state.selectRoundDialogState,
                        helpListener = { listener(HelpShowcaseAction(it)) },
                        listener = { listener(SelectRoundDialogAction(it)) },
                )

                SelectRoundFaceDialog(
                        state = state.selectFaceDialogState,
                        helpListener = { listener(HelpShowcaseAction(it)) },
                        listener = { listener(SelectFaceDialogAction(it)) },
                )
                if (state.isEditing && state.tooManyArrowsWarningShown) {
                    Text(
                            text = stringResource(
                                    R.string.err_create_round__too_many_arrows,
                                    state.roundBeingEdited!!.arrowsShot,
                                    state.selectRoundDialogState.displayName!!,
                                    state.totalArrowsInSelectedRound!!,
                            ),
                            color = CodexTheme.colors.errorOnAppBackground,
                            textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun NewScoreEndButtons(
        listener: (NewScoreIntent) -> Unit,
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(HelpShowcaseAction(it)) }
    CodexButton(
            text = stringResource(R.string.create_round__submit),
            onClick = { listener(Submit) },
            helpState = HelpShowcaseItem(
                    helpTitle = stringResource(R.string.help_create_round__new_submit_title),
                    helpBody = stringResource(R.string.help_create_round__new_submit_body),
            ).asHelpState(helpListener),
            modifier = Modifier
                    .padding(top = 10.dp)
                    .testTag(NewScoreTestTag.SUBMIT_BUTTON)
    )
}

@Composable
private fun EditingEndButtons(
        state: NewScoreState,
        listener: (NewScoreIntent) -> Unit,
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(HelpShowcaseAction(it)) }
    Row(
            horizontalArrangement = Arrangement.spacedBy(15.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
    ) {
        // TODO Lower the emphasis on cancel
        CodexButton(
                text = stringResource(R.string.general_cancel),
                onClick = { listener(CancelEditInfo) },
                helpState = HelpShowcaseItem(
                        helpTitle = stringResource(R.string.help_create_round__edit_cancel_title),
                        helpBody = stringResource(R.string.help_create_round__edit_cancel_body),
                ).asHelpState(helpListener),
                modifier = Modifier.testTag(NewScoreTestTag.CANCEL_BUTTON)
        )
        CodexButton(
                text = stringResource(R.string.general__reset_edits),
                onClick = { listener(ResetEditInfo) },
                helpState = HelpShowcaseItem(
                        helpTitle = stringResource(R.string.help_create_round__edit_reset_title),
                        helpBody = stringResource(R.string.help_create_round__edit_reset_body),
                ).asHelpState(helpListener),
                modifier = Modifier.testTag(NewScoreTestTag.RESET_BUTTON)
        )
    }
    CodexButton(
            text = stringResource(R.string.general_save),
            enabled = !state.tooManyArrowsWarningShown,
            onClick = { listener(Submit) },
            helpState = HelpShowcaseItem(
                    helpTitle = stringResource(R.string.help_create_round__edit_submit_title),
                    helpBody = stringResource(R.string.help_create_round__edit_submit_body),
            ).asHelpState(helpListener),
            modifier = Modifier.testTag(NewScoreTestTag.SUBMIT_BUTTON)
    )
}

enum class NewScoreTestTag : CodexTestTag {
    SCREEN,
    TYPE_SWITCH,
    H2H_STYLE_SWITCH,
    H2H_FORMAT_SWITCH,
    H2H_TEAM_SIZE_INPUT,
    H2H_TEAM_SIZE_ERROR,
    H2H_TOTAL_ARCHERS_INPUT,
    H2H_TOTAL_ARCHERS_ERROR,
    H2H_QUALI_RANK_INPUT,
    H2H_QUALI_RANK_ERROR,
    DATABASE_WARNING,
    SUBMIT_BUTTON,
    CANCEL_BUTTON,
    RESET_BUTTON,
    ;

    override val screenName: String
        get() = "NEW_SCORE"

    override fun getElement(): String = name
}


@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun NewScoreScreen_Preview(
        @PreviewParameter(NewScoreStatePreviewProvider::class) params: NewScoreState
) {
    CodexTheme {
        NewScoreScreen(
                state = params,
                listener = {},
        )
    }
}

class NewScoreStatePreviewProvider : PreviewParameterProvider<NewScoreState> {
    val roundsData = listOf(
            RoundPreviewHelper.outdoorImperialRoundData,
            RoundPreviewHelper.indoorMetricRoundData,
            RoundPreviewHelper.singleSubtypeRoundData,
    )
    val round = RoundPreviewHelper.outdoorImperialRoundData
    private val initialState = NewScoreState(
            selectRoundDialogState = SelectRoundDialogState(
                    allRounds = roundsData,
            ),
            updateDefaultRoundsState = UpdateDefaultRoundsState.Complete(
                    databaseVersion = 1,
                    type = UpdateDefaultRoundsState.CompletionType.ALREADY_UP_TO_DATE
            ),
    )

    override val values = sequenceOf(
            // No Round Selected
            initialState,

            // Has Round
            initialState.copy(
                    selectRoundDialogState = SelectRoundDialogState(
                            allRounds = roundsData,
                            selectedRoundId = round.round.roundId,
                            selectedSubTypeId = round.roundSubTypes!!.first().subTypeId,
                    ),
            ),

            // H2h
            initialState.copy(
                    type = NewScoreType.HEAD_TO_HEAD,
            ),

            // Editing
            initialState.copy(
                    roundBeingEdited = ShootPreviewHelperDsl.create {},
            ),

            // DbInProgress
            initialState.copy(
                    updateDefaultRoundsState = UpdateDefaultRoundsState.DeletingOld(1),
            ),

            // TooManyArrows
            initialState.copy(
                    selectRoundDialogState = SelectRoundDialogState(
                            allRounds = roundsData,
                            selectedRoundId = round.round.roundId,
                            selectedSubTypeId = round.roundSubTypes.first().subTypeId,
                    ),
                    roundBeingEdited = ShootPreviewHelperDsl.create {
                        this.round = this@NewScoreStatePreviewProvider.round
                        addIdenticalArrows(1000, 1)
                    },
            ),

            // Select Round Dialog
            initialState.copy(
                    selectRoundDialogState = SelectRoundDialogState(
                            allRounds = roundsData,
                            isRoundDialogOpen = true,
                    ),
            ),

            // Select Subtype Dialog
            initialState.copy(
                    selectRoundDialogState = SelectRoundDialogState(
                            allRounds = roundsData,
                            selectedRoundId = round.round.roundId,
                            selectedSubTypeId = round.roundSubTypes.first().subTypeId,
                            isSubtypeDialogOpen = true,
                    ),
            ),

            // No Rounds in DB
            NewScoreState(
                    updateDefaultRoundsState = UpdateDefaultRoundsState.Complete(
                            databaseVersion = 1,
                            type = UpdateDefaultRoundsState.CompletionType.ALREADY_UP_TO_DATE
                    ),
            ),
    )
}
