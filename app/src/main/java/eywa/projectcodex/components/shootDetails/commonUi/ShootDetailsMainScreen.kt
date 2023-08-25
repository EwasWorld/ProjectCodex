package eywa.projectcodex.components.shootDetails.commonUi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import eywa.projectcodex.R
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.navigation.NavArgument
import eywa.projectcodex.common.sharedUi.CodexButton
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.components.shootDetails.ShootDetailsIntent
import eywa.projectcodex.components.shootDetails.ShootDetailsResponse

@Composable
fun <T> ShootDetailsMainScreen(
        currentScreen: CodexNavRoute,
        state: ShootDetailsResponse<T>,
        listener: (ShootDetailsIntent) -> Unit,
        content: @Composable (T, Modifier) -> Unit,
) {
    Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                    .fillMaxSize()
                    .background(CodexTheme.colors.appBackground)
    ) {
        when (state) {
            is ShootDetailsResponse.Loading -> CircularProgressIndicator(color = CodexTheme.colors.onAppBackground)
            is ShootDetailsResponse.Error -> ShootDetailsErrorScreen(listener)
            is ShootDetailsResponse.Loaded -> ShootDetailsMainScreen(currentScreen, state.data, listener, content)
        }
    }
}

@Composable
fun <T> HandleMainEffects(
        navController: NavController,
        state: ShootDetailsResponse<T>,
        listener: (ShootDetailsIntent) -> Unit,
) {
    val mainMenuClicked = (state as? ShootDetailsResponse.Error<T>)?.mainMenuClicked ?: false
    val navBarClickedItem = (state as? ShootDetailsResponse.Loaded<T>)?.navBarClicked

    LaunchedEffect(mainMenuClicked, navBarClickedItem) {
        if (mainMenuClicked) {
            navController.popBackStack(CodexNavRoute.MAIN_MENU.routeBase, false)
            listener(ShootDetailsIntent.ReturnToMenuHandled)
        }
        if (navBarClickedItem != null) {
            navBarClickedItem.navigate(
                    navController,
                    mapOf(NavArgument.SHOOT_ID to state.shootId.toString()),
            ) {
                val currentRoute = navController.currentDestination?.route
                if (currentRoute != null) {
                    popUpTo(currentRoute) { inclusive = true }
                }
            }
            listener(ShootDetailsIntent.NavBarClickHandled(navBarClickedItem))
        }
    }
}

@Composable
private fun <T> ShootDetailsMainScreen(
        currentScreen: CodexNavRoute,
        state: T,
        listener: (ShootDetailsIntent) -> Unit,
        content: @Composable (T, Modifier) -> Unit,
) {
    Column {
        content(
                state,
                Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
        )
        if (ShootDetailsBottomNavBarItem.isItem(currentScreen)) {
            ShootDetailsBottomNavBar(
                    currentScreen = currentScreen,
                    listener = { listener(ShootDetailsIntent.NavBarClicked(it)) },
            )
        }
    }
}

@Composable
private fun ShootDetailsErrorScreen(
        listener: (ShootDetailsIntent) -> Unit,
) {
    Column(
            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
                text = stringResource(R.string.archer_round_not_found),
                style = CodexTypography.NORMAL,
                color = CodexTheme.colors.onAppBackground,
        )
        CodexButton(
                text = stringResource(R.string.archer_round_not_found_button),
                onClick = { listener(ShootDetailsIntent.ReturnToMenuClicked) },
        )
    }
}
