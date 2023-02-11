package eywa.projectcodex.components.newScore

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
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
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.ActionBarHelp
import eywa.projectcodex.common.helpShowcase.ComposeHelpShowcaseItem
import eywa.projectcodex.common.helpShowcase.ComposeHelpShowcaseMap
import eywa.projectcodex.common.helpShowcase.updateHelpDialogPosition
import eywa.projectcodex.common.sharedUi.CodexButton
import eywa.projectcodex.common.sharedUi.CodexButtonDefaults
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.codexTheme.asClickableStyle
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundRows
import eywa.projectcodex.common.utils.DateTimeFormat
import eywa.projectcodex.common.utils.UpdateCalendarInfo
import eywa.projectcodex.common.utils.get
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsState
import eywa.projectcodex.common.utils.updateDefaultRounds.asDisplayString
import eywa.projectcodex.components.newScore.NewScoreIntent.*
import java.util.*


class NewScoreScreen : ActionBarHelp {
    private val helpInfo = ComposeHelpShowcaseMap()

    @Composable
    fun ComposeContent(
            state: NewScoreState,
            listener: (NewScoreIntent) -> Unit,
    ) {
        helpInfo.clear()

        Column(
                verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxSize()
                        .background(CodexTheme.colors.appBackground)
                        .padding(25.dp)
        ) {
            DateRow(state, listener)

            if (state.isUpdateDefaultRoundsInProgress) {
                Text(
                        text = stringResource(R.string.create_round__default_rounds_updating_warning),
                        style = CodexTypography.NORMAL.copy(
                                color = CodexTheme.colors.warningOnAppBackground,
                                textAlign = TextAlign.Center,
                        ),
                        modifier = Modifier.testTag(TestTag.DATABASE_WARNING)
                )
                DataRow(
                        title = R.string.create_round__default_rounds_updating_warning_status,
                        extraText = state.updateDefaultRoundsState.asDisplayString(LocalContext.current.resources),
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
                        helpInfo = helpInfo,
                        listener = { listener(SelectRoundDialogAction(it)) },
                )
            }

            if (state.isEditing) EditingEndRows(state, listener) else NewScoreEndRows(listener)
        }
    }

    @Composable
    private fun NewScoreEndRows(
            listener: (NewScoreIntent) -> Unit,
    ) {
        helpInfo.add(
                ComposeHelpShowcaseItem(
                        helpTitle = R.string.help_create_round__new_submit_title,
                        helpBody = R.string.help_create_round__new_submit_body,
                )
        )

        CodexButton(
                text = stringResource(R.string.create_round__submit),
                buttonStyle = CodexButtonDefaults.DefaultButton(),
                onClick = { listener(Submit) },
                modifier = Modifier
                        .padding(top = 10.dp)
                        .updateHelpDialogPosition(helpInfo, R.string.help_create_round__new_submit_title)
                        .testTag(TestTag.SUBMIT_BUTTON)
        )
    }

