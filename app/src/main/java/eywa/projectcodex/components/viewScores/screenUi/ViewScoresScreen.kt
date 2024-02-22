package eywa.projectcodex.components.viewScores.screenUi

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.logging.CustomLogger
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.navigation.NavArgument
import eywa.projectcodex.common.sharedUi.LoadingScreen
import eywa.projectcodex.common.sharedUi.SetOfDialogs
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.codexTheme.asClickableStyle
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.common.utils.ToastSpamPrevention
import eywa.projectcodex.components.viewScores.ViewScoresIntent
import eywa.projectcodex.components.viewScores.ViewScoresIntent.*
import eywa.projectcodex.components.viewScores.ViewScoresState
import eywa.projectcodex.components.viewScores.ViewScoresViewModel
import eywa.projectcodex.components.viewScores.actionBar.ViewScoresActionBar
import eywa.projectcodex.components.viewScores.actionBar.filters.ViewScoresFilters
import eywa.projectcodex.components.viewScores.actionBar.filters.ViewScoresFiltersIntent
import eywa.projectcodex.components.viewScores.actionBar.filters.ViewScoresFiltersState
import eywa.projectcodex.components.viewScores.actionBar.multiSelectBar.MultiSelectBar
import eywa.projectcodex.components.viewScores.data.ViewScoresEntryList
import eywa.projectcodex.components.viewScores.data.ViewScoresEntryPreviewProvider
import eywa.projectcodex.components.viewScores.data.ViewScoresEntryPreviewProvider.setPersonalBests
import eywa.projectcodex.components.viewScores.dialogs.ViewScoresDeleteEntryDialog
import eywa.projectcodex.components.viewScores.dialogs.ViewScoresEmptyListDialog
import eywa.projectcodex.components.viewScores.dialogs.convertScoreDialog.ConvertScoreDialog

private const val LOG_TAG = "ViewScores"

@Composable
fun ViewScoresScreen(
        navController: NavController,
        viewModel: ViewScoresViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val listener = { it: ViewScoresIntent -> viewModel.handle(it) }
    ViewScoresScreen(state, listener)

    val context = LocalContext.current
    LaunchedEffect(state) { handleEffects(state, navController, context, listener) }
}

private fun handleEffects(
        state: ViewScoresState,
        navController: NavController,
        context: Context,
        listener: (ViewScoresIntent) -> Unit,
) {
    if (state.openEmailClicked) {
        CodexNavRoute.EMAIL_SCORE.navigate(navController)
        listener(HandledEmailOpened)
    }

    if (state.multiSelectEmailNoSelection) {
        ToastSpamPrevention.displayToast(
                context,
                context.resources.getString(R.string.err_view_score__no_rounds_selected)
        )
        listener(HandledEmailNoSelection)
    }

    if (state.openScorePadClicked) {
        if (state.lastClickedEntryId != null) {
            CodexNavRoute.SHOOT_DETAILS_SCORE_PAD.navigate(
                    navController,
                    mapOf(NavArgument.SHOOT_ID to state.lastClickedEntryId.toString()),
            )
        }
        listener(HandledScorePadOpened)
    }

    if (state.openAddCountClicked) {
        if (state.lastClickedEntryId != null) {
            CodexNavRoute.SHOOT_DETAILS_ADD_COUNT.navigate(
                    navController,
                    mapOf(NavArgument.SHOOT_ID to state.lastClickedEntryId.toString()),
            )
        }
        listener(HandledAddCountOpened)
    }

    if (state.openAddEndOnCompletedRound) {
        if (state.lastClickedEntryId != null) {
            CustomLogger.customLogger.w(LOG_TAG, "Tried to continue completed round")
            ToastSpamPrevention.displayToast(
                    context,
                    context.resources.getString(R.string.err_view_score__round_already_complete)
            )
        }
        listener(HandledAddEndOnCompletedRound)
    }

    if (state.openAddEndClicked) {
        if (state.lastClickedEntryId != null) {
            CodexNavRoute.SHOOT_DETAILS_ADD_END.navigate(
                    navController,
                    mapOf(NavArgument.SHOOT_ID to state.lastClickedEntryId.toString()),
            )
        }
        listener(HandledAddEndOpened)
    }

    if (state.openEditInfoClicked) {
        if (state.lastClickedEntryId != null) {
            CodexNavRoute.NEW_SCORE.navigate(
                    navController,
                    mapOf(NavArgument.SHOOT_ID to state.lastClickedEntryId.toString()),
            )
        }
        listener(HandledEditInfoOpened)
    }

    if (state.noRoundsDialogOkClicked) {
        navController.popBackStack()
        listener(HandledNoRoundsDialogOkClicked)
    }
}

