package eywa.projectcodex.components.archerRoundScore

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import eywa.projectcodex.R
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.sharedUi.*
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.utils.ToastSpamPrevention
import eywa.projectcodex.components.archerRoundScore.ArcherRoundIntent.*
import eywa.projectcodex.components.archerRoundScore.state.ArcherRoundScreen
import eywa.projectcodex.components.archerRoundScore.state.ArcherRoundState
import eywa.projectcodex.components.archerRoundScore.state.ArcherRoundState.Loaded
import eywa.projectcodex.components.archerRoundScore.state.ArcherRoundState.Loading
import kotlinx.coroutines.launch

abstract class ArcherRoundSubScreen {
    @Composable
    abstract fun ComposeContent(
            state: Loaded,
            modifier: Modifier,
            listener: (ArcherRoundIntent) -> Unit,
    )
}

@Composable
fun ArcherRoundMainScreen(
        navController: NavController,
        viewModel: ArcherRoundViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val listener = { it: ArcherRoundIntent -> viewModel.handle(it) }
    ArcherRoundMainScreen(state, listener)

    BackHandler((state as? Loaded)?.currentScreen?.isMainScreen == false) {
        viewModel.handle(ArrowInputsIntent.CancelClicked)
    }

    handleEffects(navController, state, listener)
}

@Composable
private fun handleEffects(
        navController: NavController,
        state: ArcherRoundState,
        listener: (ArcherRoundIntent) -> Unit,
) {
    val context = LocalContext.current
    val errors = (state as? ArcherRoundState.Loaded)?.errors
    val returnToMainMenu = (state as? ArcherRoundState.InvalidArcherRoundError)?.mainMenuClicked ?: false
    LaunchedEffect(errors, returnToMainMenu) {
        launch {
            errors?.forEach {
                ToastSpamPrevention.displayToast(context, context.resources.getString(it.messageId))
                listener(ErrorHandled(it))
            }
            if (returnToMainMenu) {
                navController.popBackStack(CodexNavRoute.MAIN_MENU.routeBase, false)
                listener(InvalidArcherRoundIntent.ReturnToMenuHandled)
            }
        }
    }
}

@Composable
fun ArcherRoundMainScreen(
        state: ArcherRoundState,
        listener: (ArcherRoundIntent) -> Unit,
) {
    Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                    .fillMaxSize()
                    .background(CodexTheme.colors.appBackground)
    ) {
        when (state) {
            is ArcherRoundState.InvalidArcherRoundError -> {
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
                            buttonStyle = CodexButtonDefaults.DefaultButton(),
                            onClick = { listener(InvalidArcherRoundIntent.ReturnToMenuClicked) },
                    )
                }
            }
            is Loading -> CircularProgressIndicator()
            is Loaded -> ArcherRoundMainScreen(state, listener)
        }
    }
}

@Composable
private fun ArcherRoundMainScreen(
        state: Loaded,
        listener: (ArcherRoundIntent) -> Unit,
) {
    Column {
        state.currentScreen.getScreen().ComposeContent(
                state = state,
                listener = listener,
                modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
        )
        if (state.showNavBar) {
            ArcherRoundBottomNavBar(
                    currentScreen = state.currentScreen,
                    listener = { listener(NavBarClicked(it)) },
            )
        }
    }

    SimpleDialog(
            isShown = state.displayRoundCompletedDialog,
            onDismissListener = { listener(RoundCompleteDialogOkClicked) },
    ) {
        SimpleDialogContent(
                title = stringResource(R.string.input_end__round_complete),
                positiveButton = ButtonState(
                        text = stringResource(R.string.input_end__go_to_summary),
                        onClick = { listener(RoundCompleteDialogOkClicked) }
                ),
        )
    }
    SimpleDialog(
            isShown = state.displayCannotInputEndDialog,
            onDismissListener = { listener(CannotInputEndDialogOkClicked) },
    ) {
        SimpleDialogContent(
                title = stringResource(R.string.input_end__cannot_open_input_end_title),
                message = stringResource(R.string.input_end__cannot_open_input_end_body),
                positiveButton = ButtonState(
                        text = stringResource(R.string.general_ok),
                        onClick = { listener(CannotInputEndDialogOkClicked) }
                ),
        )
    }
}

object ArcherRoundMainTestTag {
    private const val PREFIX = "ARCHER_ROUND_SCREEN_"

    fun bottomNavBarItem(screen: ArcherRoundScreen): String {
        require(screen.bottomNavItemInfo != null) { "${screen.name} isn't on the nav bar" }
        return "${PREFIX}BOTTOM_NAV_BAR_" + screen.name
    }
}
