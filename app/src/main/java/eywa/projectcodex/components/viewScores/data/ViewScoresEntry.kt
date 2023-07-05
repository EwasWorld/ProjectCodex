package eywa.projectcodex.components.viewScores.data

import eywa.projectcodex.common.archeryObjects.FullArcherRoundInfo
import eywa.projectcodex.common.logging.CustomLogger
import eywa.projectcodex.common.utils.DateTimeFormat
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

    val golds = info.golds()

    val hitsScoreGolds = listOf(info.hits, info.score, golds)
            .takeIf { info.arrowsShot > 0 }
            ?.joinToString("/")

    val handicap =
            try {
                info.handicap
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
