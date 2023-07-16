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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.helpShowcase.HelpShowcaseUseCase
import eywa.projectcodex.common.helpShowcase.updateHelpDialogPosition
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.components.viewScores.ViewScoresIntent
import eywa.projectcodex.components.viewScores.data.ViewScoresEntry
import eywa.projectcodex.components.viewScores.utils.ViewScoresDropdownMenuItem

/**
 * Wraps [content] in an appropriate [Surface] with a [DropdownMenu] and generates click handlers related to [entry]
 * to pass to [content]
 */
@Composable
fun ViewScoresListItem(
        entry: ViewScoresEntry,
        entryIndex: Int,
        genericHelpInfo: HelpShowcaseUseCase,
        isInMultiSelectMode: Boolean,
        dropdownMenuItems: List<ViewScoresDropdownMenuItem>?,
        dropdownExpanded: Boolean,
        listener: (ViewScoresIntent) -> Unit,
        semanticsContentDescription: String,
        content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    fun stringFromRes(@StringRes resId: Int) = context.resources.getString(resId)

    genericHelpInfo.handle(
            HelpShowcaseIntent.Add(
                    HelpShowcaseItem(
                            helpTitle = stringResource(R.string.help_view_score__row_title),
                            helpBody = stringResource(R.string.help_view_score__row_body),
                            priority = ViewScoreHelpPriority.GENERIC_ROW_ACTIONS.ordinal
                    )
            ),
            CodexNavRoute.VIEW_SCORES::class,
    )

    Box(
            modifier = Modifier
                    .testTag(ViewScoresTestTag.LIST_ITEM.getTestTag())
                    .updateHelpDialogPosition(genericHelpInfo, stringResource(R.string.help_view_score__row_title))
                    .pointerInput(entryIndex, isInMultiSelectMode) {
                        detectTapGestures(
                                onTap = { listener(ViewScoresIntent.EntryClicked(entry.id)) },
                                onLongPress = { listener(ViewScoresIntent.EntryLongClicked(entry.id)) },
                        )
                    }
                    .semantics(mergeDescendants = true) {
                        contentDescription = semanticsContentDescription
                        if (isInMultiSelectMode) {
                            selected = entry.isSelected
                        }

                        onClick(
                                label = stringFromRes(
                                        when {
                                            !isInMultiSelectMode -> entry.getSingleClickAction().title
                                            entry.isSelected -> R.string.view_scores__deselect_entry
                                            else -> R.string.view_scores__select_entry
                                        }
                                ),
                                action = { listener(ViewScoresIntent.EntryClicked(entry.id)); true }
                        )

                        customActions = dropdownMenuItems?.map {
                            CustomAccessibilityAction(stringFromRes(it.title)) {
                                listener(ViewScoresIntent.DropdownMenuClicked(it))
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
                modifier = Modifier
                        .fillMaxWidth()
                        .clearAndSetSemantics {}
        )
        DropdownMenu(
                expanded = dropdownExpanded && !dropdownMenuItems.isNullOrEmpty(),
                onDismissRequest = { listener(ViewScoresIntent.DropdownMenuClosed) },
                modifier = Modifier.clearAndSetSemantics { }
        ) {
            dropdownMenuItems?.forEach { item ->
                if (item.shouldShow == null || item.shouldShow.invoke(entry)) {
                    DropdownMenuItem(
                            onClick = { listener(ViewScoresIntent.DropdownMenuClicked(item)) },
                            modifier = Modifier.testTag(ViewScoresTestTag.DROPDOWN_MENU_ITEM.getTestTag())
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
