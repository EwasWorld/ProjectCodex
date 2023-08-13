package eywa.projectcodex.components.shootDetails.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.sharedUi.NumberSetting
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.components.shootDetails.commonUi.HandleMainEffects
import eywa.projectcodex.components.shootDetails.commonUi.ShootDetailsMainScreen
import eywa.projectcodex.components.shootDetails.commonUi.ShootDetailsStatePreviewHelper
import eywa.projectcodex.components.shootDetails.settings.SettingsIntent.*
import eywa.projectcodex.components.shootDetails.settings.SettingsTestTag.*

@Composable
fun SettingsScreen(
        navController: NavController,
        viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val listener = { it: SettingsIntent -> viewModel.handle(it) }

    ShootDetailsMainScreen(
            currentScreen = CodexNavRoute.SHOOT_DETAILS_SETTINGS,
            state = state,
            listener = { listener(ShootDetailsAction(it)) },
    ) { it, modifier -> SettingsScreen(it, modifier, listener) }

    HandleMainEffects(
            navController = navController,
            state = state,
            listener = { listener(ShootDetailsAction(it)) },
    )
}

@Composable
private fun SettingsScreen(
        state: SettingsState,
        modifier: Modifier = Modifier,
        listener: (SettingsIntent) -> Unit,
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(HelpShowcaseAction(it)) }

    ProvideTextStyle(value = CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground)) {
        Column(
                verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = modifier
                        .padding(25.dp)
                        .testTag(SCREEN.getTestTag())
        ) {
            NumberSetting(
                    clazz = Int::class,
                    title = R.string.archer_round_settings__input_end_size,
                    currentValue = state.addEndSizePartial,
                    placeholder = 6,
                    testTag = ADD_END_SIZE.getTestTag(),
                    onValueChanged = { listener(AddEndSizeChanged(it)) },
                    helpListener = helpListener,
                    helpTitle = R.string.help_archer_round_settings__input_end_size_title,
                    helpBody = R.string.help_archer_round_settings__input_end_size_body,
            )
            NumberSetting(
                    clazz = Int::class,
                    title = R.string.archer_round_settings__score_pad_end_size,
                    currentValue = state.scorePadEndSizePartial,
                    placeholder = 6,
                    testTag = SCORE_PAD_END_SIZE.getTestTag(),
                    onValueChanged = { listener(ScorePadEndSizeChanged(it)) },
                    helpListener = helpListener,
                    helpTitle = R.string.help_archer_round_settings__score_pad_size_title,
                    helpBody = R.string.help_archer_round_settings__score_pad_size_body,
            )
            // TODO Change golds type
        }
    }
}

enum class SettingsTestTag : CodexTestTag {
    SCREEN,
    SCORE_PAD_END_SIZE,
    ADD_END_SIZE,
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
        SettingsScreen(
                SettingsState(
                        main = ShootDetailsStatePreviewHelper.SIMPLE,
                        extras = SettingsExtras(
                                addEndSizePartial = 3,
                                scorePadEndSizePartial = 6,
                        )
                )
        ) {}
    }
}
