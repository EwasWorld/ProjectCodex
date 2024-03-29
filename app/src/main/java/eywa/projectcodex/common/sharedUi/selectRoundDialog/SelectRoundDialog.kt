package eywa.projectcodex.common.sharedUi.selectRoundDialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpState
import eywa.projectcodex.common.sharedUi.*
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.helperInterfaces.NamedItem
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogIntent.RoundIntent.*
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogIntent.SubTypeIntent.*
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.common.utils.Sorting
import eywa.projectcodex.components.newScore.NewScoreTestTag
import eywa.projectcodex.database.rounds.*
import java.text.DecimalFormat
import kotlin.math.roundToInt

// TODO Turn empty rounds into a different field and show a warning message instead
@Composable
fun SelectRoundRows(
        state: SelectRoundDialogState,
        helpListener: (HelpShowcaseIntent) -> Unit,
        style: TextStyle = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground),
        listener: (SelectRoundDialogIntent) -> Unit,
) = SelectRoundRows(
        displayedRound = (
                if (state.allRounds.isNullOrEmpty()) stringResource(R.string.create_round__no_rounds_found)
                else state.selectedRound?.round?.displayName ?: stringResource(R.string.create_round__no_round)
                ),
        displayedSubtype = state.selectedSubType?.name,
        isSelectRoundDialogOpen = state.isRoundDialogOpen,
        isSelectSubtypeDialogOpen = state.isSubtypeDialogOpen,
        rounds = state.allRounds?.map { it.round } ?: emptyList(),
        filters = state.filters,
        subTypes = state.selectedRound?.roundSubTypes ?: emptyList(),
        arrowCounts = state.selectedRound?.roundArrowCounts,
        roundSubtypeDistances = state.selectedRound?.getDistances(state.selectedSubTypeId),
        distanceUnit = state.selectedRound?.getDistanceUnit(),
        getDistance = { subType -> state.getFurthestDistance(subType)!!.distance },
        helpListener = helpListener,
        style = style,
        listener = listener,
)

@Composable
fun SelectRoundRows(
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
        style: TextStyle = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground),
        listener: (SelectRoundDialogIntent) -> Unit,
) {
    ProvideTextStyle(value = style) {
        SelectRoundDialog(
                isShown = isSelectRoundDialogOpen,
                displayedRounds = rounds,
                enabledFilters = filters,
                listener = listener,
        )
        SelectSubtypeDialog(
                isShown = isSelectSubtypeDialogOpen,
                subTypes = subTypes,
                getDistance = getDistance,
                distanceUnit = distanceUnit,
                listener = listener,
        )


        DataRow(
                title = stringResource(R.string.create_round__round),
                text = displayedRound,
                helpState = HelpState(
                        helpListener = helpListener,
                        helpTitle = stringResource(R.string.help_create_round__round_title),
                        helpBody = stringResource(R.string.help_create_round__round_body),
                ),
                modifier = Modifier.testTag(NewScoreTestTag.SELECTED_ROUND.getTestTag()),
                onClick = { listener(OpenRoundDialog) },
        )
        if (displayedSubtype != null) {
            DataRow(
                    title = stringResource(R.string.create_round__round_sub_type),
                    text = displayedSubtype,
                    helpState = HelpState(
                            helpListener = helpListener,
                            helpTitle = stringResource(R.string.help_create_round__sub_round_title),
                            helpBody = stringResource(R.string.help_create_round__sub_round_body),
                    ),
                    modifier = Modifier.testTag(NewScoreTestTag.SELECTED_SUBTYPE.getTestTag()),
                    onClick = { listener(OpenSubTypeDialog) },
            )
        }


        RoundInfoHints(distanceUnit, arrowCounts, roundSubtypeDistances, helpListener)
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
                helpState = HelpState(
                        helpTitle = stringResource(R.string.help_create_round__arrow_count_indicator_title),
                        helpBody = stringResource(R.string.help_create_round__arrow_count_indicator_body),
                        helpListener = helpListener,
                ),
                style = LocalTextStyle.current.copy(textAlign = TextAlign.Start),
        )
    }
    roundSubtypeDistances?.takeIf { it.isNotEmpty() }?.let { distances ->
        DataRow(
                title = stringResource(R.string.create_round__distance_indicator),
                text = distances
                        .sortedBy { it.distanceNumber }
                        .joinToString(separator) { it.distance.toString() + distanceUnit },
                helpState = HelpState(
                        helpTitle = stringResource(R.string.help_create_round__distance_indicator_title),
                        helpBody = stringResource(R.string.help_create_round__distance_indicator_body),
                        helpListener = helpListener,
                ),
                style = LocalTextStyle.current.copy(textAlign = TextAlign.Start),
        )
    }
    if (arrowCounts != null) {
        DataRow(
                title = stringResource(R.string.create_round__face_size_indicator),
                text = arrowCounts
                        .sortedBy { it.distanceNumber }
                        .joinToString(separator) { (it.faceSizeInCm.roundToInt()).toString() + faceSizeUnit },
                helpState = HelpState(
                        helpTitle = stringResource(R.string.help_create_round__face_size_indicator_title),
                        helpBody = stringResource(R.string.help_create_round__face_size_indicator_body),
                        helpListener = helpListener,
                ),
                style = LocalTextStyle.current.copy(textAlign = TextAlign.Start),
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
                modifier = Modifier.testTag(SelectRoundDialogTestTag.ROUND_DIALOG.getTestTag())
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
                        items(SelectRoundFilter.values()) { filter ->
                            CodexChip(
                                    text = stringResource(filter.chipText),
                                    state = CodexNewChipState(
                                            selected = enabledFilters.contains(filter),
                                            testTag = SelectRoundDialogTestTag.FILTER.getTestTag()
                                    ),
                                    colours = ChipColours.Defaults.onDialog(),
                            ) { listener(FilterClicked(filter)) }
                        }
                    }
                    IconButton(onClick = { listener(ClearFilters) }) {
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
                modifier = Modifier.testTag(SelectRoundDialogTestTag.SUBTYPE_DIALOG.getTestTag())
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
                        modifier = Modifier.testTag(SelectRoundDialogTestTag.ROUND_DIALOG_ITEM.getTestTag())
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

enum class SelectRoundDialogTestTag : CodexTestTag {
    ROUND_DIALOG,
    SUBTYPE_DIALOG,
    ROUND_DIALOG_ITEM,
    FILTER,
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
