package eywa.projectcodex.components.shootDetails.editEnd

import eywa.projectcodex.components.archerRoundScore.ArcherRoundError
import eywa.projectcodex.components.shootDetails.ShootDetailsState
import eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.ArrowInputsState
import eywa.projectcodex.model.Arrow

class EditEndState(
        main: ShootDetailsState,
        extras: EditEndExtras,
) : ArrowInputsState {
    override val enteredArrows = extras.enteredArrows
    override val fullShootInfo = main.fullShootInfo!!
    override val endSize = main.selectedEndSize!!
    val endNumber = main.scorePadSelectedEnd!!
    val errors = extras.errors
    val firstArrowNumber = main.firstArrowNumberInSelectedEnd!!
    val originalEnd = main.fullShootInfo!!.arrows!!
            .sortedBy { it.arrowNumber }
            .dropWhile { it.arrowNumber != firstArrowNumber }
            .take(endSize)
    val closeScreen: Boolean = extras.closeScreen

    init {
        check(originalEnd.isNotEmpty()) { "Original end cannot be empty" }
    }
}

data class EditEndExtras(
        val enteredArrows: List<Arrow> = emptyList(),
        val errors: Set<ArcherRoundError> = emptySet(),
        val closeScreen: Boolean = false,
)
