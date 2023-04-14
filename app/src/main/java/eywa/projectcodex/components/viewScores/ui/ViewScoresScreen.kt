package eywa.projectcodex.components.viewScores.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.sharedUi.SetOfDialogs
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.components.viewScores.ViewScoresIntent
import eywa.projectcodex.components.viewScores.ViewScoresIntent.*
import eywa.projectcodex.components.viewScores.ViewScoresState
import eywa.projectcodex.components.viewScores.ui.ViewScoresEntryPreviewProvider.setPersonalBests
import eywa.projectcodex.components.viewScores.ui.convertScoreDialog.ConvertScoreDialog
import eywa.projectcodex.components.viewScores.ui.multiSelectBar.MultiSelectBar
import eywa.projectcodex.components.viewScores.utils.ViewScoresShowcaseInfo
import eywa.projectcodex.database.archerRound.ArcherRoundsFilter

class ViewScoresScreen {
    private val lazyListState = LazyListState()

    private lateinit var viewScoresShowcaseInfo: ViewScoresShowcaseInfo

    @Composable
    fun ComposeContent(
            state: ViewScoresState,
            listener: (ViewScoresIntent) -> Unit,
    ) {
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
                        .testTag(TestTag.SCREEN)
        ) {
            LazyColumn(
                    state = lazyListState,
                    contentPadding = PaddingValues(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.testTag(TestTag.LAZY_COLUMN)
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
    fun UnobstructedBox(
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
    internal enum class HelpItemPriority {
        GENERIC_ROW_ACTIONS, SPECIFIC_ROW_ACTION, MULTI_SELECT
    }

    object TestTag {
        private const val PREFIX = "VIEW_SCORES_"

        const val SCREEN = "${PREFIX}SCREEN"
        const val LAZY_COLUMN = "${PREFIX}LAZY_COLUMN"
        const val LIST_ITEM = "${PREFIX}LIST_ITEM"
        const val MULTI_SELECT_START = "${PREFIX}MULTI_SELECT_START"
        const val MULTI_SELECT_CANCEL = "${PREFIX}MULTI_SELECT_CANCEL"
        const val MULTI_SELECT_ALL = "${PREFIX}MULTI_SELECT_ALL"
        const val MULTI_SELECT_EMAIL = "${PREFIX}MULTI_SELECT_EMAIL"
        const val DROPDOWN_MENU_ITEM = "${PREFIX}DROPDOWN_MENU_ITEM"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Preview(
            showBackground = true,
            backgroundColor = CodexColors.Raw.COLOR_PRIMARY
    )
    @Composable
    fun ViewScoresScreen_Preview() {
        CodexTheme {
            ComposeContent(
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
            ComposeContent(
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
                ComposeContent(
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
                ComposeContent(
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
}
