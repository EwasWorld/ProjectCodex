package eywa.projectcodex.components.shootDetails.commonUi

import androidx.compose.material.BottomNavigation
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import eywa.projectcodex.R
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.sharedUi.CodexBottomNavItem
import eywa.projectcodex.common.sharedUi.CodexIconInfo
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.sharedUi.testTag
import eywa.projectcodex.common.utils.CodexTestTag
import eywa.projectcodex.common.utils.ResOrActual

@Composable
fun ShootDetailsBottomNavBar(
        currentScreen: CodexNavRoute,
        items: List<ShootDetailsBottomNavBarItem>,
        listener: (CodexNavRoute) -> Unit,
        modifier: Modifier = Modifier,
) {
    BottomNavigation(
            backgroundColor = CodexTheme.colors.bottomNavBar,
            contentColor = CodexTheme.colors.onBottomNavBar,
            modifier = modifier
    ) {
        items.forEach {
            if (it.shouldShow(currentScreen)) {
                CodexBottomNavItem(
                        icon = it.notSelectedIcon,
                        selectedIcon = it.selectedIcon ?: it.notSelectedIcon,
                        label = it.label.get(),
                        contentDescription = it.label.get(),
                        isCurrentDestination = currentScreen == it.navRoute,
                        modifier = Modifier.testTag(it),
                        onClick = { listener(it.navRoute) },
                )
            }
        }
    }
}

interface ShootDetailsBottomNavBarItem : CodexTestTag {
    val navRoute: CodexNavRoute
    val notSelectedIcon: CodexIconInfo
    val selectedIcon: CodexIconInfo?
    val label: ResOrActual<String>

    fun shouldShow(currentScreen: CodexNavRoute): Boolean = true
}

enum class StandardBottomNavBarItem(
        override val navRoute: CodexNavRoute,
        override val notSelectedIcon: CodexIconInfo,
        override val selectedIcon: CodexIconInfo? = null,
        override val label: ResOrActual<String>,
) : ShootDetailsBottomNavBarItem {
    ADD_END(
            navRoute = CodexNavRoute.SHOOT_DETAILS_ADD_END,
            notSelectedIcon = CodexIconInfo.PainterIcon(R.drawable.ic_add_box_outline),
            selectedIcon = CodexIconInfo.PainterIcon(R.drawable.ic_add_box_filled),
            label = ResOrActual.StringResource(R.string.input_end__title),
    ),
    SCORE_PAD(
            navRoute = CodexNavRoute.SHOOT_DETAILS_SCORE_PAD,
            notSelectedIcon = CodexIconInfo.PainterIcon(R.drawable.ic_assignment_outline),
            selectedIcon = CodexIconInfo.PainterIcon(R.drawable.ic_assignment_filled),
            label = ResOrActual.StringResource(R.string.score_pad__title),
    ),
    STATS(
            navRoute = CodexNavRoute.SHOOT_DETAILS_STATS,
            notSelectedIcon = CodexIconInfo.PainterIcon(R.drawable.ic_chart_outline),
            selectedIcon = CodexIconInfo.PainterIcon(R.drawable.ic_chart_filled),
            label = ResOrActual.StringResource(R.string.archer_round_stats__nav_bar_title),
    ),
    SETTINGS(
            navRoute = CodexNavRoute.SHOOT_DETAILS_SETTINGS,
            notSelectedIcon = CodexIconInfo.VectorIcon(Icons.Outlined.Settings),
            selectedIcon = CodexIconInfo.VectorIcon(Icons.Filled.Settings),
            label = ResOrActual.StringResource(R.string.archer_round_settings__title),
    ),
    ;

    override val screenName: String
        get() = "SHOOT_DETAILS_NAV_BAR_ITEM"

    override fun getElement(): String = name

    companion object {
        private val backwards = entries.associateBy { it.navRoute }

        fun isItem(navRoute: CodexNavRoute) = backwards.containsKey(navRoute)
    }
}

@Preview
@Composable
fun ShootDetailsBottomNavBar_Preview() {
    CodexTheme {
        ShootDetailsBottomNavBar(
                currentScreen = CodexNavRoute.SHOOT_DETAILS_SCORE_PAD,
                items = StandardBottomNavBarItem.entries,
                listener = {},
        )
    }
}
