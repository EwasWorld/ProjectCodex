package eywa.projectcodex.database.rounds

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.room.Embedded
import androidx.room.Relation


data class FullRoundInfo(
        @Embedded val round: Round,

        @Relation(parentColumn = "roundId", entityColumn = "roundId")
        val roundSubTypes: List<RoundSubType>? = null,

        @Relation(parentColumn = "roundId", entityColumn = "roundId")
        val roundArrowCounts: List<RoundArrowCount>? = null,

        @Relation(parentColumn = "roundId", entityColumn = "roundId")
        val roundDistances: List<RoundDistance>? = null,
) {
    fun getDistanceUnitRes() = round.getDistanceUnitRes()

    @Composable
    fun getDistanceUnit() = getDistanceUnitRes()?.let { stringResource(it) }

    fun getDistances(subTypeId: Int?) =
            when {
                roundSubTypes == null || roundSubTypes.size <= 1 -> roundDistances
                else -> roundDistances?.filter { it.subTypeId == (subTypeId ?: 1) }
            }

    fun getDisplayName(subTypeId: Int?) = roundSubTypes
            ?.takeIf { it.isNotEmpty() }
            ?.find { it.subTypeId == (subTypeId ?: 1) }
            ?.name
            ?.takeIf { it.isNotBlank() }
            ?: round.displayName

    val isValidWithSingleSubType: Boolean
        get() {
            if (roundArrowCounts.isNullOrEmpty() || roundDistances.isNullOrEmpty()) return false
            if (roundArrowCounts.any { it.roundId != round.roundId }) return false

            if (!roundSubTypes.isNullOrEmpty() && roundSubTypes.size > 1) return false
            val subType = roundSubTypes?.getOrNull(0)

            if (roundDistances.any { it.roundId == round.roundId && it.subTypeId == (subType?.subTypeId ?: 1) }) {
                return false
            }

            return true
        }

    val maxScore: Int
        get() {
            val arrowCount = roundArrowCounts.orEmpty().sumOf { it.arrowCount }
            val maxArrowValue = when {
                round.name.contains("WORCESTER", ignoreCase = true) -> 5
                round.isMetric -> 10
                else -> 9
            }
            return arrowCount * maxArrowValue
        }

    fun maxDistanceOnlyWithMinArrowCount(arrowCount: Int, subTypeId: Int? = null): FullRoundInfo {
        val subType = subTypeId ?: roundSubTypes?.takeIf { it.size == 1 }?.first()?.subTypeId ?: 1

        val distance = getDistances(subTypeId)?.maxByOrNull { it.distance }?.let { listOf(it) }
        val roundArrowCounts = roundArrowCounts?.find { it.distanceNumber == distance?.firstOrNull()?.distanceNumber }
                ?.let { listOf(it.copy(arrowCount = it.arrowCount.coerceAtLeast(arrowCount))) }

        return copy(
                roundArrowCounts = roundArrowCounts,
                roundDistances = distance,
                roundSubTypes = roundSubTypes?.filter { it.subTypeId == subType },
        )
    }
}
