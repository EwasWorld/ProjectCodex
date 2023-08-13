package eywa.projectcodex.components.shootDetails

import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.model.Arrow
import eywa.projectcodex.model.FullShootInfo
import eywa.projectcodex.model.ScorePadData

private const val DEFAULT_END_SIZE = 6

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
        val scorePadEndSize: Int = DEFAULT_END_SIZE,
        val scorePadSelectedEnd: Int? = null,

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

            val end = scorePadSelectedEnd - 1
            return all.take(end)
                    .sumOf { it.arrowScores.size }
                    // +1 because arrowNumbers are 1-indexed
                    .plus(1) to all[end].arrowScores.size
        }

    val firstArrowNumberInSelectedEnd
        get() = selectedEndFirstArrowNumberAndEndSize?.first
    val selectedEndSize
        get() = selectedEndFirstArrowNumberAndEndSize?.second
}
