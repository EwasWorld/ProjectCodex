package eywa.projectcodex.components.viewScores.data

import androidx.annotation.VisibleForTesting
import eywa.projectcodex.components.viewScores.ViewScoresFragment
import eywa.projectcodex.database.archerRound.ArcherRoundWithRoundInfoAndName
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance

/**
 * Stores the list of [ViewScoresEntry]s to be displayed by the [ViewScoresFragment]
 */
class ViewScoreData private constructor() {
    companion object {
        private var INSTANCE: ViewScoreData? = null

        fun getViewScoreData(): ViewScoreData {
            return synchronized(ViewScoreData::class) {
                val newInstance = INSTANCE ?: ViewScoreData()
                INSTANCE = newInstance
                newInstance
            }
        }

        // TODO Use dagger for this
        @VisibleForTesting(otherwise = VisibleForTesting.NONE)
        fun clearInstance() {
            INSTANCE = null
        }

        @VisibleForTesting(otherwise = VisibleForTesting.NONE)
        fun createInstance(): ViewScoreData {
            return ViewScoreData()
        }
    }

    private var data = mutableMapOf<Int, ViewScoresEntry>()

    fun getData(): List<ViewScoresEntry> {
        synchronized(this) {
            return data.values.sortedByDescending { it.archerRound.dateShot }
        }
    }

    /**
     * Updates the [ViewScoresEntry]s currently stored. Creates new instances for archerRoundIds that aren't current
     * stored and updates the archer round on stored items
     *
     * @return whether any changes were made
     */
    fun updateArcherRounds(allArcherRounds: List<ArcherRoundWithRoundInfoAndName>): Boolean {
        var changeMade = false
        synchronized(this) {
            val allKeys = allArcherRounds.map { it.archerRound.archerRoundId }
            val beforeSize = data.size
            data = data.filter { allKeys.contains(it.key) }.toMutableMap()
            if (beforeSize != data.size) {
                changeMade = true
            }

            for (archerRound in allArcherRounds) {
                val key = archerRound.archerRound.archerRoundId
                val currentData = data[key]
                if (currentData == null) {
                    data[key] = ViewScoresEntry(archerRound)
                    changeMade = true
                }
                else {
                    changeMade = currentData.updateArcherRound(archerRound) || changeMade
                }
            }
        }
        return changeMade
    }

    /**
     * Updates the [ViewScoresEntry]s with the arrows that share their archerRoundId. Passes an empty list if there are
     * no arrows with that id
     *
     * @return whether any changes were made
     */
    fun updateArrows(allArrows: List<ArrowValue>): Boolean {
        var changeMade = false
        synchronized(this) {
            val grouped = allArrows.groupBy { it.archerRoundId }
            for (entry in data.entries) {
                changeMade = entry.value.updateArrows(grouped.getOrElse(entry.key, { listOf() })) || changeMade
            }
        }
        return changeMade
    }

    /**
     * Updates the [ViewScoresEntry]s which have a round with the arrowCounts that share their roundId. Passes an empty
     * list if there are no arrowCounts with that roundId. Doesn't call if the [ViewScoresEntry] doesn't have a round
     *
     * @return whether any changes were made
     */
    fun updateArrowCounts(allArrowCounts: List<RoundArrowCount>): Boolean {
        var changeMade = false
        synchronized(this) {
            val grouped = allArrowCounts.groupBy { it.roundId }
            for (groupedEntries in data.values.groupBy { it.round?.roundId }) {
                // Ignore those without a round
                if (groupedEntries.key == null) {
                    continue
                }
                val arrowCounts = grouped[groupedEntries.key] ?: listOf()
                groupedEntries.value.forEach {
                    changeMade = it.updateArrowCounts(arrowCounts) || changeMade
                }
            }
        }
        return changeMade
    }

    /**
     * Updates the [ViewScoresEntry]s which have a round with the distances that share their roundId. Passes an empty
     * list if there are no distances with that roundId. Doesn't call if the [ViewScoresEntry] doesn't have a round
     *
     * @return whether any changes were made
     */
    fun updateDistances(allDistances: List<RoundDistance>): Boolean {
        var changeMade = false
        synchronized(this) {
            val grouped = allDistances.groupBy { it.roundId }
            for (groupedEntries in data.values.groupBy { it.round?.roundId }) {
                // Ignore those without a round
                if (groupedEntries.key == null) {
                    continue
                }
                val distances = grouped[groupedEntries.key] ?: listOf()
                groupedEntries.value.forEach { entry ->
                    changeMade = entry.updateDistances(distances.filter {
                        entry.archerRound.roundSubTypeId ?: 1 == it.subTypeId
                    }) || changeMade
                }
            }
        }
        return changeMade
    }

    /**
     * Sets [ViewScoresEntry.isSelected] to [isSelected] for all items
     * @return the IDs of the [ViewScoresEntry] that changed
     */
    fun setAllSelected(isSelected: Boolean): Set<Int> {
        val changedItems = mutableSetOf<Int>()
        synchronized(this) {
            for (item in data.values) {
                if (item.isSelected != isSelected) {
                    changedItems.add(item.id)
                    item.isSelected = isSelected
                }
            }
        }
        return changedItems
    }
}