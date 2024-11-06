package eywa.projectcodex.components.shootDetails.headToHeadEnd.addEnd

import eywa.projectcodex.common.sharedUi.numberField.NumberFieldState
import eywa.projectcodex.common.sharedUi.numberField.NumberValidator
import eywa.projectcodex.common.sharedUi.numberField.TypeValidator
import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.components.referenceTables.headToHead.HeadToHeadUseCase
import eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.ArrowInputsError
import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadArcherType
import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadResult
import eywa.projectcodex.components.shootDetails.headToHeadEnd.grid.HeadToHeadGridRowData
import eywa.projectcodex.components.shootDetails.headToHeadEnd.grid.HeadToHeadGridRowDataPreviewHelper
import eywa.projectcodex.components.shootDetails.headToHeadEnd.grid.HeadToHeadGridState
import eywa.projectcodex.database.RoundFace
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHeadDetail
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHeadHeat
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHeadHeatPreviewHelper
import eywa.projectcodex.model.FullHeadToHeadSet
import eywa.projectcodex.model.SightMark

sealed class HeadToHeadAddState {
    abstract val roundCommon: RoundCommon?
    abstract val effects: Effects

    data class Loading(
            override val roundCommon: RoundCommon? = null,
            override val effects: Effects = Effects(),
    ) : HeadToHeadAddState()

    data class AddHeat(
            override val roundCommon: RoundCommon? = null,
            override val effects: Effects = Effects(),
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
            val previousHeat: PreviousHeat? = null,
    ) : HeadToHeadAddState() {
        fun asHeadToHeadHeat(shootId: Int) =
                if (heat == null) null
                else DatabaseHeadToHeadHeat(
                        shootId = shootId,
                        heat = heat,
                        opponent = opponent,
                        opponentQualificationRank = opponentQualiRank.parsed,
                        isShootOffWin = false,
                        sightersCount = 0,
                        isBye = isBye,
                )

        data class PreviousHeat(
                val heat: Int,
                val result: HeadToHeadResult,
                val teamRunningTotal: Int,
                val opponentRunningTotal: Int,
        )
    }

    data class AddEnd(
            override val roundCommon: RoundCommon? = null,
            override val effects: Effects = Effects(),
            val set: FullHeadToHeadSet = FullHeadToHeadSet(
                    data = HeadToHeadGridRowDataPreviewHelper.selfAndOpponent,
                    teamSize = 1,
                    isShootOff = false,
                    isShootOffWin = false,
                    setNumber = 1,
            ),
            val selected: HeadToHeadArcherType? = set.data.map { it.type }.minByOrNull { it.ordinal },
            val teamRunningTotal: Int = 0,
            val opponentRunningTotal: Int = 2,
            val isRecurveStyle: Boolean = true,
            val heat: DatabaseHeadToHeadHeat = DatabaseHeadToHeadHeatPreviewHelper.data,
            val arrowInputsError: ArrowInputsError? = null,
            val incompleteError: Boolean = false,
            val openSighters: Boolean = false,
            val dbSet: FullHeadToHeadSet? = null,
    ) : HeadToHeadAddState() {
        init {
            if (dbSet != null) {
                check(set.setNumber == dbSet.setNumber)
            }
        }

        fun toGridState() = HeadToHeadGridState(
                enteredArrows = listOf(set),
                selected = selected,
                isSingleEditableSet = true,
        )

        fun toDbDetails(): List<DatabaseHeadToHeadDetail> =
                DatabaseHeadToHeadDetail(
                        headToHeadArrowScoreId = 0,
                        shootId = heat.shootId,
                        heat = heat.heat,
                        setNumber = set.setNumber,

                        // Dummy values
                        type = HeadToHeadArcherType.TEAM,
                        isTotal = false,
                        arrowNumber = 0,
                        score = 0,
                        isX = false,
                ).let { mainData ->
                    set.data.flatMap { rowData ->
                        val typeData = mainData.copy(
                                type = rowData.type,
                                isTotal = rowData.isTotalRow,
                        )

                        if (rowData is HeadToHeadGridRowData.Arrows) {
                            rowData.arrows.mapIndexed { index, arrow ->
                                typeData.copy(arrowNumber = index + 1, score = arrow.score, isX = arrow.isX)
                            }
                        }
                        else {
                            listOf(typeData.copy(arrowNumber = 1, score = rowData.totalScore, isX = false))
                        }
                    }
                }
    }

    data class RoundCommon(
            val distance: Int? = 70,
            val isMetric: Boolean? = true,
            val sightMark: SightMark? = null,

            val round: Round? = RoundPreviewHelper.wa70RoundData.round,
            val face: RoundFace? = RoundFace.FULL,
    )

    data class Effects(
            val openEditSightMark: Boolean = false,
            val openAllSightMarks: Boolean = false,
    )
}

