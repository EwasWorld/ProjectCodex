package eywa.projectcodex.common.helpShowcase

import androidx.annotation.StringRes
import androidx.compose.ui.layout.LayoutCoordinates
import eywa.projectcodex.common.utils.ResOrActual
import kotlin.reflect.KClass

sealed class HelpShowcaseIntent {
    data class Add(val item: HelpShowcaseItem) : HelpShowcaseIntent()
    data class AddDynamicInfo(val info: DynamicHelpShowcaseInfo) : HelpShowcaseIntent()

    data class Remove(
            val key: ResOrActual<String>
    ) : HelpShowcaseIntent() {
        constructor(@StringRes key: Int) : this(ResOrActual.StringResource(key))
        constructor(key: String) : this(ResOrActual.Actual(key))
    }

    data class UpdateCoordinates(
            val key: ResOrActual<String>,
            val layoutCoordinates: LayoutCoordinates,
    ) : HelpShowcaseIntent() {
        constructor(@StringRes key: Int, layoutCoordinates: LayoutCoordinates)
                : this(ResOrActual.StringResource(key), layoutCoordinates)

        constructor(key: String, layoutCoordinates: LayoutCoordinates)
                : this(ResOrActual.Actual(key), layoutCoordinates)
    }

    object Clear : HelpShowcaseIntent()
    data class SetScreen(val screen: KClass<out ActionBarHelp>) : HelpShowcaseIntent()
}
