package eywa.projectcodex.components.archerRoundScore.inputEnd.arrowButton

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import eywa.projectcodex.R
import eywa.projectcodex.common.archeryObjects.Arrow
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.utils.ResOrActual

enum class WorcesterTargetScoreButton(
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
    ),
    TWO(
            text = ResOrActual.fromActual("2"),
            arrow = Arrow(2, false)
    ),
    THREE(
            text = ResOrActual.fromActual("3"),
            arrow = Arrow(3, false)
    ),
    FOUR(
            text = ResOrActual.fromActual("4"),
            arrow = Arrow(4, false)
    ),
    FIVE(
            text = ResOrActual.fromActual("5"),
            arrow = Arrow(5, false)
    ) {
        @Composable override fun getBackgroundColour(): Color = CodexTheme.colors.targetFaceWhite
        @Composable override fun getContentColour(): Color = CodexTheme.colors.onTargetFaceWhite
    },
    ;

    @Composable override fun getBackgroundColour(): Color = CodexTheme.colors.targetFaceBlack
    @Composable override fun getContentColour(): Color = CodexTheme.colors.onTargetFaceBlack
}