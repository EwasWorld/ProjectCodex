package eywa.projectcodex.components.archerRoundScore.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.sharedUi.CodexTextField
import eywa.projectcodex.common.sharedUi.CodexTextFieldState
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.components.archerRoundScore.ArcherRoundIntent
import eywa.projectcodex.components.archerRoundScore.ArcherRoundIntent.SettingsIntent
import eywa.projectcodex.components.archerRoundScore.ArcherRoundIntent.SettingsIntent.InputEndSizeChanged
import eywa.projectcodex.components.archerRoundScore.ArcherRoundSubScreen
import eywa.projectcodex.components.archerRoundScore.DataRow
import eywa.projectcodex.components.archerRoundScore.state.ArcherRoundState

class ArcherRoundSettingsScreen : ArcherRoundSubScreen() {
    @Composable
    override fun ComposeContent(
            state: ArcherRoundState.Loaded,
            listener: (ArcherRoundIntent) -> Unit,
    ) {
        ScreenContent(state, listener)
    }

    override fun getHelpShowcases(): List<HelpShowcaseItem> {
        // TODO_CURRENT Help info
        return listOf()
    }

    override fun getHelpPriority(): Int? = null

    @Composable
    private fun ScreenContent(
            state: ArcherRoundSettingsState,
            listener: (SettingsIntent) -> Unit,
    ) {
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
            )
            NumberSetting(
                    title = R.string.archer_round_settings__score_pad_end_size,
                    currentValue = state.scorePadEndSize,
                    testTag = TestTag.SCORE_PAD_END_SIZE,
                    onValueChanged = { listener(SettingsIntent.ScorePadEndSizeChanged(it)) },
            )
            // TODO Change golds type
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    private fun NumberSetting(
            @StringRes title: Int,
            currentValue: Int?,
            testTag: String,
            onValueChanged: (Int?) -> Unit,
    ) {
        val keyboardController = LocalSoftwareKeyboardController.current

        DataRow(
                title = title,
        ) {
            Surface(
                    color = CodexTheme.colors.surfaceOnBackground,
                    shape = RoundedCornerShape(5.dp),
            ) {
                CodexTextField(
                        state = CodexTextFieldState(
                                text = currentValue?.toString() ?: "",
                                onValueChange = { onValueChanged(it.takeIf { it.isNotBlank() }?.toInt()) },
                                testTag = "",
                        ),
                        placeholderText = "6",
                        textStyle = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onSurfaceOnBackground),
                        keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                                onDone = { keyboardController?.hide() },
                        ),
                        modifier = Modifier.testTag(testTag)
                )
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