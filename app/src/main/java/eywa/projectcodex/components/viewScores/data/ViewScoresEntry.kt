package eywa.projectcodex.components.viewScores.data

import android.content.res.Resources
import eywa.projectcodex.CustomLogger
import eywa.projectcodex.R
import eywa.projectcodex.common.archeryObjects.GoldsType
import eywa.projectcodex.common.utils.DateTimeFormat
import eywa.projectcodex.common.utils.resourceStringReplace
import eywa.projectcodex.components.archerRoundScore.Handicap
import eywa.projectcodex.components.archerRoundScore.scorePad.infoTable.ScorePadData
import eywa.projectcodex.components.viewScores.listAdapter.ViewScoresAdapter
import eywa.projectcodex.components.viewScores.listAdapter.ViewScoresEntryViewHolder
import eywa.projectcodex.database.archerRound.ArcherRound
import eywa.projectcodex.database.archerRound.ArcherRoundWithRoundInfoAndName
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.rounds.Round
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance

/**
 * Stores all the information pertaining to an [ArcherRound] so that it can be displayed in a
 * [ViewScoresEntryViewHolder]. Caches calculations like handicaps and totals
 */
class ViewScoresEntry(initialInfo: ArcherRoundWithRoundInfoAndName) {
    companion object {
        const val LOG_TAG = "ViewScoresEntry"
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

    var goldsType = GoldsType.defaultGoldsType
        private set
        get() {
            synchronized(this) {
                field = if (round == null) GoldsType.defaultGoldsType else GoldsType.getGoldsType(round!!)
                return field
            }
        }

    var hitsScoreGolds = ""
        private set
        get() {
            synchronized(this) {
                return "%s/%s/%s".format(hits?.toString() ?: "-", score?.toString() ?: "-", golds?.toString() ?: "-")
            }
        }

    var hits: Int? = null
        private set
        get() {
            synchronized(this) {
                if (field != null) {
                    return field
                }
                if (arrows.isNullOrEmpty()) {
                    return null
                }
                field = arrows!!.count { it.score != 0 }
                return field
            }
        }

    var score: Int? = null
        private set
        get() {
            synchronized(this) {
                if (field != null) {
                    return field
                }
                if (arrows.isNullOrEmpty()) {
                    return null
                }
                field = arrows!!.sumOf { it.score }
                return field
            }
        }

    var golds: Int? = null
        private set
        get() {
            synchronized(this) {
                if (field != null) {
                    return field
                }
                if (arrows.isNullOrEmpty()) {
                    return null
                }
                field = arrows!!.count { goldsType.isGold(it) }
                return field
            }
        }

    var handicap: Int? = null
        private set
        get() {
            synchronized(this) {
                if (field != null) {
                    return field
                }
                if (round == null || arrows.isNullOrEmpty() || arrowCounts.isNullOrEmpty() || distances.isNullOrEmpty()) {
                    return null
                }
                try {
                    field = Handicap.getHandicapForRound(
                            round!!,
                            arrowCounts!!,
                            distances!!,
                            arrows!!.sumOf { it.score },
                            false,
                            arrows!!.size
                    )
                }
                catch (e: IllegalArgumentException) {
                    CustomLogger.customLogger.e(
                            LOG_TAG,
                            "Failed to get handicap for round with id $id (date shot: %s)"
                                    .format(DateTimeFormat.SHORT_DATE_TIME_FORMAT.format(archerRound.dateShot))
                    )
                    CustomLogger.customLogger.e(LOG_TAG, "Handicap Error: " + e.message)
                    return null
                }
                return field
            }
        }

    private var scorePadDataEndSize: Int? = null
    private var scorePadData: ScorePadData? = null

    var isSelected = false
        set(value) {
            field = value
            updatedListener?.onUpdate()
        }

    fun getScorePadData(endSize: Int, resources: Resources): ScorePadData? {
        synchronized(this) {
            if (scorePadData != null && scorePadDataEndSize == endSize) {
                return scorePadData
            }
            if (arrows.isNullOrEmpty()) {
                return null
            }
            val distanceUnit = when {
                round == null -> null
                round!!.isMetric -> resources.getString(R.string.units_meters_short)
                else -> resources.getString(R.string.units_yards_short)
            }
            scorePadData = ScorePadData(arrows!!, endSize, goldsType, resources, arrowCounts, distances, distanceUnit)
            scorePadDataEndSize = endSize
            return scorePadData
        }
    }

    fun getScoreSummary(resources: Resources): String {
        return resourceStringReplace(
                resources.getString(R.string.email_round_summary),
                mapOf(
                        Pair("roundName", displayName ?: resources.getString(R.string.create_round__no_round)),
                        Pair("date", DateTimeFormat.SHORT_DATE_FORMAT.format(archerRound.dateShot)),
                        Pair("hits", hits.toString()),
                        Pair("score", score.toString()),
                        Pair("goldsType", resources.getString(goldsType.longStringId)),
                        Pair("golds", golds.toString()),
                )
        )
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

    fun updateArcherRound(info: ArcherRoundWithRoundInfoAndName): Boolean {
        synchronized(this) {
            if (archerRound.archerRoundId != info.archerRound.archerRoundId) {
                return false
            }
            if (archerRound == info.archerRound && round == info.round && displayName == info.displayName) {
                return false
            }

            archerRound = info.archerRound
            round = info.round
            displayName = info.displayName

            clearCache(true)
        }
        updatedListener?.onUpdate()
        return true
    }

    fun updateArrows(allArrows: List<ArrowValue>): Boolean {
        synchronized(this) {
            val incoming = allArrows.filter { it.archerRoundId == archerRound.archerRoundId }
            if (incoming == arrows) {
                return false
            }
            arrows = incoming

            clearCache(true)
        }
        updatedListener?.onUpdate()
        return true
    }

    fun updateArrowCounts(allArrowCounts: List<RoundArrowCount>): Boolean {
        synchronized(this) {
            if (round == null) {
                return false
            }
            val incoming = allArrowCounts.filter { it.roundId == round!!.roundId }
            if (incoming == arrowCounts) {
                return false
            }
            arrowCounts = incoming
            clearCache(false)
        }
        updatedListener?.onUpdate()
        return true
    }

    fun updateDistances(allDistances: List<RoundDistance>): Boolean {
        synchronized(this) {
            if (round == null) {
                return false
            }
            val incoming = allDistances.filter {
                it.roundId == round!!.roundId
                        && (archerRound.roundSubTypeId == null || archerRound.roundSubTypeId == it.subTypeId)
            }
            if (incoming == distances) {
                return false
            }
            distances = incoming
            clearCache(false)
        }
        updatedListener?.onUpdate()
        return true
    }

    private fun clearCache(clearHitsScoreGolds: Boolean) {
        if (clearHitsScoreGolds) {
            hits = null
            score = null
            golds = null
        }

        handicap = null
        scorePadData = null
        scorePadDataEndSize = null
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