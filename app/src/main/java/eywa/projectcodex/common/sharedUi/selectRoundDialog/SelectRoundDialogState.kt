package eywa.projectcodex.common.sharedUi.selectRoundDialog

import eywa.projectcodex.database.rounds.FullRoundInfo
import eywa.projectcodex.database.rounds.RoundSubType
import eywa.projectcodex.database.rounds.getDistances

data class SelectRoundDialogState(
        val isRoundDialogOpen: Boolean = false,
        val isSubtypeDialogOpen: Boolean = false,
        val selectedRoundId: Int? = null,
        val selectedSubTypeId: Int? = null,
        val allRounds: List<FullRoundInfo>? = null,
        val filters: SelectRoundEnabledFilters = SelectRoundEnabledFilters(),
) {
    init {
        check(!(isRoundDialogOpen && isSubtypeDialogOpen)) { "Both dialogs open at once" }
        check(!isRoundDialogOpen || !allRounds.isNullOrEmpty()) { "Rounds dialog open but none to select" }
        check(
                !isSubtypeDialogOpen || selectedRound?.roundSubTypes.orEmpty().size > 1
        ) { "Not enough subtypes for a dialog" }
    }

    val selectedRound
        get() = allRounds?.find { it.round.roundId == selectedRoundId }

    val selectedSubType
        get() = selectedRound?.roundSubTypes?.takeIf { it.size > 1 }?.find { it.subTypeId == (selectedSubTypeId ?: 1) }

    val roundSubTypeDistances
        get() = selectedRound?.getDistances(selectedSubTypeId)

    fun getFurthestDistance(subType: RoundSubType?) =
            selectedRound
                    ?.getDistances(subType?.subTypeId)
                    ?.maxByOrNull { it.distance }

    /**
     * The subtype with the furthest max distance
     */
    val furthestSubType
        get() = selectedRound?.roundSubTypes?.maxByOrNull { getFurthestDistance(it)!!.distance }

    val displayName
        get() = selectedSubType?.name ?: selectedRound?.round?.displayName
}
