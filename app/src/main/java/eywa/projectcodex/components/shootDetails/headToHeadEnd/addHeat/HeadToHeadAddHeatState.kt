package eywa.projectcodex.components.shootDetails.headToHeadEnd.addHeat

import eywa.projectcodex.common.sharedUi.numberField.NumberFieldState
import eywa.projectcodex.common.sharedUi.numberField.NumberValidator
import eywa.projectcodex.common.sharedUi.numberField.TypeValidator
import eywa.projectcodex.components.referenceTables.headToHead.HeadToHeadUseCase
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHeadHeat

data class HeadToHeadAddHeatState(
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
        val shouldCloseScreen: Boolean = false,
) {
    fun asHeatToHeatHeat(shootId: Int) =
            if (heat == null) null
            else DatabaseHeadToHeadHeat(
                    shootId = shootId,
                    heat = heat,
                    opponent = opponent,
                    opponentQualificationRank = opponentQualiRank.parsed,
                    hasShootOff = false,
                    isShootOffWin = false,
                    sightersCount = 0,
                    isBye = isBye,
            )
}
