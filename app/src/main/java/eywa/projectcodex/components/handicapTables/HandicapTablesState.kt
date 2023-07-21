package eywa.projectcodex.components.handicapTables

import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundEnabledFilters
import eywa.projectcodex.common.utils.getDistances
import eywa.projectcodex.database.RoundFace
import eywa.projectcodex.database.rounds.FullRoundInfo

data class HandicapTablesState(
        val input: Int? = null,
        val inputHandicap: Boolean = true,
        val round: RoundInfo? = null,
        val roundFilters: SelectRoundEnabledFilters = SelectRoundEnabledFilters(),
        val subType: Int? = null,
        val use2023System: Boolean = false,
        val faces: List<RoundFace>? = null,
        val allRounds: List<FullRoundInfo>? = null,
        val isSelectRoundDialogOpen: Boolean = false,
        val isSelectSubtypeDialogOpen: Boolean = false,
        val handicaps: List<HandicapScore> = emptyList(),
        val highlightedHandicap: HandicapScore? = null,
        val isSelectFaceDialogOpen: Boolean = false,
        val isSelectFaceDialogSingleMode: Boolean = false,
        val selectFaceDialogDropdownOpenFor: Int? = null,
) {
    val subtypeDistances = round?.info?.getDistances(subType ?: 1)

    sealed class RoundInfo(val info: FullRoundInfo) {
        class Round(info: FullRoundInfo) : RoundInfo(info)
//        data class Manual(
//                val distance: Int,
//                val isMeters: Boolean,
//                val numberOfArrows: Int,
//                val faceSize: Int,
//                val faceSizeInCm: Boolean,
//        ) : RoundInfo()
    }
}

data class HandicapScore(val handicap: Int, val score: Int)
