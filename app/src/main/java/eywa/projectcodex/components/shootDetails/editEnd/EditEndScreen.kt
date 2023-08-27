package eywa.projectcodex.components.shootDetails.editEnd

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
import eywa.projectcodex.components.shootDetails.editEnd.EditEndIntent.*
import eywa.projectcodex.components.shootDetails.getData

@Composable
fun EditEndScreen(
        navController: NavController,
        viewModel: EditEndViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val listener = { it: EditEndIntent -> viewModel.handle(it) }

    ShootDetailsMainScreen(
            currentScreen = CodexNavRoute.SHOOT_DETAILS_EDIT_END,
            state = state,
            listener = { listener(ShootDetailsAction(it)) },
    ) { it, modifier -> EditEndScreen(it, modifier, listener) }

    HandleMainEffects(
            navController = navController,
            state = state,
            listener = { listener(ShootDetailsAction(it)) },
    )
    HandleEffects(navController, state, listener)
}

@Composable
fun HandleEffects(
        navController: NavController,
        state: ShootDetailsResponse<EditEndState>,
        listener: (EditEndIntent) -> Unit,
) {
    val loadedState = state.getData() ?: return
    val context = LocalContext.current

    LaunchedEffect(loadedState) {
        loadedState.errors.forEach {
            ToastSpamPrevention.displayToast(context, context.resources.getString(it.messageId))
            listener(ErrorHandled(it))
        }
        if (loadedState.closeScreen) {
            navController.popBackStack()
            listener(CloseHandled)
        }
    }
}

@Composable
private fun EditEndScreen(
        state: EditEndState,
        modifier: Modifier = Modifier,
        listener: (EditEndIntent) -> Unit,
) {
    ArrowInputsScaffold(
            state = state,
            showCancelButton = true,
            showResetButton = true,
            contentText = stringResource(R.string.edit_end__edit_info, state.endNumber),
            modifier = modifier,
            helpListener = { listener(HelpShowcaseAction(it)) },
            submitHelpInfoTitle = stringResource(R.string.help_edit_end__complete_title),
            submitHelpInfoBody = stringResource(R.string.help_edit_end__complete_body),
            cancelHelpInfoTitle = stringResource(R.string.help_edit_end__cancel_title),
            cancelHelpInfoBody = stringResource(R.string.help_edit_end__cancel_body),
            testTag = EditEndTestTag.SCREEN.getTestTag(),
            listener = { listener(ArrowInputsAction(it)) },
    )
}

enum class EditEndTestTag : CodexTestTag {
    SCREEN,
    ;

    override val screenName: String
        get() = "SHOOT_DETAILS_EDIT_END"

    override fun getElement(): String = name
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
        heightDp = 700,
)
@Composable
fun EditEndScreen_Preview() {
    CodexTheme {
        EditEndScreen(
                EditEndState(
                        main = ShootDetailsStatePreviewHelper.WITH_SHOT_ARROWS.copy(scorePadEndSize = 0),
                        extras = EditEndExtras(),
                )
        ) {}
    }
}
