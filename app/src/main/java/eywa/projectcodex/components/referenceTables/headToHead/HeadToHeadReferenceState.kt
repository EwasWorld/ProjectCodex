package eywa.projectcodex.components.referenceTables.headToHead

import eywa.projectcodex.common.sharedUi.numberField.NumberFieldState
import eywa.projectcodex.common.sharedUi.numberField.NumberValidator
import eywa.projectcodex.common.sharedUi.numberField.NumberValidatorGroup
import eywa.projectcodex.common.sharedUi.numberField.PartialNumberFieldState
import eywa.projectcodex.common.sharedUi.numberField.TypeValidator
import kotlin.math.max

data class HeadToHeadReferenceState(
        val archerRank: NumberFieldState<Int> = NumberFieldState(
                validators = NumberValidatorGroup(TypeValidator.IntValidator, NumberValidator.AtLeast(1)),
        ),
        val opponentRank: NumberFieldState<Int> = NumberFieldState(
                validators = NumberValidatorGroup(TypeValidator.IntValidator, NumberValidator.AtLeast(1)),
        ),
        val totalArchers: PartialNumberFieldState = PartialNumberFieldState(),
) {
    val totalArchersFull: NumberFieldState<Int>
        get() {
            val rank = archerRank.parsed
            val opponent = opponentRank.parsed

            val validator = if (rank == null && opponent == null) NumberValidator.AtLeast(2)
            else if (rank == null || opponent == null) NumberValidator.AtLeast(rank ?: opponent!!)
            else NumberValidator.AtLeast(max(rank, opponent).coerceAtLeast(2))

            return totalArchers.asNumberFieldState(
                    NumberValidatorGroup(TypeValidator.IntValidator, validator, NumberValidator.AtMost(10_000)),
            )
        }

    private val totalArchersParsed =
            if (totalArchersFull.parsed != null) totalArchersFull.parsed
            else if (archerRank.parsed == null && opponentRank.parsed == null) null
            else if (archerRank.parsed == null || opponentRank.parsed == null) archerRank.parsed ?: opponentRank.parsed
            else max(archerRank.parsed, opponentRank.parsed)

    val meetIn
        get() =
            if (archerRank.parsed == null || opponentRank.parsed == null) null
            else HeadToHeadUseCase.heatName(HeadToHeadUseCase.meetInHeat(archerRank.parsed, opponentRank.parsed))

    val tableData
        get() = listOfNotNull(getOpponents(archerRank.parsed), getOpponents(opponentRank.parsed))
                .takeIf { it.isNotEmpty() }

    val totalRounds
        get() = tableData?.maxOf { it.opponents.size }

    private fun getOpponents(rank: Int?) =
            if (rank == null || totalArchersParsed == null) null
            else HeadToHeadReferenceRow(rank, HeadToHeadUseCase.getOpponents(rank, totalArchersParsed))
}
