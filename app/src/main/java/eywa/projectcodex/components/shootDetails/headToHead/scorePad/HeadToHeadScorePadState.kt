package eywa.projectcodex.components.shootDetails.headToHead.scorePad

import eywa.projectcodex.components.shootDetails.headToHead.grid.DropdownMenuItem
import eywa.projectcodex.model.headToHead.FullHeadToHeadMatch

fun FullHeadToHeadMatch.allowNewEndsToBeAdded() = !match.isBye && (!result.isComplete || !isStandardFormat)

data class HeadToHeadScorePadState(
        val entries: List<FullHeadToHeadMatch>,
        val extras: HeadToHeadScorePadExtras = HeadToHeadScorePadExtras(),
) {
    /**
     * <matchNumber, setNumber, dropdownMenuItems>
     */
    val dropdownMenuExpandedFor: Triple<Int, Int, List<DropdownMenuItem>>? =
            extras.menuOpenForSet?.let { (match, set) ->
                var dropdownItems = DropdownMenuItem.entries.toList()

                val allowedNewEnds = entries.find { it.match.matchNumber == match }?.allowNewEndsToBeAdded() ?: false
                if (!allowedNewEnds) {
                    dropdownItems = dropdownItems.minus(DropdownMenuItem.INSERT)
                }

                Triple(match, set, dropdownItems)
            }
}

data class HeadToHeadScorePadExtras(
        /**
         * <matchNumber, setNumber (ignored for [MenuAction.NEW_SET])>
         */
        val menuOpenForSet: Pair<Int, Int>? = null,
        val openAddMatch: Boolean = false,
        val openEditSightersForMatch: Int? = null,
        val openEditMatchInfo: Int? = null,
        val menuActionClicked: MenuAction? = null,
)

enum class MenuAction { INSERT, NEW_SET, EDIT, DELETE }
