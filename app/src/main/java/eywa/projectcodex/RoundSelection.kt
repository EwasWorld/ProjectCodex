package eywa.projectcodex

import android.content.res.Resources
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import eywa.projectcodex.database.entities.Round
import eywa.projectcodex.database.entities.RoundArrowCount
import eywa.projectcodex.database.entities.RoundDistance
import eywa.projectcodex.database.entities.RoundSubType
import eywa.projectcodex.viewModels.NewRoundViewModel

/**
 * Helper class for selecting a round on the create round screen
 */
class RoundSelection(
        val resources: Resources, newRoundViewModel: NewRoundViewModel, viewLifecycleOwner: LifecycleOwner
) {
    private var allRoundSubTypes: List<RoundSubType> = listOf()
    private var allRoundArrowCounts: List<RoundArrowCount> = listOf()
    private var allRoundDistances: List<RoundDistance> = listOf()

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
        newRoundViewModel.allRounds.observe(viewLifecycleOwner, Observer { dbRounds ->
            availableRounds = listOf<Round?>(null).plus(dbRounds)
        })
        newRoundViewModel.allRoundSubTypes.observe(viewLifecycleOwner, Observer { dbRounds ->
            allRoundSubTypes = dbRounds
        })
        newRoundViewModel.allRoundArrowCounts.observe(viewLifecycleOwner, Observer { dbRounds ->
            allRoundArrowCounts = dbRounds
        })
        newRoundViewModel.allRoundDistances.observe(viewLifecycleOwner, Observer { dbRounds ->
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
            availableRounds.map { it?.displayName ?: resources.getString(R.string.create_round__no_round) }
        }
    }

    /**
     * Updates available subtypes and returns their display names
     *
     * @return null if there are one or no subtypes available. Note: the order should not be edited if later
     * functions are to work as this class' functions work on item positions
     */
    fun getRoundSubtypes(selectedRoundPosition: Int): List<String>? {
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
    fun getArrowCountIndicatorText(selectedRoundPosition: Int): String? {
        require(selectedRoundPosition >= 0) { "Selected round index out of bounds, must be >= 0" }
        require(selectedRoundPosition <= availableRounds.size) { "Selected round index out of bounds" }

        val roundInfo = availableRounds[selectedRoundPosition] ?: return null
        val relevantCounts = allRoundArrowCounts.filter { it.roundId == roundInfo.roundId }
        if (relevantCounts.isEmpty()) return null
        return relevantCounts.sortedBy { it.distanceNumber }.map { it.arrowCount / 12 }.joinToString(", ")
    }

    /**
     * @return the distances for the given round and subtype including units, comma separated. Returns null if a valid
     * set of distances cannot be deduced
     */
    fun getDistanceIndicatorText(selectedRoundPosition: Int, selectedSubtypePosition: Int?): String? {
        require(selectedRoundPosition >= 0) { "Selected round index out of bounds, must be >= 0" }
        require(selectedRoundPosition <= availableRounds.size) { "Selected round index out of bounds" }
        if (selectedSubtypePosition != null) {
            require(selectedSubtypePosition >= 0) { "Selected round index out of bounds, must be >= 0" }
            require(selectedSubtypePosition <= availableSubtypes.size) { "Selected round index out of bounds" }
        }

        val roundInfo = availableRounds[selectedRoundPosition] ?: return null

        var distances = allRoundDistances.filter { it.roundId == roundInfo.roundId }
        if (selectedSubtypePosition != null) {
            distances = distances.filter { it.subTypeId == availableSubtypes[selectedSubtypePosition] }
        }
        if (distances.isEmpty()) return null
        if (distances.distinctBy { it.subTypeId }.size > 1) return null
        distances = distances.sortedBy { it.distanceNumber }

        val unitText =
                resources.getString(if (roundInfo.isMetric) R.string.units_meters_short else R.string.units_yards_short)
        return (distances.joinToString(", ") { it.distance.toString() + unitText })
    }

    fun getSelectedRoundId(selectedRoundPosition: Int): Int? {
        require(selectedRoundPosition >= 0) { "Selected round index out of bounds, must be >= 0" }
        require(selectedRoundPosition <= availableRounds.size) { "Selected round index out of bounds" }

        return availableRounds[selectedRoundPosition]?.roundId
    }

    /**
     * @return the subtype ID at the given position, the only available subtype ID, or null
     */
    fun getSelectedSubtypeId(selectedSubtypePosition: Int?): Int? {
        if (selectedSubtypePosition != null) {
            require(selectedSubtypePosition >= 0) { "Selected round index out of bounds, must be >= 0" }
            require(selectedSubtypePosition <= availableSubtypes.size) { "Selected round index out of bounds" }
        }

        if (selectedSubtypePosition == null) {
            return if (availableSubtypes.size == 1) availableSubtypes[0] else null
        }
        return availableSubtypes[selectedSubtypePosition]
    }
}