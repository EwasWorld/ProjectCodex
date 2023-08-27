package eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.arrowButton

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import eywa.projectcodex.R
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.database.RoundFace
import eywa.projectcodex.model.Arrow

enum class WorcesterTargetScoreButton(
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
    ),
    TWO(
            text = ResOrActual.Actual("2"),
            arrow = Arrow(2, false),
            shouldShow = { it == RoundFace.FULL },
    ),
    THREE(
            text = ResOrActual.Actual("3"),
            arrow = Arrow(3, false),
            shouldShow = { it == RoundFace.FULL },
    ),
    FOUR(
            text = ResOrActual.Actual("4"),
            arrow = Arrow(4, false),
    ),
    FIVE(
            text = ResOrActual.Actual("5"),
            arrow = Arrow(5, false),
    ) {
        @Composable override fun getBackgroundColour(): Color = CodexTheme.colors.targetFaceWhite
        @Composable override fun getContentColour(): Color = CodexTheme.colors.onTargetFaceWhite
    },
    ;

    @Composable override fun getBackgroundColour(): Color = CodexTheme.colors.targetFaceBlack
    @Composable override fun getContentColour(): Color = CodexTheme.colors.onTargetFaceBlack
}
