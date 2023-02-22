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
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcase
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.sharedUi.SetOfDialogs
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.components.viewScores.ViewScoresFragment
import eywa.projectcodex.components.viewScores.ViewScoresIntent
import eywa.projectcodex.components.viewScores.ViewScoresIntent.*
import eywa.projectcodex.components.viewScores.ViewScoresState
import eywa.projectcodex.components.viewScores.ui.convertScoreDialog.ConvertScoreDialog
import eywa.projectcodex.components.viewScores.ui.multiSelectBar.MultiSelectBar
import kotlin.reflect.KClass

class ViewScoresScreen {
    private val helpInfo = HelpShowcase()

    private val lazyListState = LazyListState()

    /**
     * The classes of the entries currently being displayed in order
     */
    private lateinit var entryClasses: List<KClass<*>>

    /**
     * The help info of the entries currently being displayed.
     * This info is specific to the row type.
     * Indexes match that in [entryClasses]
     */
    private lateinit var specificEntryHelpInfo: List<HelpShowcase>

    /**
     * The help info of the entries currently being displayed.
     * This info is generic and common to all rows.
     * Indexes match that in [entryClasses]
     */
    private lateinit var genericEntryHelpInfo: List<HelpShowcase>

    /**
     * The height in px of the list of entries that is unobstructed, used to decide which row's
     * [HelpShowcaseItem]s should be shown
     */
    private var unobstructedHeight: Float = 0f

    @Composable
    fun ComposeContent(
            state: ViewScoresState,
            listener: (ViewScoresIntent) -> Unit,
    ) {
        listener(HelpShowcaseAction(HelpShowcaseIntent.Clear))

        entryClasses = state.data.map { it::class }
        specificEntryHelpInfo = List(state.data.size) { HelpShowcase() }
        genericEntryHelpInfo = List(state.data.size) {
            HelpShowcase().apply {
                handle(
                        HelpShowcaseIntent.Add(
                                HelpShowcaseItem(
                                        helpTitle = R.string.help_view_score__row_title,
                                        helpBody = R.string.help_view_score__row_body,
                                        priority = HelpItemPriority.GENERIC_ROW_ACTIONS.ordinal
                                )
                        ),
                        ViewScoresFragment::class,
                )
            }
        }

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
                            genericHelpInfo = genericEntryHelpInfo[entryIndex],
                            semanticsContentDescription = viewScoresEntryRowAccessibilityString(
                                    LocalContext.current, entry
                            )
                    ) {
                        ViewScoresEntryRow(
                                entry = entry,
                                helpInfo = specificEntryHelpInfo[entryIndex],
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
                with(LocalDensity.current) { unobstructedHeight = maxHeight.toPx() }
            }

            bottomObstruction()
        }
    }

    fun getHelpShowcases(): List<HelpShowcase> {
        // TODO_CURRENT ViewScores help showcases :(

        val fullyVisibleItems = lazyListState.layoutInfo.visibleItemsInfo
                // Ignore indexes that are only partially visible
                .filter {
                    it.offset >= 0 && it.offset + it.size < unobstructedHeight
                }

        val genericItemHelp = genericEntryHelpInfo[fullyVisibleItems[0].index]
        val specificItemHelp = fullyVisibleItems
                .distinctBy { entryClasses[it.index] }
                .map { specificEntryHelpInfo[it.index] }

        return listOf(helpInfo, genericItemHelp).plus(specificItemHelp)
    }


    /**
     * Ordinals are used for [HelpShowcaseItem.priority]
     */
    internal enum class HelpItemPriority {
        GENERIC_ROW_ACTIONS, SPECIFIC_ROW_ACTION, MULTI_SELECT
    }

    object TestTag {
        const val LAZY_COLUMN = "VIEW_SCORES_LAZY_COLUMN"
        const val LIST_ITEM = "VIEW_SCORES_LIST_ITEM"
        const val MULTI_SELECT_START = "VIEW_SCORES_MULTI_SELECT_START"
        const val MULTI_SELECT_CANCEL = "VIEW_SCORES_MULTI_SELECT_CANCEL"
        const val MULTI_SELECT_ALL = "VIEW_SCORES_MULTI_SELECT_ALL"
        const val MULTI_SELECT_EMAIL = "VIEW_SCORES_MULTI_SELECT_EMAIL"
        const val DROPDOWN_MENU_ITEM = "VIEW_SCORES_DROPDOWN_MENU_ITEM"
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
                            data = ViewScoresEntryPreviewProvider.generateEntries(20),
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
                            data = ViewScoresEntryPreviewProvider.generateEntries(20),
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
