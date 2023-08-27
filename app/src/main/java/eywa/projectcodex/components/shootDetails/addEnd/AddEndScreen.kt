package eywa.projectcodex.components.shootDetails.addEnd

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import eywa.projectcodex.R
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.sharedUi.ButtonState
import eywa.projectcodex.common.sharedUi.SimpleDialog
import eywa.projectcodex.common.sharedUi.SimpleDialogContent
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.common.utils.ToastSpamPrevention
import eywa.projectcodex.components.shootDetails.ShootDetailsResponse
import eywa.projectcodex.components.shootDetails.addEnd.AddEndIntent.*
import eywa.projectcodex.components.shootDetails.commonUi.HandleMainEffects
import eywa.projectcodex.components.shootDetails.commonUi.ShootDetailsMainScreen
import eywa.projectcodex.components.shootDetails.commonUi.ShootDetailsStatePreviewHelper
import eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.ArrowInputsScaffold
import eywa.projectcodex.components.shootDetails.getData
import eywa.projectcodex.model.FullShootInfo

// TODO_CURRENT Help info for table and remaining arrows

@Composable
fun AddEndScreen(
        navController: NavController,
        viewModel: AddEndViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val listener = { it: AddEndIntent -> viewModel.handle(it) }

    ShootDetailsMainScreen(
            currentScreen = CodexNavRoute.SHOOT_DETAILS_ADD_END,
            state = state,
            listener = { listener(ShootDetailsAction(it)) },
    ) { it, modifier -> AddEndScreen(it, modifier, listener) }

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
        state: ShootDetailsResponse<AddEndState>,
        listener: (AddEndIntent) -> Unit,
) {
    val loadedState = state.getData() ?: return
    val context = LocalContext.current

    LaunchedEffect(loadedState) {
        loadedState.errors.forEach {
            ToastSpamPrevention.displayToast(context, context.resources.getString(it.messageId))
            listener(ErrorHandled(it))
        }
    }
}

@Composable
private fun AddEndScreen(
        state: AddEndState,
        modifier: Modifier = Modifier,
        listener: (AddEndIntent) -> Unit,
) {
    ArrowInputsScaffold(
            state = state,
            showCancelButton = false,
            showResetButton = false,
            submitButtonText = stringResource(R.string.input_end__next_end),
            modifier = modifier,
            helpListener = { listener(HelpShowcaseAction(it)) },
            submitHelpInfoTitle = stringResource(R.string.help_input_end__next_end_title),
            submitHelpInfoBody = stringResource(R.string.help_input_end__next_end_body),
            testTag = AddEndTestTag.SCREEN.getTestTag(),
            listener = { listener(ArrowInputsAction(it)) },
    ) {
        ScoreIndicator(
                state.fullShootInfo.score,
                state.fullShootInfo.arrowsShot,
        )
        RemainingArrowsIndicator(state.fullShootInfo)
        Spacer(modifier = Modifier.size(DpSize.Zero))
    }

    SimpleDialog(
            isShown = state.roundCompleted,
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
            isShown = state.isRoundFull && !state.roundCompleted,
            onDismissListener = { listener(RoundFullDialogOkClicked) },
    ) {
        SimpleDialogContent(
                title = stringResource(R.string.input_end__cannot_open_input_end_title),
                message = stringResource(R.string.input_end__cannot_open_input_end_body),
                positiveButton = ButtonState(
                        text = stringResource(R.string.input_end__go_to_summary),
                        onClick = { listener(RoundFullDialogOkClicked) }
                ),
        )
    }
}

@Composable
private fun ScoreIndicator(
        totalScore: Int,
        arrowsShot: Int,
) {
    Row {
        Column(
                modifier = Modifier.width(IntrinsicSize.Max)
        ) {
            ScoreIndicatorCell(
                    text = stringResource(R.string.input_end__archer_score_header),
                    isHeader = true,
            )
            ScoreIndicatorCell(
                    text = totalScore.toString(),
                    isHeader = false,
                    modifier = Modifier.testTag(AddEndTestTag.ROUND_SCORE.getTestTag())
            )
        }
        Column(
                modifier = Modifier.width(IntrinsicSize.Max)
        ) {
            ScoreIndicatorCell(
                    text = stringResource(R.string.input_end__archer_arrows_count_header),
                    isHeader = true,
            )
            ScoreIndicatorCell(
                    text = arrowsShot.toString(),
                    isHeader = false,
                    modifier = Modifier.testTag(AddEndTestTag.ROUND_ARROWS.getTestTag())
            )
        }
    }
}

@Composable
private fun ScoreIndicatorCell(
        text: String,
        isHeader: Boolean,
        modifier: Modifier = Modifier,
) {
    Text(
            text = text,
            style = CodexTypography.LARGE,
            color = CodexTheme.colors.onAppBackground,
            textAlign = TextAlign.Center,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            fontSize = if (isHeader) CodexTypography.NORMAL.fontSize else CodexTypography.LARGE.fontSize,
            modifier = modifier
                    .fillMaxWidth()
                    .border(1.dp, CodexTheme.colors.onAppBackground)
                    .padding(vertical = 5.dp, horizontal = 10.dp)
    )
}

@Composable
private fun RemainingArrowsIndicator(
        fullShootInfo: FullShootInfo
) {
    fullShootInfo.remainingArrowsAtDistances?.let {
        val remainingStrings = it.map { (count, distance) ->
            stringResource(
                    R.string.input_end__round_indicator_at,
                    count,
                    distance,
                    stringResource(fullShootInfo.distanceUnit!!)
            )
        }

        Column(
                horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                    text = stringResource(R.string.input_end__round_indicator_label),
                    style = CodexTypography.NORMAL,
                    color = CodexTheme.colors.onAppBackground,
            )
            Text(
                    text = remainingStrings.first(),
                    style = CodexTypography.LARGE,
                    color = CodexTheme.colors.onAppBackground,
                    modifier = Modifier.testTag(AddEndTestTag.REMAINING_ARROWS_CURRENT.getTestTag())
            )
            if (it.size > 1) {
                Text(
                        text = remainingStrings
                                .drop(1)
                                .joinToString(stringResource(R.string.general_comma_separator)),
                        style = CodexTypography.NORMAL,
                        color = CodexTheme.colors.onAppBackground,
                        modifier = Modifier.testTag(AddEndTestTag.REMAINING_ARROWS_LATER.getTestTag())
                )
            }
        }
    }
}

enum class AddEndTestTag : CodexTestTag {
    SCREEN,
    REMAINING_ARROWS_CURRENT,
    REMAINING_ARROWS_LATER,
    ROUND_SCORE,
    ROUND_ARROWS,
    ;

    override val screenName: String
        get() = "SHOOT_DETAILS_ADD_END"

    override fun getElement(): String = name
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
        heightDp = 700,
)
@Composable
fun AddEndScreen_Preview() {
    CodexTheme {
        AddEndScreen(
                AddEndState(
                        main = ShootDetailsStatePreviewHelper.WITH_SHOT_ARROWS,
                        extras = AddEndExtras(),
                )
        ) {}
    }
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
        heightDp = 450,
        widthDp = 330,
)
@Composable
fun Mini_AddEndScreen_Preview() {
    CodexTheme {
        AddEndScreen(
                AddEndState(
                        main = ShootDetailsStatePreviewHelper.WITH_SHOT_ARROWS,
                        extras = AddEndExtras(),
                )
        ) {}
    }
}
