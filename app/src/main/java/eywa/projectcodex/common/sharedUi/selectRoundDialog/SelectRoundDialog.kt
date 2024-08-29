package eywa.projectcodex.common.sharedUi.selectRoundDialog

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.helpShowcase.asHelpState
import eywa.projectcodex.common.sharedUi.ButtonState
import eywa.projectcodex.common.sharedUi.ChipColours
import eywa.projectcodex.common.sharedUi.CodexChip
import eywa.projectcodex.common.sharedUi.CodexNewChipState
import eywa.projectcodex.common.sharedUi.DataRow
import eywa.projectcodex.common.sharedUi.DialogPreviewHelper
import eywa.projectcodex.common.sharedUi.SimpleDialog
import eywa.projectcodex.common.sharedUi.SimpleDialogContent
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.codexTheme.asClickableStyle
import eywa.projectcodex.common.sharedUi.helperInterfaces.NamedItem
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogIntent.RoundIntent.*
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogIntent.SubTypeIntent.*
import eywa.projectcodex.common.sharedUi.testTag
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.common.utils.Sorting
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsState
import eywa.projectcodex.components.newScore.NewScoreTestTag
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import eywa.projectcodex.database.rounds.RoundSubType
import java.text.DecimalFormat
import kotlin.math.roundToInt

@Composable
fun RoundsUpdatingWrapper(
        state: UpdateDefaultRoundsState,
        @SuppressLint("ModifierParameter") // Not being used as the top-level modifier
        warningModifier: Modifier = Modifier,
        style: TextStyle = LocalTextStyle.current,
        errorText: String = stringResource(R.string.default_rounds_updating_warning),
        errorTextColour: Color = CodexTheme.colors.warningOnAppBackground,
        spacing: Dp = 10.dp,
        content: @Composable () -> Unit,
) {
    if (!state.hasTaskFinished) {
        Column(
                verticalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = warningModifier
        ) {
            ProvideTextStyle(style) {
                Text(
                        text = errorText,
                        color = errorTextColour,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag(NewScoreTestTag.DATABASE_WARNING)
                )
                DataRow(
                        title = stringResource(R.string.create_round__default_rounds_updating_warning_status),
                        text = state.displayString.get(),
                )
            }
        }
    }
    else {
        content()
    }
}

// TODO Turn empty rounds into a different field and show a warning message instead
@Composable
fun ColumnScope.SelectRoundRows(
        state: SelectRoundDialogState,
        helpListener: (HelpShowcaseIntent) -> Unit,
        textClickableStyle: TextStyle = LocalTextStyle.current.asClickableStyle(),
        listener: (SelectRoundDialogIntent) -> Unit,
) = SelectRoundRows(
        hasNoRounds = state.allRounds.isNullOrEmpty(),
        displayedRound = state.selectedRound?.round?.displayName ?: stringResource(R.string.create_round__no_round),
        displayedSubtype = state.selectedSubType?.name,
        isSelectRoundDialogOpen = state.isRoundDialogOpen,
        isSelectSubtypeDialogOpen = state.isSubtypeDialogOpen,
        rounds = state.filteredRounds,
        filters = state.filters,
        subTypes = state.selectedRound?.roundSubTypes ?: emptyList(),
        arrowCounts = state.selectedRound?.roundArrowCounts,
        roundSubtypeDistances = state.selectedRound?.getDistances(state.selectedSubTypeId),
        distanceUnit = state.selectedRound?.getDistanceUnit(),
        getDistance = { subType -> state.getFurthestDistance(subType)!!.distance },
        helpListener = helpListener,
        textClickableStyle = textClickableStyle,
        listener = listener,
)

@Composable
fun RoundRows(
        state: SelectRoundDialogState,
        helpListener: (HelpShowcaseIntent) -> Unit,
        modifier: Modifier = Modifier,
        textClickableStyle: TextStyle = LocalTextStyle.current.asClickableStyle(),
        listener: (SelectRoundDialogIntent) -> Unit,
) = RoundRows(
        hasNoRounds = state.allRounds.isNullOrEmpty(),
        displayedRound = state.selectedRound?.round?.displayName ?: stringResource(R.string.create_round__no_round),
        isSelectRoundDialogOpen = state.isRoundDialogOpen,
        rounds = state.filteredRounds,
        filters = state.filters,
        helpListener = helpListener,
        textClickableStyle = textClickableStyle,
        listener = listener,
        modifier = modifier
)

