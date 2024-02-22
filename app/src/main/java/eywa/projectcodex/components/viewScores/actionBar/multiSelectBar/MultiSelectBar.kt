package eywa.projectcodex.components.viewScores.actionBar.multiSelectBar

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.outlined.Email
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.helpShowcase.HelpState
import eywa.projectcodex.common.sharedUi.CodexIconButton
import eywa.projectcodex.common.sharedUi.CodexIconInfo
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.components.viewScores.actionBar.ViewScoresActionBar
import eywa.projectcodex.components.viewScores.screenUi.ViewScoreHelpPriority
import eywa.projectcodex.components.viewScores.screenUi.ViewScoresTestTag

@Composable
internal fun MultiSelectBar(
        isInMultiSelectMode: Boolean,
        isEveryItemSelected: Boolean,
        modifier: Modifier = Modifier,
        listener: (MultiSelectBarIntent) -> Unit,
        helpShowcaseListener: (HelpShowcaseIntent) -> Unit,
) {
    @Composable
    fun MultiSelectIconButton(
            onClick: () -> Unit,
            icon: CodexIconInfo,
            @StringRes helpTitle: Int,
            @StringRes helpBody: Int,
            modifier: Modifier = Modifier,
    ) {
        CodexIconButton(
                icon = icon.copyIcon(tint = CodexTheme.colors.onFloatingActions),
                helpState = HelpState(
                        helpShowcaseListener,
                        HelpShowcaseItem(
                                helpTitle = stringResource(helpTitle),
                                helpBody = stringResource(helpBody),
                                priority = ViewScoreHelpPriority.MULTI_SELECT.ordinal,
                        )
                ),
                onClick = onClick,
                modifier = modifier
        )
    }

    Row(
            verticalAlignment = Alignment.CenterVertically,
    ) {
        AnimatedVisibility(
                isInMultiSelectMode,
        ) {
            Text(
                    text = stringResource(R.string.view_scores__multi_select_title),
                    style = CodexTypography.NORMAL.copy(
                            color = CodexTheme.colors.onFloatingActions,
                            fontWeight = FontWeight.Bold,
                    ),
                    modifier = Modifier.padding(start = 20.dp, end = 15.dp)
            )
        }

        if (isInMultiSelectMode) {
            MultiSelectIconButton(
                    onClick = { listener(MultiSelectBarIntent.ClickAllOrNone) },
                    icon = CodexIconInfo.VectorIcon(
                            imageVector = Icons.Default.SelectAll,
                            contentDescription = stringResource(
                                    id = if (isEveryItemSelected) {
                                        R.string.view_scores__multi_deselect_all
                                    }
                                    else {
                                        R.string.view_scores__multi_select_all
                                    }
                            ),
                    ),
                    helpTitle = R.string.help_view_score__select_all_or_none_title,
                    helpBody = R.string.help_view_score__select_all_or_none_body,
                    modifier = Modifier.testTag(ViewScoresTestTag.MULTI_SELECT_ALL.getTestTag()),
            )
        }
        else {
            MultiSelectIconButton(
                    onClick = { listener(MultiSelectBarIntent.ClickOpen) },
                    icon = CodexIconInfo.VectorIcon(
                            imageVector = Icons.Default.SelectAll,
                            contentDescription = stringResource(R.string.view_scores__multi_select_start),
                    ),
                    helpTitle = R.string.help_view_score__start_multi_select_title,
                    helpBody = R.string.help_view_score__start_multi_select_body,
                    modifier = Modifier.testTag(ViewScoresTestTag.MULTI_SELECT_START.getTestTag()),
            )
        }

        AnimatedVisibility(
                isInMultiSelectMode,
        ) {
            MultiSelectIconButton(
                    onClick = { listener(MultiSelectBarIntent.ClickEmail) },
                    icon = CodexIconInfo.VectorIcon(
                            imageVector = Icons.Outlined.Email,
                            contentDescription = stringResource(R.string.view_scores__multi_select_email),
                    ),
                    helpTitle = R.string.help_view_score__action_multi_select_title,
                    helpBody = R.string.help_view_score__action_multi_select_body,
                    modifier = Modifier.testTag(ViewScoresTestTag.MULTI_SELECT_EMAIL.getTestTag()),
            )
        }

        AnimatedVisibility(
                isInMultiSelectMode,
        ) {
            MultiSelectIconButton(
                    onClick = { listener(MultiSelectBarIntent.ClickClose) },
                    icon = CodexIconInfo.VectorIcon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.view_scores__multi_select_cancel),
                    ),
                    helpTitle = R.string.help_view_score__cancel_multi_select_title,
                    helpBody = R.string.help_view_score__cancel_multi_select_body,
                    modifier = Modifier.testTag(ViewScoresTestTag.MULTI_SELECT_CANCEL.getTestTag()),
            )
        }
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun Collapsed_MultiSelectBar_Preview() {
    CodexTheme {
        ViewScoresActionBar(
                modifier = Modifier.padding(10.dp)
        ) {
            MultiSelectBar(
                    isInMultiSelectMode = false,
                    isEveryItemSelected = false,
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
fun Expanded_MultiSelectBar_Preview() {
    CodexTheme {
        ViewScoresActionBar(
                modifier = Modifier.padding(10.dp)
        ) {
            MultiSelectBar(
                    isInMultiSelectMode = true,
                    isEveryItemSelected = false,
                    listener = {},
                    helpShowcaseListener = {},
            )
        }
    }
}
