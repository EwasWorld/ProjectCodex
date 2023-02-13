package eywa.projectcodex.components.archerRoundScore.settings

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.sharedUi.NumberSetting
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.components.archerRoundScore.ArcherRoundIntent
import eywa.projectcodex.components.archerRoundScore.ArcherRoundIntent.SettingsIntent
import eywa.projectcodex.components.archerRoundScore.ArcherRoundIntent.SettingsIntent.InputEndSizeChanged
import eywa.projectcodex.components.archerRoundScore.ArcherRoundSubScreen
import eywa.projectcodex.components.archerRoundScore.state.ArcherRoundState

class ArcherRoundSettingsScreen : ArcherRoundSubScreen() {
    @Composable
    override fun ComposeContent(
            state: ArcherRoundState.Loaded,
            listener: (ArcherRoundIntent) -> Unit,
    ) {
        ScreenContent(state, listener)
    }

    @Composable
    private fun ScreenContent(
            state: ArcherRoundSettingsState,
            listener: (SettingsIntent) -> Unit,
    ) {
        val helpListener = { it: HelpShowcaseIntent -> listener(SettingsIntent.HelpShowcaseAction(it)) }

        ProvideTextStyle(value = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground)) {
            Column(
                    verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(25.dp)
            ) {
                NumberSetting(
                        title = R.string.archer_round_settings__input_end_size,
                        currentValue = state.inputEndSize,
                        testTag = TestTag.INPUT_END_SIZE,
                        onValueChanged = { listener(InputEndSizeChanged(it)) },
                        helpListener = helpListener,
                        helpTitle = R.string.help_archer_round_settings__input_end_size_title,
                        helpBody = R.string.help_archer_round_settings__input_end_size_body,
                )
                NumberSetting(
                        title = R.string.archer_round_settings__score_pad_end_size,
                        currentValue = state.scorePadEndSize,
                        testTag = TestTag.SCORE_PAD_END_SIZE,
                        onValueChanged = { listener(SettingsIntent.ScorePadEndSizeChanged(it)) },
                        helpListener = helpListener,
                        helpTitle = R.string.help_archer_round_settings__score_pad_size_title,
                        helpBody = R.string.help_archer_round_settings__score_pad_size_body,
                )
                // TODO Change golds type
            }
        }
    }

    object TestTag {
        private const val PREFIX = "ARCHER_ROUND_SETTINGS_"

        const val SCORE_PAD_END_SIZE = "${PREFIX}SCORE_END_SIZE"
        const val INPUT_END_SIZE = "${PREFIX}INPUT_END_SIZE"
    }

    @Preview(
            showBackground = true,
            backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
    )
    @Composable
    fun ArcherRoundSettingsScreen_Preview() {
        CodexTheme {
            ScreenContent(
                    object : ArcherRoundSettingsState {
                        override val inputEndSize: Int = 3
                        override val scorePadEndSize: Int = 6
                    }
            ) {}
        }
    }
}


