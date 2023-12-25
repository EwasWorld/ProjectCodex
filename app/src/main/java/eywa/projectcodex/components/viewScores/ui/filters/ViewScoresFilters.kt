package eywa.projectcodex.components.viewScores.ui.filters

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpState
import eywa.projectcodex.common.sharedUi.CodexIconButton
import eywa.projectcodex.common.sharedUi.CodexIconInfo
import eywa.projectcodex.common.sharedUi.DataRow
import eywa.projectcodex.common.sharedUi.UpdateCalendarInfo
import eywa.projectcodex.common.sharedUi.codexDateSelector
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.codexTheme.asClickableStyle
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialog
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogIntent
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogState
import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundRows
import eywa.projectcodex.common.utils.DateTimeFormat
import eywa.projectcodex.components.viewScores.ui.ViewScoresActionBar
import java.util.Calendar


@Composable
private fun TextStyle.asCustomClickableStyle() = asClickableStyle().copy(color = CodexTheme.colors.appBackground)

@Composable
internal fun ViewScoresFilters(
        state: ViewScoresFiltersState,
        listener: (ViewScoresFiltersIntent) -> Unit,
        helpShowcaseListener: (HelpShowcaseIntent) -> Unit,
) {
    Column {
        AnimatedVisibility(
                visible = state.isExpanded,
                enter = fadeIn() + expandIn(expandFrom = Alignment.BottomCenter),
                exit = fadeOut() + shrinkOut(shrinkTowards = Alignment.BottomCenter),
        ) {
            ProvideTextStyle(CodexTypography.NORMAL.copy(color = CodexTheme.colors.onFloatingActions)) {
                val clickableStyle = LocalTextStyle.current.asCustomClickableStyle()

                Box(
                        contentAlignment = Alignment.TopEnd,
                ) {
                    Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                    .padding(horizontal = 20.dp, vertical = 15.dp)
                                    .fillMaxWidth()
                    ) {
                        Text(
                                text = stringResource(R.string.view_scores__filters_title),
                                style = CodexTypography.NORMAL,
                                color = CodexTheme.colors.onFloatingActions,
                                fontWeight = FontWeight.Bold,
                        )

                        DateFilters(state, listener, helpShowcaseListener)

                        RoundsFilter(
                                roundFilter = state.roundFilter,
                                selectRoundDialogState = state.selectRoundDialogState,
                                helpShowcaseListener = helpShowcaseListener,
                                onClear = { listener(ViewScoresFiltersIntent.ClearRoundsFilter) },
                                onUpdate = { listener(ViewScoresFiltersIntent.UpdateRoundsFilter(it)) },
                        )

                        DataRow(
                                title = stringResource(R.string.view_scores__filters_personal_bests),
                                text = stringResource(
                                        if (state.personalBestsFilter) R.string.view_scores__filters_personal_bests_only
                                        else R.string.view_scores__filters_no_filter
                                ),
                                textClickableStyle = clickableStyle,
                                onClick = { listener(ViewScoresFiltersIntent.ClickPbFilter) }
                        )

                        DataRow(
                                title = stringResource(R.string.view_scores__filters_type_title),
                                text = state.typeFilter.label.get(),
                                textClickableStyle = clickableStyle,
                                onClick = { listener(ViewScoresFiltersIntent.ClickTypeFilter) }
                        )
                    }

                    CodexIconInfo.VectorIcon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.view_scores__filters_close),
                            tint = CodexTheme.colors.onFloatingActions,
                    ).CodexIcon(
                            modifier = Modifier
                                    .clickable { listener(ViewScoresFiltersIntent.CloseFilters) }
                                    .padding(15.dp)
                    )
                }
            }
        }
        AnimatedVisibility(
                visible = !state.isExpanded,
                enter = fadeIn() + expandIn(expandFrom = Alignment.BottomCenter),
                exit = fadeOut() + shrinkOut(shrinkTowards = Alignment.BottomCenter),
        ) {
            Box(
                    contentAlignment = Alignment.BottomEnd,
            ) {
                val semantics = pluralStringResource(
                        R.plurals.view_scores__active_filters_indicator_semantics,
                        state.activeFilterCount,
                        state.activeFilterCount,
                )
                CodexIconButton(
                        icon = CodexIconInfo.VectorIcon(
                                imageVector = Icons.Default.FilterAlt,
                                contentDescription = stringResource(R.string.view_scores__filters_dialog_toggle),
                                modifier = Modifier.padding(vertical = 5.dp)
                        ),
                        helpState = HelpState(
                                helpListener = helpShowcaseListener,
                                helpTitle = stringResource(R.string.help_view_scores__filters_dialog_toggle_title),
                                helpBody = stringResource(R.string.help_view_scores__filters_dialog_toggle_body),
                        ),
                        onClick = { listener(ViewScoresFiltersIntent.OpenFilters) },
                        caption = state.activeFilterCount.toString(),
                        captionModifier = Modifier.semantics { contentDescription = semantics },
                        captionStyle = CodexTypography.SMALL,
                        modifier = Modifier.semantics(true) {}
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.DateFilters(
        state: ViewScoresFiltersState,
        listener: (ViewScoresFiltersIntent) -> Unit,
        helpShowcaseListener: (HelpShowcaseIntent) -> Unit,
) {
    DateFilter(
            title = stringResource(R.string.view_scores__filters_from_date),
            date = state.from,
            helpState = HelpState(
                    helpListener = helpShowcaseListener,
                    helpTitle = stringResource(R.string.help_view_scores__filters_from_title),
                    helpBody = stringResource(R.string.help_view_scores__filters_from_body),
            ),
            onUpdate = { listener(ViewScoresFiltersIntent.UpdateFromFilter(it)) },
            onClear = { listener(ViewScoresFiltersIntent.ClearFromFilter) },
    )
    DateFilter(
            title = stringResource(R.string.view_scores__filters_until_date),
            date = state.until,
            helpState = HelpState(
                    helpListener = helpShowcaseListener,
                    helpTitle = stringResource(R.string.help_view_scores__filters_until_title),
                    helpBody = stringResource(R.string.help_view_scores__filters_until_body),
            ),
            isValidDate = state.dateRangeIsValid,
            onUpdate = { listener(ViewScoresFiltersIntent.UpdateUntilFilter(it)) },
            onClear = { listener(ViewScoresFiltersIntent.ClearUntilFilter) },
    )
    if (!state.dateRangeIsValid) {
        Text(
                text = stringResource(R.string.view_scores__filters_invalid_dates),
                style = CodexTypography.SMALL,
                color = CodexTheme.colors.warningOnAppBackground,
                fontStyle = FontStyle.Italic,
                modifier = Modifier
                        .padding(bottom = 5.dp)
                        .clearAndSetSemantics { }
        )
    }
}

@Composable
private fun DateFilter(
        title: String,
        date: Calendar?,
        helpState: HelpState,
        isValidDate: Boolean = true,
        onUpdate: (UpdateCalendarInfo) -> Unit,
        onClear: () -> Unit,
) {
    val clickableStyle = LocalTextStyle.current.asCustomClickableStyle()
    val datePicker = codexDateSelector(
            context = LocalContext.current,
            date = date ?: Calendar.getInstance(),
            updateDateListener = onUpdate,
    )
    val customActions = listOf(
            CustomAccessibilityAction(
                    label = stringResource(R.string.view_scores__filters_clear),
                    action = { onClear(); true },
            ),
    )
    val errorText = stringResource(R.string.view_scores__filters_invalid_dates)

    DataRow(
            title = title,
            helpState = helpState,
    ) {
        Row(
                verticalAlignment = Alignment.CenterVertically,
        ) {
            if (date == null) {
                Text(
                        text = stringResource(R.string.view_scores__filters_no_filter),
                        style = clickableStyle,
                        modifier = Modifier.clickable { datePicker.show() }
                )
            }
            else {
                Text(
                        text = DateTimeFormat.LONG_DATE.format(date),
                        style = clickableStyle,
                        modifier = Modifier
                                .clickable { datePicker.show() }
                                .semantics {
                                    this.customActions = customActions
                                    if (!isValidDate) error(errorText)
                                }
                                .padding(end = 3.dp)
                )
                if (!isValidDate) {
                    CodexIconInfo.VectorIcon(
                            imageVector = Icons.Default.WarningAmber,
                            tint = CodexTheme.colors.warningOnAppBackground,
                    ).CodexIcon(
                            modifier = Modifier
                                    .clearAndSetSemantics { }
                                    .scale(0.7f)
                                    .padding(start = 3.dp)
                    )
                }
                ClearIcon(onClear)
            }
        }
    }
}

@Composable
private fun RoundsFilter(
        roundFilter: Boolean,
        selectRoundDialogState: SelectRoundDialogState,
        helpShowcaseListener: (HelpShowcaseIntent) -> Unit,
        onClear: () -> Unit,
        onUpdate: (SelectRoundDialogIntent) -> Unit
) {
    val clickableStyle = LocalTextStyle.current.asCustomClickableStyle()

    Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        if (roundFilter) {
            SelectRoundRows(
                    state = selectRoundDialogState,
                    helpListener = helpShowcaseListener,
                    textClickableStyle = clickableStyle,
                    listener = onUpdate
            )
            ClearIcon(onClear)
        }
        else {
            SelectRoundDialog(
                    isShown = selectRoundDialogState.isRoundDialogOpen,
                    displayedRounds = selectRoundDialogState.filteredRounds,
                    enabledFilters = selectRoundDialogState.filters,
                    listener = { onUpdate(it) },
            )

            DataRow(
                    title = stringResource(R.string.create_round__round),
                    text = stringResource(R.string.view_scores__filters_no_filter),
                    onClick = { onUpdate(SelectRoundDialogIntent.RoundIntent.OpenRoundDialog) },
                    textClickableStyle = clickableStyle
            )
        }
    }
}

@Composable
private fun ClearIcon(
        onClear: () -> Unit,
) {
    CodexIconInfo.VectorIcon(
            imageVector = Icons.Default.Close,
            tint = CodexTheme.colors.onFloatingActions,
    ).CodexIcon(
            modifier = Modifier
                    .clickable { onClear() }
                    .clearAndSetSemantics { }
                    .scale(0.7f)
    )
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun ViewScoresFilters_Preview() {
    CodexTheme {
        ViewScoresActionBar(
                modifier = Modifier.padding(10.dp)
        ) {
            ViewScoresFilters(
                    ViewScoresFiltersState(
                            isExpanded = true,
                            selectRoundDialogState = SelectRoundDialogState(
                                    allRounds = listOf(RoundPreviewHelper.outdoorImperialRoundData),
                            ),
                            from = Calendar.getInstance(),
                            until = Calendar.getInstance(),
                            roundFilter = true,
                            personalBestsFilter = true,
                            typeFilter = ViewScoresFiltersTypes.COUNT,
                    ),
                    listener = {},
                    helpShowcaseListener = {},
            )
        }
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun NoFiltersOn_ViewScoresFilters_Preview() {
    CodexTheme {
        ViewScoresActionBar(
                modifier = Modifier.padding(10.dp)
        ) {
            ViewScoresFilters(
                    ViewScoresFiltersState(
                            isExpanded = true,
                            selectRoundDialogState = SelectRoundDialogState(
                                    allRounds = listOf(RoundPreviewHelper.outdoorImperialRoundData),
                            ),
                    ),
                    listener = {},
                    helpShowcaseListener = {},
            )
        }
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun Collapsed_ViewScoresFilters_Preview() {
    CodexTheme {
        ViewScoresActionBar(
                modifier = Modifier.padding(10.dp)
        ) {
            ViewScoresFilters(
                    ViewScoresFiltersState(
                            isExpanded = false,
                            selectRoundDialogState = SelectRoundDialogState(
                                    allRounds = listOf(RoundPreviewHelper.outdoorImperialRoundData),
                            ),
                            personalBestsFilter = true,
                            roundFilter = true
                    ),
                    listener = {},
                    helpShowcaseListener = {},
            )
        }
    }
}
