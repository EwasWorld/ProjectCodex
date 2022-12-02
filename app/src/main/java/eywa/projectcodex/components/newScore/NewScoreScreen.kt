package eywa.projectcodex.components.newScore

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.*
import eywa.projectcodex.common.sharedUi.*
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.codexTheme.asClickableStyle
import eywa.projectcodex.common.sharedUi.helperInterfaces.NamedItem
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.utils.DateTimeFormat
import eywa.projectcodex.common.utils.Sorting
import eywa.projectcodex.common.utils.UpdateCalendarInfo
import eywa.projectcodex.common.utils.get
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsState
import eywa.projectcodex.common.utils.updateDefaultRounds.asDisplayString
import eywa.projectcodex.components.newScore.NewScoreIntent.*
import eywa.projectcodex.components.newScore.helpers.NewScoreRoundEnabledFilters
import eywa.projectcodex.components.newScore.helpers.NewScoreRoundFilter
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundSubType
import java.text.DecimalFormat
import java.util.*
import kotlin.math.roundToInt


class NewScoreScreen : ActionBarHelp {
    private val helpInfo = ComposeHelpShowcaseMap()

    @Composable
    fun ComposeContent(
            state: NewScoreState,
            listener: (NewScoreIntent) -> Unit,
    ) {
        helpInfo.clear()

        val distanceUnit = state.distanceUnitStringRes?.let { stringResource(it) }

        SelectRoundDialog(
                isShown = state.isSelectRoundDialogOpen,
                displayedRounds = state.roundsOnSelectDialog,
                enabledFilters = state.enabledRoundFilters,
                listener = listener,
        )
        SelectSubtypeDialog(
                isShown = state.isSelectSubTypeDialogOpen,
                subTypes = state.selectedRoundInfo?.roundSubTypes ?: listOf(),
                getDistance = { state.getFurthestDistance(it).distance },
                distanceUnit = distanceUnit,
                listener = listener,
        )

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
                DataRow(
                        title = R.string.create_round__round,
                        helpTitle = R.string.help_create_round__round_title,
                        helpBody = R.string.help_create_round__round_body,
                ) {
                    Text(
                            text = state.displayedRound.get(),
                            style = CodexTypography.NORMAL.asClickableStyle(),
                            modifier = Modifier
                                    .clickable { listener(OpenRoundSelectDialog) }
                                    .testTag(TestTag.SELECTED_ROUND)
                    )
                }
                state.displayedSubtype?.let { displayedSubtype ->
                    DataRow(
                            title = R.string.create_round__round_sub_type,
                            helpTitle = R.string.help_create_round__sub_round_title,
                            helpBody = R.string.help_create_round__sub_round_body,
                    ) {
                        Text(
                                text = displayedSubtype.name!!,
                                style = CodexTypography.NORMAL.asClickableStyle(),
                                modifier = Modifier
                                        .clickable { listener(OpenSubTypeSelectDialog) }
                                        .testTag(TestTag.SELECTED_SUBTYPE)
                        )
                    }
                }

                RoundInfoHints(state)
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
    private fun RoundInfoHints(
            state: NewScoreState,
    ) {
        val separator = stringResource(R.string.general_comma_separator)
        val faceSizeUnit = stringResource(R.string.units_cm_short)
        val distanceUnit = state.distanceUnitStringRes?.let { stringResource(it) }

        val arrowCounts = state.selectedRoundInfo?.roundArrowCounts?.takeIf { it.isNotEmpty() }

        if (arrowCounts != null) {
            DataRow(
                    title = R.string.create_round__arrow_count_indicator,
                    extraText = arrowCounts
                            .sortedBy { it.distanceNumber }
                            .joinToString(separator) {
                                DecimalFormat("#.#").format(it.arrowCount / 12.0)
                            },
                    helpTitle = R.string.help_create_round__arrow_count_indicator_title,
                    helpBody = R.string.help_create_round__arrow_count_indicator_body,
            )
        }
        state.roundSubtypeDistances?.takeIf { it.isNotEmpty() }?.let { distances ->
            DataRow(
                    title = R.string.create_round__distance_indicator,
                    extraText = distances
                            .sortedBy { it.distanceNumber }
                            .joinToString(separator) { it.distance.toString() + distanceUnit },
                    helpTitle = R.string.help_create_round__distance_indicator_title,
                    helpBody = R.string.help_create_round__distance_indicator_body,
            )
        }
        if (arrowCounts != null) {
            DataRow(
                    title = R.string.create_round__face_size_indicator,
                    extraText = arrowCounts
                            .sortedBy { it.distanceNumber }
                            .joinToString(separator) { (it.faceSizeInCm.roundToInt()).toString() + faceSizeUnit },
                    helpTitle = R.string.help_create_round__face_size_indicator_title,
                    helpBody = R.string.help_create_round__face_size_indicator_title,
            )
        }
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

    @Composable
    private fun SelectRoundDialog(
            isShown: Boolean,
            enabledFilters: NewScoreRoundEnabledFilters,
            displayedRounds: List<Round>,
            listener: (NewScoreIntent) -> Unit,
    ) {
        SimpleDialog(
                isShown = isShown,
                onDismissListener = { listener(CloseRoundSelectDialog) },
        ) {
            SimpleDialogContent(
                    title = stringResource(R.string.create_round__select_a_round_title),
                    negativeButton = ButtonState(
                            text = stringResource(R.string.general_cancel),
                            onClick = { listener(CloseRoundSelectDialog) },
                    ),
                    positiveButton = ButtonState(
                            text = stringResource(R.string.create_round__no_round),
                            onClick = { listener(NoRoundSelected) },
                    ),
                    modifier = Modifier.testTag(TestTag.ROUND_DIALOG)
            ) {
                Column {
                    Row(
                            verticalAlignment = Alignment.CenterVertically,
                    ) {
                        LazyRow(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                modifier = Modifier.weight(1f)
                        ) {
                            item {
                                Text(
                                        text = stringResource(R.string.create_round__select_a_round_filters_title),
                                        style = CodexTypography.SMALL,
                                        modifier = Modifier.padding(end = 5.dp)
                                )
                            }
                            items(NewScoreRoundFilter.values()) { filter ->
                                CodexChip(
                                        text = stringResource(filter.chipText),
                                        state = CodexNewChipState(
                                                selected = enabledFilters.contains(filter),
                                                testTag = TestTag.fromFilterName(filter)
                                        ),
                                        colours = ChipColours.Defaults.onDialog(),
                                ) { listener(SelectRoundDialogFilterClicked(filter)) }
                            }
                        }
                        IconButton(onClick = { listener(SelectRoundDialogClearFilters) }) {
                            Icon(
                                    painter = painterResource(R.drawable.ic_baseline_clear_filter),
                                    contentDescription = stringResource(
                                            R.string.create_round__select_a_round_filter_clear_all
                                    ),
                            )
                        }
                    }
                    ItemSelector(
                            displayItems = displayedRounds.sortedWith { round1, round2 ->
                                Sorting.NUMERIC_STRING_SORT.compare(round1.name, round2.name)
                            },
                            onItemClicked = { listener(RoundSelected(it)) },
                            modifier = Modifier.padding(vertical = 10.dp)
                    )
                }
            }
        }
    }

    @Composable
    private fun SelectSubtypeDialog(
            isShown: Boolean,
            subTypes: List<RoundSubType>,
            getDistance: (RoundSubType) -> Int,
            distanceUnit: String?,
            listener: (NewScoreIntent) -> Unit,
    ) {
        SimpleDialog(
                isShown = isShown,
                onDismissListener = { listener(CloseSubTypeSelectDialog) },
        ) {
            SimpleDialogContent(
                    title = stringResource(R.string.create_round__select_a_subtype_title),
                    message = stringResource(R.string.create_round__select_a_subtype_message),
                    negativeButton = ButtonState(
                            text = stringResource(R.string.general_cancel),
                            onClick = { listener(CloseSubTypeSelectDialog) },
                    ),
                    modifier = Modifier.testTag(TestTag.SUBTYPE_DIALOG)
            ) {
                ItemSelector(
                        displayItems = subTypes.sortedByDescending { getDistance(it) },
                        onItemClicked = { listener(SubTypeSelected(it)) },
                ) { item ->
                    val distanceString = getDistance(item).toString() + distanceUnit!!
                    Text(
                            text = "($distanceString)",
                            style = CodexTypography.SMALL,
                    )
                }
            }
        }
    }

    @Composable
    private fun <T : NamedItem> ItemSelector(
            displayItems: Iterable<T>,
            onItemClicked: (T) -> Unit,
            modifier: Modifier = Modifier,
            extraContent: (@Composable (T) -> Unit)? = null,
    ) {
        LazyColumn(
                horizontalAlignment = Alignment.Start,
                modifier = modifier,
        ) {
            items(displayItems.toList()) { item ->
                Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier
                                .clickable { onItemClicked(item) }
                                .fillMaxWidth()
                                .padding(10.dp)
                ) {
                    WrappingRow(
                            verticalAlignment = Alignment.Bottom,
                    ) {
                        item.label.split(" ").forEach { itemLabelWord ->
                            Text(
                                    text = itemLabelWord,
                                    style = CodexTypography.NORMAL,
                            )
                        }
                        extraContent?.invoke(item)
                    }
                }
            }
        }
    }


    override fun getHelpShowcases(): List<HelpShowcaseItem> = helpInfo.getItems()
    override fun getHelpPriority(): Int? = null

    object TestTag {
        private const val PREFIX = "NEW_SCORE_"
        const val DATABASE_WARNING = "${PREFIX}DATABASE_WARNING"
        const val SUBMIT_BUTTON = "${PREFIX}SUBMIT"
        const val CANCEL_BUTTON = "${PREFIX}CANCEL"
        const val RESET_BUTTON = "${PREFIX}RESET"
        const val SELECTED_ROUND = "${PREFIX}ROUND_BUTTON"
        const val ROUND_DIALOG = "${PREFIX}ROUND_DIALOG"
        const val SELECTED_SUBTYPE = "${PREFIX}SUBTYPE_BUTTON"
        const val SUBTYPE_DIALOG = "${PREFIX}SUBTYPE_DIALOG"
        const val DATE_BUTTON = "${PREFIX}DATE_BUTTON"
        const val TIME_BUTTON = "${PREFIX}TIME_BUTTON"


        fun fromFilterName(filter: NewScoreRoundFilter) = "${PREFIX}FILTER_${filter.name}"
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