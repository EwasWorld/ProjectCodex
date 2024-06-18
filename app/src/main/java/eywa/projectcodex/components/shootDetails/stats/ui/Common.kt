package eywa.projectcodex.components.shootDetails.stats.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import eywa.projectcodex.common.helpShowcase.HelpState
import eywa.projectcodex.common.helpShowcase.updateHelpDialogPosition
import eywa.projectcodex.common.sharedUi.CodexIconButton
import eywa.projectcodex.common.sharedUi.CodexIconInfo
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.testTag

@Composable
internal fun StatsDivider(
        modifier: Modifier = Modifier,
) {
    Divider(
            thickness = 1.dp,
            color = CodexTheme.colors.onAppBackground,
            modifier = modifier.padding(horizontal = CodexTheme.dimens.screenPadding)
    )
}

@Composable
internal fun Delimiter(
        modifier: Modifier = Modifier,
        style: TextStyle = CodexTypography.NORMAL,
        color: Color = CodexTheme.colors.onAppBackground,
) {
    Text(
            text = ":",
            style = style,
            color = color,
            modifier = modifier.clearAndSetSemantics { },
    )
}

@Composable
internal fun Section(
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit,
) {
    Column(
            verticalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
    ) {
        content()
    }
}

@Composable
internal fun EditBox(
        testTag: StatsTestTag,
        modifier: Modifier = Modifier,
        editContentDescription: String? = null,
        editHelpState: HelpState? = null,
        editListener: (() -> Unit)? = null,
        expandContentDescription: String? = null,
        expandListener: (() -> Unit)? = null,
        expandHelpState: HelpState? = null,
        content: @Composable () -> Unit,
) {
    Box(
            contentAlignment = Alignment.Center,
            modifier = modifier.testTag(testTag)
    ) {
        Surface(
                shape = RoundedCornerShape(20),
                border = BorderStroke(1.dp, CodexTheme.colors.listItemOnAppBackground),
                color = CodexTheme.colors.appBackground,
                modifier = Modifier.padding(5.dp)
        ) {
            Section(
                    modifier = Modifier.padding(horizontal = 35.dp, vertical = 20.dp)
            ) {
                content()
            }
        }
        if (editListener != null && editContentDescription != null) {
            CodexIconButton(
                    icon = CodexIconInfo.VectorIcon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = editContentDescription,
                    ),
                    onClick = editListener,
                    modifier = Modifier
                            .testTag(StatsTestTag.EDIT_SHOOT_INFO)
                            .align(Alignment.BottomEnd)
                            .updateHelpDialogPosition(editHelpState)
            )
        }
        if (expandListener != null && expandContentDescription != null) {
            CodexIconButton(
                    icon = CodexIconInfo.VectorIcon(
                            imageVector = Icons.Default.OpenInFull,
                            contentDescription = expandContentDescription,
                            modifier = Modifier.scale(-1f, 1f)
                    ),
                    onClick = expandListener,
                    modifier = Modifier
                            .testTag(StatsTestTag.EXPAND_SHOOT_INFO)
                            .align(Alignment.BottomStart)
                            .updateHelpDialogPosition(expandHelpState)
            )
        }
    }
}