    @Composable
    private fun EditingEndRows(
            state: NewScoreState,
            listener: (NewScoreIntent) -> Unit,
    ) {
        helpInfo.add(
                ComposeHelpShowcaseItem(
                        helpTitle = R.string.help_create_round__edit_cancel_title,
                        helpBody = R.string.help_create_round__edit_cancel_body,
                )
        )
        helpInfo.add(
                ComposeHelpShowcaseItem(
                        helpTitle = R.string.help_create_round__edit_reset_title,
                        helpBody = R.string.help_create_round__edit_reset_body,
                )
        )
        helpInfo.add(
                ComposeHelpShowcaseItem(
                        helpTitle = R.string.help_create_round__edit_submit_title,
                        helpBody = R.string.help_create_round__edit_submit_body,
                )
        )

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
                    modifier = Modifier
                            .updateHelpDialogPosition(
                                    helpInfo, R.string.help_create_round__edit_cancel_title
                            )
                            .testTag(TestTag.CANCEL_BUTTON)
            )
            CodexButton(
                    text = stringResource(R.string.general__reset_edits),
                    buttonStyle = CodexButtonDefaults.DefaultButton(),
                    onClick = { listener(ResetEditInfo) },
                    modifier = Modifier
                            .updateHelpDialogPosition(
                                    helpInfo, R.string.help_create_round__edit_reset_title
                            )
                            .testTag(TestTag.RESET_BUTTON)
            )
        }
        CodexButton(
                text = stringResource(R.string.general_complete),
                enabled = !state.tooManyArrowsWarningShown,
                buttonStyle = CodexButtonDefaults.DefaultButton(),
                onClick = { listener(Submit) },
                modifier = Modifier
                        .updateHelpDialogPosition(
                                helpInfo, R.string.help_create_round__edit_submit_title
                        )
                        .testTag(TestTag.SUBMIT_BUTTON)
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
                title = R.string.create_round__date,
                helpTitle = R.string.help_create_round__date_title,
                helpBody = R.string.help_create_round__date_body,
        ) {
            Text(
                    text = DateTimeFormat.TIME_24_HOUR.format(state.dateShot),
                    style = CodexTypography.NORMAL.asClickableStyle(),
                    modifier = Modifier
                            .clickable { timePicker.show() }
                            .testTag(TestTag.TIME_BUTTON)
            )
            Text(
                    text = DateTimeFormat.LONG_DATE.format(state.dateShot),
                    style = CodexTypography.NORMAL.asClickableStyle(),
                    modifier = Modifier
                            .clickable { datePicker.show() }
                            .testTag(TestTag.DATE_BUTTON)
            )
        }
    }

    @Composable
    private fun DataRow(
            @StringRes title: Int,
            @StringRes helpTitle: Int? = null,
            @StringRes helpBody: Int? = null,
            modifier: Modifier = Modifier,
            extraText: String? = null,
            content: (@Composable RowScope.() -> Unit)? = null
    ) {
        require(helpTitle == null || helpBody != null) { "If a title is given, a body must be given too" }
        val style = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground)
        var rowModifier = modifier

        if (helpTitle != null) {
            helpInfo.add(ComposeHelpShowcaseItem(helpTitle = helpTitle, helpBody = helpBody!!))
            rowModifier = rowModifier.then(Modifier.updateHelpDialogPosition(helpInfo, helpTitle))
        }

        Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                modifier = rowModifier
        ) {
            Text(
                    text = stringResource(title),
                    style = style.copy(textAlign = TextAlign.End),
            )
            extraText?.let {
                Text(
                        text = extraText,
                        style = style.copy(textAlign = TextAlign.Start),
                )
            }
            content?.invoke(this)
        }
    }


    override fun getHelpShowcases() = helpInfo.getItems()
    override fun getHelpPriority(): Int? = null

    object TestTag {
        private const val PREFIX = "NEW_SCORE_"
        const val DATABASE_WARNING = "${PREFIX}DATABASE_WARNING"
        const val SUBMIT_BUTTON = "${PREFIX}SUBMIT"
        const val CANCEL_BUTTON = "${PREFIX}CANCEL"
        const val RESET_BUTTON = "${PREFIX}RESET"
        const val SELECTED_ROUND = "${PREFIX}ROUND_BUTTON"
        const val SELECTED_SUBTYPE = "${PREFIX}SUBTYPE_BUTTON"
        const val DATE_BUTTON = "${PREFIX}DATE_BUTTON"
        const val TIME_BUTTON = "${PREFIX}TIME_BUTTON"
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
            NewScoreScreen().ComposeContent(
                    state = params,
                    listener = {},
            )
        }
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
            NewScoreState(roundsData = roundsData, roundBeingEdited = RoundPreviewHelper.archerRoundNoRound),

            // DbInProgress
            NewScoreState(roundsData = roundsData, updateDefaultRoundsState = UpdateDefaultRoundsState.DeletingOld(1)),

            // TooManyArrows
            NewScoreState(
                    roundsData = roundsData,
                    selectedRound = RoundPreviewHelper.outdoorImperialRoundData.round,
                    roundBeingEdited = RoundPreviewHelper.archerRoundNoRound,
                    roundBeingEditedArrowsShot = 1000,
            ),

            // Select Round Dialog
            NewScoreState(roundsData = roundsData, isSelectRoundDialogOpen = true),

            // Select Subtype Dialog
            NewScoreState(
                    roundsData = roundsData,
                    selectedRound = RoundPreviewHelper.outdoorImperialRoundData.round,
                    isSelectSubTypeDialogOpen = true,
            )
    )
}
