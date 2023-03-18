package eywa.projectcodex.components.viewScores.data

import android.content.res.Resources
import eywa.projectcodex.R
import eywa.projectcodex.common.archeryObjects.FullArcherRoundInfo
import eywa.projectcodex.common.archeryObjects.GoldsType
import eywa.projectcodex.common.logging.CustomLogger
import eywa.projectcodex.common.utils.DateTimeFormat
import eywa.projectcodex.components.archerRoundScore.Handicap
import eywa.projectcodex.components.archerRoundScore.scorePad.infoTable.ScorePadDataNew
import eywa.projectcodex.components.viewScores.ui.ViewScoresEntryRow
import eywa.projectcodex.components.viewScores.utils.ViewScoresDropdownMenuItem
import eywa.projectcodex.database.archerRound.ArcherRound

/**
 * Stores all the information relating to an [ArcherRound] so that it can be displayed in a [ViewScoresEntryRow]
 */
data class ViewScoresEntry(
        val info: FullArcherRoundInfo,
        val isSelected: Boolean = false,
        val customLogger: CustomLogger,
) {
    companion object {
        const val LOG_TAG = "ViewScoresEntry"
        val data: List<ViewScoresEntry> = listOf()
    }

    val id = info.archerRound.archerRoundId

    val goldsType = if (info.round == null) GoldsType.defaultGoldsType else GoldsType.getGoldsType(info.round)
    val hits = info.arrows.takeIf { !it.isNullOrEmpty() }
            ?.let { arrowValues -> arrowValues.count { it.score != 0 } }
    val score = info.arrows.takeIf { !it.isNullOrEmpty() }
            ?.let { arrowValues -> arrowValues.sumOf { it.score } }
    val golds = info.arrows.takeIf { !it.isNullOrEmpty() }
            ?.let { arrowValues -> arrowValues.count { goldsType.isGold(it) } }

    val hitsScoreGolds = listOf(hits, score, golds)
            .takeIf { list -> list.all { it != null } }
            ?.joinToString("/")

    val handicap =
            if (
                info.round == null || info.arrows.isNullOrEmpty() || info.roundArrowCounts.isNullOrEmpty()
                || info.roundDistances.isNullOrEmpty()
            ) {
                null
            }
            else {
                try {
                    Handicap.getHandicapForRound(
                            round = info.round,
                            roundArrowCounts = info.roundArrowCounts,
                            roundDistances = info.roundDistances,
                            score = info.arrows.sumOf { it.score },
                            innerTenArcher = false,
                            arrows = info.arrows.size
                    )
                }
                catch (e: IllegalArgumentException) {
                    customLogger.e(
                            LOG_TAG,
                            "Failed to get handicap for round with id $id (date shot: %s), reason: "
                                    .format(DateTimeFormat.SHORT_DATE_TIME.format(info.archerRound.dateShot))
                                    + e.message
                    )
                    null
                }
            }

    fun getScorePadData(endSize: Int): ScorePadDataNew? {
        if (info.arrows.isNullOrEmpty()) {
            return null
        }
        return ScorePadDataNew(info, endSize, goldsType)
    }

    fun getScoreSummary(resources: Resources): String =
            if (info.arrowsShot > 0) {
                val res = resources.getString(R.string.create_round__no_round)
                resources.getString(
                        R.string.email_round_summary,
                        info.displayName ?: res,
                        DateTimeFormat.SHORT_DATE.format(info.archerRound.dateShot),
                        hits,
                        score,
                        resources.getString(goldsType.longStringId),
                        golds,
                )
            }
            else {
                resources.getString(
                        R.string.email_round_summary_no_arrows,
                        info.displayName ?: resources.getString(R.string.create_round__no_round),
                        DateTimeFormat.SHORT_DATE.format(info.archerRound.dateShot),
                )
            }

    fun isRoundComplete(): Boolean {
        if (info.roundArrowCounts.isNullOrEmpty() || info.arrows.isNullOrEmpty()) {
            return false
        }
        if (info.roundArrowCounts.sumOf { it.arrowCount } == info.arrows.count()) {
            return true
        }
        return false
    }

    fun getSingleClickAction() = ViewScoresDropdownMenuItem.SCORE_PAD

    fun getDropdownMenuItems() = listOf(
            ViewScoresDropdownMenuItem.SCORE_PAD,
            ViewScoresDropdownMenuItem.CONTINUE,
            ViewScoresDropdownMenuItem.EMAIL_SCORE,
            ViewScoresDropdownMenuItem.EDIT_INFO,
            ViewScoresDropdownMenuItem.DELETE,
            ViewScoresDropdownMenuItem.CONVERT,
    ).filter { it.shouldShow?.invoke(this) ?: true }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ViewScoresEntry

        if (info != other.info) return false
        if (isSelected != other.isSelected) return false

        return true
    }

    override fun hashCode(): Int {
        var result = info.hashCode()
        result = 31 * result + isSelected.hashCode()
        return result
    }
}
