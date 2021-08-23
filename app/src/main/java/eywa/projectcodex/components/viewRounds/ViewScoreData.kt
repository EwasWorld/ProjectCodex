package eywa.projectcodex.components.viewRounds

import eywa.projectcodex.database.archerRound.ArcherRoundWithRoundInfoAndName
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance

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
    }

    private var data = mutableMapOf<Int, ViewScoresEntry>()

    fun getData(): List<ViewScoresEntry> {
        synchronized(this) {
            return data.values.sortedByDescending { it.archerRound.dateShot }
        }
    }

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

    fun updateArrows(allArrows: List<ArrowValue>) {
        synchronized(this) {
            for (arrowGroup in allArrows.groupBy { it.archerRoundId }) {
                data[arrowGroup.key]?.updateArrows(arrowGroup.value)
            }
        }
    }

    fun updateArrowCounts(allArrowCounts: List<RoundArrowCount>) {
        synchronized(this) {
            val grouped = allArrowCounts.groupBy { it.roundId }
            for (groupedEntries in data.values.groupBy { it.round?.roundId }) {
                if (groupedEntries.key == null) {
                    continue
                }
                grouped[groupedEntries.key]?.let { arrowCounts ->
                    groupedEntries.value.forEach {
                        it.updateArrowCounts(arrowCounts)
                    }
                }
            }
        }
    }

    fun updateDistances(allDistances: List<RoundDistance>) {
        synchronized(this) {
            val grouped = allDistances.groupBy { it.roundId }
            for (groupedEntries in data.values.groupBy { it.round?.roundId }) {
                if (groupedEntries.key == null) {
                    continue
                }
                grouped[groupedEntries.key]?.let { distances ->
                    groupedEntries.value.forEach {
                        it.updateDistances(distances)
                    }
                }
            }
        }
    }
}