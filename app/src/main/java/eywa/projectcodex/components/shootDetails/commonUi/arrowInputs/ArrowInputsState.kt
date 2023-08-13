package eywa.projectcodex.components.shootDetails.commonUi.arrowInputs

import eywa.projectcodex.model.Arrow
import eywa.projectcodex.model.FullShootInfo

interface ArrowInputsState {
    val enteredArrows: List<Arrow>
    val fullShootInfo: FullShootInfo
    val endSize: Int
}
