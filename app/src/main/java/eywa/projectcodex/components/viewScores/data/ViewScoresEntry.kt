package eywa.projectcodex.components.viewScores.data

import android.content.res.Resources
import eywa.projectcodex.CustomLogger
import eywa.projectcodex.R
import eywa.projectcodex.common.archeryObjects.GoldsType
import eywa.projectcodex.common.utils.DateTimeFormat
import eywa.projectcodex.common.utils.resourceStringReplace
import eywa.projectcodex.components.archerRoundScore.Handicap
import eywa.projectcodex.components.archerRoundScore.scorePad.infoTable.ScorePadData
import eywa.projectcodex.components.viewScores.ui.ViewScoresEntryRow
import eywa.projectcodex.database.archerRound.ArcherRound
import eywa.projectcodex.database.archerRound.ArcherRoundWithRoundInfoAndName
import eywa.projectcodex.database.arrowValue.ArrowValue
import eywa.projectcodex.database.rounds.RoundArrowCount
import eywa.projectcodex.database.rounds.RoundDistance

/**
 * Stores all the information relating to an [ArcherRound] so that it can be displayed in a [ViewScoresEntryRow]
 */
data class ViewScoresEntry(
        private val initialInfo: ArcherRoundWithRoundInfoAndName,
        val arrows: List<ArrowValue>? = null,
        private val arrowCounts: List<RoundArrowCount>? = null,
        private val distances: List<RoundDistance>? = null,
        val isSelected: Boolean = false
) {
    companion object {
        const val LOG_TAG = "ViewScoresEntry"
        val data: List<ViewScoresEntry> = listOf()
    }

    val archerRound = initialInfo.archerRound
    val round = initialInfo.round
    val displayName = initialInfo.displayName

    val id = archerRound.archerRoundId

    init {
        require(arrows?.all { it.archerRoundId == id } != false) { "Arrows mismatched id" }
        require(arrowCounts?.all { it.roundId == round?.roundId } != false) { "Arrow counts mismatched id" }
        require(
                distances?.all {
                    it.roundId == round?.roundId && it.subTypeId == (archerRound.roundSubTypeId ?: 1)
                } != false
        ) { "Distances mismatched id" }
    }

    val goldsType = if (round == null) GoldsType.defaultGoldsType else GoldsType.getGoldsType(round)
    val hits = arrows.takeIf { !it.isNullOrEmpty() }
            ?.let { arrowValues -> arrowValues.count { it.score != 0 } }
    val score = arrows.takeIf { !it.isNullOrEmpty() }
            ?.let { arrowValues -> arrowValues.sumOf { it.score } }
    val golds = arrows.takeIf { !it.isNullOrEmpty() }
            ?.let { arrowValues -> arrowValues.count { goldsType.isGold(it) } }

    val hitsScoreGolds = listOf(hits, score, golds)
            .takeIf { list -> list.all { it != null } }
            ?.joinToString("/")

    val handicap =
            if (round == null || arrows.isNullOrEmpty() || arrowCounts.isNullOrEmpty() || distances.isNullOrEmpty()) {
                null
            }
            else {
                try {
                    Handicap.getHandicapForRound(
                            round, arrowCounts, distances, arrows.sumOf { it.score }, false, arrows.size
                    )
                }
                catch (e: IllegalArgumentException) {
                    CustomLogger.customLogger.e(
                            LOG_TAG,
                            "Failed to get handicap for round with id $id (date shot: %s), reason: "
                                    .format(DateTimeFormat.SHORT_DATE_TIME.format(archerRound.dateShot))
                                    + e.message
                    )
                    CustomLogger.customLogger.e(LOG_TAG, "Handicap Error: " + e.message)
                    null
                }
            }

    fun getScorePadData(endSize: Int, resources: Resources): ScorePadData? {
        if (arrows.isNullOrEmpty()) {
            return null
        }
        val distanceUnit = when {
            round == null -> null
            round.isMetric -> resources.getString(R.string.units_meters_short)
            else -> resources.getString(R.string.units_yards_short)
        }
        return ScorePadData(arrows, endSize, goldsType, resources, arrowCounts, distances, distanceUnit)
    }

    fun getScoreSummary(resources: Resources): String {
        return resourceStringReplace(
                resources.getString(R.string.email_round_summary),
                mapOf(
                        Pair("roundName", displayName ?: resources.getString(R.string.create_round__no_round)),
                        Pair("date", DateTimeFormat.SHORT_DATE.format(archerRound.dateShot)),
                        Pair("hits", hits.toString()),
                        Pair("score", score.toString()),
                        Pair("goldsType", resources.getString(goldsType.longStringId)),
                        Pair("golds", golds.toString()),
                )
        )
    }

    fun isRoundComplete(): Boolean {
        synchronized(this) {
            if (arrowCounts.isNullOrEmpty() || arrows.isNullOrEmpty()) {
                return false
            }
            if (arrowCounts.sumOf { it.arrowCount } <= arrows.count()) {
                return true
            }
            return false
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ViewScoresEntry

        if (initialInfo != other.initialInfo) return false
        if (arrows != other.arrows) return false
        if (arrowCounts != other.arrowCounts) return false
        if (distances != other.distances) return false
        if (isSelected != other.isSelected) return false

        return true
    }

    override fun hashCode(): Int {
        var result = initialInfo.hashCode()
        result = 31 * result + (arrows?.hashCode() ?: 0)
        result = 31 * result + (arrowCounts?.hashCode() ?: 0)
        result = 31 * result + (distances?.hashCode() ?: 0)
        result = 31 * result + isSelected.hashCode()
        return result
    }
}