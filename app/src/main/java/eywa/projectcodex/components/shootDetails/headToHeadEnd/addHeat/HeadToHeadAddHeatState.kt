package eywa.projectcodex.components.shootDetails.headToHeadEnd.addHeat

import eywa.projectcodex.common.sharedUi.numberField.NumberFieldState
import eywa.projectcodex.common.sharedUi.numberField.NumberValidator
import eywa.projectcodex.common.sharedUi.numberField.NumberValidatorGroup
import eywa.projectcodex.common.sharedUi.numberField.TypeValidator
import eywa.projectcodex.components.referenceTables.headToHead.HeadToHeadUseCase
import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadResult
import eywa.projectcodex.components.shootDetails.headToHeadEnd.addEnd.HeadToHeadRoundInfo
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHeadHeat

data class HeadToHeadAddHeatState(
        val roundInfo: HeadToHeadRoundInfo? = null,
        val previousHeat: PreviousHeat? = null,
        val editing: DatabaseHeadToHeadHeat? = null,

        val extras: HeadToHeadAddHeatExtras = HeadToHeadAddHeatExtras(),
        val matchNumber: Int = editing?.matchNumber ?: previousHeat?.matchNumber?.plus(1) ?: 1
) {

    fun asHeadToHeadHeat(shootId: Int) =
            DatabaseHeadToHeadHeat(
                    shootId = shootId,
                    matchNumber = matchNumber,
                    heat = extras.heat,
                    opponent = extras.opponent.takeIf { it.isNotBlank() },
                    opponentQualificationRank = extras.opponentQualiRank.parsed,
                    isShootOffWin = false,
                    sightersCount = 0,
                    maxPossibleRank = extras.maxPossibleRank.parsed,
                    isBye = extras.isBye,
            )

    data class PreviousHeat(
            val matchNumber: Int,
            val heat: Int?,
            val result: HeadToHeadResult,
            val isBye: Boolean,
            val runningTotal: Pair<Int, Int>?,
    )
}

data class HeadToHeadAddHeatExtras(
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
        ),
        val maxPossibleRank: NumberFieldState<Int> = NumberFieldState(
                text = "1",
                validators = NumberValidatorGroup(
                        TypeValidator.IntValidator,
                        NumberValidator.InRange(1..HeadToHeadUseCase.MAX_QUALI_RANK),
                ),
        ),
        val isBye: Boolean = false,
)
