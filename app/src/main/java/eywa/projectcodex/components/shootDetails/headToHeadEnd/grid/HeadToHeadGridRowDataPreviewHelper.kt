package eywa.projectcodex.components.shootDetails.headToHeadEnd.grid

import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadArcherType
import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadResult
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
            if (type == HeadToHeadArcherType.RESULT || isTotal) {
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
            result: HeadToHeadResult = HeadToHeadResult.WIN,
            typesToIsTotal: Map<HeadToHeadArcherType, Boolean> = mapOf(
                    HeadToHeadArcherType.SELF to false,
                    HeadToHeadArcherType.OPPONENT to true,
            ),
            isEditable: Boolean = false,
            winnerScore: Int? = null,
            loserScore: Int? = null,
            selfScore: Int? = null,
    ): List<HeadToHeadGridRowData> {
        require(result != HeadToHeadResult.INCOMPLETE)

        val endSize = if (isShootOff) 1 else 3
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

        return typesToIsTotal.map { (type, isTotal) ->
            val total = when (type) {
                HeadToHeadArcherType.SELF -> selfTotal
                HeadToHeadArcherType.OPPONENT -> opponentTotal
                HeadToHeadArcherType.TEAM_MATE -> teamTotal - selfTotal
                HeadToHeadArcherType.TEAM -> teamTotal
                HeadToHeadArcherType.RESULT -> result.defaultPoints
            }
            require(total >= 0)
            val expectedArrowCount = type.expectedArrowCount(endSize, teamSize)
            if (type == HeadToHeadArcherType.RESULT || isTotal) {
                if (isEditable) {
                    HeadToHeadGridRowData.EditableTotal(type, expectedArrowCount).let {
                        it.copy(text = it.text.onTextChanged(total.toString()))
                    }
                }
                else {
                    HeadToHeadGridRowData.Total(type, expectedArrowCount, total)
                }
            }
            else {
                HeadToHeadGridRowData.Arrows(
                        type = type,
                        expectedArrowCount = expectedArrowCount,
                        arrows = createArrows(expectedArrowCount, total).toArrows(),
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
