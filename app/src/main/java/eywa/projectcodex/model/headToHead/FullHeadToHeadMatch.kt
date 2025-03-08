package eywa.projectcodex.model.headToHead

import eywa.projectcodex.components.referenceTables.headToHead.HeadToHeadUseCase
import eywa.projectcodex.components.shootDetails.headToHead.HeadToHeadArcherType
import eywa.projectcodex.components.shootDetails.headToHead.HeadToHeadResult
import eywa.projectcodex.components.shootDetails.headToHead.grid.HeadToHeadGridState
import eywa.projectcodex.components.shootDetails.headToHead.grid.SetDropdownMenuItem
import eywa.projectcodex.database.shootData.headToHead.DatabaseHeadToHeadMatch
import eywa.projectcodex.model.Either

typealias HeadToHeadRunningTotal = Either<Pair<Int, Int>, HeadToHeadNoResult>

data class FullHeadToHeadMatch(
        val match: DatabaseHeadToHeadMatch,
        val sets: List<FullHeadToHeadSet>,
        val teamSize: Int,
        val isSetPoints: Boolean,
        val isStandardFormat: Boolean,
) {
    init {
        check(match.heat == null || match.heat in 0..HeadToHeadUseCase.MAX_HEAT)
        check(
                sets.all {
                    it.teamSize == teamSize
                            && it.isSetPoints == isSetPoints
                            && (!isStandardFormat || it.endSize == HeadToHeadUseCase.endSize(teamSize, it.isShootOff))
                },
        )
    }

    val runningTotals = runningTotals()
    val result = result()

    val hasStarted: Boolean
        get() = sets.isNotEmpty()

    val arrowsShot: Int
        get() = getArrows(HeadToHeadArcherType.SELF)?.arrowCount
                ?: getArrows(HeadToHeadArcherType.TEAM)?.arrowCount?.div(teamSize)
                ?: 0

    fun getArrows(type: HeadToHeadArcherType) =
            sets.fold<FullHeadToHeadSet, RowArrows?>(null) { acc, set ->
                set.getArrows(type)?.let { it + acc } ?: acc
            }

    val isComplete
        get() = isStandardFormat && sets.isNotEmpty() && (
                (result.isComplete && result != HeadToHeadResult.TIE)
                        // If result is UNKNOWN, check if last match is a shoot off with a known result
                        || sets.last().let { it.isShootOff && it.isComplete && it.result != HeadToHeadResult.TIE }
                )

    /**
     * @return for each set in [sets], provide cumulative team/opponent score.
     * For [isSetPoints] this is the set points, else it's the total score.
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
                    if (previous == HeadToHeadNoResult.Unknown || result == HeadToHeadResult.UNKNOWN) {
                        HeadToHeadNoResult.Unknown
                    }
                    else if (previous == HeadToHeadNoResult.Incomplete || result == HeadToHeadResult.INCOMPLETE) {
                        HeadToHeadNoResult.Incomplete
                    }
                    else {
                        null
                    }

            if (previous != null) {
                Either.Right(previous!!)
            }
            else {
                teamScore +=
                        if (isSetPoints && isShootOff) result.shootOffPoints
                        else if (isSetPoints) result.defaultPoints
                        else set.teamEndScore ?: 0

                opponentScore +=
                        if (isSetPoints && isShootOff) result.opposite().shootOffPoints
                        else if (isSetPoints) result.opposite().defaultPoints
                        else set.opponentEndScore ?: 0
                Either.Left(teamScore to opponentScore)
            }
        }
    }

    private fun result(): HeadToHeadResult {
        if (match.isBye) {
            check(runningTotals.isEmpty()) { "Byes cannot have sets" }
            return HeadToHeadResult.WIN
        }

        if (runningTotals.isEmpty()) return HeadToHeadResult.INCOMPLETE
        runningTotals.last().right?.let {
            when (it) {
                is HeadToHeadNoResult.Partial -> return HeadToHeadResult.UNKNOWN
                HeadToHeadNoResult.Unknown -> return HeadToHeadResult.UNKNOWN
                HeadToHeadNoResult.Incomplete -> return HeadToHeadResult.INCOMPLETE
            }
        }

        val scores = runningTotals.last().left!!

        /*
         * Recurve
         */
        if (isSetPoints) {
            val pointsForWin = HeadToHeadUseCase.pointsForWin(teamSize)

            // Incomplete sets / not reached required set points
            if (scores.first < pointsForWin && scores.second < pointsForWin) return HeadToHeadResult.INCOMPLETE

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

    fun toGridState(dropdownMenuExpandedFor: Triple<Int, Int, List<SetDropdownMenuItem>>?) =
            HeadToHeadGridState.NonEditable(
                    matchNumber = match.matchNumber,
                    enteredArrows = sets,
                    runningTotals = runningTotals,
                    finalResult = result,
                    dropdownMenuExpandedFor = dropdownMenuExpandedFor,
                    showSetResult = isSetPoints,
            )
}
