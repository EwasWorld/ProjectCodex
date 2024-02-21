package eywa.projectcodex.common.helpShowcase.ui

import androidx.annotation.FloatRange
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eywa.projectcodex.R
import eywa.projectcodex.common.sharedUi.CodexButton
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTypography
import eywa.projectcodex.common.utils.CodexTestTag

// TODO Better above/below logic - can it default to below unless there isn't enough room rather than taking the larger?
// TODO Look at alignment? Right align if oval is on the right?

/**
 * @param animationState 1 for fully visible, 0 for fully invisible (expanded off screen)
 */
@Composable
fun HelpShowcase(
        state: HelpShowcaseState,
        @FloatRange(from = 0.0, to = 1.0) animationState: Float = 1f,
) {
    require(animationState in 0f..1f) { "Invalid animation state" }

    val overlayColor = CodexTheme.colors.helpShowcaseScrim

    Box(
            contentAlignment = Alignment.TopStart
    ) {
        Canvas(
                modifier = Modifier
                        .fillMaxSize()
                        // The clear oval doesn't work unless this is <1 - can't remember why, maybe the graphics layer?
                        .alpha(0.998f)
                        .clearAndSetSemantics { }
                        .clickable(onClick = state.overlayClickedListener)
        ) {
            drawRect(
                    color = overlayColor,
                    topLeft = Offset(0f, 0f),
                    size = this.size
            )
            state.drawCutout(this, animationState)
        }
        HelpShowcaseText(state, animationState)
    }
}

@Composable
fun HelpShowcaseText(
        state: HelpShowcaseState,
        @FloatRange(from = 0.0, to = 1.0) animationState: Float = 1f,
) {
    Column(
            modifier = Modifier
                    .offset { state.textAreaTopLeft }
                    .height(with(LocalDensity.current) { state.textAreaHeight.toDp() })
                    .padding(16.dp)
                    .padding(horizontal = 5.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp, state.textAreaVerticalArrangement),
    ) {
        Text(
                text = state.title,
                style = CodexTypography.NORMAL.copy(
                        fontSize = 30.sp,
                        color = CodexTheme.colors.helpShowcaseTitle,
                ),
                modifier = Modifier
                        .padding(bottom = 5.dp)
                        .alpha(animationState)
                        .testTag(ComposeHelpShowcaseTestTag.TITLE.getTestTag())
        )
        Text(
                text = state.message,
                style = CodexTypography.NORMAL.copy(
                        fontSize = 20.sp,
                        color = CodexTheme.colors.helpShowcaseMessage,
                ),
                modifier = Modifier.alpha(animationState)
        )
        if (state.hasNextItem) {
            ClickableText(
                    text = AnnotatedString(stringResource(id = R.string.general_next)),
                    onClick = { state.nextItemListener() },
                    style = CodexTypography.NORMAL.copy(
                            fontSize = 22.sp,
                            color = CodexTheme.colors.helpShowcaseButton
                    ),
                    modifier = Modifier
                            .padding(bottom = 5.dp)
                            .alpha(animationState)
                            .testTag(ComposeHelpShowcaseTestTag.NEXT_BUTTON.getTestTag())
            )
        }
        ClickableText(
                text = AnnotatedString(stringResource(id = R.string.action_bar__close_help)),
                onClick = { state.closeListener() },
                style = CodexTypography.NORMAL.copy(
                        fontSize = 18.sp,
                        color = CodexTheme.colors.helpShowcaseButton
                ),
                modifier = Modifier
                        .alpha(animationState)
                        .testTag(ComposeHelpShowcaseTestTag.CLOSE_BUTTON.getTestTag())
        )
    }
}

@Preview(
        showBackground = true,
        device = Devices.PIXEL_2,
        backgroundColor = 0xFF69BEFF
)
@Composable
private fun ComposeHelpShowcasePreview(
        @PreviewParameter(ComposeHelpShowcasePreviewProvider::class) param: ComposeHelpShowcasePreviewParams,
) {
    Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CodexButton(text = "Hello") {}
        CodexButton(text = "Hello") {}
    }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        HelpShowcase(
                HelpShowcaseOvalState(
                        title = "Title",
                        message = "Message",
                        hasNextItem = param.hasNextItem,
                        nextItemListener = {},
                        closeListener = {},
                        overlayClickedListener = {},
                        ovalTopLeft = param.ovalTopLeft,
                        ovalHeight = param.ovalHeight,
                        ovalWidth = param.ovalWidth,
                        screenSize = Size(
                                height = constraints.minHeight.toFloat(),
                                width = constraints.minWidth.toFloat(),
                        ),
                )
        )
    }
}

data class ComposeHelpShowcasePreviewParams(
        val name: String,
        val ovalTopLeft: Offset = Offset(10f, 100f),
        val ovalHeight: Float = 100f,
        val ovalWidth: Float = 150f,
        val hasNextItem: Boolean = true,
)

class ComposeHelpShowcasePreviewProvider : PreviewParameterProvider<ComposeHelpShowcasePreviewParams> {
    override val values = sequenceOf(
            ComposeHelpShowcasePreviewParams("Text below", Offset(10f, 100f)),
            ComposeHelpShowcasePreviewParams("Text above", Offset(10f, 1300f)),
            ComposeHelpShowcasePreviewParams("Roughly on button", Offset(430f, 850f), ovalWidth = 220f),
            ComposeHelpShowcasePreviewParams("No next", hasNextItem = false),
    )
}

enum class ComposeHelpShowcaseTestTag : CodexTestTag {
    TITLE,
    NEXT_BUTTON,
    CLOSE_BUTTON,
    ;

    override val screenName: String
        get() = "HELP_SHOWCASE"

    override fun getElement(): String = name
}
