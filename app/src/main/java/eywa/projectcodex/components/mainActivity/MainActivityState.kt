package eywa.projectcodex.components.mainActivity

import eywa.projectcodex.common.helpShowcase.HelpShowcaseItem
import eywa.projectcodex.common.helpShowcase.HelpShowcaseState

data class MainActivityState(
        val helpShowcaseState: HelpShowcaseState? = null,
) {
    val currentHelpItem: HelpShowcaseMainActivityState?
        get() = helpShowcaseState?.currentItem?.let { HelpShowcaseMainActivityState(it, helpShowcaseState.hasNextItem) }
}

data class HelpShowcaseMainActivityState(
        val helpShowcaseItem: HelpShowcaseItem,
        val hasNextItem: Boolean,
)
