package eywa.projectcodex.components.viewScores.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.*
import eywa.projectcodex.common.sharedUi.*
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.components.viewScores.data.ViewScoresEntry
import eywa.projectcodex.components.viewScores.utils.ConvertScoreType
import eywa.projectcodex.components.viewScores.utils.ViewScoresDropdownMenuItem
import kotlin.reflect.KClass

class ViewScoreScreen : ActionBarHelp {
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
     * The height in px of the list of entries that is unobstructed
     */
    private var unobstructedHeight: Float = 0f

    @Composable
    fun ComposeContent(
            entries: List<ViewScoresEntry>,
            dropdownMenuState: ViewScoresDropdownMenuState = remember { ViewScoresDropdownMenuState() },
            entrySingleClickActions: Map<KClass<ViewScoresEntry>, ViewScoresDropdownMenuItem>,
            dropdownMenuItems: Map<KClass<ViewScoresEntry>, List<ViewScoresDropdownMenuItem>>,
            isInMultiSelectMode: Boolean,
            listener: ViewScoreScreenListener,
    ) {
        listener.helpShowcaseInfo = helpInfo
        listener.contextMenuState = dropdownMenuState

        ViewScoresDialogs(
                entries.isEmpty(),
                dropdownMenuState.isConvertScoreOpen,
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
                            singleClickAction = entrySingleClickActions[entry::class],
                            dropdownMenuItems = dropdownMenuItems[entry::class],
                            dropdownMenuState = dropdownMenuState,
                            isInMultiSelectMode = isInMultiSelectMode,
                            listener = listener,
                    )
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
    fun ViewScoresListItem(
            entry: ViewScoresEntry,
            entryIndex: Int,
            dropdownMenuState: ViewScoresDropdownMenuState,
            singleClickAction: ViewScoresDropdownMenuItem?,
            dropdownMenuItems: List<ViewScoresDropdownMenuItem>?,
            isInMultiSelectMode: Boolean,
            listener: ViewScoreScreenListener,
    ) {
        val context = LocalContext.current
        fun stringFromRes(@StringRes resId: Int) = context.resources.getString(resId)

        val specificHelpInfo = specificEntryHelpInfo[entryIndex]
        val genericHelpInfo = genericEntryHelpInfo[entryIndex]

        val singleClickListener: () -> Unit =
                if (isInMultiSelectMode) {
                    { listener.toggleEntrySelected(entryIndex) }
                }
                else {
                    { singleClickAction?.let { listener.dropdownMenuItemClicked(entry, singleClickAction) } }
                }

        val semanticsString = viewScoresEntryRowAccessibilityString(entry, isInMultiSelectMode)

        Box {
            Surface(
                    border = BorderStroke(SELECTED_ITEM_BORDER_STROKE, CodexColors.COLOR_PRIMARY_DARK)
                            .takeIf { isInMultiSelectMode && entry.isSelected },
                    color = CodexColors.COLOR_LIGHT_ACCENT,
                    modifier = Modifier
                            .fillMaxWidth()
                            .updateHelpDialogPosition(genericHelpInfo, R.string.help_view_score__row_title)
            ) {
                ViewScoresEntryRow(
                        entry = entry,
                        helpInfo = specificHelpInfo,
                        modifier = Modifier
                                .pointerInput(entryIndex, isInMultiSelectMode) {
                                    detectTapGestures(
                                            onTap = { singleClickListener() },
                                            onLongPress = {
                                                if (!dropdownMenuItems.isNullOrEmpty()) {
                                                    dropdownMenuState.openForIndex(entryIndex)
                                                }
                                            },
                                    )
                                }
                                .clearAndSetSemantics {
                                    contentDescription = semanticsString
                                    customActions = dropdownMenuItems?.map {
                                        CustomAccessibilityAction(stringFromRes(it.title)) {
                                            listener.dropdownMenuItemClicked(entry, it)
                                            true
                                        }
                                    } ?: listOf()
                                    onClick(
                                            label = when {
                                                !isInMultiSelectMode -> singleClickAction?.title
                                                entry.isSelected -> R.string.view_scores__deselect_entry
                                                else -> R.string.view_scores__select_entry
                                            }?.let { stringFromRes(it) },
                                            action = { singleClickListener(); true }
                                    )
                                }
                )
            }
            DropdownMenu(
                    expanded = dropdownMenuState.isContextMenuOpenForItem(entryIndex),
                    onDismissRequest = { dropdownMenuState.close() }
            ) {
                dropdownMenuItems?.forEach { item ->
                    if (item.showCondition == null || item.showCondition.invoke(entry)) {
                        DropdownMenuItem(
                                onClick = {
                                    if (listener.dropdownMenuItemClicked(entry, item)) {
                                        dropdownMenuState.close()
                                    }
                                }
                        ) {
                            Text(
                                    text = stringResource(id = item.title),
                                    style = CodexTypography.NORMAL
                            )
                        }
                    }
                }
            }
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
                    onPositiveButtonPressed = { listener.convertDialogActionListener(it) },
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

    // TODO_CURRENT Can this separate into an interface and a class?
    abstract class ViewScoreScreenListener : MultiSelectBarListener {
        internal lateinit var helpShowcaseInfo: ComposeHelpShowcaseMap
        internal lateinit var contextMenuState: ViewScoresDropdownMenuState

        final override fun addHelpShowcase(item: ComposeHelpShowcaseItem) {
            helpShowcaseInfo.add(item)
        }

        final override fun updateHelpDialogPosition(helpTitle: Int, layoutCoordinates: LayoutCoordinates) {
            helpShowcaseInfo.updateItem(helpTitle, layoutCoordinates)
        }

        /**
         * @return true if the action was successful
         */
        abstract fun dropdownMenuItemClicked(entry: ViewScoresEntry, menuItem: ViewScoresDropdownMenuItem): Boolean

        abstract fun toggleEntrySelected(entryIndex: Int)

        abstract fun noRoundsDialogDismissedListener()
        abstract fun convertDialogActionListener(entryIndex: Int?, convertType: ConvertScoreType)

        fun convertDialogActionListener(convertType: ConvertScoreType) {
            contextMenuState.isConvertScoreOpen = false
            convertDialogActionListener(contextMenuState.lastOpenedForIndex, convertType)
        }

        fun convertScoreDialogDismissedListener() {
            contextMenuState.isConvertScoreOpen = false
        }
    }

    /**
     * Ordinals are used for [ComposeHelpShowcaseItem.priority]
     */
    internal enum class HelpItemPriority {
        GENERIC_ROW_ACTIONS, SPECIFIC_ROW_ACTION, MULTI_SELECT
    }

    @Stable
    interface ViewScoresDropdownMenuState {
        var isConvertScoreOpen: Boolean
        var isDropdownOpen: Boolean
        var lastOpenedForIndex: Int?

        fun isContextMenuOpenForItem(entryIndex: Int) = isDropdownOpen && entryIndex == lastOpenedForIndex

        fun openForIndex(entryIndex: Int) {
            isDropdownOpen = true
            lastOpenedForIndex = entryIndex
        }

        fun close() {
            isDropdownOpen = false
        }
    }

    private class ViewScoresDropdownMenuStateImpl(
            isDropdownOpen: Boolean = false,
            isConvertScoreOpen: Boolean = false,
            lastOpenedForIndex: Int? = null
    ) : ViewScoresDropdownMenuState {
        override var isDropdownOpen by mutableStateOf(isDropdownOpen)
        override var isConvertScoreOpen by mutableStateOf(isConvertScoreOpen)
        override var lastOpenedForIndex by mutableStateOf(lastOpenedForIndex)
    }

    private fun ViewScoresDropdownMenuState(
            isDropdownOpen: Boolean = false,
            isConvertScoreOpen: Boolean = false,
            lastOpenedForIndex: Int? = null
    ): ViewScoresDropdownMenuState =
            ViewScoresDropdownMenuStateImpl(isDropdownOpen, isConvertScoreOpen, lastOpenedForIndex)

    object TestTag {
    }

    private val listenersForPreviews = object : ViewScoreScreenListener() {
        override fun dropdownMenuItemClicked(entry: ViewScoresEntry, menuItem: ViewScoresDropdownMenuItem): Boolean =
                true

        override fun toggleMultiSelectMode() {}
        override fun noRoundsDialogDismissedListener() {}
        override fun convertDialogActionListener(entryIndex: Int?, convertType: ConvertScoreType) {}
        override fun selectAllOrNoneClicked() {}
        override fun emailClicked() {}
        override fun toggleEntrySelected(entryIndex: Int) {}
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
                    entrySingleClickActions = mapOf(),
                    dropdownMenuItems = mapOf(),
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
                    entrySingleClickActions = mapOf(),
                    dropdownMenuItems = mapOf(),
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
                        entrySingleClickActions = mapOf(),
                        dropdownMenuItems = mapOf(),
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
                        dropdownMenuState = remember {
                            ViewScoresDropdownMenuState(
                                    isDropdownOpen = false,
                                    isConvertScoreOpen = true,
                                    lastOpenedForIndex = 2
                            )
                        },
                        entrySingleClickActions = mapOf(),
                        dropdownMenuItems = mapOf(),
                        isInMultiSelectMode = false,
                        listener = listenersForPreviews,
                )
            }
        }
    }
}