@Composable
fun ColumnScope.SelectRoundRows(
        hasNoRounds: Boolean,
        displayedRound: String,
        displayedSubtype: String?,
        isSelectRoundDialogOpen: Boolean,
        isSelectSubtypeDialogOpen: Boolean,
        rounds: List<Round>,
        filters: SelectRoundEnabledFilters,
        subTypes: List<RoundSubType>,
        arrowCounts: List<RoundArrowCount>?,
        roundSubtypeDistances: List<RoundDistance>?,
        distanceUnit: String?,
        getDistance: (RoundSubType) -> Int,
        helpListener: (HelpShowcaseIntent) -> Unit,
        textClickableStyle: TextStyle = LocalTextStyle.current.asClickableStyle(),
        listener: (SelectRoundDialogIntent) -> Unit,
) {
    SelectSubtypeDialog(
            isShown = isSelectSubtypeDialogOpen,
            subTypes = subTypes,
            getDistance = getDistance,
            distanceUnit = distanceUnit,
            listener = listener,
    )

    RoundRows(
            hasNoRounds = hasNoRounds,
            displayedRound = displayedRound,
            isSelectRoundDialogOpen = isSelectRoundDialogOpen,
            rounds = rounds,
            filters = filters,
            helpListener = helpListener,
            textClickableStyle = textClickableStyle,
            listener = listener,
    )
    if (displayedSubtype != null) {
        DataRow(
                title = stringResource(R.string.create_round__round_sub_type),
                text = displayedSubtype,
                helpState = HelpShowcaseItem(
                        helpTitle = stringResource(R.string.help_create_round__sub_round_title),
                        helpBody = stringResource(R.string.help_create_round__sub_round_body),
                ).asHelpState(helpListener),
                onClick = { listener(OpenSubTypeDialog) },
                textClickableStyle = textClickableStyle,
                modifier = Modifier.testTag(SelectRoundDialogTestTag.SELECTED_SUBTYPE_ROW)
        )
    }

    RoundInfoHints(distanceUnit, arrowCounts, roundSubtypeDistances, helpListener)
}

