package eywa.projectcodex.components.shootDetails.insertEnd

import eywa.projectcodex.components.shootDetails.ShootDetailsState
import eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.ArrowInputsError
import eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.ArrowInputsState
import eywa.projectcodex.model.Arrow

class InsertEndState(
        main: ShootDetailsState,
        extras: InsertEndExtras,
) : ArrowInputsState {
    override val enteredArrows = extras.enteredArrows
    override val fullShootInfo = main.fullShootInfo!!
    override val endSize = main.selectedEndSize!!
    val endNumber = main.scorePadSelectedEnd!!
    val errors = extras.errors
    val firstArrowNumber = main.firstArrowNumberInSelectedEnd!!
    val closeScreen: Boolean = extras.closeScreen
}

data class InsertEndExtras(
        val enteredArrows: List<Arrow> = emptyList(),
        val errors: Set<ArrowInputsError> = emptySet(),
        val closeScreen: Boolean = false,
)
