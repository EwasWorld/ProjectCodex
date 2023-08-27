package eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.arrowButton

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import eywa.projectcodex.R
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.database.RoundFace
import eywa.projectcodex.model.Arrow

interface ArrowButton {
    val text: ResOrActual<String>
    val arrow: Arrow
    val shouldShow: (RoundFace) -> Boolean
    @Composable fun getBackgroundColour(): Color
    @Composable fun getContentColour(): Color
    @Composable fun contentDescription() = stringResource(R.string.input_end__arrow_button_accessibility, text.get())
}
