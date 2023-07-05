package eywa.projectcodex.components.viewScores.ui

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.logging.CustomLogger
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.sharedUi.SetOfDialogs
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.common.utils.ToastSpamPrevention
import eywa.projectcodex.components.archerRoundScore.state.ArcherRoundScreen
import eywa.projectcodex.components.viewScores.ViewScoresIntent
import eywa.projectcodex.components.viewScores.ViewScoresIntent.*
import eywa.projectcodex.components.viewScores.ViewScoresState
import eywa.projectcodex.components.viewScores.ViewScoresViewModel
import eywa.projectcodex.components.viewScores.ui.ViewScoresEntryPreviewProvider.setPersonalBests
import eywa.projectcodex.components.viewScores.ui.convertScoreDialog.ConvertScoreDialog
import eywa.projectcodex.components.viewScores.ui.multiSelectBar.MultiSelectBar
import eywa.projectcodex.components.viewScores.utils.ViewScoresShowcaseInfo
import eywa.projectcodex.database.archerRound.ArcherRoundsFilter

private lateinit var viewScoresShowcaseInfo: ViewScoresShowcaseInfo
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
    if (state.multiSelectEmailClicked) {
        CodexNavRoute.EMAIL_SCORE.navigate(navController)
        listener(HandledEmailClicked)
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
            val args = Bundle().apply {
                putString("screen", ArcherRoundScreen.SCORE_PAD.name)
                putInt("archerRoundId", state.lastClickedEntryId)
            }
            navController.navigate(R.id.archerRoundFragment, args)
        }
        listener(HandledScorePadOpened)
    }

    if (state.openInputEndOnCompletedRound) {
        if (state.lastClickedEntryId != null) {
            CustomLogger.customLogger.w(LOG_TAG, "Tried to continue completed round")
            ToastSpamPrevention.displayToast(
                    context,
                    context.resources.getString(R.string.err_view_score__round_already_complete)
            )
        }
        listener(HandledInputEndOnCompletedRound)
    }

    if (state.openInputEndClicked) {
        if (state.lastClickedEntryId != null) {
            val args = Bundle().apply {
                putString("screen", ArcherRoundScreen.INPUT_END.name)
                putInt("archerRoundId", state.lastClickedEntryId)
            }
            navController.navigate(R.id.archerRoundFragment, args)
        }
        listener(HandledInputEndOpened)
    }

    if (state.openEmailClicked) {
        // TODO_CURRENT Combine with other email open
        CodexNavRoute.EMAIL_SCORE.navigate(navController)
        listener(HandledEmailOpened)
    }

    if (state.openEditInfoClicked) {
        if (state.lastClickedEntryId != null) {
            val args = Bundle().apply {
                putInt("archerRoundId", state.lastClickedEntryId)
            }
            navController.navigate(R.id.newScoreFragment, args)
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

    viewScoresShowcaseInfo = ViewScoresShowcaseInfo(state.data.map { it::class }, lazyListState)
    listener(HelpShowcaseAction(HelpShowcaseIntent.AddDynamicInfo(viewScoresShowcaseInfo)))

    SetOfDialogs(
            state.data.isEmpty() to { ViewScoresEmptyListDialog(isShown = it, listener = listener) },
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
            modifier = Modifier
                    .fillMaxSize()
                    .background(CodexTheme.colors.appBackground)
                    .testTag(ViewScoresTestTag.SCREEN.getTestTag())
    ) {
        LazyColumn(
                state = lazyListState,
                contentPadding = PaddingValues(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.testTag(ViewScoresTestTag.LAZY_COLUMN.getTestTag())
        ) {
            items(state.data.size) { entryIndex ->
                val entry = state.data[entryIndex]
                ViewScoresListItem(
                        entry = entry,
                        entryIndex = entryIndex,
                        isInMultiSelectMode = state.isInMultiSelectMode,
                        dropdownMenuItems = state.dropdownItems
                                ?.takeIf { entry.id == state.lastClickedEntryId },
                        listener = listener,
                        genericHelpInfo = viewScoresShowcaseInfo.genericEntryHelpInfo[entryIndex],
                        semanticsContentDescription = viewScoresEntryRowAccessibilityString(
                                LocalContext.current, entry
                        )
                ) {
                    ViewScoresEntryRow(
                            entry = entry,
                            helpInfo = viewScoresShowcaseInfo.specificEntryHelpInfo[entryIndex],
                            showPbs = !state.filters.contains<ArcherRoundsFilter.PersonalBests>(),
                    )
                }
            }
        }

        UnobstructedBox {
            MultiSelectBar(
                    isInMultiSelectMode = state.isInMultiSelectMode,
                    isEveryItemSelected = state.data.all { it.isSelected },
                    listener = { listener(MultiSelectAction(it)) },
                    helpShowcaseListener = { listener(HelpShowcaseAction(it)) },
                    modifier = Modifier.padding(bottom = 20.dp)
            )
        }
    }
}

@Composable
private fun UnobstructedBox(
        bottomObstruction: @Composable () -> Unit
) {
    Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
    ) {
        BoxWithConstraints(
                modifier = Modifier.weight(1f)
        ) {
            with(LocalDensity.current) { viewScoresShowcaseInfo.unobstructedHeight = maxHeight.toPx() }
        }

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
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
        device = Devices.PIXEL_2
)
@Composable
fun NoEntries_ViewScoresScreen_Preview() {
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
