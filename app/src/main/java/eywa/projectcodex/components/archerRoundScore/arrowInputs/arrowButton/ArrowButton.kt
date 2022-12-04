package eywa.projectcodex.components.archerRoundScore.arrowInputs.arrowButton

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import eywa.projectcodex.common.archeryObjects.Arrow
import eywa.projectcodex.common.utils.ResOrActual

interface ArrowButton {
    val text: ResOrActual<String>
    val arrow: Arrow
    @Composable fun getBackgroundColour(): Color
    @Composable fun getContentColour(): Color
}