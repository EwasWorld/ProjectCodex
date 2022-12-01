package eywa.projectcodex.components.archerRoundScore

import androidx.compose.material.BottomNavigation
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import eywa.projectcodex.common.sharedUi.CodexBottomNavItem
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme

@Composable
fun ArcherRoundBottomNavBar(
        currentScreen: ArcherRoundScreen?,
        listener: (ArcherRoundScreen) -> Unit,
) {
    BottomNavigation(
            backgroundColor = CodexTheme.colors.bottomNavBar,
            contentColor = CodexTheme.colors.onBottomNavBar,
    ) {
        ArcherRoundScreen.values().filter { it.bottomNavItemInfo != null }.forEach {
            val navItemInfo = it.bottomNavItemInfo!!
            CodexBottomNavItem(
                    icon = navItemInfo.notSelectedIcon,
                    selectedIcon = navItemInfo.selectedIcon ?: navItemInfo.notSelectedIcon,
                    label = stringResource(navItemInfo.label),
                    contentDescription = stringResource(navItemInfo.label),
                    isCurrentDestination = currentScreen == it,
                    onClick = { listener(it) },
            )
        }
    }
}
