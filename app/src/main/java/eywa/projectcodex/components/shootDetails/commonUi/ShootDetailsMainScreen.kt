package eywa.projectcodex.components.shootDetails.commonUi

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import eywa.projectcodex.components.shootDetails.headToHead.HeadToHeadBottomNavBarItem

@Composable
fun <T : Any> ShootDetailsMainScreen(
        currentScreen: CodexNavRoute?,
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
fun <T : Any> HandleMainEffects(
        navController: NavController,
        state: ShootDetailsResponse<T>,
        listener: (ShootDetailsIntent) -> Unit,
) {
    val mainMenuClicked = (state as? ShootDetailsResponse.Error<T>)?.mainMenuClicked ?: false
    val navBarClickedItem = (state as? ShootDetailsResponse.Loaded<T>)?.navBarClicked
    val backClicked = (state as? ShootDetailsResponse.Loaded<T>)?.backClicked ?: false
    val countingShootId = (state as? ShootDetailsResponse.Loaded<T>)?.let { if (it.isCounting) it.shootId else null }

    /**
     * Using a handler so the viewmodel can exit the shoot scope
     */
    BackHandler {
        listener(ShootDetailsIntent.BackClicked)
    }

    LaunchedEffect(mainMenuClicked, navBarClickedItem, countingShootId, backClicked) {
        if (mainMenuClicked) {
            navController.popBackStack(CodexNavRoute.MAIN_MENU.routeBase, false)
            listener(ShootDetailsIntent.ReturnToMenuHandled)
        }
        if (navBarClickedItem != null) {
            navBarClickedItem.navigate(
                    navController,
                    mapOf(NavArgument.SHOOT_ID to state.shootId.toString()),
                    popCurrentRoute = true,
            )
            listener(ShootDetailsIntent.NavBarClickHandled(navBarClickedItem))
        }
        if (countingShootId != null) {
            CodexNavRoute.SHOOT_DETAILS_ADD_COUNT.navigate(
                    navController,
                    mapOf(NavArgument.SHOOT_ID to countingShootId.toString()),
                    popCurrentRoute = true,
            )
        }
        if (backClicked) {
            navController.popBackStack()
            listener(ShootDetailsIntent.BackHandled)
        }
    }
}

@Composable
private fun <T> ShootDetailsMainScreen(
        currentScreen: CodexNavRoute?,
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
        if (currentScreen == null) {
            // Don't show bottom nav bar
        }
        else if (StandardBottomNavBarItem.isItem(currentScreen)) {
            ShootDetailsBottomNavBar(
                    currentScreen = currentScreen,
                    items = StandardBottomNavBarItem.entries,
                    listener = { listener(ShootDetailsIntent.NavBarClicked(it)) },
            )
        }
        else if (HeadToHeadBottomNavBarItem.isItem(currentScreen)) {
            ShootDetailsBottomNavBar(
                    currentScreen = currentScreen,
                    items = HeadToHeadBottomNavBarItem.entries,
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
