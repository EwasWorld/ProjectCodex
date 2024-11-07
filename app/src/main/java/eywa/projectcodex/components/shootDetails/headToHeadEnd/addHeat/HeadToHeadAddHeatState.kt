package eywa.projectcodex.components.shootDetails.headToHeadEnd.addHeat

import eywa.projectcodex.common.sharedUi.numberField.NumberFieldState
import eywa.projectcodex.common.sharedUi.numberField.NumberValidator
import eywa.projectcodex.common.sharedUi.numberField.TypeValidator
import eywa.projectcodex.components.referenceTables.headToHead.HeadToHeadUseCase
import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadResult
import eywa.projectcodex.components.shootDetails.headToHeadEnd.addEnd.HeadToHeadRoundInfo
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHeadHeat

data class HeadToHeadAddHeatState(
        val headToHeadRoundInfo: HeadToHeadRoundInfo? = null,
        val previousHeat: PreviousHeat? = null,

        val extras: HeadToHeadAddHeatExtras = HeadToHeadAddHeatExtras(),
) {
    fun asHeadToHeadHeat(shootId: Int) =
            if (extras.heat == null) null
            else DatabaseHeadToHeadHeat(
                    shootId = shootId,
                    heat = extras.heat,
                    opponent = extras.opponent,
                    opponentQualificationRank = extras.opponentQualiRank.parsed,
                    isShootOffWin = false,
                    sightersCount = 0,
                    isBye = extras.isBye,
            )

    data class PreviousHeat(
            val heat: Int,
            val result: HeadToHeadResult,
            val teamRunningTotal: Int,
            val opponentRunningTotal: Int,
    )
}

data class HeadToHeadAddHeatExtras(
        val openAddEndScreen: Boolean = false,
        val openEditSightMark: Boolean = false,
        val openAllSightMarks: Boolean = false,
        /**
         * 0 is final, 1 is semi, etc.
         */
        val heat: Int? = null,
        val showHeatRequiredError: Boolean = false,
        val showSelectHeatDialog: Boolean = false,
        val opponent: String = "",
        val opponentQualiRank: NumberFieldState<Int> = NumberFieldState(
                TypeValidator.IntValidator,
                NumberValidator.InRange(1..HeadToHeadUseCase.MAX_QUALI_RANK),
        ),
        val isBye: Boolean = false,
)