@Composable
private fun RoundRows(
        hasNoRounds: Boolean,
        displayedRound: String,
        isSelectRoundDialogOpen: Boolean,
        rounds: List<Round>,
        filters: SelectRoundEnabledFilters,
        helpListener: (HelpShowcaseIntent) -> Unit,
        modifier: Modifier = Modifier,
        textClickableStyle: TextStyle = LocalTextStyle.current.asClickableStyle(),
        listener: (SelectRoundDialogIntent) -> Unit,
) {
    SelectRoundDialog(
            isShown = isSelectRoundDialogOpen,
            displayedRounds = rounds,
            enabledFilters = filters,
            listener = listener,
    )

    Column(
            verticalArrangement = Arrangement.spacedBy(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
    ) {
        DataRow(
                title = stringResource(R.string.create_round__round),
                text = displayedRound,
                helpState = HelpShowcaseItem(
                        helpTitle = stringResource(R.string.help_create_round__round_title),
                        helpBody = stringResource(R.string.help_create_round__round_body),
                ).asHelpState(helpListener),
                onClick = { listener(OpenRoundDialog) }.takeIf { !hasNoRounds },
                textClickableStyle = textClickableStyle,
                modifier = Modifier.testTag(SelectRoundDialogTestTag.SELECTED_ROUND_ROW)
        )
        if (hasNoRounds) {
            Text(
                    text = stringResource(R.string.create_round__no_rounds_found),
                    color = CodexTheme.colors.warningOnAppBackground,
                    textAlign = TextAlign.Center,
                    style = CodexTypography.SMALL_PLUS,
                    fontStyle = FontStyle.Italic,
            )
        }
    }
}

@Composable
private fun RoundInfoHints(
        distanceUnit: String?,
        arrowCounts: List<RoundArrowCount>?,
        roundSubtypeDistances: List<RoundDistance>?,
        helpListener: (HelpShowcaseIntent) -> Unit,
) {
    val separator = stringResource(R.string.general_comma_separator)
    val faceSizeUnit = stringResource(R.string.units_cm_short)

    if (arrowCounts != null) {
        DataRow(
                title = stringResource(R.string.create_round__arrow_count_indicator),
                text = arrowCounts
                        .sortedBy { it.distanceNumber }
                        .joinToString(separator) {
                            DecimalFormat("#.#").format(it.arrowCount / 12.0)
                        },
                helpState = HelpShowcaseItem(
                        helpTitle = stringResource(R.string.help_create_round__arrow_count_indicator_title),
                        helpBody = stringResource(R.string.help_create_round__arrow_count_indicator_body),
                ).asHelpState(helpListener),
                titleStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Start),
        )
    }
    roundSubtypeDistances?.takeIf { it.isNotEmpty() }?.let { distances ->
        DataRow(
                title = stringResource(R.string.create_round__distance_indicator),
                text = distances
                        .sortedBy { it.distanceNumber }
                        .joinToString(separator) { it.distance.toString() + distanceUnit },
                helpState = HelpShowcaseItem(
                        helpTitle = stringResource(R.string.help_create_round__distance_indicator_title),
                        helpBody = stringResource(R.string.help_create_round__distance_indicator_body),
                ).asHelpState(helpListener),
                titleStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Start),
        )
    }
    if (arrowCounts != null) {
        DataRow(
                title = stringResource(R.string.create_round__face_size_indicator),
                text = arrowCounts
                        .sortedBy { it.distanceNumber }
                        .joinToString(separator) { (it.faceSizeInCm.roundToInt()).toString() + faceSizeUnit },
                helpState = HelpShowcaseItem(
                        helpTitle = stringResource(R.string.help_create_round__face_size_indicator_title),
                        helpBody = stringResource(R.string.help_create_round__face_size_indicator_body),
                ).asHelpState(helpListener),
                titleStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Start),
        )
    }
}

@Composable
fun SelectRoundDialog(
        isShown: Boolean,
        enabledFilters: SelectRoundEnabledFilters,
        displayedRounds: List<Round>,
        listener: (SelectRoundDialogIntent) -> Unit,
) {
    SimpleDialog(
            isShown = isShown,
            onDismissListener = { listener(CloseRoundDialog) },
    ) {
        SimpleDialogContent(
                title = stringResource(R.string.create_round__select_a_round_title),
                negativeButton = ButtonState(
                        text = stringResource(R.string.general_cancel),
                        onClick = { listener(CloseRoundDialog) },
                ),
                positiveButton = ButtonState(
                        text = stringResource(R.string.create_round__no_round),
                        onClick = { listener(NoRoundSelected) },
                ),
                modifier = Modifier.testTag(SelectRoundDialogTestTag.ROUND_DIALOG)
        ) {
            Column {
                Row(
                        verticalAlignment = Alignment.CenterVertically,
                ) {
                    LazyRow(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier
                                    .weight(1f)
                                    .testTag(SelectRoundDialogTestTag.FILTER_LIST)
                    ) {
                        item {
                            Text(
                                    text = stringResource(R.string.create_round__select_a_round_filters_title),
                                    style = CodexTypography.SMALL,
                                    modifier = Modifier.padding(end = 5.dp)
                            )
                        }
                        items(SelectRoundFilter.entries) { filter ->
                            CodexChip(
                                    text = stringResource(filter.chipText),
                                    state = CodexNewChipState(
                                            selected = enabledFilters.contains(filter),
                                            testTag = SelectRoundDialogTestTag.FILTER,
                                    ),
                                    colours = ChipColours.Defaults.onDialog(),
                            ) { listener(FilterClicked(filter)) }
                        }
                    }
                    IconButton(onClick = { listener(ClearFilters) }) {
                        Icon(
                                painter = painterResource(R.drawable.ic_baseline_clear_filter),
                                contentDescription = stringResource(
                                        R.string.create_round__select_a_round_filter_clear_all,
                                ),
                        )
                    }
                }
                ItemSelector(
                        displayItems = displayedRounds.sortedWith { round1, round2 ->
                            Sorting.NUMERIC_STRING_SORT.compare(round1.name, round2.name)
                        },
                        onItemClicked = { listener(RoundSelected(it)) },
                        modifier = Modifier
                                .padding(vertical = 10.dp)
                                .weight(1f)
                )
            }
        }
    }
}

