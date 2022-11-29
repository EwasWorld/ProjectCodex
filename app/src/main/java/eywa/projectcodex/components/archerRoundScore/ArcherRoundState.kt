package eywa.projectcodex.components.archerRoundScore

import eywa.projectcodex.common.archeryObjects.Arrow
import eywa.projectcodex.common.archeryObjects.GoldsType
import eywa.projectcodex.components.archerRoundScore.scorePad.infoTable.ScorePadDataNew
import eywa.projectcodex.database.archerRound.FullArcherRoundInfo

data class ArcherRoundState(
        val fullArcherRoundInfo: FullArcherRoundInfo,
        val goldsType: GoldsType,
        val inputEndSize: Int = 6,
        val scorePadEndSize: Int = 6,
        val isEditingEndNumber: Int? = null,
        val isInsertingEndNumber: Int? = null,
        val inputArrows: List<Arrow> = listOf(),
) {
    val scorePadData by lazy { ScorePadDataNew(fullArcherRoundInfo, scorePadEndSize, goldsType) }

    val isEditing by lazy { isEditingEndNumber != null }
}