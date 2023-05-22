package eywa.projectcodex.components.sightMarks.diagram

import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.Placeable

interface SightMarksDiagramIndicator {
    val originalCentreOffset: Float
    val sightMark: Float

    var isLeft: Boolean
    fun indicatorPlaceable(): Placeable
    fun width(): Int
    fun height(): Int

    var horizontalLine1Measurable: Measurable?
    var horizontalLine2Measurable: Measurable?
    var verticalLineMeasurable: Measurable?
    var horizontalLine1Placeable: Placeable?
    var horizontalLine2Placeable: Placeable?
    var verticalLinePlaceable: Placeable?

    fun getChevron(isLeft: Boolean): Placeable
    fun getPadding(): Float

    /**
     * Lower is higher priority
     */
    fun getPlacePriority(): Int
}
