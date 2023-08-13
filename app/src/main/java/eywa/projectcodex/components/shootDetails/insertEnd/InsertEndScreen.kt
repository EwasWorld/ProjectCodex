package eywa.projectcodex.components.shootDetails.insertEnd

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import eywa.projectcodex.R
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.common.utils.ToastSpamPrevention
import eywa.projectcodex.components.shootDetails.ShootDetailsResponse
import eywa.projectcodex.components.shootDetails.commonUi.HandleMainEffects
import eywa.projectcodex.components.shootDetails.commonUi.ShootDetailsMainScreen
import eywa.projectcodex.components.shootDetails.commonUi.ShootDetailsStatePreviewHelper
import eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.ArrowInputsScaffold

@Composable
fun InsertEndScreen(
        navController: NavController,
        viewModel: InsertEndViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val listener = { it: InsertEndIntent -> viewModel.handle(it) }

    ShootDetailsMainScreen(
            currentScreen = CodexNavRoute.SHOOT_DETAILS_ADD_END,
            state = state,
            listener = { listener(InsertEndIntent.ShootDetailsAction(it)) },
    ) { it, modifier -> InsertEndScreen(it, modifier, listener) }

    HandleMainEffects(
            navController = navController,
            state = state,
            listener = { listener(InsertEndIntent.ShootDetailsAction(it)) },
    )
    HandleEffects(navController, state, listener)
}

@Composable
fun HandleEffects(
        navController: NavController,
        state: ShootDetailsResponse<InsertEndState>,
        listener: (InsertEndIntent) -> Unit,
) {
    val loadedState = state.data ?: return
    val context = LocalContext.current

    LaunchedEffect(loadedState) {
        loadedState.errors.forEach {
            ToastSpamPrevention.displayToast(context, context.resources.getString(it.messageId))
            listener(InsertEndIntent.ErrorHandled(it))
        }
        if (loadedState.closeScreen) {
            navController.popBackStack()
            listener(InsertEndIntent.CloseHandled)
        }
    }
}

@Composable
private fun InsertEndScreen(
        state: InsertEndState,
        modifier: Modifier = Modifier,
        listener: (InsertEndIntent) -> Unit,
) {
    val insertLocationString = if (state.endNumber == 1) {
        stringResource(R.string.insert_end__info_at_start)
    }
    else {
        stringResource(R.string.insert_end__info, state.endNumber - 1, state.endNumber)
    }

    ArrowInputsScaffold(
            state = state,
            showCancelButton = true,
            showResetButton = false,
            contentText = insertLocationString,
            modifier = modifier,
            helpListener = { listener(InsertEndIntent.HelpShowcaseAction(it)) },
            submitHelpInfoTitle = stringResource(R.string.help_edit_end__complete_title),
            submitHelpInfoBody = stringResource(R.string.help_edit_end__complete_body),
            cancelHelpInfoTitle = stringResource(R.string.help_edit_end__cancel_title),
            cancelHelpInfoBody = stringResource(R.string.help_edit_end__cancel_body),
            testTag = InsertEndTestTag.SCREEN.getTestTag(),
            listener = { listener(InsertEndIntent.ArrowInputsAction(it)) },
    )
}

enum class InsertEndTestTag : CodexTestTag {
    SCREEN,
    ;

    override val screenName: String
        get() = "SHOOT_DETAILS_INSERT_END"

    override fun getElement(): String = name
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
        heightDp = 700,
)
@Composable
fun InsertEndScreen_Preview() {
    CodexTheme {
        InsertEndScreen(
                InsertEndState(
                        main = ShootDetailsStatePreviewHelper.WITH_SHOT_ARROWS.copy(scorePadEndSize = 1),
                        extras = InsertEndExtras(),
                )
        ) {}
    }
}
