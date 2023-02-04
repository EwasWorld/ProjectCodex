package eywa.projectcodex.common.sharedUi.selectRoundDialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.ComposeHelpShowcaseMap
import eywa.projectcodex.common.sharedUi.*
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.helperInterfaces.NamedItem
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogIntent.*
import eywa.projectcodex.common.utils.Sorting
import eywa.projectcodex.common.utils.getDistanceUnit
import eywa.projectcodex.common.utils.getDistances
import eywa.projectcodex.components.newScore.NewScoreScreen
import eywa.projectcodex.database.rounds.*
import java.text.DecimalFormat
import kotlin.math.roundToInt

@Composable
fun SelectRoundRows(
        isSelectRoundDialogOpen: Boolean,
        isSelectSubtypeDialogOpen: Boolean,
        selectedRound: FullRoundInfo?,
        selectedSubtypeId: Int?,
        rounds: List<FullRoundInfo>?,
        filters: SelectRoundEnabledFilters,
        helpInfo: ComposeHelpShowcaseMap,
        style: TextStyle = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground),
        listener: (SelectRoundDialogIntent) -> Unit,
) = SelectRoundRows(
        displayedRound =
        if (rounds == null) stringResource(R.string.create_round__no_rounds_found)
        else selectedRound?.round?.displayName ?: stringResource(R.string.create_round__no_round),
        displayedSubtype = selectedRound?.roundSubTypes?.find { it.subTypeId == selectedSubtypeId }?.name,
        isSelectRoundDialogOpen = isSelectRoundDialogOpen,
        isSelectSubtypeDialogOpen = isSelectSubtypeDialogOpen,
        rounds = rounds?.map { it.round } ?: emptyList(),
        filters = filters,
        subTypes = selectedRound?.roundSubTypes ?: emptyList(),
        arrowCounts = selectedRound?.roundArrowCounts,
        roundSubtypeDistances = selectedRound?.getDistances(selectedSubtypeId),
        distanceUnit = selectedRound?.getDistanceUnit(),
        getDistance = { subType ->
            selectedRound?.getDistances(subType.subTypeId)?.maxByOrNull { it.distance }!!.distance
        },
        helpInfo = helpInfo,
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
        helpInfo: ComposeHelpShowcaseMap,
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
                title = R.string.create_round__round,
                helpTitle = R.string.help_create_round__round_title,
                helpBody = R.string.help_create_round__round_body,
                helpInfo = helpInfo,
        ) {
            Text(
                    text = displayedRound,
                    color = CodexTheme.colors.linkText,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                            .clickable { listener(OpenRoundSelectDialog) }
                            .testTag(NewScoreScreen.TestTag.SELECTED_ROUND)
            )
        }
        if (displayedSubtype != null) {
            DataRow(
                    title = R.string.create_round__round_sub_type,
                    helpTitle = R.string.help_create_round__sub_round_title,
                    helpBody = R.string.help_create_round__sub_round_body,
                    helpInfo = helpInfo,
            ) {
                Text(
                        text = displayedSubtype,
                        color = CodexTheme.colors.linkText,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier
                                .clickable { listener(OpenSubTypeSelectDialog) }
                                .testTag(NewScoreScreen.TestTag.SELECTED_SUBTYPE)
                )
            }
        }


        RoundInfoHints(distanceUnit, arrowCounts, roundSubtypeDistances, helpInfo)
    }
}

@Composable
private fun RoundInfoHints(
        distanceUnit: String?,
        arrowCounts: List<RoundArrowCount>?,
        roundSubtypeDistances: List<RoundDistance>?,
        helpInfo: ComposeHelpShowcaseMap,
) {
    val separator = stringResource(R.string.general_comma_separator)
    val faceSizeUnit = stringResource(R.string.units_cm_short)

    if (arrowCounts != null) {
        DataRow(
                title = R.string.create_round__arrow_count_indicator,
                helpTitle = R.string.help_create_round__arrow_count_indicator_title,
                helpBody = R.string.help_create_round__arrow_count_indicator_body,
                helpInfo = helpInfo,
        ) {
            Text(
                    text = arrowCounts
                            .sortedBy { it.distanceNumber }
                            .joinToString(separator) {
                                DecimalFormat("#.#").format(it.arrowCount / 12.0)
                            },
                    textAlign = TextAlign.Start,
            )
        }
    }
    roundSubtypeDistances?.takeIf { it.isNotEmpty() }?.let { distances ->
        DataRow(
                title = R.string.create_round__distance_indicator,
                helpTitle = R.string.help_create_round__distance_indicator_title,
                helpBody = R.string.help_create_round__distance_indicator_body,
                helpInfo = helpInfo,
        ) {
            Text(
                    text = distances
                            .sortedBy { it.distanceNumber }
                            .joinToString(separator) { it.distance.toString() + distanceUnit },
                    textAlign = TextAlign.Start,
            )
        }
    }
    if (arrowCounts != null) {
        DataRow(
                title = R.string.create_round__face_size_indicator,
                helpTitle = R.string.help_create_round__face_size_indicator_title,
                helpBody = R.string.help_create_round__face_size_indicator_body,
                helpInfo = helpInfo,
        ) {
            Text(
                    text = arrowCounts
                            .sortedBy { it.distanceNumber }
                            .joinToString(separator) { (it.faceSizeInCm.roundToInt()).toString() + faceSizeUnit },
                    textAlign = TextAlign.Start,
            )
        }
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
                modifier = Modifier.testTag(SelectRoundDialogTestTag.ROUND_DIALOG)
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
                                            testTag = SelectRoundDialogTestTag.fromFilterName(filter)
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
fun SelectSubtypeDialog(
        isShown: Boolean,
        subTypes: List<RoundSubType>,
        getDistance: (RoundSubType) -> Int,
        distanceUnit: String?,
        listener: (SelectRoundDialogIntent) -> Unit,
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
                modifier = Modifier.testTag(SelectRoundDialogTestTag.SUBTYPE_DIALOG)
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

object SelectRoundDialogTestTag {
    const val ROUND_DIALOG = "SELECT_ROUND_DIALOG"
    const val SUBTYPE_DIALOG = "SELECT_ROUND_SUBTYPE_DIALOG"

    fun fromFilterName(filter: SelectRoundFilter) = "SELECT_ROUND_FILTER_${filter.name}"
}
