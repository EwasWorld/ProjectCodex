package eywa.projectcodex.components.shootDetails.headToHeadEnd.grid

import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadArcherType
import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadResult
import eywa.projectcodex.model.Arrow
import kotlin.random.Random

object HeadToHeadGridRowDataPreviewHelper {
    val selfAndOneTeamMateWithOpponent = listOf(
            HeadToHeadGridRowData.Arrows(HeadToHeadArcherType.SELF_ARROW, listOf(10, 10, 10).toArrows()),
            HeadToHeadGridRowData.Arrows(HeadToHeadArcherType.TEAM_MATE_ARROW, listOf(10, 10, 10).toArrows()),
            HeadToHeadGridRowData.Total(HeadToHeadArcherType.OPPONENT_ARROW, 59),
    )

    val selfAndOpponent = listOf(
            HeadToHeadGridRowData.Arrows(HeadToHeadArcherType.SELF_ARROW, listOf(10, 10, 10).toArrows()),
            HeadToHeadGridRowData.Total(HeadToHeadArcherType.OPPONENT_ARROW, 29),
    )

    fun create(
            teamSize: Int = 1,
            isShootOff: Boolean = false,
            result: HeadToHeadResult = HeadToHeadResult.WIN,
            typesToIsTotal: Map<HeadToHeadArcherType, Boolean> = mapOf(
                    HeadToHeadArcherType.SELF_ARROW to false,
                    HeadToHeadArcherType.OPPONENT_ARROW to true,
            ),
    ): List<HeadToHeadGridRowData> {
        require(result != HeadToHeadResult.INCOMPLETE)

        val endSize = if (isShootOff) 1 else 3

        val winnerTotal = Random.nextInt(if (isShootOff) 1 else teamSize * 10, teamSize * endSize * 10 + 1)
        val loserTotal = winnerTotal - Random.nextInt(1, winnerTotal - 1)

        val teamTotal =
                if (result == HeadToHeadResult.WIN || result == HeadToHeadResult.TIE) winnerTotal else loserTotal
        val selfTotal = if (teamSize == 1) teamTotal else teamTotal - Random.nextInt(1, teamTotal - 1)
        val opponentTotal =
                if (result == HeadToHeadResult.LOSS || result == HeadToHeadResult.TIE) winnerTotal else loserTotal

        return typesToIsTotal.map { (type, isTotal) ->
            val total = when (type) {
                HeadToHeadArcherType.SELF_ARROW -> selfTotal
                HeadToHeadArcherType.OPPONENT_ARROW -> opponentTotal
                HeadToHeadArcherType.TEAM_MATE_ARROW -> teamTotal - selfTotal
                HeadToHeadArcherType.TEAM_ARROW -> teamTotal
                HeadToHeadArcherType.TEAM_POINTS -> result.defaultPoints
            }
            if (type == HeadToHeadArcherType.TEAM_POINTS || isTotal) HeadToHeadGridRowData.Total(type, total)
            else HeadToHeadGridRowData.Arrows(
                    type = type,
                    arrows = createArrows(type.expectedArrowCount(endSize, teamSize), total).toArrows(),
            )
        }
    }

    fun createArrows(size: Int, score: Int): List<Int> {
        val value = score / size
        val test = score % size
        return List(size) { if (it < test) value + 1 else value }
    }

    fun List<Int>.toArrows() = map { Arrow(it, false) }
}
