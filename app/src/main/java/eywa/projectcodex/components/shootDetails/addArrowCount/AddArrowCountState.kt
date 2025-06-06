package eywa.projectcodex.components.shootDetails.addArrowCount

import eywa.projectcodex.common.sharedUi.numberField.NumberValidator
import eywa.projectcodex.common.sharedUi.numberField.NumberValidatorGroup
import eywa.projectcodex.common.sharedUi.numberField.PartialNumberFieldState
import eywa.projectcodex.common.sharedUi.numberField.TypeValidator
import eywa.projectcodex.components.shootDetails.DEFAULT_END_SIZE
import eywa.projectcodex.components.shootDetails.ShootDetailsState

class AddArrowCountState(
        main: ShootDetailsState,
        extras: AddArrowCountExtras,
        val isEditingSighters: Boolean,
) {
    val fullShootInfo = main.fullShootInfo!!
    val editShootInfoClicked = extras.editShootInfoClicked

    /**
     * Cannot go below 0
     */
    private val minArrows = if (isEditingSighters) 0 else (-fullShootInfo.arrowsShot)

    /**
     * Cannot go above the number of arrows in the round or 3000 if there is no round
     */
    private val maxArrows =
            if (isEditingSighters) MAX_ARROW_COUNT
            else (fullShootInfo.remainingArrows ?: (MAX_ARROW_COUNT - fullShootInfo.arrowsShot))

    /**
     * The amount displayed on the counter. The amount that will be added to [fullShootInfo] when 'Add' is pressed.
     * Limited so that [fullShootInfo]'s arrow count cannot go out of the range of [minArrows]..[maxArrows]
     */
    val endSize = extras.endSize.asNumberFieldState(
            NumberValidatorGroup(
                    TypeValidator.IntValidator,
                    NumberValidator.InRange(minArrows..maxArrows),
            )
    )

    val heatId = extras.h2hMatchNumber
    val sightMark = main.sightMark
    val openFullSightMarks = extras.openFullSightMarks
    val openEditSightMark = extras.openEditSightMark
    val openEditSighters = extras.openEditSighters

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AddArrowCountState) return false

        if (fullShootInfo != other.fullShootInfo) return false
        if (editShootInfoClicked != other.editShootInfoClicked) return false
        if (endSize != other.endSize) return false
        if (heatId != other.heatId) return false
        if (sightMark != other.sightMark) return false
        if (openFullSightMarks != other.openFullSightMarks) return false
        if (openEditSightMark != other.openEditSightMark) return false
        if (openEditSighters != other.openEditSighters) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fullShootInfo.hashCode()
        result = 31 * result + editShootInfoClicked.hashCode()
        result = 31 * result + endSize.hashCode()
        result = 31 * result + (heatId?.hashCode() ?: 0)
        result = 31 * result + (sightMark?.hashCode() ?: 0)
        result = 31 * result + openFullSightMarks.hashCode()
        result = 31 * result + openEditSightMark.hashCode()
        result = 31 * result + openEditSighters.hashCode()
        return result
    }

    companion object {
        private const val MAX_ARROW_COUNT = 3000
    }
}

data class AddArrowCountExtras(
        val endSize: PartialNumberFieldState = PartialNumberFieldState(DEFAULT_END_SIZE.toString()),
        val h2hMatchNumber: Int? = null,
        val editShootInfoClicked: Boolean = false,
        val openFullSightMarks: Boolean = false,
        val openEditSightMark: Boolean = false,
        val openEditSighters: Boolean = false,
)
