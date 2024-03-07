package eywa.projectcodex.common.helpShowcase

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.LayoutCoordinates
import kotlin.reflect.KClass

sealed class HelpShowcaseIntent {
    data class SetScreenSize(val size: Size) : HelpShowcaseIntent()
    data class SetVisibleScreenSize(val id: Int, val boundary: Pair<Offset, Size>) : HelpShowcaseIntent()
    data class Add(val item: HelpShowcaseItem) : HelpShowcaseIntent()

    data class Remove(val key: String) : HelpShowcaseIntent()

    data class UpdateCoordinates(
            val key: String,
            val layoutCoordinates: LayoutCoordinates,
            val id: Int,
    ) : HelpShowcaseIntent()

    object Clear : HelpShowcaseIntent()
    data class SetScreen(val screen: KClass<out ActionBarHelp>) : HelpShowcaseIntent()
}
