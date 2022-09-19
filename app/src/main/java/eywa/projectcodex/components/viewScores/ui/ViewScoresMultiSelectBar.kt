package eywa.projectcodex.components.viewScores.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.outlined.Email
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.ComposeHelpShowcaseItem
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography

@Composable
internal fun ViewScoresMultiSelectBar(
        isInMultiSelectMode: Boolean,
        multiSelectClickedListener: () -> Unit,
        selectAllOrNoneClickedListener: () -> Unit,
        emailSelectedClickedListener: () -> Unit,
        cancelMultiSelectClickedListener: () -> Unit,
        addHelpInfo: (ComposeHelpShowcaseItem) -> Unit,
        updateHelpDialogPosition: Modifier.(helpTitle: Int) -> Modifier,
        modifier: Modifier = Modifier,
) {
    @Composable
    fun MultiSelectIconButton(
            onClick: () -> Unit,
            imageVector: ImageVector,
            contentDescription: String?,
            @StringRes helpTitle: Int,
            @StringRes helpBody: Int,
    ) {
        addHelpInfo(
                ComposeHelpShowcaseItem(
                        helpTitle = helpTitle,
                        helpBody = helpBody,
                        priority = ViewScoreScreen.HelpItemPriority.MULTI_SELECT.ordinal
                )
        )
        IconButton(onClick = onClick) {
            Icon(
                    imageVector = imageVector,
                    contentDescription = contentDescription,
                    tint = Color.White,
                    modifier = Modifier
                            .scale(1.2f)
                            .updateHelpDialogPosition(helpTitle)
            )
        }
    }

    Surface(
            shape = RoundedCornerShape(CornerSize(35.dp)),
            color = CodexColors.COLOR_PRIMARY_DARK,
            modifier = modifier
    ) {
        if (!isInMultiSelectMode) {
            // TODO_CURRENT Help bubble looks off centre
            MultiSelectIconButton(
                    onClick = multiSelectClickedListener,
                    imageVector = Icons.Default.SelectAll,
                    contentDescription = stringResource(id = R.string.view_scores_menu__multi_select_title),
                    helpTitle = R.string.help_view_score__start_multi_select_title,
                    helpBody = R.string.help_view_score__start_multi_select_body,
            )
        }
        else {
            Row(
                    verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                        text = stringResource(id = R.string.view_scores_menu__multi_select_title),
                        style = CodexTypography.NORMAL.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                        ),
                        modifier = Modifier.padding(start = 20.dp, end = 15.dp)
                )
                MultiSelectIconButton(
                        onClick = selectAllOrNoneClickedListener,
                        imageVector = Icons.Default.SelectAll,
                        contentDescription = stringResource(id = R.string.view_scores_menu__multi_select_all_or_none),
                        helpTitle = R.string.help_view_score__select_all_or_none_title,
                        helpBody = R.string.help_view_score__select_all_or_none_body,
                )
                MultiSelectIconButton(
                        onClick = emailSelectedClickedListener,
                        imageVector = Icons.Outlined.Email,
                        contentDescription = stringResource(id = R.string.view_scores_menu__multi_select_email),
                        helpTitle = R.string.help_view_score__action_multi_select_title,
                        helpBody = R.string.help_view_score__action_multi_select_body,
                )
                MultiSelectIconButton(
                        onClick = cancelMultiSelectClickedListener,
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(id = R.string.view_scores_menu__multi_select_email),
                        helpTitle = R.string.help_view_score__cancel_multi_select_title,
                        helpBody = R.string.help_view_score__cancel_multi_select_body,
                )
            }
        }
    }
}

@Preview
@Composable
fun Collapsed_MultiSelectBar_Preview() {
    CodexTheme {
        ViewScoresMultiSelectBar(
                isInMultiSelectMode = false,
                multiSelectClickedListener = {},
                selectAllOrNoneClickedListener = {},
                emailSelectedClickedListener = {},
                cancelMultiSelectClickedListener = {},
                addHelpInfo = {},
                updateHelpDialogPosition = { Modifier }
        )
    }
}

@Preview
@Composable
fun Expanded_MultiSelectBar_Preview() {
    CodexTheme {
        ViewScoresMultiSelectBar(
                isInMultiSelectMode = true,
                multiSelectClickedListener = {},
                selectAllOrNoneClickedListener = {},
                emailSelectedClickedListener = {},
                cancelMultiSelectClickedListener = {},
                addHelpInfo = {},
                updateHelpDialogPosition = { Modifier }
        )
    }
}