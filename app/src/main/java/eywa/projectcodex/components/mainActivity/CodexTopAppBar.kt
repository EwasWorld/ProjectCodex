package eywa.projectcodex.components.mainActivity

import androidx.annotation.DrawableRes
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import eywa.projectcodex.R

/*
 * Use it like this:
 * Scaffold(topBar = { CodexTopAppBar(R.string.main_menu__title) }) {
 *     // The normal content
 * }
 */

@Composable
fun CodexTopAppBar(title: Int) {
    CodexTopAppBar(title = stringResource(id = title))
}

@Composable
fun CodexTopAppBar(title: String) {
    TopAppBar(
            title = { Text(title) },
            backgroundColor = colorResource(id = R.color.colorPrimary),
            contentColor = Color.White,
            actions = {
                CodexTopAppBarIcon(R.drawable.ic_help_icon) {}
                CodexTopAppBarIcon(R.drawable.ic_about_icon) {}
                CodexTopAppBarIcon(R.drawable.ic_home_icon) {}
            }
    )
}

@Composable
private fun CodexTopAppBarIcon(imageVector: ImageVector, contentDescription: String? = null, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
                imageVector = imageVector,
                contentDescription = contentDescription
        )
    }
}

@Composable
private fun CodexTopAppBarIcon(@DrawableRes icon: Int, contentDescription: String? = null, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
                painter = painterResource(id = icon),
                contentDescription = contentDescription
        )
    }
}
