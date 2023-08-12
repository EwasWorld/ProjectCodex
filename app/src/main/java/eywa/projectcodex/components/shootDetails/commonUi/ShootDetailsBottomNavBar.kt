package eywa.projectcodex.components.shootDetails.commonUi

import androidx.annotation.StringRes
import androidx.compose.material.BottomNavigation
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import eywa.projectcodex.R
import eywa.projectcodex.common.navigation.CodexNavRoute
import eywa.projectcodex.common.sharedUi.CodexBottomNavItem
import eywa.projectcodex.common.sharedUi.CodexIconInfo
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.common.utils.CodexTestTag

@Composable
fun ShootDetailsBottomNavBar(
        currentScreen: CodexNavRoute,
        listener: (CodexNavRoute) -> Unit,
        modifier: Modifier = Modifier,
) {
    BottomNavigation(
            backgroundColor = CodexTheme.colors.bottomNavBar,
            contentColor = CodexTheme.colors.onBottomNavBar,
            modifier = modifier
    ) {
        ShootDetailsBottomItem.values().forEach {
            CodexBottomNavItem(
                    icon = it.notSelectedIcon,
                    selectedIcon = it.selectedIcon ?: it.notSelectedIcon,
                    label = stringResource(it.label),
                    contentDescription = stringResource(it.label),
                    isCurrentDestination = currentScreen == it.navRoute,
                    modifier = Modifier.testTag(it.getTestTag()),
                    onClick = { listener(it.navRoute) },
            )
        }
    }
}

internal enum class ShootDetailsBottomItem(
        val navRoute: CodexNavRoute,
        val notSelectedIcon: CodexIconInfo,
        val selectedIcon: CodexIconInfo? = null,
        @StringRes val label: Int,
) : CodexTestTag {
    INPUT_END(
            navRoute = CodexNavRoute.SHOOT_DETAILS_ADD_END,
            notSelectedIcon = CodexIconInfo.PainterIcon(R.drawable.ic_add_box_outline),
            selectedIcon = CodexIconInfo.PainterIcon(R.drawable.ic_add_box_filled),
            label = R.string.input_end__title,
    ),
    SCORE_PAD(
            navRoute = CodexNavRoute.SHOOT_DETAILS_SCORE_PAD,
            notSelectedIcon = CodexIconInfo.PainterIcon(R.drawable.ic_assignment_outline),
            selectedIcon = CodexIconInfo.PainterIcon(R.drawable.ic_assignment_filled),
            label = R.string.score_pad__title,
    ),
    STATS(
            navRoute = CodexNavRoute.SHOOT_DETAILS_STATS,
            notSelectedIcon = CodexIconInfo.PainterIcon(R.drawable.ic_chart_outline),
            selectedIcon = CodexIconInfo.PainterIcon(R.drawable.ic_chart_filled),
            label = R.string.archer_round_stats__title,
    ),
    SETTINGS(
            navRoute = CodexNavRoute.SHOOT_DETAILS_SETTINGS,
            notSelectedIcon = CodexIconInfo.VectorIcon(Icons.Outlined.Settings),
            selectedIcon = CodexIconInfo.VectorIcon(Icons.Filled.Settings),
            label = R.string.archer_round_settings__title,
    ),
    ;

    override val screenName: String
        get() = "SHOOT_DETAILS_NAV_BAR_ITEM"

    override fun getElement(): String = name

    companion object {
        private val backwards = values().associateBy { it.navRoute }

        fun isItem(navRoute: CodexNavRoute) = backwards.containsKey(navRoute)
    }
}

@Preview
@Composable
fun ArcherRoundBottomNavBar_Preview() {
    CodexTheme {
        ShootDetailsBottomNavBar(currentScreen = CodexNavRoute.SHOOT_DETAILS_SCORE_PAD, listener = {})
    }
}
