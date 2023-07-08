package eywa.projectcodex.components.viewScores.emailScores

import android.content.ActivityNotFoundException
import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import eywa.projectcodex.common.archeryObjects.Arrow
import eywa.projectcodex.common.helpShowcase.HelpShowcaseIntent
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.helpShowcase.updateHelpDialogPosition
import eywa.projectcodex.common.sharedUi.*
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.sharedUi.previewHelpers.ArcherRoundPreviewHelper
import eywa.projectcodex.common.sharedUi.previewHelpers.ArcherRoundPreviewHelper.addArrows
import eywa.projectcodex.common.sharedUi.previewHelpers.ArcherRoundPreviewHelper.addRound
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.components.viewScores.emailScores.EmailScoresIntent.*


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
    LaunchedEffect(Unit) {
        // TODO_CURRENT if state == Loading
        // TODO_CURRENT check this is called every time an email flow is first started
        viewModel.handle(
                SetInitialValues(
                        subject = context.getString(R.string.email_default_message_subject),
                        messageHeader = context.getString(R.string.email_default_message_header),
                        messageFooter = context.getString(R.string.email_default_message_footer),
                )
        )
    }
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
    fun stringOrEmptyString(@StringRes id: Int?) = id?.let { stringResource(id) } ?: ""

    fun EmailScoresTextField.asState() = CodexTextFieldState(
            text = state.getText(this),
            onValueChange = { listener(UpdateText(it, this)) },
            testTag = EmailScoresTestTag.forTextField(this),
    )

    fun EmailScoresCheckbox.asState(enabled: Boolean = true) = state.isChecked(this).let {
        CodexChipState(
                selected = it,
                onToggle = { listener(UpdateBoolean(!it, this)) },
                enabled = enabled,
                testTag = EmailScoresTestTag.forCheckbox(this),
        )
    }

    val context = LocalContext.current
    val helpListener = { it: HelpShowcaseIntent -> listener(HelpShowcaseAction(it)) }

    SimpleDialog(isShown = state.error != null, onDismissListener = { listener(DismissNoEntriesError) }) {
        SimpleDialogContent(
                title = stringOrEmptyString(state.error?.title),
                message = stringOrEmptyString(state.error?.message),
                positiveButton = ButtonState(
                        text = stringOrEmptyString(state.error?.buttonText),
                        onClick = { listener(DismissNoEntriesError) },
                )
        )
    }

    HelpDialogs(helpListener)
    Box(
            contentAlignment = Alignment.BottomEnd,
            modifier = Modifier
                    .fillMaxSize()
                    .testTag(EmailScoresTestTag.SCREEN)
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
            CodexTextFieldRoundedSurface(
                    modifier = Modifier.updateHelpDialogPosition(helpListener, R.string.help_email_scores__to_title)
            ) {
                CodexTextField(
                        state = EmailScoresTextField.TO.asState(),
                        placeholderText = stringResource(id = R.string.email_scores__to_placeholder),
                        labelText = stringResource(id = R.string.email_scores__to),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                        modifier = Modifier
                                .padding(10.dp)
                                .fillMaxWidth()
                )
            }
            CodexTextFieldRoundedSurface(
                    modifier = Modifier.updateHelpDialogPosition(
                            helpListener,
                            R.string.help_email_scores__subject_title
                    )
            ) {
                CodexTextField(
                        state = EmailScoresTextField.SUBJECT.asState(),
                        placeholderText = stringResource(id = R.string.email_default_message_subject),
                        labelText = stringResource(id = R.string.email_scores__subject),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                        modifier = Modifier
                                .padding(10.dp)
                                .fillMaxWidth()
                )
            }
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
                        modifier = Modifier.updateHelpDialogPosition(
                                helpListener,
                                R.string.help_email_scores__full_score_sheet_attachment_title
                        )
                )
                CodexChip(
                        text = stringResource(id = R.string.email_scores__full_score_sheet_with_distance_totals),
                        state = EmailScoresCheckbox.DISTANCE_TOTAL.asState(
                                state.isChecked(EmailScoresCheckbox.FULL_SCORE_SHEET)
                        ),
                        modifier = Modifier.updateHelpDialogPosition(
                                helpListener,
                                R.string.help_email_scores__include_distance_totals_title
                        )
                )
            }
            CodexTextFieldRoundedSurface {
                Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(10.dp)
                ) {
                    CodexTextField(
                            state = EmailScoresTextField.MESSAGE_HEADER.asState(),
                            placeholderText = stringResource(id = R.string.email_scores__message_header_placeholder),
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.None),
                            modifier = Modifier
                                    .fillMaxWidth()
                                    .updateHelpDialogPosition(
                                            helpListener,
                                            R.string.help_email_scores__message_start_title
                                    )
                    )
                    CodexTextFieldRoundedSurface(
                            color = CodexTheme.colors.disabledOnSurfaceOnBackground,
                            modifier = Modifier
                                    .padding(horizontal = 5.dp)
                                    .updateHelpDialogPosition(
                                            helpListener,
                                            R.string.help_email_scores__scores_title
                                    )
                    ) {
                        Text(
                                text = state.getRoundsText(context.resources),
                                style = CodexTypography.SMALL,
                                color = CodexTheme.colors.onListItemAppOnBackground,
                                modifier = Modifier
                                        .padding(15.dp)
                                        .fillMaxWidth()
                                        .testTag(EmailScoresTestTag.SCORE_TEXT)
                        )
                    }
                    CodexTextField(
                            state = EmailScoresTextField.MESSAGE_FOOTER.asState(),
                            placeholderText = stringResource(id = R.string.email_scores__message_footer_placeholder),
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.None),
                            modifier = Modifier
                                    .fillMaxWidth()
                                    .updateHelpDialogPosition(
                                            helpListener,
                                            R.string.help_email_scores__message_end_title
                                    )
                    )
                }
            }
        }
        FloatingActionButton(
                backgroundColor = CodexTheme.colors.floatingActions,
                contentColor = CodexTheme.colors.onFloatingActions,
                onClick = {
                    listener(
                            SubmitClicked(
                                    context.getExternalFilesDir(null)
                                            ?: throw IllegalStateException("Unable to access storage")
                            )
                    )
                },
                modifier = Modifier
                        .padding(30.dp)
                        .updateHelpDialogPosition(helpListener, R.string.help_email_scores__send_title)
                        .testTag(EmailScoresTestTag.SEND_BUTTON)
        ) {
            Icon(
                    Icons.Default.Send,
                    contentDescription = stringResource(R.string.email_scores__send)
            )
        }
    }
}

