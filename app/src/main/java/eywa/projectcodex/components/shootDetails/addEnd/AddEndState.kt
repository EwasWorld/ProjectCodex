package eywa.projectcodex.components.shootDetails.addEnd

import eywa.projectcodex.components.shootDetails.ShootDetailsState
import eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.ArrowInputsError
import eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.ArrowInputsState

class AddEndState(
        main: ShootDetailsState,
        extras: AddEndExtras,
) : ArrowInputsState {
    override val enteredArrows = main.addEndArrows
    override val fullShootInfo = main.fullShootInfo!!
    override val endSize = main.addEndSize
    val errors = extras.errors
    val roundCompleted = extras.roundCompleted
    val isRoundFull = main.fullShootInfo!!.isRoundComplete
}

data class AddEndExtras(
        val errors: Set<ArrowInputsError> = emptySet(),
        val roundCompleted: Boolean = false,
)
