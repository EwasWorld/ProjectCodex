package eywa.projectcodex.components.viewScores.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.*
import eywa.projectcodex.common.sharedUi.RadioButtonDialogContent
import eywa.projectcodex.common.sharedUi.SimpleAlertDialog
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.components.viewScores.data.ViewScoresEntry
import eywa.projectcodex.components.viewScores.utils.ConvertScore
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

    // TODO_CURRENT Make the list of params smaller?
    @Composable
    fun ComposeContent(
            entries: List<ViewScoresEntry>,
            convertDialogSelectedIndex: Int?,
            contextMenuOpenForIndex: Int?,
            dropdownMenuItems: Map<KClass<ViewScoresEntry>, List<ViewScoresDropdownMenuItem>>,
            dropdownMenuItemClicked: (ViewScoresDropdownMenuItem) -> Unit,
            entryClickedListener: (entryIndex: Int) -> Unit,
            entryLongClickedListener: (entryIndex: Int) -> Unit,
            isInMultiSelectMode: Boolean,
            startMultiSelectListener: () -> Unit,
            selectAllOrNoneClickedListener: () -> Unit,
            emailSelectedClickedListener: () -> Unit,
            cancelMultiSelectListener: () -> Unit,
            closeDropdownMenuListener: () -> Unit,
            noRoundsDialogOkListener: () -> Unit,
            convertDialogDismissedListener: () -> Unit,
            convertDialogActionListener: (ConvertScore) -> Unit,
            convertDialogSelectionChangedListener: (Int) -> Unit,
    ) {
        if (entries.isEmpty()) {
            SimpleAlertDialog(
                    isOpen = true,
                    title = R.string.err_table_view__no_data,
                    message = R.string.err_view_score__no_rounds,
                    positiveButtonText = R.string.err_view_score__return_to_main_menu,
                    onDialogActionClicked = { noRoundsDialogOkListener() },
            )
        }
        else if (convertDialogSelectedIndex != null) {
            Dialog(
                    onDismissRequest = convertDialogDismissedListener
            ) {
                RadioButtonDialogContent(
                        title = R.string.view_score__convert_score_dialog_title,
                        message = R.string.view_score__convert_score_dialog_body,
                        radioButtonText = ConvertScore.values().map { it.title },
                        positiveButtonText = R.string.general_ok,
                        negativeButtonText = R.string.general_cancel,
                        onDialogActionClicked = { action, selectedIndex ->
                            if (action) {
                                convertDialogActionListener(ConvertScore.values()[selectedIndex])
                            }
                            else {
                                convertDialogDismissedListener()
                            }
                        },
                        currentlySelectedIndex = convertDialogSelectedIndex,
                        selectionChangedListener = convertDialogSelectionChangedListener,
                )
            }
        }

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
                    val specificHelpInfo = specificEntryHelpInfo[entryIndex]
                    val genericHelpInfo = genericEntryHelpInfo[entryIndex]

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
                                    entryClickedListener = { entryClickedListener(entryIndex) },
                                    entryLongClickedListener = { entryLongClickedListener(entryIndex) },
                                    dropdownMenuItems = dropdownMenuItems[entry::class],
                                    addHelpInfoEntry = { specificHelpInfo.add(it) },
                                    updateHelpInfoModifier = { title ->
                                        Modifier.updateHelpDialogPosition(specificHelpInfo, title)
                                    },
                            )
                        }
                        DropdownMenu(
                                expanded = entryIndex == contextMenuOpenForIndex,
                                onDismissRequest = closeDropdownMenuListener
                        ) {
                            dropdownMenuItems[entry::class]?.forEach { item ->
                                if (item.showCondition == null || item.showCondition.invoke(entry)) {
                                    DropdownMenuItem(onClick = { dropdownMenuItemClicked(item) }) {
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
            }

            Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
            ) {
                BoxWithConstraints(
                        modifier = Modifier.weight(1f)
                ) {
                    with(LocalDensity.current) { unobstructedHeight = maxHeight.toPx() }
                }

                ViewScoresMultiSelectBar(
                        isInMultiSelectMode = isInMultiSelectMode,
                        multiSelectClickedListener = startMultiSelectListener,
                        selectAllOrNoneClickedListener = selectAllOrNoneClickedListener,
                        emailSelectedClickedListener = emailSelectedClickedListener,
                        cancelMultiSelectClickedListener = cancelMultiSelectListener,
                        addHelpInfo = { helpInfo.add(it) },
                        updateHelpDialogPosition = { Modifier.updateHelpDialogPosition(helpInfo, it) },
                        modifier = Modifier.padding(bottom = 20.dp),
                )
            }
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

    /**
     * Ordinals are used for [ComposeHelpShowcaseItem.priority]
     */
    internal enum class HelpItemPriority {
        GENERIC_ROW_ACTIONS, SPECIFIC_ROW_ACTION, MULTI_SELECT
    }


    object TestTag {
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Preview(
            showBackground = true,
            backgroundColor = CodexColors.Raw.COLOR_PRIMARY
    )
    @Composable
    fun PreviewViewScoresScreen() {
        CodexTheme {
            ComposeContent(
                    ViewScoresEntryPreviewProvider.generateEntries(20),
                    null,
                    null,
                    mapOf(),
                    {},
                    {},
                    {},
                    false,
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Preview(
            showBackground = true,
            backgroundColor = CodexColors.Raw.COLOR_PRIMARY
    )
    @Composable
    fun PreviewViewScoresScreen_MultiSelectMode() {
        CodexTheme {
            ComposeContent(
                    ViewScoresEntryPreviewProvider.generateEntries(20),
                    null,
                    null,
                    mapOf(),
                    {},
                    {},
                    {},
                    true,
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
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
    fun PreviewViewScoresScreen_NoEntries() {
        CodexTheme {
            Box(modifier = Modifier.fillMaxSize()) {
                ComposeContent(
                        listOf(),
                        null,
                        null,
                        mapOf(),
                        {},
                        {},
                        {},
                        false,
                        {},
                        {},
                        {},
                        {},
                        {},
                        {},
                        {},
                        {},
                        {},
                )
            }
        }
    }
}
