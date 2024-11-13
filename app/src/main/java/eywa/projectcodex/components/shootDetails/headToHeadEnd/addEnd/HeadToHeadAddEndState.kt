package eywa.projectcodex.components.shootDetails.headToHeadEnd.addEnd

import eywa.projectcodex.common.sharedUi.previewHelpers.RoundPreviewHelper
import eywa.projectcodex.components.shootDetails.commonUi.arrowInputs.ArrowInputsError
import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadArcherType
import eywa.projectcodex.components.shootDetails.headToHeadEnd.grid.HeadToHeadGridRowDataPreviewHelper
import eywa.projectcodex.components.shootDetails.headToHeadEnd.grid.HeadToHeadGridState
import eywa.projectcodex.database.RoundFace
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHeadDetail
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHeadHeat
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHeadHeatPreviewHelper
import eywa.projectcodex.model.SightMark
import eywa.projectcodex.model.headToHead.FullHeadToHeadSet

data class HeadToHeadAddEndState(
        val headToHeadRoundInfo: HeadToHeadRoundInfo? = null,
        val extras: HeadToHeadAddEndExtras = HeadToHeadAddEndExtras(),
        val teamRunningTotal: Int = 0,
        val opponentRunningTotal: Int = 2,
        val isRecurveStyle: Boolean = true,
        val heat: DatabaseHeadToHeadHeat = DatabaseHeadToHeadHeatPreviewHelper.data,
        val dbSet: FullHeadToHeadSet? = null,
) {
    init {
        if (dbSet != null) {
            check(extras.set.setNumber == dbSet.setNumber)
        }
    }

    fun toGridState() = HeadToHeadGridState(
            enteredArrows = listOf(extras.set),
            selected = extras.selected,
            isSingleEditableSet = true,
            runningTotals = null,
            finalResult = null,
    )

    fun toDbDetails(): List<DatabaseHeadToHeadDetail> = extras.set.asDatabaseDetails(heat.shootId, heat.heat)
}

data class HeadToHeadRoundInfo(
        val distance: Int? = 70,
        val isMetric: Boolean? = true,
        val sightMark: SightMark? = null,

        val round: Round? = RoundPreviewHelper.wa70RoundData.round,
        val face: RoundFace? = RoundFace.FULL,
)

data class HeadToHeadAddEndExtras(
        val openAddHeatScreen: Boolean = false,
        val openEditSightMark: Boolean = false,
        val openAllSightMarks: Boolean = false,
        val set: FullHeadToHeadSet = FullHeadToHeadSet(
                data = HeadToHeadGridRowDataPreviewHelper.selfAndOpponent,
                teamSize = 1,
                isShootOff = false,
                isShootOffWin = false,
                setNumber = 1,
                isRecurveStyle = false,
        ),
        val selected: HeadToHeadArcherType? = set.data.map { it.type }.minByOrNull { it.ordinal },
        val arrowInputsError: Set<ArrowInputsError> = setOf(),
        val incompleteError: Boolean = false,
        val openSighters: Boolean = false,
) {
    val selectedData
        get() = set.data.find { it.type == selected }
}
