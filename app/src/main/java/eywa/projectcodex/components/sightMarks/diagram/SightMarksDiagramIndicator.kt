package eywa.projectcodex.components.sightMarks.diagram

import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.Placeable

interface SightMarksDiagramIndicator {
    val width: Int
    val height: Int
    val originalCentreOffset: Float

    val sightMark: Float
    val indicatorPlaceable: Placeable

    var horizontalLine1Measurable: Measurable?
    var horizontalLine2Measurable: Measurable?
    var verticalLineMeasurable: Measurable?
    var horizontalLine1Placeable: Placeable?
    var horizontalLine2Placeable: Placeable?
    var verticalLinePlaceable: Placeable?

    fun isLeft(): Boolean
    fun getChevron(isLeft: Boolean): Placeable
    fun getPadding(): Float

    /**
     * Lower is higher priority
     */
    fun getPlacePriority(): Int
}