@Composable
private fun HelpDialogs(
        helpListener: (HelpShowcaseIntent) -> Unit,
) {
    helpListener(
            HelpShowcaseIntent.Add(
                    HelpShowcaseItem(
                            helpTitle = R.string.help_email_scores__to_title,
                            helpBody = R.string.help_email_scores__to_body,
                    ),
            )
    )
    helpListener(
            HelpShowcaseIntent.Add(
                    HelpShowcaseItem(
                            helpTitle = R.string.help_email_scores__subject_title,
                            helpBody = R.string.help_email_scores__subject_body,
                    ),
            )
    )
    helpListener(
            HelpShowcaseIntent.Add(
                    HelpShowcaseItem(
                            helpTitle = R.string.help_email_scores__message_start_title,
                            helpBody = R.string.help_email_scores__message_start_body,
                    ),
            )
    )
    helpListener(
            HelpShowcaseIntent.Add(
                    HelpShowcaseItem(
                            helpTitle = R.string.help_email_scores__scores_title,
                            helpBody = R.string.help_email_scores__scores_body,
                    ),
            )
    )
    helpListener(
            HelpShowcaseIntent.Add(
                    HelpShowcaseItem(
                            helpTitle = R.string.help_email_scores__message_end_title,
                            helpBody = R.string.help_email_scores__message_end_body,
                    ),
            )
    )
    helpListener(
            HelpShowcaseIntent.Add(
                    HelpShowcaseItem(
                            helpTitle = R.string.help_email_scores__full_score_sheet_attachment_title,
                            helpBody = R.string.help_email_scores__full_score_sheet_attachment_body,
                    ),
            )
    )
    helpListener(
            HelpShowcaseIntent.Add(
                    HelpShowcaseItem(
                            helpTitle = R.string.help_email_scores__include_distance_totals_title,
                            helpBody = R.string.help_email_scores__include_distance_totals_body,
                    ),
            )
    )
    helpListener(
            HelpShowcaseIntent.Add(
                    HelpShowcaseItem(
                            helpTitle = R.string.help_email_scores__send_title,
                            helpBody = R.string.help_email_scores__send_body,
                    ),
            )
    )
}

object EmailScoresTestTag {
    private const val PREFIX = "EMAIL_SCORE_"

    const val SCREEN = "${PREFIX}SCREEN"
    const val SCORE_TEXT = "${PREFIX}SCORE_TEXT"
    const val SEND_BUTTON = "${PREFIX}SEND_BUTTON"

    fun forTextField(field: EmailScoresTextField) = "${PREFIX}TEXT_FIELD_" + field.toString()
    fun forCheckbox(field: EmailScoresCheckbox) = "${PREFIX}CHECK_BOX_" + field.toString()
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
                                ArcherRoundPreviewHelper
                                        .newFullArcherRoundInfo(1)
                                        .addRound(RoundPreviewHelper.indoorMetricRoundData)
                                        .addArrows(List(36) { Arrow(7, false) }),
                                ArcherRoundPreviewHelper
                                        .newFullArcherRoundInfo(1)
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
