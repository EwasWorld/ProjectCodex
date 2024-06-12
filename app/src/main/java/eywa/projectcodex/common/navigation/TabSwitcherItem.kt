package eywa.projectcodex.common.navigation

import eywa.projectcodex.common.sharedUi.helperInterfaces.NamedItem
import eywa.projectcodex.common.utils.ResOrActual

class TabSwitcherItem(
        override val label: ResOrActual<String>,
        val group: TabSwitcherGroup,
        val navRoute: ScreenNavRoute,
        val position: Int,
) : NamedItem

enum class TabSwitcherGroup(
        /**
         * If true, screen state will be saved and restored between navigations
         */
        val saveState: Boolean = false,

        /**
         * If true, when the tab switcher is clicked, all arguments from the current screen will be passed to the new
         * screen
         */
        val passArgs: Boolean = false,
) {
    REFERENCES(saveState = true, passArgs = true),
    ARCHER_INFO,
}
