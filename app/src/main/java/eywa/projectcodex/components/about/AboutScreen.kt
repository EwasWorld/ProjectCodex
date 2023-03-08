package eywa.projectcodex.components.about

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eywa.projectcodex.BuildConfig
import eywa.projectcodex.R
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.utils.updateDefaultRounds.UpdateDefaultRoundsState
import eywa.projectcodex.common.utils.updateDefaultRounds.asDisplayString

@Composable
fun AboutScreen(
        state: UpdateDefaultRoundsState?
) {
    ProvideTextStyle(value = CodexTypography.NORMAL.copy(CodexTheme.colors.onAppBackground)) {
        Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                        .fillMaxSize()
                        .testTag(AboutScreenTestTag.SCREEN)
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
                Text(
                        text = stringResource(R.string.about__app_version, BuildConfig.VERSION_NAME),
                        textAlign = TextAlign.Center,
                )
                Text(
                        text = stringResource(
                                R.string.about__rounds_version,
                                state?.databaseVersion ?: -1,
                        ),
                        textAlign = TextAlign.Center,
                )
                // TODO Add 'Retry' buttons for TemporaryErrors
                Text(
                        text = stringResource(
                                R.string.about__update_default_rounds_message,
                                state.asDisplayString(LocalContext.current.resources),
                        ),
                        textAlign = TextAlign.Center,
                )
            }
        }
    }
}

object AboutScreenTestTag {
    private const val PREFIX = "ABOUT_"

    const val SCREEN = "${PREFIX}_SCREEN"
    const val UPDATE_TASK_STATUS = "${PREFIX}_UPDATE_TASK_STATUS"
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
