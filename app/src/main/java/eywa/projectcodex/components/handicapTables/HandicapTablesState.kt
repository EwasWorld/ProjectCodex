package eywa.projectcodex.components.handicapTables

import eywa.projectcodex.common.sharedUi.selectRoundDialog.SelectRoundDialogState
import eywa.projectcodex.common.sharedUi.selectRoundFaceDialog.SelectRoundFaceDialogState

data class HandicapTablesState(
        val input: Int? = null,
        val inputHandicap: Boolean = true,
        val use2023System: Boolean = false,
        val handicaps: List<HandicapScore> = emptyList(),
        val highlightedHandicap: HandicapScore? = null,
        val selectRoundDialogState: SelectRoundDialogState = SelectRoundDialogState(),
        val selectFaceDialogState: SelectRoundFaceDialogState = SelectRoundFaceDialogState(
                round = selectRoundDialogState.selectedRound?.round,
                distances = selectRoundDialogState.roundSubTypeDistances?.map { it.distance },
        ),
)

data class HandicapScore(val handicap: Int, val score: Int)
