package eywa.projectcodex.components.archerRoundScore

import androidx.compose.material.BottomNavigation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import eywa.projectcodex.common.sharedUi.CodexBottomNavItem
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import eywa.projectcodex.components.archerRoundScore.state.ArcherRoundScreen

@Composable
fun ArcherRoundBottomNavBar(
        currentScreen: ArcherRoundScreen?,
        listener: (ArcherRoundScreen) -> Unit,
        modifier: Modifier = Modifier,
) {
    BottomNavigation(
            backgroundColor = CodexTheme.colors.bottomNavBar,
            contentColor = CodexTheme.colors.onBottomNavBar,
            modifier = modifier
    ) {
        ArcherRoundScreen.values().filter { it.bottomNavItemInfo != null }.forEach {
            val navItemInfo = it.bottomNavItemInfo!!
            CodexBottomNavItem(
                    icon = navItemInfo.notSelectedIcon,
                    selectedIcon = navItemInfo.selectedIcon ?: navItemInfo.notSelectedIcon,
                    label = stringResource(navItemInfo.label),
                    contentDescription = stringResource(navItemInfo.label),
                    isCurrentDestination = currentScreen == it,
                    modifier = Modifier.testTag(ArcherRoundMainTestTag.bottomNavBarItem(it)),
                    onClick = { listener(it) },
            )
        }
    }
}

@Preview
@Composable
fun ArcherRoundBottomNavBar_Preview() {
    CodexTheme {
        ArcherRoundBottomNavBar(currentScreen = null, listener = {})
    }
}
