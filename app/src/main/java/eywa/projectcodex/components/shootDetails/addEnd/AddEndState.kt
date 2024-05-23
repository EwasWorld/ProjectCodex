package eywa.projectcodex.components.shootDetails.addEnd

import eywa.projectcodex.components.shootDetails.ShootDetailsState
import eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.ArrowInputsError
import eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.ArrowInputsState
import kotlin.math.min

class AddEndState(
        main: ShootDetailsState,
        extras: AddEndExtras,
) : ArrowInputsState {
    override val enteredArrows = main.addEndArrows
    val fullShootInfo = main.fullShootInfo!!
    override val round = main.fullShootInfo!!.round
    override val face = main.fullShootInfo!!.currentFace
    override val endSize = min(
            main.addEndSize,
            main.fullShootInfo!!.remainingArrowsAtDistances?.first()?.first ?: main.addEndSize,
    )
    val errors = extras.errors
    val roundCompleted = extras.roundCompleted
    val isRoundFull = main.fullShootInfo!!.isRoundComplete
    val sightMark = main.sightMark
    val openFullSightMarks = extras.openFullSightMarks
    val openEditSightMark = extras.openEditSightMark

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AddEndState) return false

        if (enteredArrows != other.enteredArrows) return false
        if (fullShootInfo != other.fullShootInfo) return false
        if (round != other.round) return false
        if (face != other.face) return false
        if (endSize != other.endSize) return false
        if (errors != other.errors) return false
        if (roundCompleted != other.roundCompleted) return false
        if (isRoundFull != other.isRoundFull) return false
        if (sightMark != other.sightMark) return false
        if (openFullSightMarks != other.openFullSightMarks) return false
        if (openEditSightMark != other.openEditSightMark) return false

        return true
    }

    override fun hashCode(): Int {
        var result = enteredArrows.hashCode()
        result = 31 * result + fullShootInfo.hashCode()
        result = 31 * result + (round?.hashCode() ?: 0)
        result = 31 * result + (face?.hashCode() ?: 0)
        result = 31 * result + endSize
        result = 31 * result + errors.hashCode()
        result = 31 * result + roundCompleted.hashCode()
        result = 31 * result + isRoundFull.hashCode()
        result = 31 * result + (sightMark?.hashCode() ?: 0)
        result = 31 * result + openFullSightMarks.hashCode()
        result = 31 * result + openEditSightMark.hashCode()
        return result
    }
}

data class AddEndExtras(
        val errors: Set<ArrowInputsError> = emptySet(),
        val roundCompleted: Boolean = false,
        val openFullSightMarks: Boolean = false,
        val openEditSightMark: Boolean = false,
)
