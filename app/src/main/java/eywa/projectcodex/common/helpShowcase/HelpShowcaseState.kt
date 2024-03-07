package eywa.projectcodex.common.helpShowcase

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.core.mainActivity.MainActivity
import kotlin.reflect.KClass

data class HelpShowcaseState(
        val isInProgress: Boolean = false,
        val currentItem: HelpShowcaseItem? = null,
        val hasNextItem: Boolean = false,
        val startedButNoItems: Boolean = false,
        val boundaries: Map<Int, Pair<Offset, Size>> = emptyMap(),
)

internal data class HelpShowcaseInternalState(
        /**
         * The current screen that is displayed. This is updated by [MainActivity] when the current route changes.
         * Have to do it like this rather than the old way of last call to [HelpShowcaseIntent.Add] compose navigation
         * animates transitions between screens, causing the old screen to be recomposed multiple times while
         * fading out. This means that the last call of [HelpShowcaseIntent.Add] is not guaranteed to be the new screen
         */
        val currentScreen: KClass<out ActionBarHelp> = CodexNavRoute.MAIN_MENU::class,

        /**
         * Set of boundaries stored by ID (used to decide whether a help item is visible on the screen).
         */
        val boundaries: Map<Int, Pair<Offset, Size>> = emptyMap(),

        /**
         * Total size of the screen (used to create overlay)
         */
        val screenSize: Size? = null,
        val helpInfoMap: Map<String, HelpShowcaseItem> = emptyMap(),

        /**
         * List of keys in [helpInfoMap]
         */
        val currentShowcase: List<String>? = null,

        /**
         * [currentShowcase] index
         */
        val currentlyDisplayedIndex: Int? = null,

        val startedButNoItems: Boolean = false,
) {
    private val isInProgress
        get() = !currentShowcase.isNullOrEmpty() && currentlyDisplayedIndex != null
                && currentlyDisplayedIndex in currentShowcase.indices

    private val currentItem
        get() =
            if (!isInProgress) null
            else helpInfoMap[currentShowcase!![currentlyDisplayedIndex!!]]

    val nextItemIndex
        get() =
            if (currentShowcase.isNullOrEmpty() || screenSize == null) {
                null
            }
            else {
                val seenItemCount = currentlyDisplayedIndex?.plus(1) ?: 0
                currentShowcase
                        .drop(seenItemCount)
                        .indexOfFirst { title ->
                            helpInfoMap[title]?.firstVisible(screenSize, boundaries) != null
                        }
                        .takeIf { it != -1 }
                        ?.plus(seenItemCount)
            }

    fun asExternalState() = HelpShowcaseState(
            isInProgress = isInProgress,
            currentItem = currentItem,
            hasNextItem = nextItemIndex != null,
            startedButNoItems = startedButNoItems,
            boundaries = boundaries,
    )
}

