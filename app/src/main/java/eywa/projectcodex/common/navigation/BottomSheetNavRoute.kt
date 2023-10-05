package eywa.projectcodex.common.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.bottomSheet
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme

interface BottomSheetNavRoute {
    val routeBase: String

    @Composable
    fun ColumnScope.SheetContent(navController: NavController)

    @OptIn(ExperimentalMaterialNavigationApi::class)
    fun create(
            navGraphBuilder: NavGraphBuilder,
            navController: NavController,
    ) {
        navGraphBuilder.bottomSheet(route = asRoute()) {
            Column(
                    modifier = Modifier.padding(top = CodexTheme.dimens.cornerRounding)
            ) {
                SheetContent(navController)
            }
        }
    }

    fun navigate(
            navController: NavController,
            options: (NavOptionsBuilder.() -> Unit)? = null,
    ) {
        if (options == null) navController.navigate(asRoute())
        else navController.navigate(asRoute()) { options.invoke(this) }
    }

    fun asRoute() = "sheet_$routeBase"
}
