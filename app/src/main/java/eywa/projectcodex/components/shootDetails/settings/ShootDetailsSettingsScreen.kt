package eywa.projectcodex.components.shootDetails.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpState
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.numberField.CodexLabelledNumberField
import eywa.projectcodex.common.sharedUi.numberField.CodexNumberFieldErrorText
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.components.shootDetails.commonUi.HandleMainEffects
import eywa.projectcodex.components.shootDetails.commonUi.ShootDetailsMainScreen
import eywa.projectcodex.components.shootDetails.commonUi.ShootDetailsStatePreviewHelper
import eywa.projectcodex.components.shootDetails.settings.SettingsTestTag.ADD_END_SIZE
import eywa.projectcodex.components.shootDetails.settings.SettingsTestTag.ADD_END_SIZE_ERROR_TEXT
import eywa.projectcodex.components.shootDetails.settings.SettingsTestTag.SCORE_PAD_END_SIZE
import eywa.projectcodex.components.shootDetails.settings.SettingsTestTag.SCORE_PAD_END_SIZE_ERROR_TEXT
import eywa.projectcodex.components.shootDetails.settings.SettingsTestTag.SCREEN
import eywa.projectcodex.components.shootDetails.settings.ShootDetailsSettingsIntent.AddEndSizeChanged
import eywa.projectcodex.components.shootDetails.settings.ShootDetailsSettingsIntent.HelpShowcaseAction
import eywa.projectcodex.components.shootDetails.settings.ShootDetailsSettingsIntent.ScorePadEndSizeChanged
import eywa.projectcodex.components.shootDetails.settings.ShootDetailsSettingsIntent.ShootDetailsAction

@Composable
fun ShootDetailsSettingsScreen(
        navController: NavController,
        viewModel: ShootDetailsSettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val listener = { it: ShootDetailsSettingsIntent -> viewModel.handle(it) }

    ShootDetailsMainScreen(
            currentScreen = CodexNavRoute.SHOOT_DETAILS_SETTINGS,
            state = state,
            listener = { listener(ShootDetailsAction(it)) },
    ) { it, modifier -> ShootDetailsSettingsScreen(it, modifier, listener) }

    HandleMainEffects(
            navController = navController,
            state = state,
            listener = { listener(ShootDetailsAction(it)) },
    )
}

@Composable
private fun ShootDetailsSettingsScreen(
        state: ShootDetailsSettingsState,
        modifier: Modifier = Modifier,
        listener: (ShootDetailsSettingsIntent) -> Unit,
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(HelpShowcaseAction(it)) }

    ProvideTextStyle(value = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground)) {
        Column(
                verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = modifier
                        .padding(25.dp)
                        .testTag(SCREEN.getTestTag())
        ) {
            CodexLabelledNumberField(
                    title = stringResource(R.string.archer_round_settings__input_end_size),
                    currentValue = state.addEndSizePartial.text,
                    placeholder = "6",
                    testTag = ADD_END_SIZE,
                    onValueChanged = { listener(AddEndSizeChanged(it)) },
                    helpState = HelpState(
                            helpListener = helpListener,
                            helpTitle = stringResource(R.string.help_archer_round_settings__input_end_size_title),
                            helpBody = stringResource(R.string.help_archer_round_settings__input_end_size_body),
                    ),
            )
            CodexNumberFieldErrorText(
                    errorText = state.addEndSizePartial.error,
                    testTag = ADD_END_SIZE_ERROR_TEXT,
            )
            Spacer(modifier = Modifier.height(12.dp))
            CodexLabelledNumberField(
                    title = stringResource(R.string.archer_round_settings__score_pad_end_size),
                    currentValue = state.scorePadEndSizePartial.text,
                    placeholder = "6",
                    testTag = SCORE_PAD_END_SIZE,
                    onValueChanged = { listener(ScorePadEndSizeChanged(it)) },
                    helpState = HelpState(
                            helpListener = helpListener,
                            helpTitle = stringResource(R.string.help_archer_round_settings__score_pad_size_title),
                            helpBody = stringResource(R.string.help_archer_round_settings__score_pad_size_body),
                    ),
            )
            CodexNumberFieldErrorText(
                    errorText = state.scorePadEndSizePartial.error,
                    testTag = SCORE_PAD_END_SIZE_ERROR_TEXT,
            )
            // TODO Change golds type
        }
    }
}

enum class SettingsTestTag : CodexTestTag {
    SCREEN,
    SCORE_PAD_END_SIZE,
    SCORE_PAD_END_SIZE_ERROR_TEXT,
    ADD_END_SIZE,
    ADD_END_SIZE_ERROR_TEXT,
    ;

    override val screenName: String
        get() = "SHOOT_DETAILS_SETTINGS"

    override fun getElement(): String = name
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun SettingsScreen_Preview() {
    CodexTheme {
        ShootDetailsSettingsScreen(
                ShootDetailsSettingsState(
                        main = ShootDetailsStatePreviewHelper.SIMPLE,
                        extras = ShootDetailsSettingsExtras().let {
                            it.copy(
                                    addEndSizePartial = it.addEndSizePartial.onTextChanged("3"),
                                    scorePadEndSizePartial = it.scorePadEndSizePartial.onTextChanged("3"),
                            )
                        }
                )
        ) {}
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun Error_SettingsScreen_Preview() {
    CodexTheme {
        ShootDetailsSettingsScreen(
                ShootDetailsSettingsState(
                        main = ShootDetailsStatePreviewHelper.SIMPLE,
                        extras = ShootDetailsSettingsExtras().let {
                            it.copy(
                                    addEndSizePartial = it.addEndSizePartial.onTextChanged("-1"),
                                    scorePadEndSizePartial = it.scorePadEndSizePartial.onTextChanged("-1"),
                            )
                        }
                )
        ) {}
    }
}
