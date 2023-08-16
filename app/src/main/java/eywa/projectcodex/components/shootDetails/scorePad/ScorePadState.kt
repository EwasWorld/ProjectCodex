package eywa.projectcodex.components.shootDetails.scorePad

import eywa.projectcodex.components.shootDetails.ShootDetailsState

// TODO Half way total
class ScorePadState(
        main: ShootDetailsState,
        extras: ScorePadExtras,
) {
    val shootId = main.fullShootInfo!!.id
    val arrows = main.fullShootInfo!!.arrows
    val isRoundFull = main.fullShootInfo!!.isRoundComplete
    val deleteEndDialogIsShown = extras.deleteEndDialogIsShown
    val dropdownMenuOpenForEndNumber = main.scorePadSelectedEnd
    val endSize = main.scorePadEndSize
    val editEndClicked = extras.editEndClicked
    val insertEndClicked = extras.insertEndClicked
    val scorePadData = main.scorePadData!!
    val firstArrowNumberInSelectedEnd = main.firstArrowNumberInSelectedEnd
    val selectedEndSize = main.selectedEndSize
    val isDropdownMenuOpen = extras.isDropdownMenuOpen

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ScorePadState) return false

        if (shootId != other.shootId) return false
        if (arrows != other.arrows) return false
        if (isRoundFull != other.isRoundFull) return false
        if (deleteEndDialogIsShown != other.deleteEndDialogIsShown) return false
        if (dropdownMenuOpenForEndNumber != other.dropdownMenuOpenForEndNumber) return false
        if (endSize != other.endSize) return false
        if (editEndClicked != other.editEndClicked) return false
        if (insertEndClicked != other.insertEndClicked) return false
        if (scorePadData != other.scorePadData) return false
        if (firstArrowNumberInSelectedEnd != other.firstArrowNumberInSelectedEnd) return false
        if (selectedEndSize != other.selectedEndSize) return false
        if (isDropdownMenuOpen != other.isDropdownMenuOpen) return false

        return true
    }

    override fun hashCode(): Int {
        var result = shootId
        result = 31 * result + (arrows?.hashCode() ?: 0)
        result = 31 * result + isRoundFull.hashCode()
        result = 31 * result + deleteEndDialogIsShown.hashCode()
        result = 31 * result + (dropdownMenuOpenForEndNumber ?: 0)
        result = 31 * result + endSize
        result = 31 * result + editEndClicked.hashCode()
        result = 31 * result + insertEndClicked.hashCode()
        result = 31 * result + scorePadData.hashCode()
        result = 31 * result + (firstArrowNumberInSelectedEnd ?: 0)
        result = 31 * result + (selectedEndSize ?: 0)
        result = 31 * result + isDropdownMenuOpen.hashCode()
        return result
    }
}

data class ScorePadExtras(
        val deleteEndDialogIsShown: Boolean = false,
        val editEndClicked: Boolean = false,
        val insertEndClicked: Boolean = false,
        val inputEndClicked: Boolean = false,
        val isDropdownMenuOpen: Boolean = false,
)
