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
    val fullShootInfo = main.fullShootInfo!!
    override val round = main.fullShootInfo!!.round
    override val face = main.fullShootInfo!!.currentFace
    override val endSize = main.selectedEndSize!!
    val endNumber = main.scorePadSelectedEnd!!
    val errors = extras.errors
    val firstArrowNumber = main.firstArrowNumberInSelectedEnd!!
    val closeScreen: Boolean = extras.closeScreen

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is InsertEndState) return false

        if (enteredArrows != other.enteredArrows) return false
        if (fullShootInfo != other.fullShootInfo) return false
        if (round != other.round) return false
        if (face != other.face) return false
        if (endSize != other.endSize) return false
        if (endNumber != other.endNumber) return false
        if (errors != other.errors) return false
        if (firstArrowNumber != other.firstArrowNumber) return false
        if (closeScreen != other.closeScreen) return false

        return true
    }

    override fun hashCode(): Int {
        var result = enteredArrows.hashCode()
        result = 31 * result + fullShootInfo.hashCode()
        result = 31 * result + (round?.hashCode() ?: 0)
        result = 31 * result + (face?.hashCode() ?: 0)
        result = 31 * result + endSize
        result = 31 * result + endNumber
        result = 31 * result + errors.hashCode()
        result = 31 * result + firstArrowNumber
        result = 31 * result + closeScreen.hashCode()
        return result
    }
}

data class InsertEndExtras(
        val enteredArrows: List<Arrow> = emptyList(),
        val errors: Set<ArrowInputsError> = emptySet(),
        val closeScreen: Boolean = false,
)