@Composable
fun ViewScoresScreen(
        state: ViewScoresState,
        listener: (ViewScoresIntent) -> Unit,
) {
    val lazyListState = rememberLazyListState()
    listener(HelpShowcaseAction(HelpShowcaseIntent.Clear))

    SetOfDialogs(
            state.showNoItemsDialog to { ViewScoresEmptyListDialog(isShown = it, listener = listener) },
            (state.lastClickedEntryId != null && state.convertScoreDialogOpen) to {
                ConvertScoreDialog(
                        isShown = it,
                        listener = { action -> listener(ConvertScoreAction(action)) },
                )
            },
            state.deleteDialogOpen to {
                ViewScoresDeleteEntryDialog(
                        isShown = it,
                        listener = listener,
                        entry = state.lastClickedEntry,
                )
            },
    )

    Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                    .fillMaxSize()
                    .background(CodexTheme.colors.appBackground)
                    .testTag(ViewScoresTestTag.SCREEN.getTestTag())
    ) {
        // If data has not been loaded
        if (state.data == null) {
            LoadingScreen()
        }
        // If there is data to show
        else if (state.data.isNotEmpty()) {
            LazyColumn(
                    state = lazyListState,
                    contentPadding = PaddingValues(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                            .testTag(ViewScoresTestTag.LAZY_COLUMN.getTestTag())
                            .align(Alignment.TopCenter)
            ) {
                items(state.data.size) { entryIndex ->
                    val entry = state.data[entryIndex]
                    ViewScoresListItem(
                            entry = entry,
                            entryIndex = entryIndex,
                            isInMultiSelectMode = state.isInMultiSelectMode,
                            dropdownMenuItems = entry.getDropdownMenuItems(),
                            dropdownExpanded = state.dropdownMenuOpen && entry.id == state.lastClickedEntryId,
                            listener = listener,
                            helpListener = { listener(HelpShowcaseAction(it)) },
                    ) {
                        if (entry.isCount) {
                            ViewScoresCountRow(
                                    entries = ViewScoresEntryList(entry),
                                    entryIndex = entryIndex,
                                    helpListener = { listener(HelpShowcaseAction(it)) },
                            )
                        }
                        else {
                            ViewScoresEntryRow(
                                    entry = entry,
                                    entryIndex = entryIndex,
                                    helpListener = { listener(HelpShowcaseAction(it)) },
                            )
                        }
                    }
                }
            }
        }
        // If there is no data due to filters
        else if (state.filters.size > 0) {
            Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(CodexTheme.dimens.screenPadding)
            ) {
                Text(
                        text = stringResource(R.string.view_scores__filters_no_results),
                        style = CodexTypography.NORMAL,
                        color = CodexTheme.colors.onAppBackground,
                        textAlign = TextAlign.Center,
                )
                Text(
                        text = stringResource(R.string.view_scores__filters_clear_all),
                        style = CodexTypography.NORMAL.asClickableStyle(),
                        modifier = Modifier
                                .clickable { listener(FiltersAction(ViewScoresFiltersIntent.ClearAllFilters)) }
                )
            }
        }

        AnimatedVisibility(
                visible = state.data != null,
                enter = fadeIn(),
                exit = fadeOut(),
        ) {
            UnobstructedBox(
                    onGloballyPositioned = {
                        listener(HelpShowcaseAction(HelpShowcaseIntent.SetVisibleScreenSize(it.first, it.second)))
                    }
            ) {
                ViewScoresActionBar(
                        modifier = Modifier
                                .padding(20.dp)
                                .animateContentSize()
                ) {
                    Row(
                            modifier = Modifier.padding(horizontal = 5.dp)
                    ) {
                        AnimatedVisibility(
                                state.isInMultiSelectMode || !state.actionBarExtended
                        ) {
                            MultiSelectBar(
                                    isInMultiSelectMode = state.isInMultiSelectMode,
                                    isEveryItemSelected = state.data.orEmpty().none { !it.isSelected },
                                    listener = { listener(MultiSelectAction(it)) },
                                    helpShowcaseListener = { listener(HelpShowcaseAction(it)) },
                            )
                        }

                        AnimatedVisibility(
                                state.viewScoresFiltersState.isExpanded || !state.actionBarExtended
                        ) {
                            ViewScoresFilters(
                                    state = state.viewScoresFiltersState,
                                    listener = { listener(FiltersAction(it)) },
                                    helpShowcaseListener = { listener(HelpShowcaseAction(it)) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UnobstructedBox(
        onGloballyPositioned: (Pair<Offset, Size>) -> Unit,
        bottomObstruction: @Composable () -> Unit,
) {
    Column(
            horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
                modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .onGloballyPositioned {
                            onGloballyPositioned(it.positionInRoot() to it.size.toSize())
                        }
        )

        bottomObstruction()
    }
}

/**
 * Ordinals are used for [HelpShowcaseItem.priority]
 */
internal enum class ViewScoreHelpPriority {
    GENERIC_ROW_ACTIONS, SPECIFIC_ROW_ACTION, MULTI_SELECT
}

enum class ViewScoresTestTag : CodexTestTag {
    SCREEN,
    LAZY_COLUMN,
    LIST_ITEM,
    MULTI_SELECT_START,
    MULTI_SELECT_CANCEL,
    MULTI_SELECT_ALL,
    MULTI_SELECT_EMAIL,
    DROPDOWN_MENU_ITEM,
    ;

    override val screenName: String
        get() = "VIEW_SCORES"

    override fun getElement(): String = name
}

/**
 * @param entryIndex the index of the item in the viewScores list
 */
fun viewScoresListItemTestTag(entryIndex: Int) = object : CodexTestTag {
    override val screenName: String
        get() = ViewScoresTestTag.SCREEN.screenName

    override fun getElement() = "${ViewScoresTestTag.LIST_ITEM.getElement()}_$entryIndex"
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY
)
@Composable
fun ViewScoresScreen_Preview() {
    CodexTheme {
        ViewScoresScreen(
                state = ViewScoresState(
                        data = ViewScoresEntryPreviewProvider
                                .generateEntries(20)
                                .setPersonalBests(listOf(3, 6)),
                ),
                listener = {},
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY
)
@Composable
fun MultiSelectMode_ViewScoresScreen_Preview() {
    CodexTheme {
        ViewScoresScreen(
                state = ViewScoresState(
                        isInMultiSelectMode = true,
                        data = ViewScoresEntryPreviewProvider
                                .generateEntries(20)
                                .setPersonalBests(listOf(3, 6)),
                ),
                listener = {},
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY
)
@Composable
fun Filters_ViewScoresScreen_Preview() {
    CodexTheme {
        ViewScoresScreen(
                state = ViewScoresState(
                        data = ViewScoresEntryPreviewProvider
                                .generateEntries(20)
                                .setPersonalBests(listOf(3, 6)),
                        viewScoresFiltersState = ViewScoresFiltersState(isExpanded = true),
                ),
                listener = {},
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
        device = Devices.PIXEL_2
)
@Composable
fun NoEntries_ViewScoresScreen_Preview() {
    CodexTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            ViewScoresScreen(
                    state = ViewScoresState(data = listOf()),
                    listener = {},
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
        device = Devices.PIXEL_2
)
@Composable
fun NoEntriesWithFilter_ViewScoresScreen_Preview() {
    CodexTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            ViewScoresScreen(
                    state = ViewScoresState(
                            data = listOf(),
                            viewScoresFiltersState = ViewScoresFiltersState(
                                    isExpanded = false,
                                    firstRoundOfDayFilter = true,
                            )
                    ),
                    listener = {},
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
        device = Devices.PIXEL_2
)
@Composable
fun Loading_ViewScoresScreen_Preview() {
    CodexTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            ViewScoresScreen(
                    state = ViewScoresState(),
                    listener = {},
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
        device = Devices.PIXEL_2
)
@Composable
fun ConvertScore_ViewScoresScreen_Preview() {
    CodexTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            ViewScoresScreen(
                    state = ViewScoresState(
                            data = ViewScoresEntryPreviewProvider.generateEntries(20),
                            lastClickedEntryId = 2,
                            convertScoreDialogOpen = true,
                    ),
                    listener = {},
            )
        }
    }
}
