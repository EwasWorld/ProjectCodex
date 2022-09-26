package eywa.projectcodex.components.viewScores.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.ComposeHelpShowcaseMap
import eywa.projectcodex.common.helpShowcase.updateHelpDialogPosition
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.components.viewScores.data.ViewScoresEntry
import eywa.projectcodex.components.viewScores.utils.ConvertScoreType
import eywa.projectcodex.components.viewScores.utils.ViewScoresDropdownMenuItem
import kotlin.reflect.KClass

/**
 * Wraps [content] in an appropriate [Surface] with a [DropdownMenu] and generates click handlers related to [entry]
 * to pass to [content]
 */
@Composable
fun ViewScoresListItem(
        entry: ViewScoresEntry,
        entryIndex: Int,
        genericHelpInfo: ComposeHelpShowcaseMap,
        isInMultiSelectMode: Boolean,
        listActionState: ViewScoresListActionState,
        listener: ListActionListener,
        semanticsContentDescription: String,
        content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    fun stringFromRes(@StringRes resId: Int) = context.resources.getString(resId)

    val singleClickAction = listActionState.singleClickActions[entry::class]
    val dropdownMenuItems = listActionState.dropdownMenuItems[entry::class]

    val singleClickListener: () -> Unit =
            if (isInMultiSelectMode) {
                { listener.toggleListItemSelected(entryIndex) }
            }
            else {
                { singleClickAction?.let { listener.dropdownMenuItemClicked(entry, singleClickAction) } }
            }

    Box(
            modifier = Modifier
                    .testTag(ViewScoresScreen.TestTag.LIST_ITEM)
                    .updateHelpDialogPosition(genericHelpInfo, R.string.help_view_score__row_title)
                    .pointerInput(entryIndex, isInMultiSelectMode) {
                        detectTapGestures(
                                onTap = { singleClickListener() },
                                onLongPress = {
                                    if (!dropdownMenuItems.isNullOrEmpty()) {
                                        listActionState.openForIndex(entryIndex)
                                    }
                                },
                        )
                    }
                    .clearAndSetSemantics {
                        contentDescription = semanticsContentDescription
                        if (isInMultiSelectMode) {
                            selected = entry.isSelected
                        }

                        onClick(
                                label = when {
                                    !isInMultiSelectMode -> singleClickAction?.title
                                    entry.isSelected -> R.string.view_scores__deselect_entry
                                    else -> R.string.view_scores__select_entry
                                }?.let { stringFromRes(it) },
                                action = { singleClickListener(); true }
                        )

                        customActions = dropdownMenuItems?.map {
                            CustomAccessibilityAction(stringFromRes(it.title)) {
                                listener.dropdownMenuItemClicked(entry, it)
                                true
                            }
                        } ?: listOf()
                    }
    ) {
        Surface(
                border = BorderStroke(2.dp, CodexTheme.colors.listItemOnAppBackgroundBorder)
                        .takeIf { isInMultiSelectMode && entry.isSelected },
                color = CodexTheme.colors.listItemOnAppBackground,
                content = content,
                modifier = Modifier.fillMaxWidth()
        )
        DropdownMenu(
                expanded = listActionState.isContextMenuOpenForItem(entryIndex),
                onDismissRequest = { listActionState.close() }
        ) {
            dropdownMenuItems?.forEach { item ->
                if (item.showCondition == null || item.showCondition.invoke(entry)) {
                    DropdownMenuItem(
                            onClick = {
                                if (listener.dropdownMenuItemClicked(entry, item)) {
                                    listActionState.close()
                                }
                            },
                            modifier = Modifier.testTag(ViewScoresScreen.TestTag.DROPDOWN_MENU_ITEM)
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

interface ListActionListener {
    /**
     * @return true if the action was successful
     */
    fun dropdownMenuItemClicked(entry: ViewScoresEntry, menuItem: ViewScoresDropdownMenuItem): Boolean

    fun toggleListItemSelected(entryIndex: Int)
    fun noRoundsDialogDismissedListener()
    fun convertScoreDialogOkListener(entryIndex: Int?, convertType: ConvertScoreType)
    fun convertScoreDialogOkListener(convertType: ConvertScoreType)
    fun convertScoreDialogDismissedListener()
    fun deleteDialogOkListener(entryIndex: Int?)
    fun deleteDialogOkListener()
    fun deleteDialogDismissedListener()
}

@Stable
interface ViewScoresListActionState {
    var isConvertScoreOpen: Boolean
    var isDeleteDialogOpen: Boolean
    var isDropdownOpen: Boolean
    var lastOpenedForIndex: Int?
    var singleClickActions: Map<KClass<ViewScoresEntry>, ViewScoresDropdownMenuItem>
    var dropdownMenuItems: Map<KClass<ViewScoresEntry>, List<ViewScoresDropdownMenuItem>>

    fun isContextMenuOpenForItem(entryIndex: Int) = isDropdownOpen && entryIndex == lastOpenedForIndex

    fun openForIndex(entryIndex: Int) {
        isDropdownOpen = true
        lastOpenedForIndex = entryIndex
    }

    fun close() {
        isDropdownOpen = false
    }
}

private class ViewScoresListActionStateImpl(
        override var singleClickActions: Map<KClass<ViewScoresEntry>, ViewScoresDropdownMenuItem>,
        override var dropdownMenuItems: Map<KClass<ViewScoresEntry>, List<ViewScoresDropdownMenuItem>>,
        isDropdownOpen: Boolean = false,
        isConvertScoreOpen: Boolean = false,
        isDeleteDialogOpen: Boolean = false,
        lastOpenedForIndex: Int? = null
) : ViewScoresListActionState {
    override var isDropdownOpen by mutableStateOf(isDropdownOpen)
    override var isConvertScoreOpen by mutableStateOf(isConvertScoreOpen)
    override var isDeleteDialogOpen by mutableStateOf(isDeleteDialogOpen)
    override var lastOpenedForIndex by mutableStateOf(lastOpenedForIndex)
}

@Composable
fun rememberViewScoresListActionState(
        singleClickActions: Map<KClass<ViewScoresEntry>, ViewScoresDropdownMenuItem>,
        dropdownMenuItems: Map<KClass<ViewScoresEntry>, List<ViewScoresDropdownMenuItem>>,
        isDropdownOpen: Boolean = false,
        isConvertScoreOpen: Boolean = false,
        isDeleteDialogOpen: Boolean = false,
        lastOpenedForIndex: Int? = null
): ViewScoresListActionState = remember {
    ViewScoresListActionStateImpl(
            singleClickActions = singleClickActions,
            dropdownMenuItems = dropdownMenuItems,
            isDropdownOpen = isDropdownOpen,
            isConvertScoreOpen = isConvertScoreOpen,
            isDeleteDialogOpen = isDeleteDialogOpen,
            lastOpenedForIndex = lastOpenedForIndex
    )
}
