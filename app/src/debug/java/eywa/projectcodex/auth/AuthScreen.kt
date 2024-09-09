package eywa.projectcodex.auth

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import eywa.projectcodex.common.sharedUi.CodexButton
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.utils.auth.AuthIntent
import eywa.projectcodex.common.utils.auth.AuthState
import eywa.projectcodex.common.utils.auth.AuthUseCase

@Composable
fun AuthScreen(
        viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    AuthScreen(state, viewModel.authUseCase::sendEvent)
}

@Composable
fun AuthScreen(
        state: AuthState,
        listener: (AuthIntent) -> Unit,
) {
    ProvideTextStyle(CodexTypography.NORMAL.copy(color = CodexTheme.colors.onAppBackground)) {
        Column(
                verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                        .fillMaxSize()
                        .background(CodexTheme.colors.appBackground)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = CodexTheme.dimens.screenPadding)
//                        .testTag(ClassificationTablesTestTag.SCREEN)
        ) {
            Text(
                    text = when {
                        state.error != null -> "Error: ${state.error}"
                        state.profile == null -> "Log in"
                        else -> "Welcome ${state.profile.name} ${state.profile.email}"
                    },
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = CodexTheme.dimens.screenPadding)
            )
            if (state.error == null) {
                CodexButton(
                        text = if (state.profile == null) "Login" else "Logout",
                        onClick = { listener(if (state.profile == null) AuthIntent.Login else AuthIntent.Logout) },
                )
            }
        }
    }
}

@Preview
@Composable
fun LoggedIn_AuthScreen_Preview() {
    CodexTheme {
        AuthScreen(
                AuthState(AuthUseCase.Profile(email = "email@email.com", name = "Charles")),
        ) {}
    }
}

@Preview
@Composable
fun LoggedOut_AuthScreen_Preview() {
    CodexTheme {
        AuthScreen(AuthState()) {}
    }
}

@Preview
@Composable
fun Error_AuthScreen_Preview() {
    CodexTheme {
        AuthScreen(AuthState(error = "Login error: Exception blah blah")) {}
    }
}
