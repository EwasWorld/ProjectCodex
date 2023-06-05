package eywa.projectcodex.components.archerRoundScore.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
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
            modifier: Modifier,
            listener: (ArcherRoundIntent) -> Unit,
    ) {
        ScreenContent(state, modifier, listener)
    }

    @Composable
    private fun ScreenContent(
            state: ArcherRoundSettingsState,
            modifier: Modifier = Modifier,
            listener: (SettingsIntent) -> Unit,
    ) {
        val helpListener = { it: HelpShowcaseIntent -> listener(SettingsIntent.HelpShowcaseAction(it)) }

        ProvideTextStyle(value = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground)) {
            Column(
                    verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = modifier
                            .padding(25.dp)
                            .testTag(TestTag.SCREEN)
            ) {
                NumberSetting(
                        clazz = Int::class,
                        title = R.string.archer_round_settings__input_end_size,
                        currentValue = state.inputEndSizePartial,
                        placeholder = 6,
                        testTag = TestTag.INPUT_END_SIZE,
                        onValueChanged = { listener(InputEndSizeChanged(it)) },
                        helpListener = helpListener,
                        helpTitle = R.string.help_archer_round_settings__input_end_size_title,
                        helpBody = R.string.help_archer_round_settings__input_end_size_body,
                )
                NumberSetting(
                        clazz = Int::class,
                        title = R.string.archer_round_settings__score_pad_end_size,
                        currentValue = state.scorePadEndSizePartial,
                        placeholder = 6,
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

        const val SCREEN = "${PREFIX}SCREEN"
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
                        override val inputEndSizePartial: Int = 3
                        override val scorePadEndSizePartial: Int = 6
                    }
            ) {}
        }
    }
}


