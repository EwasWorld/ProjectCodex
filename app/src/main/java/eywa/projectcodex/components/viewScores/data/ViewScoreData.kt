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
class ViewScoreData {
    companion object {
        private var INSTANCE: ViewScoreData? = null

        fun getViewScoreData(): ViewScoreData {
            return synchronized(ViewScoreData::class) {
                val newInstance = INSTANCE ?: ViewScoreData()
                INSTANCE = newInstance
                newInstance
            }
        }

        @VisibleForTesting(otherwise = VisibleForTesting.NONE)
        fun clearInstance() {
            INSTANCE = null
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
                    currentData.updateArcherRound(archerRound)
                }
            }
        }
        return changeMade
    }

    /**
     * Updates the [ViewScoresEntry]s with the arrows that share their archerRoundId. Passes an empty list if there are
     * no arrows with that id
     */
    fun updateArrows(allArrows: List<ArrowValue>) {
        synchronized(this) {
            val grouped = allArrows.groupBy { it.archerRoundId }
            for (entry in data.entries) {
                entry.value.updateArrows(grouped.getOrElse(entry.key, { listOf() }))
            }
        }
    }

    /**
     * Updates the [ViewScoresEntry]s which have a round with the arrowCounts that share their roundId. Passes an empty
     * list if there are no arrowCounts with that roundId. Doesn't call if the [ViewScoresEntry] doesn't have a round
     */
    fun updateArrowCounts(allArrowCounts: List<RoundArrowCount>) {
        synchronized(this) {
            val grouped = allArrowCounts.groupBy { it.roundId }
            for (groupedEntries in data.values.groupBy { it.round?.roundId }) {
                if (groupedEntries.key == null) {
                    continue
                }
                val arrowCounts = grouped[groupedEntries.key] ?: listOf()
                groupedEntries.value.forEach {
                    it.updateArrowCounts(arrowCounts)
                }
            }
        }
    }

    /**
     * Updates the [ViewScoresEntry]s which have a round with the distances that share their roundId. Passes an empty
     * list if there are no distances with that roundId. Doesn't call if the [ViewScoresEntry] doesn't have a round
     */
    fun updateDistances(allDistances: List<RoundDistance>) {
        synchronized(this) {
            val grouped = allDistances.groupBy { it.roundId }
            for (groupedEntries in data.values.groupBy { it.round?.roundId }) {
                if (groupedEntries.key == null) {
                    continue
                }
                val distances = grouped[groupedEntries.key] ?: listOf()
                groupedEntries.value.forEach {
                    it.updateDistances(distances)
                }
            }
        }
    }
}