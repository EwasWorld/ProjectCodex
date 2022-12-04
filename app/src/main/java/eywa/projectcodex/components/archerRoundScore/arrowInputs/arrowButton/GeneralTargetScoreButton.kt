package eywa.projectcodex.components.archerRoundScore.arrowInputs.arrowButton

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import eywa.projectcodex.R
import eywa.projectcodex.common.archeryObjects.Arrow
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.utils.ResOrActual

enum class GeneralTargetScoreButton(
        override val text: ResOrActual<String>,
        override val arrow: Arrow,
) : ArrowButton {
    M(
            text = ResOrActual.fromRes(R.string.arrow_value_m),
            arrow = Arrow(0, false)
    ) {
        @Composable override fun getBackgroundColour(): Color = CodexTheme.colors.targetFaceGreen
        @Composable override fun getContentColour(): Color = CodexTheme.colors.onTargetFaceGreen
    },
    ONE(
            text = ResOrActual.fromActual("1"),
            arrow = Arrow(1, false)
    ) {
        @Composable override fun getBackgroundColour(): Color = CodexTheme.colors.targetFaceWhite
        @Composable override fun getContentColour(): Color = CodexTheme.colors.onTargetFaceWhite
    },
    TWO(
            text = ResOrActual.fromActual("2"),
            arrow = Arrow(2, false)
    ) {
        @Composable override fun getBackgroundColour(): Color = CodexTheme.colors.targetFaceWhite
        @Composable override fun getContentColour(): Color = CodexTheme.colors.onTargetFaceWhite
    },
    THREE(
            text = ResOrActual.fromActual("3"),
            arrow = Arrow(3, false)
    ) {
        @Composable override fun getBackgroundColour(): Color = CodexTheme.colors.targetFaceBlack
        @Composable override fun getContentColour(): Color = CodexTheme.colors.onTargetFaceBlack
    },
    FOUR(
            text = ResOrActual.fromActual("4"),
            arrow = Arrow(4, false)
    ) {
        @Composable override fun getBackgroundColour(): Color = CodexTheme.colors.targetFaceBlack
        @Composable override fun getContentColour(): Color = CodexTheme.colors.onTargetFaceBlack
    },
    FIVE(
            text = ResOrActual.fromActual("5"),
            arrow = Arrow(5, false)
    ) {
        @Composable override fun getBackgroundColour(): Color = CodexTheme.colors.targetFaceBlue
        @Composable override fun getContentColour(): Color = CodexTheme.colors.onTargetFaceBlue
    },
    SIX(
            text = ResOrActual.fromActual("6"),
            arrow = Arrow(6, false)
    ) {
        @Composable override fun getBackgroundColour(): Color = CodexTheme.colors.targetFaceBlue
        @Composable override fun getContentColour(): Color = CodexTheme.colors.onTargetFaceBlue
    },
    SEVEN(
            text = ResOrActual.fromActual("7"),
            arrow = Arrow(7, false)
    ) {
        @Composable override fun getBackgroundColour(): Color = CodexTheme.colors.targetFaceRed
        @Composable override fun getContentColour(): Color = CodexTheme.colors.onTargetFaceRed
    },
    EIGHT(
            text = ResOrActual.fromActual("8"),
            arrow = Arrow(8, false)
    ) {
        @Composable override fun getBackgroundColour(): Color = CodexTheme.colors.targetFaceRed
        @Composable override fun getContentColour(): Color = CodexTheme.colors.onTargetFaceRed
    },
    NINE(
            text = ResOrActual.fromActual("9"),
            arrow = Arrow(9, false)
    ) {
        @Composable override fun getBackgroundColour(): Color = CodexTheme.colors.targetFaceGold
        @Composable override fun getContentColour(): Color = CodexTheme.colors.onTargetFaceGold
    },
    TEN(
            text = ResOrActual.fromActual("10"),
            arrow = Arrow(10, false)
    ) {
        @Composable override fun getBackgroundColour(): Color = CodexTheme.colors.targetFaceGold
        @Composable override fun getContentColour(): Color = CodexTheme.colors.onTargetFaceGold
    },
    X(
            text = ResOrActual.fromRes(R.string.arrow_value_x),
            arrow = Arrow(10, true)
    ) {
        @Composable override fun getBackgroundColour(): Color = CodexTheme.colors.targetFaceGold
        @Composable override fun getContentColour(): Color = CodexTheme.colors.onTargetFaceGold
    },
}