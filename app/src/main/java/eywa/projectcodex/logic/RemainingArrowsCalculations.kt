package eywa.projectcodex.logic

import eywa.projectcodex.database.entities.RoundArrowCount
import eywa.projectcodex.database.entities.RoundDistance

/**
 * Generate a string to indicate how many arrows remain in the round
 *
 * @param currentArrowCount the number of arrows that have been shot
 * @param arrowCounts the arrow count entries pertaining to a single round (roundIds must match those in [distances])
 * @param distances the distances pertaining to a single round and subtype (roundIds must match those in [arrowCounts])
 * @param unit see return format
 * @param at see return format
 * @return Pair<arrows remaining at the current distance, arrows remaining for future distances>. Each String is
 * formatted as a comma-separated list of "00 [at] 00[unit]" e.g. "12 at 90yd" or an empty string if no arrows remain.
 * Either both, neither, or just the second item will be an empty string. If [currentArrowCount] is greater than the
 * total arrow count possible given [arrowCounts], will return Pair("", "")
 */
fun getRemainingArrowsPerDistance(
        currentArrowCount: Int, arrowCounts: List<RoundArrowCount>, distances: List<RoundDistance>, unit: String,
        at: String
): Pair<String, String> {
    require(unit.isNotEmpty()) { "'unit' cannot be empty" }
    require(at.isNotEmpty()) { "'at' cannot be empty" }
    require(currentArrowCount >= 0) { "Cannot have a <0 current arrow count" }
    if (arrowCounts.isEmpty() || currentArrowCount >= arrowCounts.sumBy { it.arrowCount }) {
        return Pair("", "")
    }

    require(arrowCounts.size == distances.size) { "Arrow counts and distances size mismatch" }
    require(arrowCounts.distinctBy { it.roundId }.size == 1) { "Arrow count information from multiple rounds" }
    require(distances.distinctBy { it.roundId }.size == 1) { "Distance information from multiple rounds" }
    require(distances.distinctBy { it.subTypeId }.size <= 1) { "Distance information from multiple subtypes" }
    require(arrowCounts[0].roundId == distances[0].roundId) { "Arrow counts and distances roundId mismatch" }

    val arrowCountsSorted = arrowCounts.sortedBy { it.distanceNumber }
    val distancesSorted = distances.sortedByDescending { it.distance }
    val finalStrings = mutableListOf<String>()
    var total = 0
    for (i in arrowCountsSorted.indices) {
        val distanceCount = arrowCountsSorted[i].arrowCount
        total += distanceCount
        if (total > currentArrowCount) {
            val arrowCount = if (finalStrings.isEmpty()) total - currentArrowCount else distanceCount
            finalStrings.add("$arrowCount $at ${distancesSorted[i].distance}$unit")
        }
    }

    val first = finalStrings[0]
    finalStrings.removeAt(0)
    return Pair(first, finalStrings.joinToString(", "))
}