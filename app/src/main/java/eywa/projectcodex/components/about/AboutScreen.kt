package eywa.projectcodex.components.about

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import eywa.projectcodex.BuildConfig
import eywa.projectcodex.R
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.codexTheme.asClickableStyle
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.common.utils.ToastSpamPrevention
import eywa.projectcodex.common.utils.openWebPage
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsState

@Composable
fun AboutScreen(
        viewModel: AboutViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    AboutScreen(state)
}

@Composable
fun AboutScreen(
        state: UpdateDefaultRoundsState
) {
    val context = LocalContext.current

    ProvideTextStyle(value = CodexTypography.NORMAL.copy(CodexTheme.colors.onAppBackground)) {
        Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                        .fillMaxSize()
                        .background(CodexTheme.colors.appBackground)
                        .testTag(AboutScreenTestTag.SCREEN.getTestTag())
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                    text = stringResource(R.string.about__main_text),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(20.dp)
            )
            Spacer(modifier = Modifier.weight(1f))

            Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(20.dp)
            ) {
                val privacyPolicyErrorMessage = stringResource(R.string.about__privacy_policy_error)
                Text(
                        text = stringResource(R.string.about__privacy_policy),
                        textAlign = TextAlign.Center,
                        style = CodexTypography.NORMAL.asClickableStyle(),
                        modifier = Modifier
                                .padding(10.dp)
                                .clickable {
                                    context.openWebPage(AboutViewModel.PRIVACY_POLICY_URL) {
                                        ToastSpamPrevention.displayToast(context, privacyPolicyErrorMessage)
                                    }
                                }
                )

                Text(
                        text = stringResource(R.string.about__app_version, BuildConfig.VERSION_NAME),
                        textAlign = TextAlign.Center,
                )
                Text(
                        text = stringResource(
                                R.string.about__rounds_version,
                                state.databaseVersion ?: -1,
                        ),
                        textAlign = TextAlign.Center,
                )
                // TODO Add 'Retry' buttons for TemporaryErrors
                Text(
                        text = stringResource(
                                R.string.about__update_default_rounds_message,
                                state.displayString.get(),
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag(AboutScreenTestTag.UPDATE_TASK_STATUS.getTestTag())
                )
            }
        }
    }
}

enum class AboutScreenTestTag : CodexTestTag {
    SCREEN,
    UPDATE_TASK_STATUS,
    ;

    override val screenName: String
        get() = "ABOUT"

    override fun getElement(): String = name
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
)
@Composable
fun AboutScreen_Preview() {
    CodexTheme {
        AboutScreen(state = UpdateDefaultRoundsState.Initialising)
    }
}
