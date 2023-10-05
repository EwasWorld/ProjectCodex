package eywa.projectcodex.hiltModules

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.ElementsIntoSet
import eywa.projectcodex.R
import eywa.projectcodex.common.navigation.BottomSheetNavRoute
import eywa.projectcodex.common.navigation.NavArgument
import eywa.projectcodex.common.navigation.NavRoute
import eywa.projectcodex.common.navigation.TabSwitcherGroup
import eywa.projectcodex.common.navigation.TabSwitcherItem
import eywa.projectcodex.common.utils.ResOrActual
import eywa.projectcodex.components.archerHandicaps.ArcherHandicapsScreen
import eywa.projectcodex.components.archerHandicaps.add.ArcherHandicapsBottomSheetAdd
import javax.inject.Singleton

enum class LocalNavRoute : NavRoute {
    ARCHER_HANDICAPS {
        override val tabSwitcherItem = TabSwitcherItem(
                label = ResOrActual.StringResource(R.string.archer_handicaps__tab_title),
                group = TabSwitcherGroup.ARCHER_INFO,
                navRoute = this,
                position = 1,
        )
        override val bottomSheets: List<BottomSheetNavRoute>
            get() = listOf(ArcherHandicapsBottomSheetAdd)

        @Composable
        override fun getMenuBarTitle(entry: NavBackStackEntry?): String =
                stringResource(R.string.archer_info__title)

        @Composable
        override fun Screen(navController: NavController) {
            ArcherHandicapsScreen(navController)
        }
    },
    ;

    override val routeBase = "local_" + name.lowercase()
    override val args: Map<NavArgument, Boolean> = emptyMap()
    override val tabSwitcherItem: TabSwitcherItem? = null
}

@Module
@InstallIn(SingletonComponent::class)
class LocalNavRouteModule {
    @Singleton
    @Provides
    @ElementsIntoSet
    fun providesLocalNavRoute(): Set<NavRoute> = LocalNavRoute.values().toSet()
}
