package eywa.projectcodex.components.viewRounds

import eywa.projectcodex.components.archerRoundScore.Handicap
import eywa.projectcodex.components.archeryObjects.GoldsType
import eywa.projectcodex.database.archerRound.ArcherRound
import eywa.projectcodex.database.archerRound.ArcherRoundWithRoundInfoAndName
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance

/**
 * Stores all the information pertaining to an [ArcherRound] so that it can be displayed in a
 * [ViewScoresEntryViewHolder]
 */
class ViewScoresEntry(initialInfo: ArcherRoundWithRoundInfoAndName) {
    companion object {
        private const val LOG_TAG = "ViewScoresEntry"
        private val defaultGoldsType = GoldsType.TENS
        val data: List<ViewScoresEntry> = listOf()
    }

    var archerRound: ArcherRound
        private set
    var round: Round? = null
        private set
    var displayName: String? = null
        private set
    var arrows: List<ArrowValue>? = null
        private set
    private var arrowCounts: List<RoundArrowCount>? = null
    private var distances: List<RoundDistance>? = null

    var hitsScoreGolds: String? = null
        private set
        get() {
            if (field != null) {
                return field
            }
            synchronized(this) {
                if (arrows.isNullOrEmpty()) {
                    return "0/0/0"
                }
                val hits = arrows!!.count { it.score != 0 }
                val score = arrows!!.sumOf { it.score }
                val goldsType = if (round == null) defaultGoldsType else GoldsType.getGoldsType(round!!)
                val golds = arrows!!.count { goldsType.isGold(it) }

                field = "%d/%d/%d".format(hits, score, golds)
                return field
            }
        }

    var handicap: Int? = null
        private set
        get() {
            if (field != null) {
                return field
            }
            synchronized(this) {
                if (round == null || arrows.isNullOrEmpty() || arrowCounts.isNullOrEmpty() || distances.isNullOrEmpty()) {
                    return null
                }
                field = Handicap.getHandicapForRound(
                        round!!,
                        arrowCounts!!,
                        distances!!,
                        arrows!!.sumOf { it.score },
                        false,
                        null
                )
                return field
            }
        }

    var updatedListener: UpdatedListener? = null
    val id: Int
        get() = archerRound.archerRoundId

    init {
        archerRound = initialInfo.archerRound
        round = initialInfo.round
        displayName = initialInfo.displayName
    }

    fun getType(): ViewScoresAdapter.ViewScoresEntryType {
        return ViewScoresAdapter.ViewScoresEntryType.ROUND
    }

    fun updateArcherRound(info: ArcherRoundWithRoundInfoAndName) {
        synchronized(this) {
            if (archerRound.archerRoundId != info.archerRound.archerRoundId) {
                return
            }
            if (archerRound == info.archerRound && round == info.round && displayName == info.displayName) {
                return
            }

            archerRound = info.archerRound
            round = info.round
            displayName = info.displayName

            hitsScoreGolds = null
            handicap = null
        }
        updatedListener?.onUpdate()
    }

    fun updateArrows(allArrows: List<ArrowValue>) {
        synchronized(this) {
            val incoming = allArrows.filter { it.archerRoundId == archerRound.archerRoundId }
            if (incoming == arrows) {
                return
            }
            arrows = incoming
            hitsScoreGolds = null
            handicap = null
        }
        updatedListener?.onUpdate()
    }

    fun updateArrowCounts(allArrowCounts: List<RoundArrowCount>) {
        synchronized(this) {
            if (round == null) {
                return
            }
            val incoming = allArrowCounts.filter { it.roundId == round!!.roundId }
            if (incoming == arrowCounts) {
                return
            }
            arrowCounts = incoming
            handicap = null
        }
        updatedListener?.onUpdate()
    }

    fun updateDistances(allDistances: List<RoundDistance>) {
        synchronized(this) {
            if (round == null) {
                return
            }
            val incoming = allDistances.filter {
                it.roundId == round!!.roundId
                        && (archerRound.roundSubTypeId == null || archerRound.roundSubTypeId == it.subTypeId)
            }
            if (incoming == distances) {
                return
            }
            distances = incoming
            handicap = null
        }
        updatedListener?.onUpdate()
    }

    fun isRoundComplete(): Boolean {
        synchronized(this) {
            if (arrowCounts.isNullOrEmpty() || arrows.isNullOrEmpty()) {
                return false
            }
            if (arrowCounts!!.sumOf { it.arrowCount } <= arrows!!.count()) {
                return true
            }
            return false
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other !is ViewScoresEntry) return false
        val isEqual: Boolean
        synchronized(this) {
            isEqual = (this.archerRound == other.archerRound
                    && this.round == other.round && this.displayName == other.displayName
                    && this.arrows == other.arrows && this.arrowCounts == other.arrowCounts
                    && this.distances == other.distances)
        }
        return isEqual
    }

    override fun hashCode(): Int {
        var result: Int
        synchronized(this) {
            result = archerRound.hashCode()
            result = 31 * result + (round?.hashCode() ?: 0)
            result = 31 * result + (displayName?.hashCode() ?: 0)
            result = 31 * result + (arrows?.hashCode() ?: 0)
            result = 31 * result + (arrowCounts?.hashCode() ?: 0)
            result = 31 * result + (distances?.hashCode() ?: 0)
            result = 31 * result + (updatedListener?.hashCode() ?: 0)
        }
        return result
    }


    interface UpdatedListener {
        fun onUpdate()
    }
}