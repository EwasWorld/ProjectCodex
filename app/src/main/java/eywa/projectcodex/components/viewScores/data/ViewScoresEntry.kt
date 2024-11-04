package eywa.projectcodex.components.viewScores.data

import eywa.projectcodex.common.logging.CustomLogger
import eywa.projectcodex.common.utils.DateTimeFormat
import eywa.projectcodex.components.viewScores.screenUi.ViewScoresDropdownMenuItem
import eywa.projectcodex.components.viewScores.screenUi.ViewScoresEntryRow
import eywa.projectcodex.database.shootData.DatabaseShoot
import eywa.projectcodex.model.FullShootInfo
import eywa.projectcodex.model.GoldsType

/**
 * Stores all the information relating to an [DatabaseShoot] so that it can be displayed in a [ViewScoresEntryRow]
 */
data class ViewScoresEntry(
        val info: FullShootInfo,
        val isSelected: Boolean = false,
        val customLogger: CustomLogger,
) {
    companion object {
        const val LOG_TAG = "ViewScoresEntry"
        val data: List<ViewScoresEntry> = listOf()
    }

    val id = info.shoot.shootId

    fun golds(type: GoldsType? = null) = info.golds(type)
    fun golds(types: List<GoldsType>? = null) = info.golds(types)

    val hitsScoreGolds = listOf(info.hits, info.score, golds(type = null))
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
                                .format(DateTimeFormat.SHORT_DATE_TIME.format(info.shoot.dateShot))
                                + e.message,
                )
                null
            }

    /**
     * true if the entry represents just an arrow count, false if it's a score
     */
    val isCount: Boolean
        get() = info.arrowCounter != null

    fun isRoundComplete(): Boolean =
            when {
                info.h2h != null -> info.h2h.isComplete
                info.roundArrowCounts.isNullOrEmpty() -> false
                else -> info.roundArrowCounts.sumOf { it.arrowCount } == info.arrowsShot
            }

    fun getSingleClickAction() =
            if (isCount) ViewScoresDropdownMenuItem.VIEW
            else ViewScoresDropdownMenuItem.SCORE_PAD

    fun getDropdownMenuItems() =
            when {
                isCount -> listOf(
                        ViewScoresDropdownMenuItem.VIEW,
                        ViewScoresDropdownMenuItem.EMAIL_SCORE,
                        ViewScoresDropdownMenuItem.EDIT_INFO,
                        ViewScoresDropdownMenuItem.DELETE,
                )

                else -> listOf(
                        ViewScoresDropdownMenuItem.SCORE_PAD,
                        ViewScoresDropdownMenuItem.CONTINUE,
                        ViewScoresDropdownMenuItem.EMAIL_SCORE,
                        ViewScoresDropdownMenuItem.EDIT_INFO,
                        ViewScoresDropdownMenuItem.DELETE,
                        ViewScoresDropdownMenuItem.CONVERT,
                )
            }.filter { it.shouldShow?.invoke(this) ?: true }

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
