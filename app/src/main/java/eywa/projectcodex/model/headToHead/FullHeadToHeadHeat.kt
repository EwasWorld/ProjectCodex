package eywa.projectcodex.model.headToHead

import eywa.projectcodex.components.referenceTables.headToHead.HeadToHeadUseCase
import eywa.projectcodex.components.shootDetails.headToHeadEnd.HeadToHeadResult
import eywa.projectcodex.components.shootDetails.headToHeadEnd.grid.HeadToHeadGridState
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHeadHeat
import eywa.projectcodex.model.Either

typealias HeadToHeadRunningTotal = Either<Pair<Int, Int>, HeadToHeadNoResult>

data class FullHeadToHeadHeat(
        val heat: DatabaseHeadToHeadHeat,
        val sets: List<FullHeadToHeadSet>,
        val teamSize: Int,
        val isRecurveStyle: Boolean,
) {
    init {
        check(heat.heat in 0..HeadToHeadUseCase.MAX_HEAT)
    }

    val runningTotals = runningTotals()

    val hasStarted: Boolean
        get() = sets.isNotEmpty()

    val arrowsShot: Int
        get() = sets.sumOf { it.arrowsShot }

    /**
     * @return for each set in [sets], provide cumulative team/opponent score.
     * For [isRecurveStyle] this is the set points, else it's the total score.
     * [HeadToHeadNoResult] if this or any previous set is incomplete or unknown
     */
    private fun runningTotals(): List<HeadToHeadRunningTotal> {
        var previous: HeadToHeadNoResult? = null
        var teamScore = 0
        var opponentScore = 0
        return sets.mapIndexed { _, set ->
            val isShootOff = set.isShootOff
            val result = set.result
            previous =
                    if (previous == HeadToHeadNoResult.UNKNOWN || result == HeadToHeadResult.UNKNOWN) {
                        HeadToHeadNoResult.UNKNOWN
                    }
                    else if (previous == HeadToHeadNoResult.INCOMPLETE || result == HeadToHeadResult.INCOMPLETE) {
                        HeadToHeadNoResult.INCOMPLETE
                    }
                    else {
                        null
                    }

            if (previous != null) {
                Either.Right(previous!!)
            }
            else {
                teamScore +=
                        if (isRecurveStyle && isShootOff) result.shootOffPoints
                        else if (isRecurveStyle) result.defaultPoints
                        else set.teamEndScore ?: 0

                opponentScore +=
                        if (isRecurveStyle && isShootOff) result.opposite().shootOffPoints
                        else if (isRecurveStyle) result.opposite().defaultPoints
                        else set.opponentEndScore ?: 0
                Either.Left(teamScore to opponentScore)
            }
        }
    }

    fun result(): HeadToHeadResult {
        if (heat.isBye) {
            check(runningTotals.isEmpty()) { "Byes cannot have sets" }
            return HeadToHeadResult.WIN
        }

        if (runningTotals.isEmpty()) return HeadToHeadResult.INCOMPLETE
        runningTotals.last().right?.let {
            when (it) {
                HeadToHeadNoResult.UNKNOWN -> return HeadToHeadResult.UNKNOWN
                HeadToHeadNoResult.INCOMPLETE -> return HeadToHeadResult.INCOMPLETE
            }
        }

        val scores = runningTotals.last().left!!

        /*
         * Recurve
         */
        if (isRecurveStyle) {
            val winScore = HeadToHeadUseCase.winScore(teamSize)

            // Incomplete sets / not reached required set points
            if (scores.first < winScore && scores.second < winScore) return HeadToHeadResult.INCOMPLETE

            check(scores.first != scores.second) { "Final result cannot be a tie" }
            return if (scores.first > scores.second) HeadToHeadResult.WIN else HeadToHeadResult.LOSS
        }

        /*
         * Compound
         */
        val shootOffSet = HeadToHeadUseCase.shootOffSet(teamSize)

        // Shoot off outcome
        if (sets.size == shootOffSet) {
            val result = sets.last().result
            check(result != HeadToHeadResult.TIE) { "Final result cannot be a tie" }
            return result
        }
        // Not enough sets (compound always shoot all sets with an optional shoot off)
        if (sets.size < shootOffSet - 1) return HeadToHeadResult.INCOMPLETE

        // Shoot off required
        if (scores.first == scores.second) return HeadToHeadResult.INCOMPLETE

        return if (scores.first > scores.second) HeadToHeadResult.WIN else HeadToHeadResult.LOSS
    }

    fun toGridState() = HeadToHeadGridState(
            enteredArrows = sets,
            selected = null,
            isSingleEditableSet = false,
            runningTotals = runningTotals,
            finalResult = result(),
    )
}
