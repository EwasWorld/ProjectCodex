package eywa.projectcodex.components.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.helpShowcase.updateHelpDialogPosition
import eywa.projectcodex.common.sharedUi.CodexCheckbox
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.components.archerRoundScore.settings.ArcherRoundSettingsScreen

@Composable
fun SettingsScreen(
        state: SettingsState,
        listener: (SettingsIntent) -> Unit,
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(SettingsIntent.HelpShowcaseAction(it)) }

    helpListener(
            HelpShowcaseIntent.Add(
                    HelpShowcaseItem(
                            R.string.help_settings__use_2023_system_title,
                            R.string.help_settings__use_2023_system_body,
                    ),
            )
    )

    ProvideTextStyle(value = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground)) {
        Column(
                verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                        .fillMaxSize()
                        .background(CodexTheme.colors.appBackground)
                        .verticalScroll(rememberScrollState())
                        .padding(25.dp)
                        .testTag(ArcherRoundSettingsScreen.TestTag.SCREEN)
        ) {
            CodexCheckbox(
                    text = stringResource(R.string.settings__use_2023_system),
                    checked = state.use2023System,
                    displayAsSwitch = true,
                    onToggle = { listener(SettingsIntent.ToggleUse2023System) },
                    modifier = Modifier
                            .updateHelpDialogPosition(helpListener, R.string.help_settings__use_2023_system_title)
            )
        }
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun SettingsScreen_Preview() {
    CodexTheme {
        SettingsScreen(SettingsState()) {}
    }
}
