package eywa.projectcodex.components.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpState
import eywa.projectcodex.common.sharedUi.DataRow
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.components.archerRoundScore.settings.ArcherRoundSettingsScreen

@Composable
fun SettingsScreen(
        viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    SettingsScreen(state) { viewModel.handle(it) }
}

@Composable
fun SettingsScreen(
        state: SettingsState,
        listener: (SettingsIntent) -> Unit,
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(SettingsIntent.HelpShowcaseAction(it)) }

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
            DataRow(
                    title = stringResource(R.string.settings__handicap_system_title),
                    text = stringResource(
                            if (state.use2023System) R.string.settings__handicap_system_agb_2023
                            else R.string.settings__handicap_system_david_lane
                    ),
                    helpState = HelpState(
                            helpListener = helpListener,
                            helpTitle = stringResource(R.string.help_settings__use_2023_system_title),
                            helpBody = stringResource(R.string.help_settings__use_2023_system_body),
                    ),
                    onClick = { listener(SettingsIntent.ToggleUse2023System) },
                    accessibilityRole = Role.Switch,
                    style = CodexTypography.NORMAL.copy(CodexTheme.colors.onAppBackground),
                    modifier = Modifier.padding(bottom = 2.dp)
            )

            Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                DataRow(
                        title = stringResource(R.string.settings__use_beta_features),
                        text = stringResource(
                                if (state.useBetaFeatures) R.string.settings__use_beta_features_on
                                else R.string.settings__use_beta_features_off
                        ),
                        helpState = HelpState(
                                helpListener = helpListener,
                                helpTitle = stringResource(R.string.help_settings__use_beta_features_title),
                                helpBody = stringResource(R.string.help_settings__use_beta_features_body),
                        ),
                        onClick = { listener(SettingsIntent.ToggleUseBetaFeatures) },
                        accessibilityRole = Role.Switch,
                        style = CodexTypography.NORMAL.copy(CodexTheme.colors.onAppBackground),
                        modifier = Modifier.padding(bottom = 2.dp)
                )

                Text(
                        text = stringResource(R.string.settings__use_beta_features_warning),
                        textAlign = TextAlign.Center,
                )
            }
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
