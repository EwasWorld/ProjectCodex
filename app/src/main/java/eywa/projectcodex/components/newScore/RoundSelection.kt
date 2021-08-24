package eywa.projectcodex.components.newScore

import android.content.res.Resources
import androidx.lifecycle.LifecycleOwner
import eywa.projectcodex.CustomLogger
import eywa.projectcodex.R
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance
import eywa.projectcodex.database.rounds.RoundSubType
import java.text.DecimalFormat
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Helper class for selecting a round on the create round screen
 * @param newRoundViewModel used to fetch rounds and sub-types form the database
 */
class RoundSelection(
        val resources: Resources, newRoundViewModel: NewScoreViewModel, viewLifecycleOwner: LifecycleOwner
) {
    companion object {
        const val LOG_TAG = "RoundSelection"
    }

    private val roundsRetrievedLatch = CountDownLatch(1)
    private val subTypesRetrievedLatch = CountDownLatch(1)
    private var allRoundSubTypes: List<RoundSubType> = listOf()
    private var allRoundArrowCounts: List<RoundArrowCount> = listOf()
    private var allRoundDistances: List<RoundDistance> = listOf()

    /**
     * The position in [availableRounds] that indicates no round selected
     */
    val noRoundPosition = 0
    var selectedRoundPosition = noRoundPosition
    var selectedSubtypePosition: Int? = 0

    /**
     * The rounds named on the round spinner in their correct positions
     * First element is always null referring to either the 'none selected' or 'no rounds found' option
     */
    private var availableRounds: List<Round?> = listOf()

    /**
     * The subtypes available for the currently selected round
     * These are the subtype IDs of the subtypes named on the subtype spinner in their correct positions
     */
    private var availableSubtypes: List<Int> = listOf()

    init {
        newRoundViewModel.allRounds.observe(viewLifecycleOwner, { dbRounds ->
            availableRounds = listOf<Round?>(null).plus(dbRounds)
            roundsRetrievedLatch.countDown()
        })
        newRoundViewModel.allRoundSubTypes.observe(viewLifecycleOwner, { dbRounds ->
            allRoundSubTypes = dbRounds
            subTypesRetrievedLatch.countDown()
        })
        newRoundViewModel.allRoundArrowCounts.observe(viewLifecycleOwner, { dbRounds ->
            allRoundArrowCounts = dbRounds
        })
        newRoundViewModel.allRoundDistances.observe(viewLifecycleOwner, { dbRounds ->
            allRoundDistances = dbRounds
        })
    }

    /**
     * @return the display names of the rounds currently available, including a 'none selected option'. If there are no
     * rounds available the only item returned will be a 'no rounds found' option. Note: the order should not be edited
     * if later functions are to work as this class' functions work on item positions
     */
    fun getAvailableRounds(): List<String> {
        return if (availableRounds.size <= 1) {
            listOf(resources.getString(R.string.create_round__no_rounds_found))
        }
        else {
            availableRounds.map {
                it?.displayName ?: resources.getString(
                        R.string.create_round__no_round
                )
            }
        }
    }

    fun getPositionOfRound(roundId: Int?): Int? {
        if (!roundsRetrievedLatch.await(3, TimeUnit.SECONDS)) {
            CustomLogger.customLogger.w(LOG_TAG, "Rounds not retrieved")
            return null
        }
        val pos = availableRounds.indexOfFirst {
            return@indexOfFirst if (roundId == null) {
                it == null
            }
            else {
                it?.roundId == roundId
            }
        }
        if (pos == -1) {
            throw IllegalStateException("Round not found")
        }
        return pos
    }

    fun getPositionOfSubtype(roundId: Int?, subTypeId: Int): Int? {
        val roundPos = getPositionOfRound(roundId) ?: return null
        // Ensure available subtypes is populated
        getRoundSubtypes(roundPos)

        if (!subTypesRetrievedLatch.await(3, TimeUnit.SECONDS)) {
            CustomLogger.customLogger.w(LOG_TAG, "Sub types not retrieved")
            return null
        }
        val pos = availableSubtypes.indexOfFirst { it == subTypeId }
        if (pos == -1) {
            throw IllegalStateException("Sub type not found")
        }
        return pos
    }

    fun getSelectedRoundName(): String? {
        return availableRounds[selectedRoundPosition]?.displayName
    }

    /**
     * Updates available subtypes and returns their display names
     *
     * @return null if there are one or no subtypes available. Note: the order should not be edited if later
     * functions are to work as this class' functions work on item positions
     */
    fun getRoundSubtypes(selectedRoundPosition: Int = this.selectedRoundPosition): List<String>? {
        require(selectedRoundPosition >= 0) { "Selected round index out of bounds, must be >= 0" }
        require(selectedRoundPosition <= availableRounds.size) { "Selected round index out of bounds" }

        // No need to check for empty available rounds as it will always contain at least a placeholder item
        val roundInfo = availableRounds[selectedRoundPosition]
        if (roundInfo == null) {
            availableSubtypes = listOf()
            return null
        }
        val filteredRounds = allRoundSubTypes.filter { it.roundId == roundInfo.roundId }
        availableSubtypes = filteredRounds.map { it.subTypeId }
        return if (filteredRounds.size > 1) filteredRounds.map { (it.name ?: "") } else null
    }

    /**
     * @return the arrow counts for the selected round in dozens, comma separated
     */
    fun getArrowCountIndicatorText(): String? {
        require(selectedRoundPosition >= 0) { "Selected round index out of bounds, must be >= 0" }
        require(selectedRoundPosition <= availableRounds.size) { "Selected round index out of bounds" }

        val roundInfo = availableRounds[selectedRoundPosition] ?: return null
        val relevantCounts = allRoundArrowCounts.filter { it.roundId == roundInfo.roundId }
        if (relevantCounts.isEmpty()) return null
        return relevantCounts.sortedBy { it.distanceNumber }.joinToString(", ") {
            // Ensure only a single decimal place of precision
            DecimalFormat("0.#").format(it.arrowCount / 12.0)
        }
    }

    fun getTotalArrowsInRound(): Int? {
        require(selectedRoundPosition >= 0) { "Selected round index out of bounds, must be >= 0" }
        require(selectedRoundPosition <= availableRounds.size) { "Selected round index out of bounds" }

        val roundInfo = availableRounds[selectedRoundPosition] ?: return null
        val total = allRoundArrowCounts.filter { it.roundId == roundInfo.roundId }.sumOf { it.arrowCount }
        if (total == 0) {
            return null
        }
        return total
    }

    /**
     * @return the distances for the given round and subtype including units, comma separated. Returns null if a valid
     * set of distances cannot be deduced
     */
    fun getDistanceIndicatorText(): String? {
        require(selectedRoundPosition >= 0) { "Selected round index out of bounds, must be >= 0" }
        require(selectedRoundPosition <= availableRounds.size) { "Selected round index out of bounds" }
        if (selectedSubtypePosition != null) {
            require(selectedSubtypePosition!! >= 0) { "Selected round index out of bounds, must be >= 0" }
            require(selectedSubtypePosition!! <= availableSubtypes.size) { "Selected round index out of bounds" }
        }

        val roundInfo = availableRounds[selectedRoundPosition] ?: return null

        var distances = allRoundDistances.filter { it.roundId == roundInfo.roundId }
        if (selectedSubtypePosition != null) {
            distances = distances.filter { it.subTypeId == availableSubtypes[selectedSubtypePosition!!] }
        }
        if (distances.isEmpty()) return null
        if (distances.distinctBy { it.subTypeId }.size > 1) return null
        distances = distances.sortedBy { it.distanceNumber }

        val unitText =
                resources.getString(if (roundInfo.isMetric) R.string.units_meters_short else R.string.units_yards_short)
        return (distances.joinToString(", ") { it.distance.toString() + unitText })
    }

    fun getSelectedRoundId(): Int? {
        require(selectedRoundPosition >= 0) { "Selected round index out of bounds, must be >= 0" }
        require(selectedRoundPosition <= availableRounds.size) { "Selected round index out of bounds" }

        return availableRounds[selectedRoundPosition]?.roundId
    }

    /**
     * @return the subtype ID at the given position, the only available subtype ID, or 1 (as is default in RoundDistance)
     * @see RoundDistance
     */
    fun getSelectedSubtypeId(): Int {
        if (selectedSubtypePosition != null) {
            require(selectedSubtypePosition!! >= 0) { "Selected round index out of bounds, must be >= 0" }
            require(selectedSubtypePosition!! <= availableSubtypes.size) { "Selected round index out of bounds" }
        }

        if (selectedSubtypePosition == null) {
            return if (availableSubtypes.size == 1) availableSubtypes[0] else 1
        }
        return availableSubtypes[selectedSubtypePosition!!]
    }
}