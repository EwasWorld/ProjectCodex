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
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.ActionBarHelp
import eywa.projectcodex.common.helpShowcase.ComposeHelpShowcaseItem
import eywa.projectcodex.common.helpShowcase.ComposeHelpShowcaseMap
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.sharedUi.*
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.components.viewScores.data.ViewScoresEntry
import eywa.projectcodex.components.viewScores.utils.ConvertScoreType
import eywa.projectcodex.components.viewScores.utils.ViewScoresDropdownMenuItem
import kotlin.reflect.KClass

class ViewScoresScreen : ActionBarHelp {
    private val helpInfo = ComposeHelpShowcaseMap()
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
    private lateinit var specificEntryHelpInfo: List<ComposeHelpShowcaseMap>

    /**
     * The help info of the entries currently being displayed.
     * This info is generic and common to all rows.
     * Indexes match that in [entryClasses]
     */
    private lateinit var genericEntryHelpInfo: List<ComposeHelpShowcaseMap>

    /**
     * The height in px of the list of entries that is unobstructed, used to decide which row's
     * [ComposeHelpShowcaseItem]s should be shown
     */
    private var unobstructedHeight: Float = 0f

    @Composable
    fun ComposeContent(
            entries: List<ViewScoresEntry>,
            listState: ViewScoresListActionState,
            isInMultiSelectMode: Boolean,
            listener: ViewScoreScreenListener,
    ) {
        listener.helpShowcaseInfo = helpInfo
        listener.contextMenuState = listState

        ViewScoresDialogs(
                entries.isEmpty(),
                listState.isConvertScoreOpen,
                listener
        )

        entryClasses = entries.map { it::class }
        specificEntryHelpInfo = List(entries.size) { ComposeHelpShowcaseMap() }
        genericEntryHelpInfo = List(entries.size) {
            ComposeHelpShowcaseMap().apply {
                add(
                        ComposeHelpShowcaseItem(
                                helpTitle = R.string.help_view_score__row_title,
                                helpBody = R.string.help_view_score__row_body,
                                priority = HelpItemPriority.GENERIC_ROW_ACTIONS.ordinal
                        )
                )
            }
        }

        Box(
                modifier = Modifier
                        .fillMaxSize()
                        .background(colorResource(id = R.color.colorPrimary))
        ) {
            LazyColumn(
                    state = lazyListState,
                    contentPadding = PaddingValues(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(entries.size) { entryIndex ->
                    val entry = entries[entryIndex]
                    ViewScoresListItem(
                            entry = entry,
                            entryIndex = entryIndex,
                            listActionState = listState,
                            isInMultiSelectMode = isInMultiSelectMode,
                            listener = listener,
                            genericHelpInfo = genericEntryHelpInfo[entryIndex]
                    ) { clickModifier ->
                        ViewScoresEntryRow(
                                entry = entry,
                                helpInfo = specificEntryHelpInfo[entryIndex],
                                modifier = clickModifier,
                                isInMultiSelectMode = isInMultiSelectMode,
                        )
                    }
                }
            }

            UnobstructedBox {
                ViewScoresMultiSelectBar(
                        listener = listener,
                        modifier = Modifier.padding(bottom = 20.dp),
                        isInMultiSelectMode = isInMultiSelectMode,
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

    @Composable
    fun ViewScoresDialogs(
            isEmptyList: Boolean,
            convertDialogOpen: Boolean,
            listener: ViewScoreScreenListener,
    ) {
        var isAnyDialogShown = false
        SimpleDialog(
                isShown = isEmptyList && !isAnyDialogShown,
                onDismissListener = { listener.noRoundsDialogDismissedListener() }
        ) {
            SimpleDialogContent(
                    title = R.string.err_table_view__no_data,
                    message = R.string.err_view_score__no_rounds,
                    positiveButton = ButtonState(
                            text = R.string.err_view_score__return_to_main_menu,
                            onClick = { listener.noRoundsDialogDismissedListener() }
                    ),
            )
        }
        isAnyDialogShown = isAnyDialogShown || isEmptyList

        SimpleDialog(
                isShown = convertDialogOpen && !isAnyDialogShown,
                onDismissListener = { listener.convertScoreDialogDismissedListener() }
        ) {
            RadioButtonDialogContent(
                    title = R.string.view_score__convert_score_dialog_title,
                    message = R.string.view_score__convert_score_dialog_body,
                    positiveButtonText = R.string.general_ok,
                    onPositiveButtonPressed = { listener.convertScoreDialogOkListener(it) },
                    negativeButton = ButtonState(
                            text = R.string.general_cancel,
                            onClick = { listener.convertScoreDialogDismissedListener() }
                    ),
                    state = rememberRadioButtonDialogState(items = ConvertScoreType.values().toList()),
            )
        }
    }

    override fun getHelpShowcases(): List<HelpShowcaseItem> {
        val mainItems = helpInfo.getItems()

        val fullyVisibleItems = lazyListState.layoutInfo.visibleItemsInfo
                // Ignore indexes that are only partially visible
                .filter {
                    it.offset >= 0 && it.offset + it.size < unobstructedHeight
                }

        val genericItemHelp = genericEntryHelpInfo[fullyVisibleItems[0].index].getItems()
        val specificItemHelp = fullyVisibleItems
                .distinctBy { entryClasses[it.index] }
                .map { specificEntryHelpInfo[it.index].getItems() }
                .flatten()

        return mainItems + specificItemHelp + genericItemHelp
    }

    override fun getHelpPriority(): Int? = null

    companion object {
        val SELECTED_ITEM_BORDER_STROKE = 2.dp
    }

    abstract class ViewScoreScreenListener : MultiSelectBarListener, ListActionListener {
        internal lateinit var helpShowcaseInfo: ComposeHelpShowcaseMap
        internal lateinit var contextMenuState: ViewScoresListActionState

        final override fun addHelpShowcase(item: ComposeHelpShowcaseItem) {
            helpShowcaseInfo.add(item)
        }

        final override fun updateHelpDialogPosition(helpTitle: Int, layoutCoordinates: LayoutCoordinates) {
            helpShowcaseInfo.updateItem(helpTitle, layoutCoordinates)
        }

        override fun convertScoreDialogOkListener(convertType: ConvertScoreType) {
            contextMenuState.isConvertScoreOpen = false
            convertScoreDialogOkListener(contextMenuState.lastOpenedForIndex, convertType)
        }

        override fun convertScoreDialogDismissedListener() {
            contextMenuState.isConvertScoreOpen = false
        }
    }

    /**
     * Ordinals are used for [ComposeHelpShowcaseItem.priority]
     */
    internal enum class HelpItemPriority {
        GENERIC_ROW_ACTIONS, SPECIFIC_ROW_ACTION, MULTI_SELECT
    }

    object TestTag {
    }

    private val listenersForPreviews = object : ViewScoreScreenListener() {
        override fun dropdownMenuItemClicked(entry: ViewScoresEntry, menuItem: ViewScoresDropdownMenuItem): Boolean =
                true

        override fun toggleMultiSelectMode() {}
        override fun noRoundsDialogDismissedListener() {}
        override fun convertScoreDialogOkListener(entryIndex: Int?, convertType: ConvertScoreType) {}
        override fun selectAllOrNoneClicked() {}
        override fun multiSelectEmailClicked() {}
        override fun toggleListItemSelected(entryIndex: Int) {}
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
                    entries = ViewScoresEntryPreviewProvider.generateEntries(20),
                    listState = rememberViewScoresListActionState(mapOf(), mapOf()),
                    isInMultiSelectMode = false,
                    listener = listenersForPreviews,
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
                    entries = ViewScoresEntryPreviewProvider.generateEntries(20),
                    listState = rememberViewScoresListActionState(mapOf(), mapOf()),
                    isInMultiSelectMode = true,
                    listener = listenersForPreviews,
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
                        entries = listOf(),
                        listState = rememberViewScoresListActionState(mapOf(), mapOf()),
                        isInMultiSelectMode = false,
                        listener = listenersForPreviews,
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
                        entries = ViewScoresEntryPreviewProvider.generateEntries(20),
                        listState = rememberViewScoresListActionState(
                                singleClickActions = mapOf(),
                                dropdownMenuItems = mapOf(),
                                isDropdownOpen = false,
                                isConvertScoreOpen = true,
                                lastOpenedForIndex = 2,
                        ),
                        isInMultiSelectMode = false,
                        listener = listenersForPreviews,
                )
            }
        }
    }
}
