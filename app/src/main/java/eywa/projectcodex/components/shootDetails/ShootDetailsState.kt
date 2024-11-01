package eywa.projectcodex.components.shootDetails

import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.utils.classificationTables.model.Classification
import eywa.projectcodex.database.archer.DatabaseArcher
import eywa.projectcodex.database.archer.DatabaseArcherHandicap
import eywa.projectcodex.database.bow.DatabaseBow
import eywa.projectcodex.database.rounds.FullRoundInfo
import eywa.projectcodex.database.shootData.DatabaseShootShortRecord
import eywa.projectcodex.model.Arrow
import eywa.projectcodex.model.FullShootInfo
import eywa.projectcodex.model.SightMark
import eywa.projectcodex.model.scorePadData.ScorePadData
import eywa.projectcodex.model.scorePadData.ScorePadRow

const val DEFAULT_END_SIZE = 6

data class ShootDetailsState(
        val shootId: Int,

        /*
         * Pulled from repos
         */
        val useBetaFeatures: Boolean? = null,
        val use2023System: Boolean? = null,
        val fullShootInfo: FullShootInfo? = null,
        val archerHandicaps: List<DatabaseArcherHandicap>? = null,
        val archerInfo: DatabaseArcher? = null,
        val bow: DatabaseBow? = null,
        val wa1440FullRoundInfo: FullRoundInfo? = null,
        val classification: Classification? = null,
        val roundPbs: List<DatabaseShootShortRecord>? = null,
        val pastRoundRecords: List<DatabaseShootShortRecord>? = null,
        val sightMark: SightMark? = null,

        /*
         * Whole-screen state
         */
        val isError: Boolean = false,
        val mainMenuClicked: Boolean = false,
        val backClicked: Boolean = false,
        val navBarClickedItem: CodexNavRoute? = null,

        /*
         * Score pad state
         */
        val scorePadSelectedEnd: Int? = null,
        val scorePadEndSize: Int = DEFAULT_END_SIZE,

        /*
         * Add end state
         */
        val addEndArrows: List<Arrow> = emptyList(),
        val addEndSize: Int = DEFAULT_END_SIZE,

        /*
         * Stats state
         */
        val useSimpleView: Boolean = true,
) {
    val scorePadData = fullShootInfo?.let {
        ScorePadData(
                info = fullShootInfo,
                endSize = scorePadEndSize,
                goldsTypes = fullShootInfo.goldsTypes
        )
    }

    private val selectedEndFirstArrowNumberAndEndSize: Pair<Int, Int>?
        get() {
            if (scorePadSelectedEnd == null || scorePadData == null) return null

            val all = scorePadData.data
                    .filterIsInstance<ScorePadRow.End>()
                    .sortedBy { it.endNumber }
            if (all.isEmpty()) return null

            val endIndex = scorePadSelectedEnd - 1
            if (endIndex !in all.indices) return null

            return all.take(endIndex)
                    .sumOf { it.arrowScores.size }
                    // +1 because arrowNumbers are 1-indexed
                    .plus(1) to all[endIndex].arrowScores.size
        }

    val firstArrowNumberInSelectedEnd
        get() = selectedEndFirstArrowNumberAndEndSize?.first
    val selectedEndSize
        get() = selectedEndFirstArrowNumberAndEndSize?.second
}
