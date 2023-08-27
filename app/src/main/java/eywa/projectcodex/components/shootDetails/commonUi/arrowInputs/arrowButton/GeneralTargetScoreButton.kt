package eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.arrowButton

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import eywa.projectcodex.R
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.database.RoundFace
import eywa.projectcodex.model.Arrow

enum class GeneralTargetScoreButton(
        override val text: ResOrActual<String>,
        override val arrow: Arrow,
        override val shouldShow: (RoundFace) -> Boolean = { true },
) : ArrowButton {
    M(
            text = ResOrActual.StringResource(R.string.arrow_value_m),
            arrow = Arrow(0, false),
    ) {
        @Composable override fun getBackgroundColour(): Color = CodexTheme.colors.targetFaceGreen
        @Composable override fun getContentColour(): Color = CodexTheme.colors.onTargetFaceGreen
    },
    ONE(
            text = ResOrActual.Actual("1"),
            arrow = Arrow(1, false),
            shouldShow = { it == RoundFace.FULL },
    ) {
        @Composable override fun getBackgroundColour(): Color = CodexTheme.colors.targetFaceWhite
        @Composable override fun getContentColour(): Color = CodexTheme.colors.onTargetFaceWhite
    },
    TWO(
            text = ResOrActual.Actual("2"),
            arrow = Arrow(2, false),
            shouldShow = { it == RoundFace.FULL },
    ) {
        @Composable override fun getBackgroundColour(): Color = CodexTheme.colors.targetFaceWhite
        @Composable override fun getContentColour(): Color = CodexTheme.colors.onTargetFaceWhite
    },
    THREE(
            text = ResOrActual.Actual("3"),
            arrow = Arrow(3, false),
            shouldShow = { it == RoundFace.FULL },
    ) {
        @Composable override fun getBackgroundColour(): Color = CodexTheme.colors.targetFaceBlack
        @Composable override fun getContentColour(): Color = CodexTheme.colors.onTargetFaceBlack
    },
    FOUR(
            text = ResOrActual.Actual("4"),
            arrow = Arrow(4, false),
            shouldShow = { it == RoundFace.FULL },
    ) {
        @Composable override fun getBackgroundColour(): Color = CodexTheme.colors.targetFaceBlack
        @Composable override fun getContentColour(): Color = CodexTheme.colors.onTargetFaceBlack
    },
    FIVE(
            text = ResOrActual.Actual("5"),
            arrow = Arrow(5, false),
            shouldShow = { it == RoundFace.FULL || it == RoundFace.FITA_SIX },
    ) {
        @Composable override fun getBackgroundColour(): Color = CodexTheme.colors.targetFaceBlue
        @Composable override fun getContentColour(): Color = CodexTheme.colors.onTargetFaceBlue
    },
    SIX(
            text = ResOrActual.Actual("6"),
            arrow = Arrow(6, false),
    ) {
        @Composable override fun getBackgroundColour(): Color = CodexTheme.colors.targetFaceBlue
        @Composable override fun getContentColour(): Color = CodexTheme.colors.onTargetFaceBlue
    },
    SEVEN(
            text = ResOrActual.Actual("7"),
            arrow = Arrow(7, false),
    ) {
        @Composable override fun getBackgroundColour(): Color = CodexTheme.colors.targetFaceRed
        @Composable override fun getContentColour(): Color = CodexTheme.colors.onTargetFaceRed
    },
    EIGHT(
            text = ResOrActual.Actual("8"),
            arrow = Arrow(8, false),
    ) {
        @Composable override fun getBackgroundColour(): Color = CodexTheme.colors.targetFaceRed
        @Composable override fun getContentColour(): Color = CodexTheme.colors.onTargetFaceRed
    },
    NINE(
            text = ResOrActual.Actual("9"),
            arrow = Arrow(9, false),
    ) {
        @Composable override fun getBackgroundColour(): Color = CodexTheme.colors.targetFaceGold
        @Composable override fun getContentColour(): Color = CodexTheme.colors.onTargetFaceGold
    },
    TEN(
            text = ResOrActual.Actual("10"),
            arrow = Arrow(10, false),
    ) {
        @Composable override fun getBackgroundColour(): Color = CodexTheme.colors.targetFaceGold
        @Composable override fun getContentColour(): Color = CodexTheme.colors.onTargetFaceGold
    },
    X(
            text = ResOrActual.StringResource(R.string.arrow_value_x),
            arrow = Arrow(10, true),
    ) {
        @Composable override fun getBackgroundColour(): Color = CodexTheme.colors.targetFaceGold
        @Composable override fun getContentColour(): Color = CodexTheme.colors.onTargetFaceGold
    },
}
