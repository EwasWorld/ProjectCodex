package eywa.projectcodex.components.viewScores.emailScores

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eywa.projectcodex.R
import eywa.projectcodex.common.helpShowcase.ActionBarHelp
import eywa.projectcodex.common.helpShowcase.ComposeHelpShowcaseMap
import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.sharedUi.CodexButton
import eywa.projectcodex.common.sharedUi.CodexButtonDefaults
import eywa.projectcodex.common.sharedUi.CodexChip
import eywa.projectcodex.common.sharedUi.CodexTextField
import eywa.projectcodex.common.sharedUi.codexTheme.CodexColors
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography

// TODO_CURRENT Check screen layout when soft keyboard is up
// TODO_CURRENT Check keyboard actions
class EmailScoresScreen : ActionBarHelp {
    private val helpInfo = ComposeHelpShowcaseMap()

    @Composable
    fun ComposeContent() {
        Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                        .fillMaxSize()
                        .background(CodexTheme.colors.appBackground)
                        .padding(10.dp)
        ) {
            RoundedSurface {
                CodexTextField(
                        text = "",
                        onValueChange = {},
                        placeholderText = stringResource(id = R.string.email_scores__to_placeholder),
                        labelText = stringResource(id = R.string.email_scores__to),
                        modifier = Modifier
                                .padding(10.dp)
                                .fillMaxWidth()
                )
            }
            RoundedSurface {
                CodexTextField(
                        text = stringResource(id = R.string.email_default_message_subject),
                        onValueChange = {},
                        placeholderText = stringResource(id = R.string.email_default_message_subject),
                        labelText = stringResource(id = R.string.email_scores__subject),
                        modifier = Modifier
                                .padding(10.dp)
                                .fillMaxWidth()
                )
            }
            Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                Text(
                        text = "Attachment:",
                        style = CodexTypography.SMALL.copy(CodexTheme.colors.textOnPrimary)
                )
                CodexChip(
                        text = stringResource(id = R.string.email_scores__full_score_sheet_as_attachment),
                        isChecked = true,
                        onClick = {},
                )
                CodexChip(
                        text = stringResource(id = R.string.email_scores__full_score_sheet_with_distance_totals),
                        isChecked = true,
                        isDisabled = true,
                        onClick = {},
                )
            }
            RoundedSurface {
                Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                                .verticalScroll(rememberScrollState())
                                .padding(10.dp)
                ) {
                    CodexTextField(
                            text = stringResource(id = R.string.email_default_message_header),
                            onValueChange = {},
                            placeholderText = stringResource(id = R.string.email_scores__message_header_placeholder),
                            modifier = Modifier.fillMaxWidth()
                    )
                    RoundedSurface(
                            color = CodexTheme.colors.disabledOnSurfaceOnBackground,
                            modifier = Modifier.padding(horizontal = 5.dp)
                    ) {
                        Text(
                                text = stringResource(id = R.string.email_round_summary_sample_text),
                                style = CodexTypography.SMALL,
                                modifier = Modifier
                                        .padding(15.dp)
                                        .fillMaxWidth()
                        )
                    }
                    CodexTextField(
                            text = stringResource(id = R.string.email_default_message_footer),
                            onValueChange = {},
                            placeholderText = stringResource(id = R.string.email_scores__message_footer_placeholder),
                            modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            CodexButton(
                    text = stringResource(id = R.string.email_scores__send),
                    buttonStyle = CodexButtonDefaults.DefaultButton(),
                    onClick = {},
            )
        }
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

    override fun getHelpShowcases(): List<HelpShowcaseItem> = helpInfo.getItems()
    override fun getHelpPriority(): Int? = null

    object TestTag {
    }

    @Preview(
            showBackground = true,
            backgroundColor = CodexColors.Raw.COLOR_PRIMARY,
            device = Devices.PIXEL_3,
    )
    @Composable
    fun EmailScoresScreen_Preview() {
        CodexTheme {
            ComposeContent()
        }
    }
}