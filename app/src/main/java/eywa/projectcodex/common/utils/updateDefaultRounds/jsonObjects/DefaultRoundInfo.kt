package eywa.projectcodex.common.utils.updateDefaultRounds.jsonObjects

import eywa.projectcodex.common.utils.updateDefaultRounds.DefaultRoundInfoHelper
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import eywa.projectcodex.database.rounds.RoundSubType

/**
 * @see Round
 */
class DefaultRoundInfo(
        val displayName: String,
        private val isOutdoor: Boolean,
        private val isMetric: Boolean,
        private val fiveArrowEnd: Boolean,
        private val permittedFaces: List<String>,
        private val roundSubTypes: List<RoundInfoSubType>,
        private val roundArrowCounts: List<RoundInfoArrowCount>,
        private val roundDistances: List<RoundInfoDistance>
) {
    companion object {
        internal const val defaultRoundMinimumId = 5
    }

    /**
     * Validation
     * TODO Make a builder omg
     */
    init {
        // Lengths
        require(roundArrowCounts.isNotEmpty()) { "Must have at least one arrowCount in $displayName" }
        require(roundDistances.isNotEmpty()) { "Must have at least one distance in $displayName" }
        val subTypeMultiplier = if (roundSubTypes.isNotEmpty()) roundSubTypes.size else 1
        require(subTypeMultiplier * roundArrowCounts.size == roundDistances.size) { "distance length incorrect in $displayName" }

        // Duplicate IDs
        require(roundSubTypes.size == roundSubTypes.distinctBy { it.id }.size) { "Duplicate subTypeId in $displayName" }
        require(roundArrowCounts.size == roundArrowCounts.distinctBy { it.distanceNumber }.size) { "Duplicate distanceNumber in $displayName" }

        // Check distances
        val subTypeList = if (roundSubTypes.isNotEmpty()) roundSubTypes
        else listOf(
                RoundInfoSubType(1, "", null, null)
        )
        for (subType in subTypeList) {
            val distances = roundDistances.filter { subTypeCount -> subTypeCount.roundSubTypeId == subType.id }
            require(distances.size == distances.distinctBy { it.distance }.size) { "Duplicate distance in $displayName for subType: ${subType.id}" }
            require(roundArrowCounts.map { it.distanceNumber }.toSet() == distances.map { it.distanceNumber }
                    .toSet()) { "Mismatched distanceNumbers in $displayName for subType: ${subType.id}" }
            require(distances.sortedByDescending { it.distance } == distances.sortedBy { it.distanceNumber }) { "Distances in $displayName are not non-ascending subType: ${subType.id}" }
        }

        // Names
        require(DefaultRoundInfoHelper.formatToDbName(displayName) != "") { "Round name cannot be empty" }
        require(
                roundSubTypes.size
                        == roundSubTypes.distinctBy { DefaultRoundInfoHelper.formatToDbName(it.subTypeName) }.size
        ) {
            "Duplicate sub type names in $displayName"
        }
        require(roundSubTypes.size <= 1 ||
                roundSubTypes.count { DefaultRoundInfoHelper.formatToDbName(it.subTypeName) == "" } == 0) {
            "Illegal empty sub type name in $displayName"
        }
    }

    /**
     * Properties cannot be private due to Klaxon parsing
     * @see RoundSubType
     */
    class RoundInfoSubType(
            val id: Int,
            val subTypeName: String,
            val gentsUnder: Int?,
            val ladiesUnder: Int?
    ) {
        fun toRoundSubType(roundId: Int): RoundSubType {
            return RoundSubType(roundId, id, subTypeName, gentsUnder, ladiesUnder)
        }
    }

    /**
     * Properties cannot be private due to Klaxon parsing
     * @see RoundArrowCount
     */
    class RoundInfoArrowCount(
            val distanceNumber: Int,
            val faceSizeInCm: Float,
            val arrowCount: Int
    ) {
        fun toRoundArrowCount(roundId: Int): RoundArrowCount {
            return RoundArrowCount(roundId, distanceNumber, faceSizeInCm, arrowCount)
        }
    }

    /**
     * Properties cannot be private due to Klaxon parsing
     * @see RoundDistance
     */
    class RoundInfoDistance(
            val distanceNumber: Int,
            val roundSubTypeId: Int,
            val distance: Int
    ) {
        fun toRoundDistance(roundId: Int): RoundDistance {
            return RoundDistance(roundId, distanceNumber, roundSubTypeId, distance)
        }
    }

    fun getRound(roundId: Int = 0): Round {
        return Round(
                roundId,
                DefaultRoundInfoHelper.formatToDbName(displayName),
                displayName,
                isOutdoor,
                isMetric,
                permittedFaces,
                true,
                fiveArrowEnd
        )
    }

    fun getRoundSubTypes(roundId: Int): List<RoundSubType> {
        return roundSubTypes.map { it.toRoundSubType(roundId) }
    }

    fun getRoundArrowCounts(roundId: Int): List<RoundArrowCount> {
        return roundArrowCounts.map { it.toRoundArrowCount(roundId) }
    }

    fun getRoundDistances(roundId: Int): List<RoundDistance> {
        return roundDistances.map { it.toRoundDistance(roundId) }
    }
}
