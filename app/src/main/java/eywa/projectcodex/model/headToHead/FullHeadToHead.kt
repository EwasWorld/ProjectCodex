package eywa.projectcodex.model.headToHead

import eywa.projectcodex.components.referenceTables.headToHead.HeadToHeadUseCase
import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadArcherType
import eywa.projectcodex.components.shootDetails.headToHeadEnd.grid.HeadToHeadGridRowData.*
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHead
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHeadDetail
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHeadHeat
import eywa.projectcodex.model.Arrow

data class FullHeadToHead(
        val headToHead: DatabaseHeadToHead,
        val heats: List<FullHeadToHeadHeat>,
) {
    constructor(
            headToHead: DatabaseHeadToHead,
            heats: List<DatabaseHeadToHeadHeat>,
            details: List<DatabaseHeadToHeadDetail>,
            isEditable: Boolean,
    ) : this(
            headToHead = headToHead,
            heats = details.groupBy { it.matchNumber }.let { grouped ->
                heats.map { heat ->
                    val shootOffSet = HeadToHeadUseCase.shootOffSet(headToHead.teamSize)
                    FullHeadToHeadHeat(
                            heat = heat,
                            sets = (grouped[heat.matchNumber] ?: emptyList())
                                    .groupBy { it.setNumber }
                                    .map { (setNumber, details) ->
                                        FullHeadToHeadSet(
                                                setNumber = setNumber,
                                                data = details.asRowData(
                                                        endSize = HeadToHeadUseCase.endSize(
                                                                teamSize = headToHead.teamSize,
                                                                isShootOff = setNumber == shootOffSet,
                                                        ),
                                                        teamSize = headToHead.teamSize,
                                                        isEditable = isEditable,
                                                ),
                                                teamSize = headToHead.teamSize,
                                                isShootOffWin = heat.isShootOffWin,
                                                isRecurveStyle = headToHead.isRecurveStyle,
                                        )
                                    },
                            teamSize = headToHead.teamSize,
                            isRecurveStyle = headToHead.isRecurveStyle,
                            isStandardFormat = headToHead.isStandardFormat,
                    )
                }
            },
    )

    val hasStarted: Boolean
        get() = heats.any { it.hasStarted }

    val arrowsShot: Int
        get() = heats.sumOf { it.arrowsShot }

    val isComplete: Boolean
        get() = heats.all { heat ->
            heat.result.isComplete
        }

    val arrowsToIsSelf =
            getArrows(HeadToHeadArcherType.SELF)?.let { it to true }
                    ?: getArrows(HeadToHeadArcherType.TEAM)?.let { it to false }

    fun getArrows(type: HeadToHeadArcherType) =
            heats.fold<FullHeadToHeadHeat, RowArrows?>(RowArrows.Arrows(listOf())) { acc, set ->
                if (acc == null) null else set.getArrows(type)?.let { it + acc }
            }

    companion object {
        fun List<DatabaseHeadToHeadDetail>.asRowData(
                endSize: Int,
                teamSize: Int,
                isEditable: Boolean,
        ) = groupBy { it.type }
                .map { (type, group) ->
                    val expectedArrowCount = type.expectedArrowCount(endSize, teamSize)
                    require(group.distinctBy { it.isTotal }.size == 1) { "Cannot have total and arrows" }

                    if (group[0].isTotal) {
                        require(group.size == 1) { "Cannot have more than one total" }

                        val score = group[0].score
                        if (isEditable) {
                            EditableTotal(
                                    type = type,
                                    expectedArrowCount = expectedArrowCount,
                                    dbId = group[0].headToHeadArrowScoreId.takeIf { it != 0 },
                            ).let {
                                if (score == null) it else it.copy(text = it.text.copy(score.toString()))
                            }
                        }
                        else {
                            Total(
                                    type = type,
                                    expectedArrowCount = expectedArrowCount,
                                    total = score,
                                    dbId = group[0].headToHeadArrowScoreId.takeIf { it != 0 },
                            )
                        }
                    }
                    else {
                        Arrows(
                                type = type,
                                expectedArrowCount = expectedArrowCount,
                                arrows = group.mapNotNull { dbArrow -> dbArrow.score?.let { Arrow(it, dbArrow.isX) } },
                                dbIds = group.map { it.headToHeadArrowScoreId }
                                        .takeWhile { it != 0 }.takeIf { it.isNotEmpty() },
                        )
                    }
                }
    }
}
