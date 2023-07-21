package eywa.projectcodex.components.archerRoundScore.arrowInputs.arrowButton

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import eywa.projectcodex.common.archeryObjects.Arrow
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.database.RoundFace

interface ArrowButton {
    val text: ResOrActual<String>
    val arrow: Arrow
    val shouldShow: (RoundFace) -> Boolean
    @Composable fun getBackgroundColour(): Color
    @Composable fun getContentColour(): Color
}
