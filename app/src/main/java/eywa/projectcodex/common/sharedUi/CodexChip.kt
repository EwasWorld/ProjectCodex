package eywa.projectcodex.common.sharedUi

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpState
import eywa.projectcodex.common.helpShowcase.updateHelpDialogPosition
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography

val CODEX_CHIP_SPACING = 10.dp

/**
 * Text should be no more than 20 characters
 */
@Composable
fun CodexChip(
        text: String,
        state: CodexNewChipState,
        modifier: Modifier = Modifier,
        colours: ChipColours = ChipColours.Defaults.onPrimary(),
        helpState: HelpState? = null,
        onToggle: () -> Unit,
) = CodexChip(
        text = text,
        selected = state.selected,
        enabled = state.enabled,
        modifier = modifier,
        onToggle = onToggle,
        testTag = state.testTag,
        colours = colours,
        helpState = helpState,
)

/**
 * Text should be no more than 20 characters
 */
@Composable
fun CodexChip(
        text: String,
        selected: Boolean,
        testTag: String,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        colours: ChipColours = ChipColours.Defaults.onPrimary(),
        helpState: HelpState? = null,
        onToggle: () -> Unit
) {
    val surfaceColor = when {
        !selected -> Color.Transparent
        enabled -> colours.selectedBackgroundColour
        else -> colours.disabledSelectedBackgroundColour
    }
    val onColor = when {
        !enabled -> colours.disabledContentContent
        selected -> colours.selectedContentColour
        else -> colours.notSelectedContentColour
    }
    val clickModifier = if (!enabled) Modifier else Modifier.selectable(selected = selected, onClick = onToggle)
    val description = text + "." + (if (enabled) "" else (stringResource(R.string.talk_back__disabled_toggle) + "."))

    helpState?.add()

    // TODO Animate
    Surface(
            border = if (selected) null else BorderStroke(Dp.Hairline, onColor),
            shape = RoundedCornerShape(8.dp),
            color = surfaceColor,
            modifier = modifier
                    .updateHelpDialogPosition(helpState)
                    .clearAndSetSemantics {
                        this.contentDescription = description
                        if (enabled) {
                            this.toggleableState = ToggleableState(selected)
                        }
                    }
    ) {
        Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = clickModifier
                        .height(32.dp)
                        .padding(start = 8.dp, end = 16.dp)
                        .testTag(testTag)
        ) {
            if (selected) {
                Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = onColor,
                        modifier = Modifier.size(18.dp)
                )
            }
            else {
                Spacer(modifier = Modifier.size(0.dp, 0.dp))
            }
            Text(
                    text = text,
                    style = CodexTypography.SMALL.copy(color = onColor),
            )
        }
    }
}

data class ChipColours(
        val selectedBackgroundColour: Color,
        val disabledSelectedBackgroundColour: Color,
        val notSelectedContentColour: Color,
        val selectedContentColour: Color,
        val disabledContentContent: Color,
) {
    object Defaults {
        @Composable
        fun onPrimary() = ChipColours(
                selectedBackgroundColour = CodexTheme.colors.chipOnPrimarySelected,
                disabledSelectedBackgroundColour = CodexTheme.colors.disabledButton,
                notSelectedContentColour = CodexTheme.colors.chipOnPrimaryUnselected,
                selectedContentColour = CodexTheme.colors.chipOnPrimarySelectedText,
                disabledContentContent = CodexTheme.colors.disabledButton,
        )

        @Composable
        fun onDialog() = ChipColours(
                selectedBackgroundColour = CodexTheme.colors.chipOnDialogSelected,
                disabledSelectedBackgroundColour = CodexTheme.colors.disabledButton,
                notSelectedContentColour = CodexTheme.colors.chipOnDialogUnselected,
                selectedContentColour = CodexTheme.colors.chipOnDialogSelectedText,
                disabledContentContent = CodexTheme.colors.disabledButton,
        )
    }
}

@Deprecated("Removed onToggle", ReplaceWith("CodexNewChipState"))
data class CodexChipState(
        val selected: Boolean,
        val enabled: Boolean = true,
        val onToggle: () -> Unit,
        val testTag: String,
)

data class CodexNewChipState(
        val selected: Boolean,
        val enabled: Boolean = true,
        val testTag: String,
)

@Preview
@Composable
fun CodexChip_Preview(
        @PreviewParameter(CodexChipPreviewParamProvider::class) params: CodexChipPreviewParams,
) {
    CodexTheme {
        val colours = params.chipColours()
        Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                        .background(params.previewBackground())
                        .padding(10.dp)
        ) {
            Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                CodexChip(
                        text = "First chip", selected = true, enabled = true,
                        onToggle = {}, testTag = "", colours = colours,
                )
                CodexChip(
                        text = "Chip 2", selected = false, enabled = true,
                        onToggle = {}, testTag = "", colours = colours,
                )
            }
            Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CodexChip(
                        text = "Chip 3", selected = true, enabled = false,
                        onToggle = {}, testTag = "", colours = colours,
                )
                CodexChip(
                        text = "Another chip", selected = false, enabled = false,
                        onToggle = {}, testTag = "", colours = colours,
                )
            }
        }
    }
}

data class CodexChipPreviewParams(
        val previewBackground: @Composable () -> Color,
        val chipColours: @Composable () -> ChipColours,
)

class CodexChipPreviewParamProvider : CollectionPreviewParameterProvider<CodexChipPreviewParams>(
        listOf(
                CodexChipPreviewParams({ CodexTheme.colors.appBackground }) { ChipColours.Defaults.onPrimary() },
                CodexChipPreviewParams({ CodexTheme.colors.dialogBackground }) { ChipColours.Defaults.onDialog() },
        )
)
