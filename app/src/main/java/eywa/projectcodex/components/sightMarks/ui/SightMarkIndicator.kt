package eywa.projectcodex.components.sightMarks.ui

import androidx.compose.ui.layout.Placeable

interface SightMarkIndicator {
    val width: Int
    val height: Int
    val originalCentreOffset: Float
    val placeable: Placeable
    fun isLeft(): Boolean
}
