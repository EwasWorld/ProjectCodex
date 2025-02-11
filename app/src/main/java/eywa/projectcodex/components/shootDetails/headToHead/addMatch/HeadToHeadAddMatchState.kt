package eywa.projectcodex.components.shootDetails.headToHead.addMatch

import eywa.projectcodex.common.sharedUi.numberField.NumberFieldState
import eywa.projectcodex.common.sharedUi.numberField.NumberValidator
import eywa.projectcodex.common.sharedUi.numberField.NumberValidatorGroup
import eywa.projectcodex.common.sharedUi.numberField.TypeValidator
import eywa.projectcodex.components.referenceTables.headToHead.HeadToHeadUseCase
import eywa.projectcodex.components.shootDetails.headToHead.HeadToHeadResult
import eywa.projectcodex.components.shootDetails.headToHead.addEnd.HeadToHeadRoundInfo
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHeadMatch

data class HeadToHeadAddMatchState(
        val roundInfo: HeadToHeadRoundInfo? = null,
        val previousMatch: PreviousMatch? = null,
        val editing: DatabaseHeadToHeadMatch? = null,
        /**
         * Warn the user that changing a match with sets to a bye will delete the sets
         */
        val editingMatchWithSetsToBye: Boolean = false,
        val extras: HeadToHeadAddMatchExtras = HeadToHeadAddMatchExtras(matchNumber = 1),
        val isInserting: Boolean = false,
) {

    fun asHeadToHeadMatch(shootId: Int) =
            DatabaseHeadToHeadMatch(
                    shootId = shootId,
                    matchNumber = extras.matchNumber,
                    heat = extras.heat,
                    opponent = extras.opponent.takeIf { it.isNotBlank() && !extras.isBye },
                    opponentQualificationRank = extras.opponentQualiRank.parsed.takeIf { !extras.isBye },
                    isShootOffWin = false,
                    sightersCount = 0,
                    maxPossibleRank = extras.maxPossibleRank.parsed,
                    isBye = extras.isBye,
            )

    data class PreviousMatch(
            val matchNumber: Int,
            val heat: Int?,
            val result: HeadToHeadResult,
            val isBye: Boolean,
            val runningTotal: Pair<Int, Int>?,
    )
}

data class HeadToHeadAddMatchExtras(
        val openAddEndScreenForMatch: Int? = null,
        val pressBack: Boolean = false,
        val openEditSightMark: Boolean = false,
        val openAllSightMarks: Boolean = false,
        /**
         * 0 is final, 1 is semi, etc.
         */
        val heat: Int? = null,
        val showSelectHeatDialog: Boolean = false,
        val opponent: String = "",
        val opponentQualiRank: NumberFieldState<Int> = NumberFieldState(
                TypeValidator.IntValidator,
                NumberValidator.InRange(1..HeadToHeadUseCase.MAX_QUALI_RANK),
                NumberValidator.NotRequired,
        ),
        val maxPossibleRank: NumberFieldState<Int> = NumberFieldState(
                text = "1",
                validators = NumberValidatorGroup(
                        TypeValidator.IntValidator,
                        NumberValidator.InRange(1..HeadToHeadUseCase.MAX_QUALI_RANK),
                        NumberValidator.NotRequired,
                ),
        ),
        val isBye: Boolean = false,
        val matchNumber: Int = 1,
)