@Composable
fun SelectSubtypeDialog(
        isShown: Boolean,
        subTypes: List<RoundSubType>,
        getDistance: (RoundSubType) -> Int,
        distanceUnit: String?,
        listener: (SelectRoundDialogIntent) -> Unit,
) {
    SimpleDialog(
            isShown = isShown,
            onDismissListener = { listener(CloseSubTypeDialog) },
    ) {
        SimpleDialogContent(
                title = stringResource(R.string.create_round__select_a_subtype_title),
                message = stringResource(R.string.create_round__select_a_subtype_message),
                negativeButton = ButtonState(
                        text = stringResource(R.string.general_cancel),
                        onClick = { listener(CloseSubTypeDialog) },
                ),
                modifier = Modifier.testTag(SelectRoundDialogTestTag.SUBTYPE_DIALOG)
        ) {
            ItemSelector(
                    displayItems = subTypes.sortedByDescending { getDistance(it) },
                    onItemClicked = { listener(SubTypeSelected(it)) },
            ) { item, contentModifier ->
                val distanceString = getDistance(item).toString() + distanceUnit!!
                Text(
                        text = "($distanceString)",
                        style = CodexTypography.SMALL,
                        modifier = contentModifier
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T : NamedItem> ItemSelector(
        displayItems: Iterable<T>,
        onItemClicked: (T) -> Unit,
        modifier: Modifier = Modifier,
        extraContent: (@Composable (T, Modifier) -> Unit)? = null,
) {
    LazyColumn(
            horizontalAlignment = Alignment.Start,
            modifier = modifier
    ) {
        items(displayItems.toList()) { item ->
            FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier
                            .testTag(SelectRoundDialogTestTag.ROUND_DIALOG_ITEM)
                            .clickable { onItemClicked(item) }
                            .fillMaxWidth()
                            .padding(10.dp)
            ) {
                val contentModifier = Modifier.align(Alignment.Bottom)
                item.label.get().split(" ").forEach { itemLabelWord ->
                    Text(
                            text = itemLabelWord,
                            style = CodexTypography.NORMAL,
                            modifier = contentModifier
                    )
                }
                extraContent?.invoke(item, contentModifier)
            }
        }
    }
}

enum class SelectRoundDialogTestTag : CodexTestTag {
    ROUND_DIALOG,
    SUBTYPE_DIALOG,
    ROUND_DIALOG_ITEM,
    FILTER_LIST,
    FILTER,
    SELECTED_ROUND_ROW,
    SELECTED_SUBTYPE_ROW,
    ;

    override val screenName: String
        get() = "SELECT_ROUND_DIALOG"

    override fun getElement(): String = name
}

@Preview
@Composable
fun SelectRoundDialog_Preview() {
    DialogPreviewHelper {
        SelectRoundDialog(
                isShown = true,
                displayedRounds = List(20) { RoundPreviewHelper.outdoorImperialRoundData.round },
                enabledFilters = SelectRoundEnabledFilters(),
                listener = { },
        )
    }
}

@Preview
@Composable
fun SelectSubtypeDialog_Preview() {
    DialogPreviewHelper {
        SelectSubtypeDialog(
                isShown = true,
                subTypes = RoundPreviewHelper.wa1440RoundData.roundSubTypes!!,
                getDistance = { subType ->
                    RoundPreviewHelper.wa1440RoundData.roundDistances!!
                            .find { it.subTypeId == subType.subTypeId }!!.distance
                },
                distanceUnit = "m",
                listener = {},
        )
    }
}
