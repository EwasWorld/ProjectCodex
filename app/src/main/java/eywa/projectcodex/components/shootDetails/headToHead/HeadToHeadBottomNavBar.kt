package eywa.projectcodex.components.shootDetails.headToHead

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import eywa.projectcodex.R
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.sharedUi.CodexIconInfo
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.components.shootDetails.commonUi.ShootDetailsBottomNavBar
import eywa.projectcodex.components.shootDetails.commonUi.ShootDetailsBottomNavBarItem

enum class HeadToHeadBottomNavBarItem(
        override val navRoute: CodexNavRoute,
        override val notSelectedIcon: CodexIconInfo,
        override val selectedIcon: CodexIconInfo? = null,
        override val label: ResOrActual<String>,
) : ShootDetailsBottomNavBarItem {
    ADD_END(
            navRoute = CodexNavRoute.HEAD_TO_HEAD_ADD_END,
            notSelectedIcon = CodexIconInfo.PainterIcon(R.drawable.ic_add_box_outline),
            selectedIcon = CodexIconInfo.PainterIcon(R.drawable.ic_add_box_filled),
            label = ResOrActual.StringResource(R.string.head_to_head_add_end__title),
    ) {
        override fun shouldShow(currentScreen: CodexNavRoute): Boolean =
                currentScreen != CodexNavRoute.HEAD_TO_HEAD_ADD_MATCH
    },
    ADD_MATCH(
            navRoute = CodexNavRoute.HEAD_TO_HEAD_ADD_MATCH,
            notSelectedIcon = CodexIconInfo.PainterIcon(R.drawable.ic_add_box_outline),
            selectedIcon = CodexIconInfo.PainterIcon(R.drawable.ic_add_box_filled),
            label = ResOrActual.StringResource(R.string.head_to_head_add_end__title),
    ) {
        override fun shouldShow(currentScreen: CodexNavRoute): Boolean =
                currentScreen == CodexNavRoute.HEAD_TO_HEAD_ADD_MATCH
    },
    SCORE_PAD(
            navRoute = CodexNavRoute.HEAD_TO_HEAD_SCORE_PAD,
            notSelectedIcon = CodexIconInfo.PainterIcon(R.drawable.ic_assignment_outline),
            selectedIcon = CodexIconInfo.PainterIcon(R.drawable.ic_assignment_filled),
            label = ResOrActual.StringResource(R.string.score_pad__title),
    ),
    STATS(
            navRoute = CodexNavRoute.HEAD_TO_HEAD_STATS,
            notSelectedIcon = CodexIconInfo.PainterIcon(R.drawable.ic_chart_outline),
            selectedIcon = CodexIconInfo.PainterIcon(R.drawable.ic_chart_filled),
            label = ResOrActual.StringResource(R.string.archer_round_stats__nav_bar_title),
    ),
//    SETTINGS(
//            navRoute = CodexNavRoute.SHOOT_DETAILS_SETTINGS,
//            notSelectedIcon = CodexIconInfo.VectorIcon(Icons.Outlined.Settings),
//            selectedIcon = CodexIconInfo.VectorIcon(Icons.Filled.Settings),
//            label = ResOrActual.StringResource(R.string.archer_round_settings__title),
//    ),
    ;

    override val screenName: String
        get() = "HEAD_TO_HEAD_NAV_BAR_ITEM"

    override fun getElement(): String = name

    companion object {
        private val backwards = entries.associateBy { it.navRoute }

        fun isItem(navRoute: CodexNavRoute) = backwards.containsKey(navRoute)
    }
}

@Preview
@Composable
fun HeadToHeadBottomNavBar_Preview() {
    CodexTheme {
        ShootDetailsBottomNavBar(
                currentScreen = CodexNavRoute.HEAD_TO_HEAD_ADD_END,
                items = HeadToHeadBottomNavBarItem.entries,
                listener = {},
        )
    }
}

@Preview
@Composable
fun Match_HeadToHeadBottomNavBar_Preview() {
    CodexTheme {
        ShootDetailsBottomNavBar(
                currentScreen = CodexNavRoute.HEAD_TO_HEAD_ADD_MATCH,
                items = HeadToHeadBottomNavBarItem.entries,
                listener = {},
        )
    }
}
