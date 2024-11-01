package eywa.projectcodex.components.emailScores

import android.content.ActivityNotFoundException
import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpState
import eywa.projectcodex.common.helpShowcase.updateHelpDialogPosition
import eywa.projectcodex.common.sharedUi.ButtonState
import eywa.projectcodex.common.sharedUi.CODEX_CHIP_SPACING
import eywa.projectcodex.common.sharedUi.CodexChip
import eywa.projectcodex.common.sharedUi.CodexFloatingActionButton
import eywa.projectcodex.common.sharedUi.CodexIconInfo
import eywa.projectcodex.common.sharedUi.CodexNewChipState
import eywa.projectcodex.common.sharedUi.CodexTextField
import eywa.projectcodex.common.sharedUi.CodexTextFieldRoundedSurface
import eywa.projectcodex.common.sharedUi.CodexTextFieldState
import eywa.projectcodex.common.sharedUi.SimpleDialog
import eywa.projectcodex.common.sharedUi.SimpleDialogContent
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelper.addArrows
import eywa.projectcodex.common.sharedUi.previewHelpers.ShootPreviewHelper.addRound
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.components.emailScores.EmailScoresIntent.DismissNoEntriesError
import eywa.projectcodex.components.emailScores.EmailScoresIntent.HelpShowcaseAction
import eywa.projectcodex.components.emailScores.EmailScoresIntent.IntentHandledSuccessfully
import eywa.projectcodex.components.emailScores.EmailScoresIntent.NavigateUpHandled
import eywa.projectcodex.components.emailScores.EmailScoresIntent.OpenError
import eywa.projectcodex.components.emailScores.EmailScoresIntent.SubmitClicked
import eywa.projectcodex.components.emailScores.EmailScoresIntent.UpdateBoolean
import eywa.projectcodex.components.emailScores.EmailScoresIntent.UpdateText
import eywa.projectcodex.model.Arrow


@Composable
fun EmailScoresScreen(
        navController: NavController,
        viewModel: EmailScoresViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val listener = { it: EmailScoresIntent -> viewModel.handle(it) }
    EmailScoresScreen(state, listener)

    val context = LocalContext.current
    LaunchedEffect(state) { handleEffects(state, navController, context, listener) }
}

private fun handleEffects(
        state: EmailScoresState,
        navController: NavController,
        context: Context,
        listener: (EmailScoresIntent) -> Unit,
) {
    if (state.intentWithoutTextExtra != null) {
        try {
            context.startActivity(state.intentWithoutTextExtra)
            navController.popBackStack()
            listener(IntentHandledSuccessfully)
        }
        catch (e: ActivityNotFoundException) {
            listener(OpenError(EmailScoresError.NO_EMAIL_APP_FOUND))
        }
    }
    if (state.navigateUpTriggered) {
        navController.popBackStack()
        listener(NavigateUpHandled)
    }
}

@Composable
fun EmailScoresScreen(
        state: EmailScoresState,
        listener: (EmailScoresIntent) -> Unit,
) {
    @Composable
    fun stringOrBlank(@StringRes id: Int?) = id?.let { stringResource(id) } ?: ""

    SimpleDialog(isShown = state.error != null, onDismissListener = { listener(DismissNoEntriesError) }) {
        SimpleDialogContent(
                title = stringOrBlank(state.error?.title),
                message = stringOrBlank(state.error?.message),
                positiveButton = ButtonState(
                        text = stringOrBlank(state.error?.buttonText),
                        onClick = { listener(DismissNoEntriesError) },
                )
        )
    }

    Box(
            contentAlignment = Alignment.BottomEnd,
            modifier = Modifier
                    .fillMaxSize()
                    .testTag(EmailScoresTestTag.Screen.getTestTag())
    ) {
        Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .background(CodexTheme.colors.appBackground)
                        .padding(15.dp)
        ) {
            ToAndSubject(state, listener)
            Attachments(state, listener)
            MessageBody(state, listener)
        }
        SendButton(listener)
    }
}

@Composable
private fun EmailScoresTextField.asTextFieldState(
        state: EmailScoresState,
        listener: (EmailScoresIntent) -> Unit,
) = CodexTextFieldState(
        text = state.getText(this, default?.let { stringResource(it) } ?: ""),
        onValueChange = { listener(UpdateText(it, this)) },
        testTag = EmailScoresTestTag.TextField(this),
)

