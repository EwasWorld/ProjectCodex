package eywa.projectcodex.components.newScore

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
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
import eywa.projectcodex.common.sharedUi.CodexButtonDefaults
import eywa.projectcodex.common.sharedUi.DataRow
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.codexTheme.asClickableStyle
import eywa.projectcodex.common.sharedUi.previewHelpers.ArcherRoundPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundRows
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.common.utils.DateTimeFormat
import eywa.projectcodex.common.utils.UpdateCalendarInfo
import eywa.projectcodex.common.utils.get
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsState
import eywa.projectcodex.common.utils.updateDefaultRounds.asDisplayString
import eywa.projectcodex.components.archerRoundScore.state.ArcherRoundScreen
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
    if (state.navigateToInputEnd != null) {
        CodexNavRoute.ARCHER_ROUND_SCORE.navigate(
                navController,
                mapOf(
                        NavArgument.SCREEN to ArcherRoundScreen.INPUT_END.name,
                        NavArgument.ARCHER_ROUND_ID to state.navigateToInputEnd.toString(),
                ),
        ) {
            val currentRoute = navController.currentDestination?.route
            if (currentRoute != null) {
                popUpTo(currentRoute) { inclusive = true }
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
                        .testTag(NewScoreTestTag.SCREEN.getTestTag())
        ) {
            DateRow(state, listener)

            if (state.isUpdateDefaultRoundsInProgress) {
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
                        displayedRound = state.displayedRound.get(),
                        displayedSubtype = state.displayedSubtype?.name,
                        isSelectRoundDialogOpen = state.isSelectRoundDialogOpen,
                        isSelectSubtypeDialogOpen = state.isSelectSubTypeDialogOpen,
                        rounds = state.roundsOnSelectDialog,
                        filters = state.enabledRoundFilters,
                        subTypes = state.selectedRoundInfo?.roundSubTypes ?: listOf(),
                        arrowCounts = state.selectedRoundInfo?.roundArrowCounts?.takeIf { it.isNotEmpty() },
                        roundSubtypeDistances = state.roundSubtypeDistances,
                        distanceUnit = state.distanceUnitStringRes?.let { stringResource(it) },
                        getDistance = { state.getFurthestDistance(it).distance },
                        helpListener = helpListener,
                        listener = { listener(SelectRoundDialogAction(it)) },
                )
            }

            if (state.isEditing) EditingEndRows(state, listener) else NewScoreEndRows(listener)
        }
    }
}

@Composable
private fun NewScoreEndRows(
        listener: (NewScoreIntent) -> Unit,
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(HelpShowcaseAction(it)) }
    CodexButton(
            text = stringResource(R.string.create_round__submit),
            buttonStyle = CodexButtonDefaults.DefaultButton(),
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
private fun EditingEndRows(
        state: NewScoreState,
        listener: (NewScoreIntent) -> Unit,
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(HelpShowcaseAction(it)) }
    if (state.tooManyArrowsWarningShown) {
        Text(
                text = stringResource(
                        R.string.err_create_round__too_many_arrows,
                        state.roundBeingEditedArrowsShot!!,
                        state.selectedRound!!.displayName,
                        state.totalArrowsInSelectedRound!!,
                ),
                style = CodexTypography.NORMAL.copy(
                        color = CodexTheme.colors.errorOnAppBackground,
                        textAlign = TextAlign.Center,
                ),
        )
    }

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
                buttonStyle = CodexButtonDefaults.DefaultButton(),
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
                buttonStyle = CodexButtonDefaults.DefaultButton(),
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
            buttonStyle = CodexButtonDefaults.DefaultButton(),
            onClick = { listener(Submit) },
            helpState = HelpState(
                    helpListener = helpListener,
                    helpTitle = stringResource(R.string.help_create_round__edit_submit_title),
                    helpBody = stringResource(R.string.help_create_round__edit_submit_body),
            ),
            modifier = Modifier.testTag(NewScoreTestTag.SUBMIT_BUTTON.getTestTag())
    )
}

