package eywa.projectcodex.common.helpShowcase

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.Dp

/**
 * @param priority Lower number = higher priority
 */
data class HelpShowcaseItem(
        val helpTitle: String,
        internal val helpBody: String,
        internal val shapePadding: Dp? = null,
        val priority: Int? = DEFAULT_HELP_PRIORITY,
        private val shape: HelpShowcaseShape = HelpShowcaseShape.OVAL,
        val layoutCoordinates: Map<Int, LayoutCoordinates> = mapOf(),
        val boundsId: Int? = null,
) {
    fun firstVisible(
            currentScreenSize: Size,
            boundaries: Map<Int, Pair<Offset, Size>>,
    ) =
            layoutCoordinates.entries
                    .sortedBy { it.key }
                    .map { it.value }
                    .firstOrNull {
                        if (!it.isAttached) return@firstOrNull false
                        val screen = boundsId?.let { b -> boundaries[b] } ?: (Offset.Zero to currentScreenSize)

                        val topLeft = it.positionInRoot()
                        val (w, h) = it.size
                        val bottomRight = topLeft.plus(Offset(w.toFloat(), h.toFloat()))

                        topLeft in screen && bottomRight in screen
                    }

    @Composable
    fun asShape(
            boundaries: Map<Int, Pair<Offset, Size>>,
            hasNextItem: Boolean,
            goToNextItemListener: () -> Unit,
            endShowcaseListener: () -> Unit,
            screenSize: Size,
    ) = shape.asState(
            boundaries = boundaries,
            item = this,
            hasNextItem = hasNextItem,
            goToNextItemListener = goToNextItemListener,
            endShowcaseListener = endShowcaseListener,
            screenSize = screenSize,
    )
}
