package eywa.projectcodex.model.headToHead

import eywa.projectcodex.components.referenceTables.headToHead.HeadToHeadUseCase
import eywa.projectcodex.components.shootDetails.headToHead.HeadToHeadArcherType
import eywa.projectcodex.components.shootDetails.headToHead.grid.HeadToHeadGridRowData.*
import eywa.projectcodex.database.shootData.headToHead.DatabaseFullHeadToHead
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHead
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHeadDetail
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHeadMatch
import eywa.projectcodex.model.Arrow

data class FullHeadToHead(
        val headToHead: DatabaseHeadToHead,
        val matches: List<FullHeadToHeadMatch>,
) {
    constructor(
            headToHead: DatabaseHeadToHead,
            matches: List<DatabaseHeadToHeadMatch>,
            details: List<DatabaseHeadToHeadDetail>,
            isEditable: Boolean,
    ) : this(
            headToHead = headToHead,
            matches = details.groupBy { it.matchNumber }.let { grouped ->
                val endSize = headToHead.endSize
                        ?: HeadToHeadUseCase.endSize(headToHead.teamSize, false)
                matches.map { match ->
                    FullHeadToHeadMatch(
                            match = match,
                            sets = (grouped[match.matchNumber] ?: emptyList())
                                    .groupBy { it.setNumber }
                                    .map { (setNumber, details) ->
                                        val setEndSize =
                                                if (details.any { it.type == HeadToHeadArcherType.SHOOT_OFF }) 1
                                                else endSize
                                        FullHeadToHeadSet(
                                                setNumber = setNumber,
                                                data = details.asRowData(
                                                        endSize = setEndSize,
                                                        teamSize = headToHead.teamSize,
                                                        isEditable = isEditable,
                                                ),
                                                teamSize = headToHead.teamSize,
                                                isSetPoints = headToHead.isSetPoints,
                                                endSize = setEndSize
                                        )
                                    }
                                    .sortedBy { it.setNumber },
                            teamSize = headToHead.teamSize,
                            isSetPoints = headToHead.isSetPoints,
                            isStandardFormat = headToHead.isStandardFormat,
                    )
                }
            },
    )

    constructor(
            dbH2h: DatabaseFullHeadToHead,
            isEditable: Boolean
    ) : this(
            headToHead = dbH2h.headToHead,
            matches = dbH2h.matches.orEmpty(),
            details = dbH2h.details.orEmpty(),
            isEditable = isEditable,
    )

    init {
        check(
                matches.all {
                    it.isSetPoints == headToHead.isSetPoints
                            && it.isStandardFormat == (headToHead.endSize == null)
                            && it.teamSize == headToHead.teamSize
                },
        )
    }

    val hasStarted: Boolean
        get() = matches.any { it.hasStarted }

    val arrowsShot: Int
        get() = matches.sumOf { it.arrowsShot }

    val isComplete: Boolean
        get() = matches.all { it.result.isComplete }

    val arrowsToIsSelf =
            getArrows(HeadToHeadArcherType.SELF)?.let { it to true }
                    ?: getArrows(HeadToHeadArcherType.TEAM)?.let { it to false }

    fun getArrows(type: HeadToHeadArcherType) =
            matches.fold<FullHeadToHeadMatch, RowArrows?>(null) { acc, match ->
                match.getArrows(type)?.let { it + acc }
            }

    companion object {
        private fun List<DatabaseHeadToHeadDetail>.asRowData(
                endSize: Int,
                teamSize: Int,
                isEditable: Boolean,
        ) = groupBy { it.type }
                .map { (type, group) ->
                    val expectedArrowCount = type.expectedArrowCount(endSize, teamSize)
                    require(group.distinctBy { it.isTotal }.size == 1) { "Cannot have total and arrows" }

                    if (group[0].type == HeadToHeadArcherType.RESULT) {
                        require(group.size == 1) { "Cannot have more than one result" }
                        Result.fromDbValue(group[0].score, group[0].headToHeadArrowScoreId)
                    }
                    else if (group[0].type == HeadToHeadArcherType.SHOOT_OFF) {
                        require(group.size == 1) { "Cannot have more than one shoot off row" }
                        ShootOff.fromDbValue(group[0].score, group[0].headToHeadArrowScoreId)
                    }
                    else if (group[0].isTotal) {
                        require(group.size == 1) { "Cannot have more than one total" }

                        val score = group[0].score
                        if (isEditable) {
                            EditableTotal(
                                    type = type,
                                    expectedArrowCount = expectedArrowCount,
                                    dbId = group[0].headToHeadArrowScoreId.takeIf { it != 0 },
                            ).let { it.copy(text = it.text.copy(score.toString())) }
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
                                arrows = group.map { dbArrow -> Arrow(dbArrow.score, dbArrow.isX) },
                                dbIds = group.map { it.headToHeadArrowScoreId }
                                        .takeWhile { it != 0 }.takeIf { it.isNotEmpty() },
                        )
                    }
                }
    }
}
