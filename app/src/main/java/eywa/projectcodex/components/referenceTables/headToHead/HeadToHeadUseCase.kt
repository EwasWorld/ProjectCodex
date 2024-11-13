package eywa.projectcodex.components.referenceTables.headToHead

import eywa.projectcodex.R
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.common.utils.ResOrActual.StringResource
import kotlin.math.ceil
import kotlin.math.log2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

object HeadToHeadUseCase {
    const val MAX_QUALI_RANK = 512
    const val MAX_HEAT = 8

    /**
     * Returns the ranks of the worst-case opponent an archer an archer will face in each round given their qualifying
     * [rank] (null is a bye).
     *
     * e.g. if there are 50 archers, rank 1 will start with a bye then will face rank 32, 16, 8, 4, and 2 in that order,
     * therefore the output will be [null, 32, 16, 8, 4, 2]
     */
    fun getOpponents(rank: Int, totalArchers: Int): List<Int?> {
        check(rank <= totalArchers) { "Rank must be less than or equal to total archers" }
        check(rank > 0 && totalArchers > 0) { "Rank and totalArchers must be greater than 0" }

        var newRank = rank
        val rounds = ceil(log2(totalArchers.toDouble()))

        return List(rounds.roundToInt()) { round ->
            val opponent = ((2.0.pow(rounds - round).roundToInt() + 1) - newRank).takeIf { it <= totalArchers }
            newRank = min(opponent ?: newRank, newRank)
            opponent
        }.reversed()
    }

    /**
     * Returns the round in which the two archers will face each other given their qualifying ranks.
     *
     * 0 is the final, 1 is the semi-final, 2 is the quarter-final, etc.
     */
    fun meetInRound(rankA: Int, rankB: Int): Int {
        check(rankA > 0 && rankB > 0) { "Ranks must be greater than 0" }
        val max = max(rankA, rankB)

        val aOpponents = getOpponents(rankB, max)
        val bOpponents = getOpponents(rankA, max)
        for (i in aOpponents.indices.reversed()) {
            if (aOpponents[i] != null && aOpponents[i] == bOpponents[i]) {
                return i + 1
            }
        }
        return 0
    }

    fun roundName(round: Int): ResOrActual<String> = when (round) {
        0 -> StringResource(R.string.head_to_head_ref__final)
        1 -> StringResource(R.string.head_to_head_ref__semi_final)
        2 -> StringResource(R.string.head_to_head_ref__quarter_final)
        else -> StringResource(R.string.head_to_head_ref__round_name, listOf(2.0.pow(round).roundToInt()))
    }

    fun shortRoundName(round: Int): ResOrActual<String> = when (round) {
        0 -> StringResource(R.string.head_to_head_ref__final_short)
        1 -> StringResource(R.string.head_to_head_ref__semi_final_short)
        2 -> StringResource(R.string.head_to_head_ref__round_name, listOf(4))
        else -> roundName(round)
    }

    fun shootOffSet(teamSize: Int) = if (teamSize > 1) 5 else 6
    fun winScore(teamSize: Int) = if (teamSize > 1) 5 else 6
    fun endSize(teamSize: Int, isShootOff: Boolean) = if (isShootOff) 1 else if (teamSize > 1) 2 else 3
}