@Composable
private fun DateRow(
        state: NewScoreState,
        listener: (NewScoreIntent) -> Unit,
) {
    val context = LocalContext.current
    val timePicker by lazy {
        TimePickerDialog(
                context,
                { _, hours, minutes ->
                    listener(DateChanged(UpdateCalendarInfo(hours = hours, minutes = minutes)))
                },
                state.dateShot.get(Calendar.HOUR_OF_DAY),
                state.dateShot.get(Calendar.MINUTE),
                true,
        )
    }
    val datePicker by lazy {
        DatePickerDialog(
                context,
                { _, year, month, day ->
                    listener(DateChanged(UpdateCalendarInfo(day = day, month = month, year = year)))
                },
                state.dateShot.get(Calendar.YEAR),
                state.dateShot.get(Calendar.MONTH),
                state.dateShot.get(Calendar.DATE),
        )
    }

    DataRow(
            title = stringResource(R.string.create_round__date),
            helpState = HelpState(
                    helpTitle = stringResource(R.string.help_create_round__date_title),
                    helpBody = stringResource(R.string.help_create_round__date_body),
                    helpListener = { listener(HelpShowcaseAction(it)) },
            ),
    ) {
        Text(
                text = DateTimeFormat.TIME_24_HOUR.format(state.dateShot),
                style = CodexTypography.NORMAL.asClickableStyle(),
                modifier = Modifier
                        .clickable { timePicker.show() }
                        .testTag(NewScoreTestTag.TIME_BUTTON.getTestTag())
        )
        Text(
                text = DateTimeFormat.LONG_DATE.format(state.dateShot),
                style = CodexTypography.NORMAL.asClickableStyle(),
                modifier = Modifier
                        .clickable { datePicker.show() }
                        .testTag(NewScoreTestTag.DATE_BUTTON.getTestTag())
        )
    }
}

enum class NewScoreTestTag : CodexTestTag {
    SCREEN,
    DATABASE_WARNING,
    SUBMIT_BUTTON,
    CANCEL_BUTTON,
    RESET_BUTTON,
    SELECTED_ROUND,
    SELECTED_SUBTYPE,
    DATE_BUTTON,
    TIME_BUTTON,
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

    override val values = sequenceOf(
            // No Round
            NewScoreState(),

            // Has Round
            NewScoreState(
                    roundsData = roundsData,
                    selectedRound = RoundPreviewHelper.outdoorImperialRoundData.round,
                    selectedSubtype = RoundPreviewHelper.outdoorImperialRoundData.roundSubTypes!!.first(),
            ),

            // Editing
            NewScoreState(roundsData = roundsData, roundBeingEdited = ArcherRoundPreviewHelper.newArcherRound()),

            // DbInProgress
            NewScoreState(roundsData = roundsData, updateDefaultRoundsState = UpdateDefaultRoundsState.DeletingOld(1)),

            // TooManyArrows
            NewScoreState(
                    roundsData = roundsData,
                    selectedRound = RoundPreviewHelper.outdoorImperialRoundData.round,
                    selectedSubtype = RoundPreviewHelper.outdoorImperialRoundData.roundSubTypes.first(),
                    roundBeingEdited = ArcherRoundPreviewHelper.newArcherRound(),
                    roundBeingEditedArrowsShot = 1000,
            ),

            // Select Round Dialog
            NewScoreState(roundsData = roundsData, isSelectRoundDialogOpen = true),

            // Select Subtype Dialog
            NewScoreState(
                    roundsData = roundsData,
                    selectedRound = RoundPreviewHelper.outdoorImperialRoundData.round,
                    selectedSubtype = RoundPreviewHelper.outdoorImperialRoundData.roundSubTypes.first(),
                    isSelectSubTypeDialogOpen = true,
            )
    )
}
