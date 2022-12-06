package eywa.projectcodex.common.sharedUi

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.Badge
import androidx.compose.material.BadgedBox
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow


@Composable
fun RowScope.CodexBottomNavItem(
        icon: CodexIconInfo,
        selectedIcon: CodexIconInfo = icon,
        label: String,
        contentDescription: String,
        isCurrentDestination: Boolean,
        modifier: Modifier = Modifier,
        badgeContent: String? = null,
        onClick: () -> Unit,
) {
    BottomNavigationItem(
            selected = isCurrentDestination,
            onClick = onClick,
            icon = {
                val displayIcon = if (isCurrentDestination) selectedIcon else icon
                if (badgeContent != null) {
                    BadgedBox(
                            badge = {
                                Badge(content = badgeContent.takeIf { it.isNotBlank() }?.let { { Text(badgeContent) } })
                            },
                            content = { displayIcon.ClavaIcon() },
                    )
                }
                else {
                    displayIcon.ClavaIcon()
                }
            },
            label = {
                Text(
                        text = label,
                        fontWeight = if (isCurrentDestination) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                )
            },
            modifier = modifier.semantics { this.contentDescription = contentDescription }
    )
}