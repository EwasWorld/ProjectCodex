package eywa.projectcodex.common.navigation

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.bottomSheet

interface BottomSheetNavRoute : NavRoute {
    val sheetRouteBase: String

    override val routeBase: String
        get() = "sheet_$sheetRouteBase"

    @Composable
    fun ColumnScope.SheetContent(navController: NavController)

    @OptIn(ExperimentalMaterialNavigationApi::class)
    fun create(
            navGraphBuilder: NavGraphBuilder,
            navController: NavController,
    ) {
        navGraphBuilder.bottomSheet(
                route = asRoute(),
                arguments = navGraphBuilderArgs()
        ) {
            SheetContent(navController)
        }
    }
}