@Composable
private fun ToAndSubject(
        state: EmailScoresState,
        listener: (EmailScoresIntent) -> Unit,
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(HelpShowcaseAction(it)) }

    CodexTextFieldRoundedSurface(
            helpState = HelpState(
                    helpListener = helpListener,
                    helpTitle = stringResource(R.string.help_email_scores__to_title),
                    helpBody = stringResource(R.string.help_email_scores__to_body),
            ),
    ) {
        CodexTextField(
                state = EmailScoresTextField.TO.asTextFieldState(state, listener),
                placeholderText = stringResource(id = R.string.email_scores__to_placeholder),
                labelText = stringResource(id = R.string.email_scores__to),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                colors = CodexTextField.transparentOutlinedTextFieldColors(backgroundColor = Color.Transparent),
                modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth()
        )
    }
    CodexTextFieldRoundedSurface(
            helpState = HelpState(
                    helpListener = helpListener,
                    helpTitle = stringResource(R.string.help_email_scores__subject_title),
                    helpBody = stringResource(R.string.help_email_scores__subject_body),
            ),
    ) {
        CodexTextField(
                state = EmailScoresTextField.SUBJECT.asTextFieldState(state, listener),
                placeholderText = stringResource(id = R.string.email_default_message_subject),
                labelText = stringResource(id = R.string.email_scores__subject),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                colors = CodexTextField.transparentOutlinedTextFieldColors(backgroundColor = Color.Transparent),
                modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth()
        )
    }
}

@Composable
private fun Attachments(
        state: EmailScoresState,
        listener: (EmailScoresIntent) -> Unit,
) {
    fun EmailScoresCheckbox.asState(enabled: Boolean = true) = CodexNewChipState(
            selected = state.isChecked(this),
            enabled = enabled,
            testTag = EmailScoresTestTag.Checkbox(this),
    )

    val helpListener = { it: HelpShowcaseIntent -> listener(HelpShowcaseAction(it)) }
    val checkboxListener = { it: EmailScoresCheckbox -> listener(UpdateBoolean(!state.isChecked(it), it)) }

    Row(
            horizontalArrangement = Arrangement.spacedBy(CODEX_CHIP_SPACING, Alignment.Start),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
    ) {
        Text(
                text = stringResource(R.string.email_scores__attachments_title),
                style = CodexTypography.SMALL.copy(CodexTheme.colors.onAppBackground)
        )
        CodexChip(
                text = stringResource(id = R.string.email_scores__full_score_sheet_as_attachment),
                state = EmailScoresCheckbox.FULL_SCORE_SHEET.asState(),
                helpState = HelpState(
                        helpListener = helpListener,
                        helpTitle = stringResource(R.string.help_email_scores__full_score_sheet_attachment_title),
                        helpBody = stringResource(R.string.help_email_scores__full_score_sheet_attachment_body),
                ),
                onToggle = { checkboxListener(EmailScoresCheckbox.FULL_SCORE_SHEET) },
        )
        CodexChip(
                text = stringResource(id = R.string.email_scores__full_score_sheet_with_distance_totals),
                state = EmailScoresCheckbox.DISTANCE_TOTAL.asState(
                        state.isChecked(EmailScoresCheckbox.FULL_SCORE_SHEET)
                ),
                helpState = HelpState(
                        helpListener = helpListener,
                        helpTitle = stringResource(R.string.help_email_scores__include_distance_totals_title),
                        helpBody = stringResource(R.string.help_email_scores__include_distance_totals_body),
                ),
                onToggle = { checkboxListener(EmailScoresCheckbox.DISTANCE_TOTAL) },
        )
    }
}

