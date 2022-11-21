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
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.ActionBarHelp
import eywa.projectcodex.common.helpShowcase.ComposeHelpShowcaseMap
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.sharedUi.*
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.codexTheme.asClickableStyle
import eywa.projectcodex.common.sharedUi.helperInterfaces.NamedItem
import eywa.projectcodex.common.utils.DateTimeFormat
import eywa.projectcodex.common.utils.Sorting
import eywa.projectcodex.components.newScore.NewScoreIntent.*
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
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
        // TODO_CURRENT Help showcase

        val displayedRoundText = state.selectedRound?.displayName
                ?: stringResource(
                        if (state.allRounds.isEmpty()) {
                            R.string.create_round__no_rounds_found
                        }
                        else {
                            R.string.create_round__no_round
                        }
                )
        val distanceUnit = state.distanceUnitStringRes?.let { stringResource(it) }

        SelectRoundDialog(
                isShown = state.isSelectRoundOpen,
                displayedRounds = state.roundsOnSelectDialog,
                enabledFilters = state.enabledSelectRoundDialogFilters,
                listener = listener,
        )
        SelectSubtypeDialog(
                isShown = state.isSelectSubTypeOpen,
                subTypes = state.roundSubTypes,
                getDistance = { state.getFurthestDistance(it).distance },
                distanceUnit = distanceUnit,
                listener = listener,
        )

        Column(
                verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                        .fillMaxSize()
                        .background(CodexTheme.colors.appBackground)
                        .padding(25.dp)
        ) {
            DateRow(state, listener)
            if (state.databaseUpdatingProgress != null) {
                Text(
                        text = stringResource(R.string.create_round__default_rounds_updating_warning),
                        style = CodexTypography.NORMAL.copy(
                                color = CodexTheme.colors.warningOnAppBackground,
                                textAlign = TextAlign.Center,
                        ),
                )
                DataRow(
                        title = R.string.create_round__default_rounds_updating_warning_heading,
                        extraText = stringResource(
                                R.string.create_round__default_rounds_updating_warning_progress,
                                state.databaseUpdatingProgress.first,
                                state.databaseUpdatingProgress.second,
                        )
                )
            }
            else {
                DataRow(title = R.string.create_round__round) {
                    Text(
                            text = displayedRoundText,
                            style = CodexTypography.NORMAL.asClickableStyle(),
                            modifier = Modifier.clickable { listener(OpenRoundSelectDialog) }
                    )
                }
                state.displayedSubtype?.let {
                    DataRow(title = R.string.create_round__round_sub_type) {
                        Text(
                                text = state.displayedSubtype.name!!,
                                style = CodexTypography.NORMAL.asClickableStyle(),
                                modifier = Modifier.clickable { listener(OpenSubTypeSelectDialog) }
                        )
                    }
                }
                RoundInfoHints(state)
            }

            if (state.isEditing) {
                EditingEndRows(state, listener)
            }
            else {
                CodexButton(
                        text = stringResource(R.string.create_round__submit),
                        buttonStyle = CodexButtonDefaults.DefaultButton(),
                        onClick = { listener(Submit) },
                        modifier = Modifier.padding(top = 10.dp)
                )
            }
        }
    }

    @Composable
    private fun EditingEndRows(
            state: NewScoreState,
            listener: (NewScoreIntent) -> Unit,
    ) {
        if (state.tooManyArrowsWarningShown) {
            Text(
                    text = stringResource(
                            R.string.err_create_round__too_many_arrows,
                            state.arrowsShot!!,
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
            )
            CodexButton(
                    text = stringResource(R.string.general_complete),
                    enabled = !state.tooManyArrowsWarningShown,
                    buttonStyle = CodexButtonDefaults.DefaultButton(),
                    onClick = { listener(Submit) },
            )
        }
    }

    @Composable
    private fun RoundInfoHints(
            state: NewScoreState,
    ) {
        val separator = stringResource(R.string.general_comma_separator)
        val faceSizeUnit = stringResource(R.string.units_cm_short)
        val distanceUnit = state.distanceUnitStringRes?.let { stringResource(it) }

        if (state.roundArrowCounts.isNotEmpty()) {
            DataRow(
                    title = R.string.create_round__arrow_count_indicator,
                    extraText = state.roundArrowCounts.joinToString(separator) {
                        DecimalFormat("#.#").format(it.arrowCount / 12.0)
                    }
            )
        }
        if (state.roundDistances.isNotEmpty()) {
            DataRow(
                    title = R.string.create_round__distance_indicator,
                    extraText = state.roundDistances.joinToString { it.distance.toString() + distanceUnit }
            )
        }
        if (state.roundArrowCounts.isNotEmpty()) {
            DataRow(
                    title = R.string.create_round__face_size_indicator,
                    extraText = state.roundArrowCounts
                            .joinToString(separator) {
                                (it.faceSizeInCm.roundToInt()).toString() + faceSizeUnit
                            }
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
                        listener(TimeChanged(hours = hours, minutes = minutes))
                    },
                    state.date.get(Calendar.HOUR_OF_DAY),
                    state.date.get(Calendar.MINUTE),
                    true,
            )
        }
        val datePicker by lazy {
            DatePickerDialog(
                    context,
                    { _, year, month, day ->
                        listener(DateChanged(day = day, month = month, year = year))
                    },
                    state.date.get(Calendar.YEAR),
                    state.date.get(Calendar.MONTH),
                    state.date.get(Calendar.DATE),
            )
        }

        DataRow(title = R.string.create_round__date) {
            Text(
                    text = DateTimeFormat.TIME_24_HOUR.format(state.date),
                    style = CodexTypography.NORMAL.asClickableStyle(),
                    modifier = Modifier.clickable { timePicker.show() }
            )
            Text(
                    text = DateTimeFormat.LONG_DATE.format(state.date),
                    style = CodexTypography.NORMAL.asClickableStyle(),
                    modifier = Modifier.clickable { datePicker.show() }
            )
        }
    }

    @Composable
    private fun DataRow(
            @StringRes title: Int,
            extraText: String? = null,
            content: (@Composable RowScope.() -> Unit)? = null
    ) {
        val style = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground)
        Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
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
                            items(NewScoreRoundFilter.values() + NewScoreRoundFilter.values()) { filter ->
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
                                Sorting.NumericalStringSort.compare(round1.name, round2.name)
                            },
                            onItemClicked = { listener(RoundSelected(it)) },
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
            ) {
                ItemSelector(
                        displayItems = subTypes.sortedBy { getDistance(it) },
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
            extraContent: (@Composable (T) -> Unit)? = null,
    ) {
        LazyColumn(
                horizontalAlignment = Alignment.Start
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


        fun fromFilterName(filter: NewScoreRoundFilter) = "${PREFIX}FILTER_${filter.name}"
    }


    @Preview(
            showBackground = true,
            backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
    )
    @Composable
    fun NewScoreScreen_Preview(
            @PreviewParameter(PreviewParamProvider::class) params: NewScoreState
    ) {
        CodexTheme {
            ComposeContent(
                    state = params,
                    listener = {},
            )
        }
    }

    object PreviewParamProvider : PreviewParameterProvider<NewScoreState> {
        private val basePreviewState = NewScoreState(
                isEditing = false,
                arrowsShot = null,
                databaseUpdatingProgress = null,
                date = Calendar.getInstance(),
                isSelectRoundOpen = false,
                isSelectSubTypeOpen = false,
                selectedRound = null,
                selectedSubtype = null,
                allRounds = listOf(),
                allSubTypes = listOf(),
                allArrowCounts = listOf(),
                allDistances = listOf(),
                enabledSelectRoundDialogFilters = NewScoreRoundEnabledFilters(),
        )

        override val values = sequenceOf(
                // No Round
                basePreviewState,

                // Has Round
                basePreviewState.previewHelperAddRounds()
                        .let { it.copy(selectedRound = it.allRounds[0], selectedSubtype = it.allSubTypes[0]) },

                // Editing
                basePreviewState.previewHelperAddRounds().copy(isEditing = true),

                // DbInProgress
                basePreviewState.previewHelperAddRounds().copy(databaseUpdatingProgress = 3 to 10),

                // TooManyArrows
                basePreviewState.previewHelperAddRounds()
                        .let { it.copy(isEditing = true, arrowsShot = 1000, selectedRound = it.allRounds[0]) },

                // Select Round Dialog
                basePreviewState.previewHelperAddRounds().copy(isSelectRoundOpen = true),

                // Select Subtype Dialog
                basePreviewState.previewHelperAddRounds()
                        .let { it.copy(selectedRound = it.allRounds[0], isSelectSubTypeOpen = true) },
        )

        private fun NewScoreState.previewHelperAddRounds() = copy(
                allRounds = listOf(
                        Round(
                                roundId = 1,
                                name = "",
                                displayName = "York",
                                isOutdoor = true,
                                isMetric = false,
                                permittedFaces = listOf(),
                        ),
                        Round(
                                roundId = 2,
                                name = "",
                                displayName = "FITA",
                                isOutdoor = true,
                                isMetric = true,
                                permittedFaces = listOf(),
                        ),
                        Round(
                                roundId = 3,
                                name = "",
                                displayName = "Long Metric",
                                isOutdoor = true,
                                isMetric = true,
                                permittedFaces = listOf(),
                        ),
                ),
                allSubTypes = listOf(
                        RoundSubType(
                                roundId = 1,
                                subTypeId = 1,
                                name = "A Subtype",
                        ),
                        RoundSubType(
                                roundId = 1,
                                subTypeId = 2,
                                name = "Second Subtype",
                        ),
                ),
                allArrowCounts = listOf(
                        RoundArrowCount(
                                roundId = 1,
                                distanceNumber = 1,
                                faceSizeInCm = 120.0,
                                arrowCount = 36,
                        )
                ),
                allDistances = listOf(
                        RoundDistance(
                                roundId = 1,
                                distanceNumber = 1,
                                subTypeId = 1,
                                distance = 80,
                        ),
                        RoundDistance(
                                roundId = 1,
                                distanceNumber = 1,
                                subTypeId = 2,
                                distance = 60,
                        )
                ),
        )
    }
}