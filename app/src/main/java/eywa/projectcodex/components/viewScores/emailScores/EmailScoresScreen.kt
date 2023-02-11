package eywa.projectcodex.components.viewScores.emailScores

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.ActionBarHelp
import eywa.projectcodex.common.helpShowcase.ComposeHelpShowcaseItem
import eywa.projectcodex.common.helpShowcase.ComposeHelpShowcaseMap
import eywa.projectcodex.common.helpShowcase.updateHelpDialogPosition
import eywa.projectcodex.common.sharedUi.*
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography


class EmailScoresScreen : ActionBarHelp {
    private val helpInfo = ComposeHelpShowcaseMap()

    @Composable
    fun ComposeContent(
            error: EmailScoresError?,
            toState: CodexTextFieldState,
            subjectState: CodexTextFieldState,
            messageHeaderState: CodexTextFieldState,
            messageScoreText: String,
            messageFooterState: CodexTextFieldState,
            fullScoreSheetState: CodexChipState,
            distanceTotalsSheetState: CodexChipState,
            onSubmit: () -> Unit,
            onErrorOkClicked: () -> Unit,
    ) {
        @Composable
        fun stringOrEmptyString(@StringRes id: Int?) = id?.let { stringResource(id) } ?: ""

        SimpleDialog(isShown = error != null, onDismissListener = onErrorOkClicked) {
            SimpleDialogContent(
                    title = stringOrEmptyString(error?.title),
                    message = stringOrEmptyString(error?.message),
                    positiveButton = ButtonState(
                            text = stringOrEmptyString(error?.buttonText),
                            onClick = onErrorOkClicked,
                    )
            )
        }

        HelpDialogs()
        Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier.fillMaxSize()
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
                RoundedSurface(
                        modifier = Modifier.updateHelpDialogPosition(helpInfo, R.string.help_email_scores__to_title)
                ) {
                    CodexTextField(
                            state = toState,
                            placeholderText = stringResource(id = R.string.email_scores__to_placeholder),
                            labelText = stringResource(id = R.string.email_scores__to),
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                            modifier = Modifier
                                    .padding(10.dp)
                                    .fillMaxWidth()
                    )
                }
                RoundedSurface(
                        modifier = Modifier.updateHelpDialogPosition(
                                helpInfo,
                                R.string.help_email_scores__subject_title
                        )
                ) {
                    CodexTextField(
                            state = subjectState,
                            placeholderText = stringResource(id = R.string.email_default_message_subject),
                            labelText = stringResource(id = R.string.email_scores__subject),
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                            modifier = Modifier
                                    .padding(10.dp)
                                    .fillMaxWidth()
                    )
                }
                Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.Start),
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
                            state = fullScoreSheetState,
                            modifier = Modifier.updateHelpDialogPosition(
                                    helpInfo,
                                    R.string.help_email_scores__full_score_sheet_attachment_title
                            )
                    )
                    CodexChip(
                            text = stringResource(id = R.string.email_scores__full_score_sheet_with_distance_totals),
                            state = distanceTotalsSheetState,
                            modifier = Modifier.updateHelpDialogPosition(
                                    helpInfo,
                                    R.string.help_email_scores__include_distance_totals_title
                            )
                    )
                }
                RoundedSurface {
                    Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(10.dp)
                    ) {
                        CodexTextField(
                                state = messageHeaderState,
                                placeholderText = stringResource(id = R.string.email_scores__message_header_placeholder),
                                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.None),
                                modifier = Modifier
                                        .fillMaxWidth()
                                        .updateHelpDialogPosition(
                                                helpInfo,
                                                R.string.help_email_scores__message_start_title
                                        )
                        )
                        RoundedSurface(
                                color = CodexTheme.colors.disabledOnSurfaceOnBackground,
                                modifier = Modifier
                                        .padding(horizontal = 5.dp)
                                        .updateHelpDialogPosition(helpInfo, R.string.help_email_scores__scores_title)
                        ) {
                            Text(
                                    text = messageScoreText,
                                    style = CodexTypography.SMALL,
                                    modifier = Modifier
                                            .padding(15.dp)
                                            .fillMaxWidth()
                                            .testTag(TestTag.SCORE_TEXT)
                            )
                        }
                        CodexTextField(
                                state = messageFooterState,
                                placeholderText = stringResource(id = R.string.email_scores__message_footer_placeholder),
                                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.None),
                                modifier = Modifier
                                        .fillMaxWidth()
                                        .updateHelpDialogPosition(
                                                helpInfo,
                                                R.string.help_email_scores__message_end_title
                                        )
                        )
                    }
                }
            }
            FloatingActionButton(
                    backgroundColor = CodexTheme.colors.floatingActions,
                    contentColor = CodexTheme.colors.onFloatingActions,
                    onClick = onSubmit,
                    modifier = Modifier
                            .padding(30.dp)
                            .updateHelpDialogPosition(helpInfo, R.string.help_email_scores__send_title)
                            .testTag(TestTag.SEND_BUTTON)
            ) {
                Icon(
                        Icons.Default.Send,
                        contentDescription = stringResource(R.string.email_scores__send)
                )
            }
        }
    }

    @Composable
    private fun HelpDialogs() {
        helpInfo.add(
                ComposeHelpShowcaseItem(
                        helpTitle = R.string.help_email_scores__to_title,
                        helpBody = R.string.help_email_scores__to_body,
                )
        )
        helpInfo.add(
                ComposeHelpShowcaseItem(
                        helpTitle = R.string.help_email_scores__subject_title,
                        helpBody = R.string.help_email_scores__subject_body,
                )
        )
        helpInfo.add(
                ComposeHelpShowcaseItem(
                        helpTitle = R.string.help_email_scores__message_start_title,
                        helpBody = R.string.help_email_scores__message_start_body,
                )
        )
        helpInfo.add(
                ComposeHelpShowcaseItem(
                        helpTitle = R.string.help_email_scores__scores_title,
                        helpBody = R.string.help_email_scores__scores_body,
                )
        )
        helpInfo.add(
                ComposeHelpShowcaseItem(
                        helpTitle = R.string.help_email_scores__message_end_title,
                        helpBody = R.string.help_email_scores__message_end_body,
                )
        )
        helpInfo.add(
                ComposeHelpShowcaseItem(
                        helpTitle = R.string.help_email_scores__full_score_sheet_attachment_title,
                        helpBody = R.string.help_email_scores__full_score_sheet_attachment_body,
                )
        )
        helpInfo.add(
                ComposeHelpShowcaseItem(
                        helpTitle = R.string.help_email_scores__include_distance_totals_title,
                        helpBody = R.string.help_email_scores__include_distance_totals_body,
                )
        )
        helpInfo.add(
                ComposeHelpShowcaseItem(
                        helpTitle = R.string.help_email_scores__send_title,
                        helpBody = R.string.help_email_scores__send_body,
                )
        )
    }

    @Composable
    fun RoundedSurface(
            color: Color = CodexTheme.colors.surfaceOnBackground,
            modifier: Modifier = Modifier,
            content: @Composable () -> Unit
    ) = Surface(
            color = color,
            shape = RoundedCornerShape(5.dp),
            content = content,
            modifier = modifier,
    )

    override fun getHelpShowcases() = helpInfo.getItems()
    override fun getHelpPriority(): Int? = null

    object TestTag {
        private const val prefix = "EMAIL_SCORE_"
        const val SCORE_TEXT = "${prefix}SCORE_TEXT"
        const val SEND_BUTTON = "${prefix}SEND_BUTTON"

        fun forTextField(field: EmailScoresTextField) = "${prefix}TEXT_FIELD_" + field.toString()
        fun forCheckbox(field: EmailScoresCheckbox) = "${prefix}CHECK_BOX_" + field.toString()
    }

    @Preview(
            showBackground = true,
            backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
            device = Devices.PIXEL_3,
    )
    @Composable
    fun EmailScoresScreen_Preview() {
        CodexTheme {
            ComposeContent(
                    error = null,
                    toState = CodexTextFieldState(
                            text = "",
                            onValueChange = {},
                            testTag = "",
                    ),
                    subjectState = CodexTextFieldState(
                            text = stringResource(id = R.string.email_default_message_subject),
                            onValueChange = {},
                            testTag = "",
                    ),
                    messageHeaderState = CodexTextFieldState(
                            text = stringResource(id = R.string.email_scores__message_footer_placeholder),
                            onValueChange = {},
                            testTag = "",
                    ),
                    messageScoreText = stringResource(id = R.string.email_round_summary_sample_text),
                    messageFooterState = CodexTextFieldState(
                            text = stringResource(id = R.string.email_default_message_header),
                            onValueChange = {},
                            testTag = "",
                    ),
                    fullScoreSheetState = CodexChipState(
                            selected = true,
                            enabled = true,
                            onToggle = {},
                            testTag = "",
                    ),
                    distanceTotalsSheetState = CodexChipState(
                            selected = false,
                            enabled = false,
                            onToggle = {},
                            testTag = "",
                    ),
                    onSubmit = {},
                    onErrorOkClicked = {},
            )
        }
    }
}
