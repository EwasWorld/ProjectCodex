package eywa.projectcodex.common.helpShowcase

import androidx.compose.ui.layout.LayoutCoordinates
import kotlin.reflect.KClass

sealed class HelpShowcaseIntent {
    data class Add(val item: HelpShowcaseItem) : HelpShowcaseIntent()
    data class AddDynamicInfo(val info: DynamicHelpShowcaseInfo) : HelpShowcaseIntent()

    data class Remove(val key: String) : HelpShowcaseIntent()

    data class UpdateCoordinates(
            val key: String,
            val layoutCoordinates: LayoutCoordinates,
    ) : HelpShowcaseIntent()

    object Clear : HelpShowcaseIntent()
    data class SetScreen(val screen: KClass<out ActionBarHelp>) : HelpShowcaseIntent()
}
