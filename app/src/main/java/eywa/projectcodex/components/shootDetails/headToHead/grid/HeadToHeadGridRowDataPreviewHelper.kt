package eywa.projectcodex.components.shootDetails.headToHead.grid

import eywa.projectcodex.components.referenceTables.headToHead.HeadToHeadUseCase
import eywa.projectcodex.components.shootDetails.headToHead.HeadToHeadArcherType
import eywa.projectcodex.components.shootDetails.headToHead.HeadToHeadResult
import eywa.projectcodex.model.Arrow
import kotlin.random.Random

object HeadToHeadGridRowDataPreviewHelper {
    val selfAndOneTeamMateWithOpponent = listOf(
            HeadToHeadGridRowData.Arrows(HeadToHeadArcherType.SELF, 3, listOf(10, 10, 10).toArrows()),
            HeadToHeadGridRowData.Arrows(HeadToHeadArcherType.TEAM_MATE, 3, listOf(10, 10, 10).toArrows()),
            HeadToHeadGridRowData.Total(HeadToHeadArcherType.OPPONENT, 6, 59),
    )

    val selfAndOpponent = listOf(
            HeadToHeadGridRowData.Arrows(HeadToHeadArcherType.SELF, 3, listOf(10, 10, 10).toArrows()),
            HeadToHeadGridRowData.EditableTotal(HeadToHeadArcherType.OPPONENT, 3).let {
                it.copy(text = it.text.onTextChanged("29"))
            },
    )

    fun createEmptyRows(
            teamSize: Int = 1,
            isShootOff: Boolean = false,
            typesToIsTotal: Map<HeadToHeadArcherType, Boolean> = mapOf(
                    HeadToHeadArcherType.SELF to false,
                    HeadToHeadArcherType.OPPONENT to true,
            ),
            isEditable: Boolean = false,
    ): List<HeadToHeadGridRowData> {
        val endSize = if (isShootOff) 1 else 3
        return typesToIsTotal.map { (type, isTotal) ->
            val expectedArrowCount = type.expectedArrowCount(endSize, teamSize)
            if (type == HeadToHeadArcherType.RESULT) {
                HeadToHeadGridRowData.Result(HeadToHeadResult.WIN)
            }
            else if (type == HeadToHeadArcherType.SHOOT_OFF) {
                HeadToHeadGridRowData.ShootOff(HeadToHeadResult.WIN)
            }
            else if (isTotal) {
                if (isEditable) HeadToHeadGridRowData.EditableTotal(type, expectedArrowCount)
                else HeadToHeadGridRowData.Total(type, expectedArrowCount, null)
            }
            else {
                HeadToHeadGridRowData.Arrows(
                        type = type,
                        expectedArrowCount = expectedArrowCount,
                        arrows = listOf(),
                )
            }
        }
    }

    fun create(
            teamSize: Int = 1,
            isShootOff: Boolean = false,
            endSize: Int = HeadToHeadUseCase.endSize(teamSize = teamSize, isShootOff = isShootOff),
            result: HeadToHeadResult = HeadToHeadResult.WIN,
            typesToIsTotal: Map<HeadToHeadArcherType, Boolean> = mapOf(
                    HeadToHeadArcherType.TEAM to false,
                    HeadToHeadArcherType.OPPONENT to true,
            ),
            isEditable: Boolean = false,
            winnerScore: Int? = null,
            loserScore: Int? = null,
            selfScore: Int? = null,
            dbIds: List<List<Int>>? = null,
    ): List<HeadToHeadGridRowData> {
        require(result != HeadToHeadResult.UNKNOWN)
        val isIncomplete = result == HeadToHeadResult.INCOMPLETE

        val maxScore = teamSize * endSize * 10
        require(winnerScore == null || (winnerScore in 0..maxScore))
        require(loserScore == null || (loserScore in 0..maxScore))
        require(selfScore == null || (selfScore in 0..maxScore))

        val winnerTotal = winnerScore ?: Random.nextInt(if (isShootOff) 5 else teamSize * 10, maxScore + 1)
        val loserTotal = loserScore ?: (winnerTotal - Random.nextInt(1, winnerTotal - 1))

        val teamTotal =
                if (result == HeadToHeadResult.WIN || result == HeadToHeadResult.TIE) winnerTotal else loserTotal
        val selfTotal = selfScore ?: (if (teamSize == 1) teamTotal else teamTotal - Random.nextInt(1, teamTotal - 1))
        val opponentTotal =
                if (result == HeadToHeadResult.LOSS || result == HeadToHeadResult.TIE) winnerTotal else loserTotal

        val rows =
                if (!isShootOff || typesToIsTotal.containsKey(HeadToHeadArcherType.SHOOT_OFF)) typesToIsTotal
                else typesToIsTotal.plus(HeadToHeadArcherType.SHOOT_OFF to true)

        return rows.entries.mapIndexed { i, (type, isTotal) ->
            val total = when (type) {
                HeadToHeadArcherType.SELF -> selfTotal
                HeadToHeadArcherType.OPPONENT -> opponentTotal
                HeadToHeadArcherType.TEAM_MATE -> teamTotal - selfTotal
                HeadToHeadArcherType.TEAM -> teamTotal
                HeadToHeadArcherType.RESULT -> -1
                HeadToHeadArcherType.SHOOT_OFF -> -1
            }
            require(total >= 0)
            val expectedArrowCount = type.expectedArrowCount(endSize, teamSize)
            val indexes = dbIds?.getOrNull(i)
            if (type == HeadToHeadArcherType.RESULT) {
                HeadToHeadGridRowData.Result(result)
            }
            else if (type == HeadToHeadArcherType.SHOOT_OFF) {
                HeadToHeadGridRowData.ShootOff(result)
            }
            else if (isTotal) {
                val index = indexes?.getOrNull(0)
                if (isEditable) {
                    HeadToHeadGridRowData.EditableTotal(type, expectedArrowCount, dbId = index).let {
                        if (isIncomplete) it else it.copy(text = it.text.onTextChanged(total.toString()))
                    }
                }
                else {
                    HeadToHeadGridRowData.Total(type, expectedArrowCount, total.takeIf { !isIncomplete }, dbId = index)
                }
            }
            else {
                HeadToHeadGridRowData.Arrows(
                        type = type,
                        expectedArrowCount = expectedArrowCount,
                        arrows = if (isIncomplete) listOf() else createArrows(expectedArrowCount, total).toArrows(),
                        dbIds = indexes,
                )
            }
        }
    }

    fun createArrows(size: Int, score: Int): List<Int> {
        val value = score / size
        val test = score % size
        return List(size) { if (it < test) value + 1 else value }
    }

    fun List<Int>.toArrows() = map { Arrow(it, false) }
}
