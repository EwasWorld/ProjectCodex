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
}

data class ScorePadExtras(
        val deleteEndDialogIsShown: Boolean = false,
        val editEndClicked: Boolean = false,
        val insertEndClicked: Boolean = false,
        val inputEndClicked: Boolean = false,
        val isDropdownMenuOpen: Boolean = false,
)
