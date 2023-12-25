package eywa.projectcodex.components.newScore

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
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
import eywa.projectcodex.common.helpShowcase.HelpState
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.navigation.NavArgument
import eywa.projectcodex.common.sharedUi.CodexButton
import eywa.projectcodex.common.sharedUi.CodexDateTimeSelectorRow
import eywa.projectcodex.common.sharedUi.DataRow
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelper.addIdenticalArrows
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelper.addRound
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogState
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundRows
import eywa.projectcodex.common.sharedUi.selectRoundFaceDialog.SelectRoundFaceDialog
import eywa.projectcodex.common.sharedUi.testTag
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsState
import eywa.projectcodex.components.newScore.NewScoreIntent.*
import java.util.*


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
        if (state.isScoringNotCounting) {
            CodexNavRoute.SHOOT_DETAILS_ADD_END.navigate(
                    navController,
                    mapOf(NavArgument.SHOOT_ID to state.navigateToAddEnd.toString()),
                    popCurrentRoute = true,
            )
        }
        else {
            CodexNavRoute.SHOOT_DETAILS_ADD_COUNT.navigate(
                    navController,
                    mapOf(NavArgument.SHOOT_ID to state.navigateToAddEnd.toString()),
                    popCurrentRoute = true,
            )
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
                        .testTag(NewScoreTestTag.SCREEN.getTestTag())
        ) {
            CodexDateTimeSelectorRow(
                    date = state.dateShot,
                    helpState = HelpState(
                            helpListener = helpListener,
                            helpTitle = stringResource(R.string.help_create_round__date_title),
                            helpBody = stringResource(R.string.help_create_round__date_body),
                    ),
                    updateDateListener = { listener(DateChanged(it)) },
            )
            RoundSelectionSection(state, listener)
            ScoringTypeSection(state, listener)

            if (state.isEditing) EditingEndButtons(state, listener) else NewScoreEndButtons(listener)
        }
    }
}

@Composable
private fun ScoringTypeSection(
        state: NewScoreState,
        listener: (NewScoreIntent) -> Unit,
) {
    if (!state.isEditing || state.roundBeingEdited?.arrowsShot == 0) {
        DataRow(
                title = stringResource(R.string.create_round__score_type),
                text = stringResource(
                        if (state.isScoringNotCounting) R.string.create_round__score_type_score
                        else R.string.create_round__score_type_count
                ),
                helpState = HelpState(
                        helpListener = { listener(HelpShowcaseAction(it)) },
                        helpTitle = stringResource(R.string.help_create_round__score_type_title),
                        helpBody = stringResource(R.string.help_create_round__score_type_body),
                ),
                onClick = { listener(TypeChanged) },
                modifier = Modifier.testTag(NewScoreTestTag.TYPE_SWITCH)
        )
    }
    else {
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
                    else CodexTheme.dimens.cornerRounding
            ),
            border = BorderStroke(1.dp, CodexTheme.colors.listItemOnAppBackground),
            color = CodexTheme.colors.appBackground,
            modifier = Modifier.padding(horizontal = 20.dp)
    ) {
        Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            if (!state.updateDefaultRoundsState.hasTaskFinished) {
                Text(
                        text = stringResource(R.string.create_round__default_rounds_updating_warning),
                        style = CodexTypography.NORMAL.copy(
                                color = CodexTheme.colors.warningOnAppBackground,
                                textAlign = TextAlign.Center,
                        ),
                        modifier = Modifier.testTag(NewScoreTestTag.DATABASE_WARNING.getTestTag())
                )
                DataRow(
                        title = stringResource(R.string.create_round__default_rounds_updating_warning_status),
                        text = state.updateDefaultRoundsState.asDisplayString(LocalContext.current.resources),
                )
            }
            else {
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
            helpState = HelpState(
                    helpListener = helpListener,
                    helpTitle = stringResource(R.string.help_create_round__new_submit_title),
                    helpBody = stringResource(R.string.help_create_round__new_submit_body),
            ),
            modifier = Modifier
                    .padding(top = 10.dp)
                    .testTag(NewScoreTestTag.SUBMIT_BUTTON.getTestTag())
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
                helpState = HelpState(
                        helpListener = helpListener,
                        helpTitle = stringResource(R.string.help_create_round__edit_cancel_title),
                        helpBody = stringResource(R.string.help_create_round__edit_cancel_body),
                ),
                modifier = Modifier.testTag(NewScoreTestTag.CANCEL_BUTTON.getTestTag())
        )
        CodexButton(
                text = stringResource(R.string.general__reset_edits),
                onClick = { listener(ResetEditInfo) },
                helpState = HelpState(
                        helpListener = helpListener,
                        helpTitle = stringResource(R.string.help_create_round__edit_reset_title),
                        helpBody = stringResource(R.string.help_create_round__edit_reset_body),
                ),
                modifier = Modifier.testTag(NewScoreTestTag.RESET_BUTTON.getTestTag())
        )
    }
    CodexButton(
            text = stringResource(R.string.general_save),
            enabled = !state.tooManyArrowsWarningShown,
            onClick = { listener(Submit) },
            helpState = HelpState(
                    helpListener = helpListener,
                    helpTitle = stringResource(R.string.help_create_round__edit_submit_title),
                    helpBody = stringResource(R.string.help_create_round__edit_submit_body),
            ),
            modifier = Modifier.testTag(NewScoreTestTag.SUBMIT_BUTTON.getTestTag())
    )
}

enum class NewScoreTestTag : CodexTestTag {
    SCREEN,
    TYPE_SWITCH,
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

            // Editing
            initialState.copy(
                    roundBeingEdited = ShootPreviewHelper.newFullShootInfo(),
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
                    roundBeingEdited = ShootPreviewHelper.newFullShootInfo()
                            .addRound(round)
                            .addIdenticalArrows(1000, 1),
            ),

            // Select Round Dialog
            initialState.copy(
                    selectRoundDialogState = SelectRoundDialogState(
                            allRounds = roundsData,
                            isRoundDialogOpen = true,
                    )
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