@Composable
private fun MessageBody(
        state: EmailScoresState,
        listener: (EmailScoresIntent) -> Unit,
) {
    val helpListener = { it: HelpShowcaseIntent -> listener(HelpShowcaseAction(it)) }

    CodexTextFieldRoundedSurface {
        Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(10.dp)
        ) {
            CodexTextField(
                    state = EmailScoresTextField.MESSAGE_HEADER.asTextFieldState(state, listener),
                    placeholderText = stringResource(id = R.string.email_scores__message_header_placeholder),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.None),
                    colors = CodexTextField.transparentOutlinedTextFieldColors(backgroundColor = Color.Transparent),
                    helpState = HelpState(
                            helpListener = helpListener,
                            helpTitle = stringResource(R.string.help_email_scores__message_start_title),
                            helpBody = stringResource(R.string.help_email_scores__message_start_body),
                    ),
                    modifier = Modifier.fillMaxWidth()
            )
            CodexTextFieldRoundedSurface(
                    color = CodexTheme.colors.disabledOnSurfaceOnBackground,
                    helpState = HelpState(
                            helpListener = helpListener,
                            helpTitle = stringResource(R.string.help_email_scores__scores_title),
                            helpBody = stringResource(R.string.help_email_scores__scores_body),
                    ),
                    modifier = Modifier.padding(horizontal = 5.dp)
            ) {
                // TODO Might be better accessibility-wise if each round was in a separate box?
                Text(
                        text = state.getRoundsText(LocalContext.current.resources),
                        style = CodexTypography.SMALL,
                        color = CodexTheme.colors.onListItemAppOnBackground,
                        modifier = Modifier
                                .padding(15.dp)
                                .fillMaxWidth()
                                .testTag(EmailScoresTestTag.ScoreText.getTestTag())
                )
            }
            CodexTextField(
                    state = EmailScoresTextField.MESSAGE_FOOTER.asTextFieldState(state, listener),
                    placeholderText = stringResource(id = R.string.email_scores__message_footer_placeholder),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.None),
                    colors = CodexTextField.transparentOutlinedTextFieldColors(backgroundColor = Color.Transparent),
                    helpState = HelpState(
                            helpListener = helpListener,
                            helpTitle = stringResource(R.string.help_email_scores__message_end_title),
                            helpBody = stringResource(R.string.help_email_scores__message_end_body),
                    ),
                    modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SendButton(
        listener: (EmailScoresIntent) -> Unit,
) {
    val context = LocalContext.current
    val sendHelpState = HelpState(
            helpListener = { listener(HelpShowcaseAction(it)) },
            helpTitle = stringResource(R.string.help_email_scores__send_title),
            helpBody = stringResource(R.string.help_email_scores__send_body),
    )
    sendHelpState.add()
    CodexFloatingActionButton(
            icon = CodexIconInfo.VectorIcon(
                    imageVector = Icons.Default.Send,
                    contentDescription = stringResource(R.string.email_scores__send)
            ),
            onClick = {
                listener(
                        SubmitClicked(
                                context.getExternalFilesDir(null)
                                        ?: throw IllegalStateException("Unable to access storage")
                        )
                )
            },
            modifier = Modifier
                    .padding(15.dp)
                    .updateHelpDialogPosition(sendHelpState)
                    .testTag(EmailScoresTestTag.SendButton.getTestTag())
    )
}

sealed class EmailScoresTestTag : CodexTestTag {
    object Screen : EmailScoresTestTag()
    object ScoreText : EmailScoresTestTag()
    object SendButton : EmailScoresTestTag()

    class TextField(private val field: EmailScoresTextField) : EmailScoresTestTag() {
        override fun getElement(): String = "TEXT_FIELD_$field"
    }

    class Checkbox(private val field: EmailScoresCheckbox) : EmailScoresTestTag() {
        override fun getElement(): String = "CHECK_BOX_$field"
    }

    override val screenName: String
        get() = "EMAIL_SCORE"

    override fun getElement(): String = javaClass.simpleName
}

@Preview(
        showBackground = true,
        backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
        device = Devices.PIXEL_3,
)
@Composable
fun EmailScoresScreen_Preview() {
    CodexTheme {
        EmailScoresScreen(
                state = EmailScoresState(
                        rounds = listOf(
                                ShootPreviewHelper
                                        .newFullShootInfo(1)
                                        .addRound(RoundPreviewHelper.indoorMetricRoundData)
                                        .addArrows(List(36) { Arrow(7, false) }),
                                ShootPreviewHelper
                                        .newFullShootInfo(1)
                                        .addRound(RoundPreviewHelper.outdoorImperialRoundData)
                                        .addArrows(List(12) { Arrow(4, false) }),
                        ),
                        error = null,
                        textFields = mapOf(
                                EmailScoresTextField.TO to "",
                                EmailScoresTextField.SUBJECT to "Archery Scores",
                                EmailScoresTextField.MESSAGE_HEADER to "Hi, here are my scores",
                                EmailScoresTextField.MESSAGE_FOOTER to "From",
                                EmailScoresTextField.TO to "",
                                EmailScoresTextField.TO to "",
                        ),
                        booleanFields = setOf(EmailScoresCheckbox.FULL_SCORE_SHEET),
                )
        ) {}
    }
}
