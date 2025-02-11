package eywa.projectcodex.components.shootDetails.headToHead.scorePad

import eywa.projectcodex.R
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.components.shootDetails.headToHead.grid.SetDropdownMenuItem
import eywa.projectcodex.model.headToHead.FullHeadToHeadMatch

fun FullHeadToHeadMatch.allowNewEndsToBeAdded() = !match.isBye && (!result.isComplete || !isStandardFormat)

data class HeadToHeadScorePadState(
        val entries: List<FullHeadToHeadMatch>,
        val extras: HeadToHeadScorePadExtras = HeadToHeadScorePadExtras(),
) {
    /**
     * <matchNumber, setNumber, dropdownMenuItems>
     */
    val setDropdownMenuExpandedFor: Triple<Int, Int, List<SetDropdownMenuItem>>? =
            extras.menuOpenForSet?.let { (match, set) ->
                var dropdownItems = SetDropdownMenuItem.entries.toList()

                val allowedNewEnds = entries.find { it.match.matchNumber == match }?.allowNewEndsToBeAdded() ?: false
                if (!allowedNewEnds) {
                    dropdownItems = dropdownItems.minus(SetDropdownMenuItem.INSERT)
                }

                Triple(match, set, dropdownItems)
            }
}

data class HeadToHeadScorePadExtras(
        /**
         * <matchNumber, setNumber (ignored for [MenuAction.NEW_SET])>
         */
        val menuOpenForSet: Pair<Int, Int>? = null,
        val setMenuActionClicked: MenuAction? = null,

        val menuOpenForMatchNumber: Int? = null,
        val matchMenuActionClicked: MenuAction? = null,

        val openAddMatch: Boolean = false,
        val openEditSightersForMatch: Int? = null,
)

enum class MenuAction { INSERT, NEW_SET, EDIT, DELETE }

enum class MatchDropdownMenuItem(val title: ResOrActual<String>) {
    CONTINUE(title = ResOrActual.StringResource(R.string.head_to_head_score_pad__dropdown_continue)),
    INSERT(title = ResOrActual.StringResource(R.string.head_to_head_score_pad__dropdown_insert)),
    EDIT(title = ResOrActual.StringResource(R.string.head_to_head_score_pad__dropdown_edit)),
    DELETE(title = ResOrActual.StringResource(R.string.head_to_head_score_pad__dropdown_delete)),
}
