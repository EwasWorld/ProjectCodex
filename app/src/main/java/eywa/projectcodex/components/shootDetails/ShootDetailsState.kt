package eywa.projectcodex.components.shootDetails

import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.model.Arrow
import eywa.projectcodex.model.FullShootInfo
import eywa.projectcodex.model.ScorePadData

const val DEFAULT_END_SIZE = 6

data class ShootDetailsState(
        val shootId: Int? = null,

        /*
         * Pulled from repos
         */
        val useBetaFeatures: Boolean? = null,
        val use2023System: Boolean? = null,
        val fullShootInfo: FullShootInfo? = null,

        /*
         * Whole-screen state
         */
        val isError: Boolean = false,
        val mainMenuClicked: Boolean = false,
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
) {
    val scorePadData = fullShootInfo?.let {
        ScorePadData(
                info = fullShootInfo,
                endSize = scorePadEndSize,
                goldsType = fullShootInfo.goldsType
        )
    }

    private val selectedEndFirstArrowNumberAndEndSize: Pair<Int, Int>?
        get() {
            if (scorePadSelectedEnd == null || scorePadData == null) return null

            val all = scorePadData.data
                    .filterIsInstance<ScorePadData.ScorePadRow.End>()
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