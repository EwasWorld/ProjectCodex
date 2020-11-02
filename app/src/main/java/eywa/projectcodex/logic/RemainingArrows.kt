package eywa.projectcodex.logic

import eywa.projectcodex.database.entities.RoundArrowCount
import eywa.projectcodex.database.entities.RoundDistance

/**
 * Generate a string to indicate how many arrows remain in the round
 *
 * @param currentArrowCount the number of arrows that have been shot
 * @param arrowCounts the arrow count entries pertaining to a single round (roundIds must match those in [distances])
 * @param distances the distances pertaining to a single round and subtype (roundIds must match those in [arrowCounts])
 */
class RemainingArrows(
        private val currentArrowCount: Int,
        private val arrowCounts: List<RoundArrowCount>,
        private val distances: List<RoundDistance>,
        private val distanceUnit: String
) {
    /**
     * @return Pair<arrow count, distance> e.g. (36, 70) meaning 36 arrows at 70(yd/m)
     */
    private var calculated: MutableList<Pair<Int, Int>>? = null

    fun getFirstRemainingArrowCount(): Int? {
        return calculated?.get(0)?.first
    }

    init {
        require(distanceUnit.isNotEmpty()) { "'unit' cannot be empty" }
        require(currentArrowCount >= 0) { "Cannot have a <0 current arrow count" }
        if (arrowCounts.isEmpty() || currentArrowCount >= arrowCounts.sumBy { it.arrowCount }) {
            calculated = null
        }
        else {
            require(arrowCounts.size == distances.size) { "Arrow counts and distances size mismatch" }
            require(arrowCounts.distinctBy { it.roundId }.size == 1) { "Arrow count information from multiple rounds" }
            require(distances.distinctBy { it.roundId }.size == 1) { "Distance information from multiple rounds" }
            require(distances.distinctBy { it.subTypeId }.size <= 1) { "Distance information from multiple subtypes" }
            require(arrowCounts[0].roundId == distances[0].roundId) { "Arrow counts and distances roundId mismatch" }

            val arrowCountsSorted = arrowCounts.sortedBy { it.distanceNumber }
            val distancesSorted = distances.sortedByDescending { it.distance }
            calculated = mutableListOf<Pair<Int, Int>>()
            var total = 0
            for (i in arrowCountsSorted.indices) {
                val distanceCount = arrowCountsSorted[i].arrowCount
                total += distanceCount
                if (total > currentArrowCount) {
                    val arrowCount = if (calculated!!.isEmpty()) total - currentArrowCount else distanceCount
                    calculated!!.add(arrowCount to distancesSorted[i].distance)
                }
            }
        }
    }

    /**
     * Generate a string to indicate how many arrows remain in the round
     *
     * @param at see return format
     * @return Pair<arrows remaining at the current distance, arrows remaining for future distances>. Each String is
     * formatted as a comma-separated list of "00 [at] 00[distanceUnit]" e.g. "12 at 90yd" or an empty string if no
     * arrows remain. Either both, neither, or just the second item will be an empty string. If [currentArrowCount] is
     * greater than the total arrow count possible given [arrowCounts], will return Pair("", "")
     */
    fun toString(at: String): Pair<String, String> {
        require(at.isNotEmpty()) { "'at' cannot be empty" }

        if (calculated.isNullOrEmpty()) return Pair("", "")

        val finalStrings = mutableListOf<String>()
        for (pair in calculated!!) {
            finalStrings.add("${pair.first} $at ${pair.second}$distanceUnit")
        }

        val first = finalStrings[0]
        finalStrings.removeAt(0)
        return Pair(first, finalStrings.joinToString(", "))
    }
}