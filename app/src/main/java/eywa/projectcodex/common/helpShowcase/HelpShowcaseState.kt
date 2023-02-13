package eywa.projectcodex.common.helpShowcase

import eywa.projectcodex.common.utils.ResOrActual
import kotlin.reflect.KClass

data class HelpShowcaseState(
        val currentScreen: KClass<out ActionBarHelp>? = null,
        val helpInfoMap: Map<ResOrActual<String>, HelpShowcaseItem> = emptyMap(),

        val currentShowcase: List<ResOrActual<String>>? = null,
        val currentlyDisplayedIndex: Int? = null,

        val startedButNoItems: Boolean = false,
) {
    val isInProgress
        get() = currentShowcase != null && currentlyDisplayedIndex != null
                && currentlyDisplayedIndex in currentShowcase.indices

    val currentItem
        get() = if (!isInProgress) null else helpInfoMap[currentShowcase!![currentlyDisplayedIndex!!]]!!

    val hasNextItem
        get() = currentShowcase != null && currentlyDisplayedIndex != null
                && (currentlyDisplayedIndex + 1) in currentShowcase.indices
}
