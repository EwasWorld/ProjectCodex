package eywa.projectcodex.common.helpShowcase

import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.components.mainActivity.MainActivity
import kotlin.reflect.KClass

data class HelpShowcaseState(
        val isInProgress: Boolean = false,
        val currentItem: HelpShowcaseItem? = null,
        val hasNextItem: Boolean = false,
        val startedButNoItems: Boolean = false,
)

internal data class HelpShowcaseInternalState(
        /**
         * The current screen that is displayed. This is updated by [MainActivity] when the current route changes.
         * Have to do it like this rather than the old way of last call to [HelpShowcaseIntent.Add] compose navigation
         * animates transitions between screens, causing the old screen to be recomposed multiple times while
         * fading out. This means that the last call of [HelpShowcaseIntent.Add] is not guaranteed to be the new screen
         */
        val currentScreen: KClass<out ActionBarHelp> = CodexNavRoute.MAIN_MENU::class,
        val helpInfoMap: Map<ResOrActual<String>, HelpShowcaseItem> = emptyMap(),
        val dynamicHelpShowcaseInfo: DynamicHelpShowcaseInfo? = null,

        val currentShowcase: List<ResOrActual<String>>? = null,
        val currentlyDisplayedIndex: Int? = null,

        val startedButNoItems: Boolean = false,
) {
    private val isInProgress
        get() = currentShowcase != null && currentlyDisplayedIndex != null
                && currentlyDisplayedIndex in currentShowcase.indices

    private val currentItem
        get() =
            if (!isInProgress) null
            else currentShowcase!![currentlyDisplayedIndex!!].let {
                helpInfoMap[it] ?: dynamicHelpShowcaseInfo!!.getInfoShowcases(it)!!
            }

    private val hasNextItem
        get() = currentShowcase != null && currentlyDisplayedIndex != null
                && (currentlyDisplayedIndex + 1) in currentShowcase.indices

    fun asExternalState() = HelpShowcaseState(
            isInProgress = isInProgress,
            currentItem = currentItem,
            hasNextItem = hasNextItem,
            startedButNoItems = startedButNoItems,
    )
}

