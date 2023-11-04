package eywa.projectcodex.common.navigation

import eywa.projectcodex.common.sharedUi.helperInterfaces.NamedItem
import eywa.projectcodex.common.utils.ResOrActual

class TabSwitcherItem(
        override val label: ResOrActual<String>,
        val group: TabSwitcherGroup,
        val navRoute: NavRoute,
        val position: Int,
) : NamedItem

enum class TabSwitcherGroup(
        val saveState: Boolean = false,
) {
    REFERENCES(saveState = true),
    ARCHER_INFO,
}
