package eywa.projectcodex.coaching

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ControlCamera
import androidx.compose.material.icons.filled.VerticalAlignCenter
import androidx.compose.ui.graphics.vector.ImageVector
import eywa.projectcodex.common.utils.ResOrActual

enum class CoachingCrossHairMode(
        val icon: ImageVector,
        val contentDescription: ResOrActual<String>,
) {
    /**
     * Move and rotate a single crosshair to be between the feet
     * so the horizontal line runs through both feet
     * and the vertical line runs through the centre of the body
     */
    SET_FEET(Icons.Default.ControlCamera, ResOrActual.Actual("SET_FEET")),

    /**
     * Move a point up and down the vertical line so that a horizontal line at that point runs through the shoulders
     */
    SET_SHOULDERS(Icons.Default.VerticalAlignCenter, ResOrActual.Actual("SET_SHOULDERS")),
    ;

    fun next() = values().getOrNull(ordinal + 1)
    fun previous() = values().getOrNull(ordinal - 1)
}
